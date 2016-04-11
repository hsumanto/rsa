# Building RSA and its environment using [Docker](http://docker.com):

This document describes how to build and run the RSA using Docker.

The minimum Docker version is 1.2.0.

## Building

Before building, it's a good idea to start
[`docker-proxy` with SSL support][dp]: this allows caching of libraries
used by Gradle. The rsa's build script has special support for `docker-proxy`
to allow caching of SSL requests.

```
git clone git@github.com:vpac-innovations/docker-proxy.git
cd docker-proxy
sudo docker build -t docker-proxy .
./run.sh ssl
```

Then build RSA.

RSA uses a couple of images: one for persistent files, and one for the
application itself.

```bash
sudo docker build -t vpac/rsadata src/docker/data/
sudo docker build -t vpac/rsa .
```

[dp]: https://github.com/vpac-innovations/docker-proxy

## Configuration and Storage

If you are running multiple deployments, set a suffix for the container names
and create a configuration directory. The configuration for RSA is specified by
adding the files as volumes. See [`rsa.xml.docker.SAMPLE`][rsa.xml].

```bash
RSA_ID=_foo
RSA_OPTS="-v your-rsa-config.xml:/var/src/rsa.xml"
```

By default, Docker allows 10GB of space for each container. If you are doing a
proper deployment you will probably need more than that. In that case, create
external `storagepool` and `pickup` directories:

```bash
mkdir -p /mnt/some_large_disk/storagepool /mnt/some_large_disk/pickup
RSA_OPTS="$RSA_OPTS
    -v /mnt/some_large_disk/storagepool:/var/lib/ndg/storagepool
    -v /mnt/some_large_disk/pickup:/var/spool/ndg/pickup"
```

Now create some storage containers. The `rsadata` container exits immediately,
but that's OK: the other containers can still use its volumes. As long as this
container is kept, you can restart and replace the actual RSA containers
without losing your data.

```bash
sudo docker run -d --name rsadb$RSA_ID postgres:9
sudo docker run -d --name rsadata$RSA_ID $RSA_OPTS vpac/rsadata
```

## Running

Start a rsa which has master and worker at same machine and the web services.
Multiple rsa instances can be started - just give each one a different name.

```bash
sudo docker run -d --name rsa1 \
    --link rsadb$RSA_ID:rsadb \
    --volumes-from rsadata$RSA_ID \
    vpac/rsa rsa
sleep 10
sudo docker run -d --name rsaweb$RSA_ID \
    --link rsadb$RSA_ID:rsadb \
    --link rsamaster$RSA_ID:master \
    --volumes-from rsadata$RSA_ID \
    vpac/rsa web
```

if you want to run more worker and master, you can run this way. The number of
the instance name can be changed.
```bash
sudo docker run -d --name rsa2 \
    --link rsadb$RSA_ID:rsadb \
    --volumes-from rsadata$RSA_ID \
    vpac/rsa rsa
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
