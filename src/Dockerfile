#
# RSA - Master Dockerfile
#
# https://github.com/dockerfile/mongodb
#

# Pull base image.
FROM ubuntu:14.04

# Install postgres.
RUN \
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 && \
  apt-get update && \
  apt-get install -y postgresql
  
# Install JDK
RUN apt-get install -y openjdk-7-jdk 

# Install build tools
RUN apt-get install -y build-essential ant swig wget

# Install gdal 
RUN apt-get install -y zlibc zlib1g zlib1g-dev gdal-bin libgdal-dev libgdal-java

# Install libproj
RUN apt-get install -y libproj0

# Set environment variables
ENV GDAL_DIR /usr/share/bin
ENV PATH $GDAL_DIR/bin:$PATH
ENV LD_LIBRARY_PATH $GDAL_DIR/lib:$LD_LIBRARY_PATH
ENV JAVA_HOME /usr/lib/jvm/java-1.7.0-openjdk-amd64

# Start postgre sql server & Add user ula to postgres
USER postgres 
RUN /etc/init.d/postgresql start && \
    psql --command "CREATE USER ula WITH PASSWORD 'password';" && \
    createdb -O ula uladb

# Install Tomcat6
USER root
RUN apt-get install -y tomcat6 tomcat6-admin

# Create Directory
RUN mkdir -p /var/lib/ndg/storagepool
RUN mkdir -p /var/spool/ndg/upload
RUN mkdir -p /var/spool/ndg/pickup
RUN mkdir -p /var/tmp/ndg

# Ant build storagemanager
COPY ./build.xml var/src/build.xml
COPY ./cmdclient var/src/cmdclient
COPY ./rsaworkers var/src/rsaworkers
COPY ./storagemanager var/src/storagemanager
COPY ./rsaquery var/src/rsaquery
COPY ./spatialcubeservice var/src/spatialcubeservice


# build Spatialcube Service
ENV CLASSPATH $CLASSPATH:/usr/share/java/gdal.jar:/usr/lib/jni/
RUN ldconfig
RUN cd /var/src && ant
RUN cp /var/src/spatialcubeservice/dist/rsa_*.war /var/lib/tomcat6/webapps/spatialcubeservice.war


# CMD ["/etc/init.d/postgresql start", "/var/src/rsaworkers/dist/rsaworkers/rsaworker"]
#RUN ["/var/src/rsaworkers/dist/rsaworkers/rsaworker"]

#RUN ["JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64", "CATALINA_BASE=/var/lib/tomcat6", "CATALINA_HOME=/usr/share/tomcat6", "/usr/share/tomcat6/bin/catalina.sh", "run"]
ENV JAVA_OPTS "-Djava.library.path=/usr/lib/jni"
#RUN CATALINA_BASE=/var/lib/tomcat6 CATALINA_HOME=/usr/share/tomcat6 JAVA_OPTS="-Djava.library.path=/usr/lib/jni" /usr/share/tomcat6/bin/catalina.sh run

# Expose ports.
#   - 80: web
#   - 2552: akka
#   - 5432: postgresql
EXPOSE 80
EXPOSE 2552
EXPOSE 5432