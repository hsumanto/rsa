# Deployment

> This document contains instructions for building and running RSA in
> production. If you are setting up a development environment, see
> [`developer.md`](developer.md).

Build RSA for deployment with [Docker]:

```bash
sudo docker build -t vpac/rsa .
```

[Docker]: https://www.docker.com/

## Configuration and Storage

Create a configuration directory:

```bash
cp -r config $HOME/rsa-config
RSA_OPTS="-v $HOME/rsa-config:/var/src/rsa/config"
```

Edit the config as you like. You will probably need to change at least `rsa.xml`
to use the right coordinate system, and `datasource.xml` to point to your
database instance.

Create external `storagepool` and `pickup` directories, so that your data will
persist. If you are starting nodes on separate machines, create these
directories on a shared drive:

```bash
RSA_PREFIX=/mnt/some_large_disk
mkdir -p $RSA_PREFIX/storagepool $RSA_PREFIX/pickup
RSA_OPTS="$RSA_OPTS
    -v $RSA_PREFIX/storagepool:/var/lib/ndg/storagepool
    -v $RSA_PREFIX/pickup:/var/spool/ndg/pickup"
```

If you're running on a virtual host, now is a good time to take a snapshot of
your machine.

All nodes need to open port 2552, and use the `host` network driver:

```bash
RSA_OPTS="$RSA_OPTS --net=host -p 2552:2552"
```

## Running

First, start two or more Akka seed nodes.

 1. Start the virtual hosts and note their IP addresses.
 1. Edit [`application.conf`][ac] to contain the seed node IP addresses. This file
    will need to be copied to all other nodes in the cluster.
 1. Start each seed node with:

    ```bash
    sudo docker run -d --name rsa_seed $RSA_OPTS \
        vpac/rsa seed
    ```

Now start at least two workers:

 1. Start the virtual hosts.
 1. Copy `application.conf` from a seed node.
 1. Start each worker with:

    ```bash
    sudo docker run -d --name rsa_worker $RSA_OPTS \
        vpac/rsa worker
    ```

Finally, start at least one web server:

 1. Start the virtual hosts.
 1. Copy `application.conf` from a seed node.
 1. Start each web server with:

    ```bash
    sudo docker run -d --name rsa_web $RSA_OPTS \
        -p 8080:8080 \
        vpac/rsa web
    ```

It doesn't matter what order the nodes are started in.

[ac]: ../config/application.conf

`rsa_web` exports port `8080`. Commands can be run against the RSA by using a
local build of the command line client in remote mode. If you are using RSA
locally, use Docker to find the IP address of the container:

```bash
docker inspect rsa_web | grep IPAddress
```

[rsa.xml]: ../config/rsa.xml
