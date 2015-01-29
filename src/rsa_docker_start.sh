#!/bin/bash

mode=$1

case ${mode} in
    "web" | "scweb")
        cp /var/src/rsa.xml /var/lib/tomcat6/webapps/spatialcubeservice/WEB-INF/classes/
        exec /usr/share/tomcat6/bin/catalina.sh run
        ;;
    "master")
        cp /var/src/rsa.xml /var/src/rsaworkers/dist/rsaworkers/resources/
        export RSA_IS_MASTER=true
        exec /var/src/rsaworkers/dist/rsaworkers/rsaworker
        ;;
    "worker")
        cp /var/src/rsa.xml /var/src/rsaworkers/dist/rsaworkers/resources/
        export RSA_IS_MASTER=false
        exec /var/src/rsaworkers/dist/rsaworkers/rsaworker
        ;;
    *)
        echo "Specify a mode: [web, master, worker]" >&2
        echo "If you want to run a different command, use --entrypoint" >&2
        exit 1
        ;;
esac

