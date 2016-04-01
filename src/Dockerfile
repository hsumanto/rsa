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
    TOMCAT_VERSION=6

ENV DEBIAN_FRONTEND=noninteractive \
    TERM=linux \
    GDAL_DIR=/usr/share/bin \
    JAVA_HOME=/usr/lib/jvm/java-${JDK_VERSION}-openjdk-amd64 \
    PATH=$JAVA_HOME/bin:$GDAL_DIR/bin:/usr/lib/jni:/usr/lib:$PATH \
    CLASSPATH=$CLASSPATH:/usr/share/java/gdal.jar:/usr/lib/jni/ \
    CATALINA_BASE=/var/lib/tomcat${TOMCAT_VERSION} \
    CATALINA_HOME=/usr/share/tomcat${TOMCAT_VERSION}

ARG APT_PROXY_PORT=
COPY detect-apt-proxy.sh /root
RUN /root/detect-apt-proxy.sh ${APT_PROXY_PORT} && \
    apt-get install -y software-properties-common && \
    add-apt-repository ppa:openjdk-r/ppa && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        ant \
        ant-optional \
        curl \
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
    ln -s /usr/share/java/gdal.jar /usr/lib/ && \
    rm -rf /var/lib/apt/lists/* /etc/apt/apt.conf.d/30proxy

ENV LD_LIBRARY_PATH=$GDAL_DIR/lib:$LD_LIBRARY_PATH
RUN ldconfig

# Temporary working space. No need for this to be a published volume.
RUN mkdir -p /var/tmp/ndg \
    mkdir -p /var/spool/ndg/tmp

# Ant build storagemanager
COPY build.xml rsa_docker_start.sh /var/src/
COPY cmdclient /var/src/cmdclient/
COPY rsaquery /var/src/rsaquery/
COPY spatialcubeservice /var/src/spatialcubeservice/
COPY storagemanager /var/src/storagemanager/

# build Spatialcube Service
RUN cd /var/src/spatialcubeservice && ant

COPY rsaworkers /var/src/rsaworkers/

RUN cd /var/src/rsaworkers && ant

RUN mkdir -p /var/lib/tomcat6/webapps/spatialcubeservice && \
    cp /var/src/spatialcubeservice/dist/rsa_*.war /var/lib/tomcat6/webapps/spatialcubeservice/spatialcubeservice.war && \
    cd /var/lib/tomcat6/webapps/spatialcubeservice && jar -xvf spatialcubeservice.war && \
    ln -s /usr/share/java/gdal.jar /usr/share/tomcat6/lib/

# Use separate database config for the web services and workers.
COPY rsaworkers/resources/spring/database/datasource.xml.docker.SAMPLE \
    /var/src/rsaworkers/dist/rsaworkers/resources/spring/database/datasource.xml

COPY spatialcubeservice/config/datasource.xml.docker.SAMPLE \
    /var/lib/tomcat6/webapps/spatialcubeservice/WEB-INF/classes/datasource.xml

COPY rsaworkers/resources/spring/database/datasource.xml.docker.SAMPLE \
    /var/src/cmdclient/dist/rsacli/config/datasource.xml

COPY storagemanager/config/logback.xml.docker.SAMPLE \
    /var/lib/tomcat6/webapps/spatialcubeservice/WEB-INF/classes/logback.xml

# Use shared rsa.xml. This can be overridden for all RSA containers (worker,
# master and web) by setting a volume on the data-only rsadata container. This
# is copied to the appropriate location by the startup script. We can't just
# create a symlink because then it can't be opened from Java in Tomcat.
COPY storagemanager/config/rsa.xml.docker.SAMPLE /var/src/rsa.xml

WORKDIR /var/src/
ENTRYPOINT ["/var/src/rsa_docker_start.sh"]

# Expose ports.
#   - 8080: web
#   - 2552: akka
EXPOSE 8080
EXPOSE 2552
