# Building RSA and its environment using [Docker](http://docker.com):

This document describes how to build and run the RSA using Docker.

The minimum Docker version is 1.2.0.

## Building

RSA uses a few images: one for the database, one for persistent files, and one
for the application itself.

```bash
sudo docker build -t vpac/rsadb src/docker/postgresql/
sudo docker build -t vpac/rsadata src/docker/data/
sudo docker build -t vpac/rsa src/
```

## Running

If you are running multiple deployments, set a suffix for the container names:

```bash
$RSA_ID=_foo
```

First create storage containers. The configuration for RSA is specified by
adding the files as volumes. See [`rsa.xml.docker.SAMPLE`][rsa.xml].

```bash
sudo docker run -d --name rsadb$RSA_ID vpac/rsadb
sudo docker run -d --name rsadata$RSA_ID \
    -v your-rsa-config.xml:/var/src/rsa.xml \
    -v $LARGE_DISK/storagepool:/var/lib/ndg/storagepool \
    -v $LARGE_DISK/pickup:/var/spool/ndg/pickup \
    vpac/rsadata
```

The `rsadata` container exits immediately - but that's OK, the other containers
can still use its volumes. As long as this container is kept, you can restart
and replace the actual RSA containers without losing your data.

By default, Docker allows 10GB of space for each container. So for a test
system, the `$LARGE_DISK` lines are optional.

Now start a master, a worker and the web services. Multiple workers may be
started - just give each one a different name.

```bash
sudo docker run -d --name rsamaster$RSA_ID \
    --link rsadb$RSA_ID:rsadb \
    --volumes-from rsadata$RSA_ID \
    vpac/rsa master
sudo docker run -d --name rsaweb$RSA_ID \
    --link rsadb$RSA_ID:rsadb \
    --link rsamaster$RSA_ID:master \
    --volumes-from rsadata$RSA_ID \
    vpac/rsa web
sudo docker run -d --name rsaworker$RSA_ID \
    --link rsadb$RSA_ID:rsadb \
    --link rsamaster$RSA_ID:master \
    --volumes-from rsadata$RSA_ID \
    vpac/rsa worker
```

`rsaweb` exports port `8080`. Commands can be run against the RSA by using a
local build of the command line client in remote mode. If you are using RSA
locally, use Docker to find the IP address of the container:

```bash
docker inspect rsaweb$RSA_ID | grep IPAddress
```

If you want to publish the RSA on the network, either set up a proxy or pass
the `-p 8080:8080` argument to Docker when starting `rsaweb`.

[rsa.xml]: ../src/storagemanager/config/rsa.xml.docker.SAMPLE

