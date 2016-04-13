#
# RSA Dockerfile for web services and workers.
#
# Give an argument of "web", "master" or "worker" when running. Uses ENTRYPOINT.
# Needs to be linked to an RSA Postgres container and using volumes from an
# rsa data container. See ../doc/docker.md for more information.
#

FROM ubuntu:14.04

MAINTAINER Jin Park <forjin@vpac-innovations.com.au>, Alex Fraser <alex@vpac-innovations.com.au>

ENV JDK_VERSION=8 \
    TOMCAT_VERSION=7 \
    GRADLE_VERSION=2.12

# Packages with a trailing dash (-) will be removed / not installed.
COPY detect-proxy.sh /root
RUN export DEBIAN_FRONTEND=noninteractive TERM=linux && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        software-properties-common && \
    /root/detect-proxy.sh && \
    add-apt-repository -y ppa:openjdk-r/ppa && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        default-jre-headless- \
        gdal-bin \
        libgdal-dev \
        libgdal-java \
        libjna-java \
        libproj0 \
        libtcnative-1 \
        nmap \
        nano \
        openjdk-${JDK_VERSION}-jdk \
        python-gdal \
        tomcat${TOMCAT_VERSION} \
        tomcat${TOMCAT_VERSION}-admin \
        unzip \
        zlib1g \
        zlib1g-dev \
        zlibc && \
    ln -s /usr/share/java/gdal.jar /usr/lib/
#    rm -rf /var/lib/apt/lists/*

ENV GDAL_DIR=/usr/share/bin \
    JAVA_HOME=/usr/lib/jvm/java-${JDK_VERSION}-openjdk-amd64 \
    GRADLE_HOME=/root/gradle
# ENV commands can't refer to variables set in the same command
ENV PATH=$GRADLE_HOME/bin:$JAVA_HOME/bin:$GDAL_DIR/bin:/usr/lib/jni:/usr/lib:$PATH \
    LD_LIBRARY_PATH=$GDAL_DIR/lib:$LD_LIBRARY_PATH \
    CLASSPATH=$CLASSPATH:/usr/share/java/gdal.jar:/usr/lib/jni/ \
    CATALINA_BASE=/var/lib/tomcat${TOMCAT_VERSION} \
    CATALINA_HOME=/usr/share/tomcat${TOMCAT_VERSION}

RUN ldconfig

WORKDIR /root
RUN curl -O https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip && \
    rm gradle-${GRADLE_VERSION}-bin.zip && \
    mv gradle-${GRADLE_VERSION} gradle

RUN mkdir -p /var/tmp/ndg \
        /var/spool/ndg/tmp \
        /var/spool/ndg/upload \
        /var/lib/ndg/storagepool

COPY src /var/src/rsa/src
COPY data /var/src/rsa/data
COPY config /var/src/rsa/config
VOLUME /var/src/rsa/config \
    /var/spool/ndg/upload \
    /var/lib/ndg/storagepool

WORKDIR /var/src/rsa/src

#ENTRYPOINT ["/var/src/rsa/src/rsa_docker_start.sh"]
CMD bash

# Expose ports.
#   - 8080: web
#   - 2552: akka
EXPOSE 8080
EXPOSE 2552
