#
# RSA Dockerfile for web services and workers.
#
# Give an argument of "web", "master" or "worker" when running. Uses ENTRYPOINT.
# Needs to be linked to an RSA Postgres container and using volumes from an
# rsa data container. See ../doc/docker.md for more information.
#

FROM ubuntu:17.10

MAINTAINER Jin Park <forjin@vpac-innovations.com.au>, Alex Fraser <alex@vpac-innovations.com.au>

ENV JDK_VERSION=8 \
    TOMCAT_VERSION=8 \
    GRADLE_VERSION=2.13

# Packages with a trailing dash (-) will be removed / not installed.
COPY detect-proxy.sh /root
RUN export DEBIAN_FRONTEND=noninteractive TERM=linux && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        software-properties-common && \
        /root/detect-proxy.sh && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        default-jre-headless- \
        gdal-bin \
        libgdal-dev \
        libgdal-java \
        libproj-dev \
        libproj-java \
        libproj12 \
        proj-bin \
        proj-data \
        libtcnative-1 \
        nano \
        openjdk-${JDK_VERSION}-jdk \
        openjdk-${JDK_VERSION}-jre \
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
        /var/spool/ndg/pickup \
        /var/lib/ndg/storagepool \
        /var/src/rsa/config \
        /var/lib/tomcat${TOMCAT_VERSION}/webapps/rsa

# Copy over gradle build files by themselves first, so we can don't need to
# fetch dependencies every time the source code changes.
COPY src/*.gradle /var/src/rsa/src/
COPY src/cmdclient/build.gradle /var/src/rsa/src/cmdclient/
COPY src/rsa-common/build.gradle /var/src/rsa/src/rsa-common/
COPY src/rsaquery/build.gradle /var/src/rsa/src/rsaquery/
COPY src/rsaworkers/build.gradle /var/src/rsa/src/rsaworkers/
COPY src/spatialcubeservice/build.gradle /var/src/rsa/src/spatialcubeservice/
COPY src/storagemanager/build.gradle /var/src/rsa/src/storagemanager/

WORKDIR /var/src/rsa/src

# Try gradle multiple times in case the first time fails; this can happen if
# one of the repositories returns a transitory error
RUN (gradle || gradle || gradle)

COPY src /var/src/rsa/src
COPY data /var/src/rsa/data
COPY config /var/src/rsa/config
COPY test-config /var/src/rsa/test-config
VOLUME /var/spool/ndg/pickup \
    /var/spool/ndg/upload \
    /var/lib/ndg/storagepool

# Run the build again now that the source is available.
RUN (gradle || gradle || gradle)

ENTRYPOINT ["/var/src/rsa/src/rsa_docker_start.sh"]

# Expose ports.
#   - 8080: web
#   - 2551, 2552: akka-seed
EXPOSE 8080
EXPOSE 2551
EXPOSE 2552
