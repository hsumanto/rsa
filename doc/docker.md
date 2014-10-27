# Building RSA and its environment using [Docker](http://docker.com):

This document describes easy deployment for RSA and LCM project using docker. This means easy install to machine and easily create devlopement environment or real deply.

Prerequisite
 
rsa branch on git : rsa-workers

 1. pull repository for rsa and change branch to `rsa-workers`
 ```bash
 git clone git@github.com:vpac-innovations/rsa.git
 git checkout rsa-workers
 ```

 1. postgres build
    
 1. run postgres in docker

 1. run rsa worker(master) in docker
    ``` bash
    docker run -d --name master --link postgres:postgres rsa
    ```
 1. run rsa worker(worker) in docker
    ``` bash
    docker run -d --name worker1 --link postgres:postgres --link master:master rsa
    ```

 1. run spatialcube servicce in docker
    ``` bash
    docker run -d -p 80:8080 --name scweb --link postgres:postgres --link master:master rsa /usr/share/tomcat6/bin/catalina.sh run
    ```
 1. lcm
