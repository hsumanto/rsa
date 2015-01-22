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

First create storage containers. The configuration for RSA is specified by
adding the files as volumes. See [`rsa.xml.docker.SAMPLE`][rsa.xml].

```bash
sudo docker run -d --name rsadb vpac/rsadb
sudo docker run -d --name rsadata \
    -v your-rsa-config.xml:/var/src/rsa.xml \
    -v $LARGE_DISK/storagepool:/var/lib/ndg/storagepool
    -v $LARGE_DISK/pickup:/var/spool/ndg/pickup
    vpac/rsadata
```

By default, Docker allows 10GB of space for each container. So for a test
system, the `$LARGE_DISK` lines are optional.

Then start a master, a worker and the web services. Multiple workers may be
started.

```bash
sudo docker run -d --name rsamaster \
    --link rsadb:rsadb \
    --volumes-from rsadata \
    vpac/rsa master
sudo docker run -d --name rsaweb \
    -p 8080:8080 \
    --link rsadb:rsadb \
    --link rsamaster:master \
    --volumes-from rsadata \
    vpac/rsa web
sudo docker run -d \
    --link rsadb:rsadb \
    --link rsamaster:master \
    --volumes-from rsadata \
    vpac/rsa worker
```

Commands can be run against the RSA by using a local build of the command line
client in remote mode.

[rsa.xml]: ../src/storagemanager/config/rsa.xml.docker.SAMPLE

