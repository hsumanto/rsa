#
# RSA - Master Dockerfile
#
# https://github.com/dockerfile/mongodb
#

# Pull base image.
FROM ubuntu:14.04

# Install postgres.
RUN apt-get update

# Install JDK
RUN apt-get install -y openjdk-7-jdk 

# Install build tools
RUN apt-get install -y build-essential ant swig wget

# Install gdal 
RUN apt-get install -y zlibc zlib1g zlib1g-dev gdal-bin libgdal-dev libgdal-java libproj0

# Set environment variables
ENV GDAL_DIR /usr/share/bin
ENV PATH $GDAL_DIR/bin:$PATH
ENV LD_LIBRARY_PATH $GDAL_DIR/lib:$LD_LIBRARY_PATH
ENV JAVA_HOME /usr/lib/jvm/java-1.7.0-openjdk-amd64
RUN ln -s /usr/bin/gdalwarp /usr/local/bin/gdalwarp
RUN ln -s /usr/bin/gdalbuildvrt /usr/local/bin/gdalbuildvrt

# Install Tomcat6
USER root
RUN apt-get install -y tomcat6 tomcat6-admin

# Create Directory
RUN mkdir -p /var/lib/ndg/storagepool
RUN mkdir -p /var/spool/ndg/upload
RUN mkdir -p /var/spool/ndg/pickup
RUN mkdir -p /var/tmp/ndg

VOLUME /var/lib/ndg/storagepool
VOLUME /var/spool/ndg/upload
VOLUME /var/spool/ndg/pickup
VOLUME /var/tmp/ndg

# Ant build storagemanager
COPY ./build.xml /var/src/build.xml
COPY ./cmdclient /var/src/cmdclient
COPY ./rsaworkers /var/src/rsaworkers
COPY ./storagemanager /var/src/storagemanager
COPY ./rsaquery /var/src/rsaquery
COPY ./spatialcubeservice /var/src/spatialcubeservice

# copy config file for docker
COPY ./storagemanager/config/datasource.xml.docker.SAMPLE /var/src/storagemanager/config/datasource.xml
COPY ./rsaworkers/resources/master.conf.docker.SAMPLE /var/src/rsaworkers/resources/master.conf
COPY ./rsaworkers/resources/spring/database/datasource.xml.docker.SAMPLE /var/src/rsaworkers/resources/spring/database/datasource.xml
COPY ./spatialcubeservice/config/datasource.xml.docker.SAMPLE /var/src/spatialcubeservice/config/datasource.xml
COPY ./spatialcubeservice/config/master.conf.docker.SAMPLE /var/src/spatialcubeservice/config/master.conf

# build Spatialcube Service
ENV CLASSPATH $CLASSPATH:/usr/share/java/gdal.jar:/usr/lib/jni/
ENV CATALINA_BASE /var/lib/tomcat6
ENV CATALINA_HOME /usr/share/tomcat6
RUN ldconfig
RUN cd /var/src && ant
RUN mkdir -p /var/lib/tomcat6/webapps/spatialcubeservice
RUN cp /var/src/spatialcubeservice/dist/rsa_*.war /var/lib/tomcat6/webapps/spatialcubeservice/spatialcubeservice.war
RUN ln -s /usr/share/java/gdal.jar /usr/share/tomcat6/lib/

RUN cd /var/lib/tomcat6/webapps/spatialcubeservice && jar -xvf spatialcubeservice.war 
COPY ./spatialcubeservice/config/datasource.xml.docker.SAMPLE /var/lib/tomcat6/webapps/spatialcubeservice/WEB-INF/classes/datasource.xml

CMD ["/var/src/rsaworkers/dist/rsaworkers/rsaworker"]

#RUN /usr/share/tomcat6/bin/catalina.sh run

# Expose ports.
#   - 8080: web
#   - 2552: akka
#   - 5432: postgresql
EXPOSE 8080
EXPOSE 2552
EXPOSE 5432
