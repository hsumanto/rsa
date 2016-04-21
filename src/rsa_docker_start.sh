#!/bin/bash

mode=$1

case ${mode} in
    "web")
        cp -f /var/src/rsa/config/* \
            /var/lib/tomcat${TOMCAT_VERSION}/webapps/rsa/WEB-INF/classes/
        exec /usr/share/tomcat${TOMCAT_VERSION}/bin/catalina.sh run
        ;;
    "worker")
        exec /var/src/rsa/src/rsaworkers/build/install/rsaworkers/bin/rsaworkers
        ;;

    *)
        echo "Specify a mode: [web, rsa]" >&2
        echo "If you want to run a different command, use --entrypoint" >&2
        exit 1
        ;;
esac
