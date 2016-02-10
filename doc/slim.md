# Building RSA and its environment using [Docker](http://docker.com) for SlIM:

This document describes how to describe and run the RSA using Docker on AWS.

The minimum Docker version is 1.9.0.

## Prerequisite

On AWS, RSA port scan to find out where the master node is. So need to be created
on same network. This document assume all nodes of rsa in same subnet(24), so it
can be 256 ip can be assigned.

RDS will be used for RSA main databases so it should be in the same network. 
This case the instance would be t2.micro is good enough and [AWS RDS[
(https://aws.amazon.com/documentation/rds/) setup document. 


## Building

Building RSA is same as existing document[docker.md].

```bash
sudo docker build -t vpac/rsadata src/docker/data/
sudo docker build -t vpac/rsa src/
```

## Gluster setup

Gulster is one of the cluster storage option for shared space. The reason of choosing
Gluster is open-source and AWS EFS is not launched in Sydney at the monent.

Setting up gluster is well documented on it's [setup document]
(http://gluster.readthedocs.org/en/latest/Quick-Start-Guide/Quickstart/#installing-glusterfs-a-quick-start-guide).

So here are steps for setting up AWS Gluster, and make two instances.

1. Create EC2 instance - t2.micro
2. Add EBS disk on EC2 instance - 100G, (assume `/dev/xvdb1`)
3. Choose same network as RSA EC2 instance
4. Ssh to the instance.
5. Mount EBS to local
```bash
    mkfs.xfs -i size=512 /dev/xvdb1
    mkdir -p /data/brick1
    echo '/dev/xvdb1 /data/brick1 xfs defaults 1 2' >> /etc/fstab
    mount -a && mount
```
1.Install gluster server

```bash
    sudo apt-get install software-properties-common
    sudo add-apt-repository ppa:gluster/glusterfs-3.5
    sudo apt-get update
    sudo apt-get install glusterfs-server
```
1. Probe each other server 
```bash
     gluster peer probe <ip or hostname of another host>
```
From here, need to make one as a main server. Choose one of them.
1. Create volumn & check the info

```bash
    gluster volume create gv0 transport tcp node01:/export/xvdb1/brick node02:/export/xvdb1/brick force
    gluster info gv0
```


## Configuration and Storage

If you are running multiple deployments, set a suffix for the container names
and create a configuration directory. The configuration for RSA is specified by
adding the files as volumes. See [`rsa.xml.docker.SAMPLE`][rsa.xml].

```bash
RSA_OPTS="-v your-rsa-config.xml:/var/src/rsa.xml"
```

By default, Docker allows 10GB of space for each container. If you are doing a
proper deployment you will probably need more than that. In that case, create
external `storagepool` and `pickup` directories, these two directories should
be in Gluster:

```bash
sudo apt-get install software-properties-common
sudo apt-get install glusterfs-client
mkdir -p /mnt/gluster/storagepool /mnt/gluster/pickup
mkdir /mnt/gluster; mount -t glusterfs node01:/testvol; cp -r /var/log /mnt/gluster

RSA_OPTS="$RSA_OPTS
    -v /mnt/gluster/storagepool:/var/lib/ndg/storagepool
    -v /mnt/gluster/pickup:/var/spool/ndg/pickup"
```

Now create some storage containers. The `rsadata` container exits immediately,
but that's OK: the other containers can still use its volumes. As long as this
container is kept, you can restart and replace the actual RSA containers
without losing your data.

```bash
sudo docker run -d --name rsadata $RSA_OPTS vpac/rsadata
```

## Running

Start a rsa which has master and worker at same machine and the web services. 
Multiple rsa instances can be started - just give each one a different name.

```bash
sudo docker run -d --name rsa \
    --volumes-from rsadata \
    vpac/rsa rsa
```

If everything successful, can be created AWS AMI image to create multiple nodes.
More RSA instance can be created with this image. And one of them should be RSA
Web server to provide service to SLIM landblade

```bash
sudo docker run -d --name web \
    --volumes-from rsadata \
    vpac/rsa web
```

`rsaweb` exports port `8080`. Commands can be run against the RSA by using a
local build of the command line client in remote mode. If you are using RSA
locally, use Docker to find the IP address of the container:

```bash
docker inspect rsaweb$RSA_ID | grep IPAddress
```

If you want to publish the RSA on the network, either set up a proxy or pass
the `-p 8080:8080` argument to Docker when starting `web`.

## Importing example dataset

If you want to use RSA Client, you can use `docker exec -it <rsa/web>` to any
rsa / web instance.

But the other way recommended is using curl.

Create Dataset and returns datasetId.
```bash
curl -F name="rainfall" -F resolution="100m" \
            <web_public_dns>:8080/spatialcubeservice/Dataset.xml
```

Create Timeslice and return timesliceId
```bash
curl -F datasetId="<datasetId>" -F creationDate="2000-01-01" \
     -F abs="" -F xmin=""
            <web_public_dns>:8080/spatialcubeservice/TimeSlice.xml
```

Create Band and return bandId
```bash
curl -F datasetId="<datasetId>" -F name="band1" \
            <web_public_dns>:8080/spatialcubeservice/Band.xml
```

Create Upload a file and return uploadId
```bash
curl -F timesliceId="<timesliceId>" -F file="@rainfall.tiff" \
            <web_public_dns>:8080/spatialcubeservice/Data/Upload.xml
```

Import dataset
```bash
curl -F taskId="<uploadId>" -F bandId="<bandId>" \
            <web_public_dns>:8080/spatialcubeservice/Data/Import.xml
```

Query
```bash
curl --data-urlencode "query=${cat blur.xml}"  \
            <web_public_dns>:8080/spatialcubeservice/Data/Query.xml
```

[rsa.xml]: ../src/storagemanager/config/rsa.xml.docker.SAMPLE
