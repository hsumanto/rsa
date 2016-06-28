#!/bin/bash

mode=$1
projdir=$(dirname $0)

case ${mode} in
    "web")
        echo "Starting RSA web server"
        # The web server resources need to be in a special directory. To support
        # out-of-container rebuilds (via a mounted Docker volume), these
        # resources are copied in every time.
        cd /var/lib/tomcat${TOMCAT_VERSION}/webapps/rsa \
            && jar -xvf ${projdir}/spatialcubeservice/build/libs/rsa*.war \
            && cp -f ${projdir}/../config/* ./WEB-INF/classes/ \
            && cd ${HOME} \
            || exit 1
        exec /usr/share/tomcat${TOMCAT_VERSION}/bin/catalina.sh run
        ;;

    "worker")
        echo "Starting RSA worker"
        exec ${projdir}/rsaworkers/build/install/rsaworkers/bin/rsaworkers
        ;;

    "seed")
        echo "Starting RSA cluster seed"
        exec ${projdir}/rsaworkers/build/install/rsaworkers/bin/rsaworkers seed
        ;;

    *)
        echo "Specify a mode: [web, worker, seed]" >&2
        echo "If you want to run a different command, use --entrypoint" >&2
        exit 1
        ;;
esac
