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
ARG APT_PROXY_PORT=
COPY detect-apt-proxy.sh /root
RUN export DEBIAN_FRONTEND=noninteractive TERM=linux && \
    /root/detect-apt-proxy.sh ${APT_PROXY_PORT} no && \
    apt-get install -y software-properties-common && \
    add-apt-repository ppa:openjdk-r/ppa && \
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
#    rm -rf /var/lib/apt/lists/* \
#        /etc/apt/apt.conf.d/30proxy \
#        /root/detect-apt-proxy.sh

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

# Temporary working space. No need for this to be a published volume.
RUN mkdir -p /var/tmp/ndg /var/spool/ndg/tmp

COPY . /var/src/rsa
WORKDIR /var/src/rsa

#ENTRYPOINT ["/var/src/rsa_docker_start.sh"]
RUN bash

# Expose ports.
#   - 8080: web
#   - 2552: akka
EXPOSE 8080
EXPOSE 2552
