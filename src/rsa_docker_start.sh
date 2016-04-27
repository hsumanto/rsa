#!/bin/bash

mode=$1

SEED_CONF=/var/src/rsa/src/rsaworkers/src/main/resources/seed.conf

function update_config() {
    # When starting a seed node, check for a placeholder in the config file for
    # the IP address. That case would indicate a half-configured RSA. To make
    # development easier, the seed node's IP address is automatically set here.
    local seed_ip
    if ! grep -q '<seed-node-ip>' ${SEED_CONF}; then
        return
    fi
    seed_ip=$(ip route get 1 | awk '{print $NF;exit}')
    sed -i.bak "s,<seed-node-ip>,${seed_ip},g" ${SEED_CONF}
    echo "Updated ${SEED_CONF} to contain this node's IP address"
    trap unset_config INT
    trap unset_config TERM
}

function unset_config() {
    if ! [ -f ${SEED_CONF}.bak ]; then
        return
    fi
    mv ${SEED_CONF}.bak ${SEED_CONF}
    echo "Reset ${SEED_CONF}"
}

case ${mode} in
    "web")
        cp -f /var/src/rsa/config/* \
            /var/lib/tomcat${TOMCAT_VERSION}/webapps/rsa/WEB-INF/classes/
        exec /usr/share/tomcat${TOMCAT_VERSION}/bin/catalina.sh run
        ;;

    "worker")
        exec /var/src/rsa/src/rsaworkers/build/install/rsaworkers/bin/rsaworkers
        ;;

    "seed")
        update_config
        /var/src/rsa/src/rsaworkers/build/install/rsaworkers/bin/rsaworkers seed
        unset_config
        ;;

    *)
        echo "Specify a mode: [web, worker, seed]" >&2
        echo "If you want to run a different command, use --entrypoint" >&2
        exit 1
        ;;
esac
