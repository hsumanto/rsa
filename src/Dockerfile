#
# RSA Dockerfile for web services and workers.
#
# Give an argument of "web", "master" or "worker" when running. Uses ENTRYPOINT.
# Needs to be linked to an RSA Postgres container and using volumes from an
# rsa data container. See ../doc/docker.md for more information.
#

FROM ubuntu:14.04

MAINTAINER Jin Park <forjin@vpac-innovations.com.au>, Alex Fraser <alex@vpac-innovations.com.au>

ENV DEBIAN_FRONTEND noninteractive
ENV TERM linux

# Install Tomcat, Python and GDAL.
RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update && \
    apt-get install -y \
        libtcnative-1 \
        openjdk-8-jdk \
        tomcat6 \
        tomcat6-admin \
        --no-install-recommends

RUN apt-get install -y \
        ant \
        ant-optional \
        --no-install-recommends

RUN apt-get install -y \
        gdal-bin \
        libgdal-dev \
        libgdal-java \
        libjna-java \
        libproj0 \
        python-gdal \
        zlibc \
        zlib1g \
        zlib1g-dev \
        nmap \
        nano \
        --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV GDAL_DIR /usr/share/bin
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$GDAL_DIR/bin:/usr/lib/jni:/usr/lib:$PATH
ENV LD_LIBRARY_PATH $GDAL_DIR/lib:$LD_LIBRARY_PATH

# Temporary working space. No need for this to be a published volume.
RUN mkdir -p /var/tmp/ndg
RUN mkdir -p /var/spool/ndg/tmp
RUN ln -s /usr/share/java/gdal.jar /usr/lib/

# Ant build storagemanager
COPY build.xml rsa_docker_start.sh /var/src/
COPY cmdclient /var/src/cmdclient/
COPY rsaquery /var/src/rsaquery/
COPY spatialcubeservice /var/src/spatialcubeservice/
COPY storagemanager /var/src/storagemanager/

# build Spatialcube Service
ENV CLASSPATH $CLASSPATH:/usr/share/java/gdal.jar:/usr/lib/jni/
ENV CATALINA_BASE /var/lib/tomcat6
ENV CATALINA_HOME /usr/share/tomcat6

RUN ldconfig && \
    cd /var/src/spatialcubeservice && ant

COPY rsaworkers /var/src/rsaworkers/

RUN ldconfig && \
    cd /var/src/rsaworkers && ant

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

