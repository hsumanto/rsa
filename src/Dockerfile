#
# RSA - Master Dockerfile
#
# https://github.com/dockerfile/mongodb
#

# Pull base image.
<<<<<<< HEAD
FROM ubuntu:14.04
=======
FROM ubuntu:12.04
>>>>>>> origin

# Install postgres.
RUN \
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 && \
  apt-get update && \
<<<<<<< HEAD
  apt-get install -y postgresql
=======
  apt-get install -y postgresql-9.1
>>>>>>> origin
  
# Install JDK
RUN apt-get install -y openjdk-7-jdk 

# Install build tools
RUN apt-get install -y build-essential ant swig wget

<<<<<<< HEAD
# Install gdal 
RUN apt-get install -y zlibc zlib1g zlib1g-dev gdal-bin libgdal-dev libgdal-java
=======
# Install Zlib
RUN cd /root && \
    wget -nv http://zlib.net/zlib-1.2.8.tar.gz && \
    tar xzf zlib-1.2.8.tar.gz && rm -rf zlib-1.2.8.tar.gz && \
    cd zlib-1.2.8 && ./configure && make && make install

# Install hdf5
RUN cd /root && \
    wget -nv http://www.hdfgroup.org/ftp/HDF5/current/src/hdf5-1.8.13.tar.gz && \
    tar xzf hdf5-1.8.13.tar.gz && rm -rf hdf5-1.8.13.tar.gz && \
    cd hdf5-1.8.13 && ./configure --prefix=/usr/local --with-zlib=/usr/local && make && make install

# Install netcdf
RUN cd /root && \
    wget -nv ftp://ftp.unidata.ucar.edu/pub/netcdf/netcdf-4.3.2.tar.gz && \
    tar xzf netcdf-4.3.2.tar.gz && rm -rf netcdf-4.3.2.tar.gz && \
    cd netcdf-4.3.2 && CPPFLAGS=-I/usr/local/include LDFLAGS=-L/usr/local/lib ./configure && make && make install
>>>>>>> origin

# Install libproj
RUN apt-get install -y libproj0

<<<<<<< HEAD
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
=======
# Install GDAL
RUN apt-get install -y subversion
RUN cd /root && \
	svn checkout https://svn.osgeo.org/gdal/trunk  gdal-trunk && \
	cd gdal-trunk/gdal && ./configure --with-netcdf=/usr/local --with-hdf5=/usr/local && \
	make && make install
RUN GDAL_DIR=/usr/local
RUN export PATH=$GDAL_DIR/bin:$PATH
RUN export LD_LIBRARY_PATH=$GDAL_DIR/lib:$LD_LIBRARY_PATH

RUN export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 && \
	cd /root/gdal-trunk/gdal/swig/java && make JAVA_HOME=$JAVA_HOME && cp *.jar *.so /usr/local/lib/ && \
	cd /usr/local/lib && \
	ln -s libgdalconstjni.so libgdalconstjni.so.1 && \
	ln -s libgdaljni.so libgdaljni.so.1 && \
	ln -s libogrjni.so libogrjni.so.1 && \
	ln -s libosrjni.so libosrjni.so.1

RUN cd /usr/lib && ln -s libproj.so.0 libproj.so && ldconfig
	
# Start postgre sql server
RUN /etc/init.d/postgresql start

# Add user ula to postgres
USER postgres 
RUN /etc/init.d/postgresql start && \
    psql --command "CREATE USER ula WITH SUPERUSER PASSWORD 'password';" && \
    createdb -O ula uladb
USER root	

# Install Tomcat6
RUN apt-get install -y tomcat6
>>>>>>> origin

# Create Directory
RUN mkdir -p /var/lib/ndg/storagepool
RUN mkdir -p /var/spool/ndg/upload
RUN mkdir -p /var/spool/ndg/pickup
RUN mkdir -p /var/tmp/ndg

# Ant build storagemanager
<<<<<<< HEAD
COPY ./build.xml var/src/build.xml
COPY ./cmdclient var/src/cmdclient
COPY ./rsaworkers var/src/rsaworkers
=======
>>>>>>> origin
COPY ./storagemanager var/src/storagemanager
COPY ./rsaquery var/src/rsaquery
COPY ./spatialcubeservice var/src/spatialcubeservice

<<<<<<< HEAD

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
=======
RUN cd /var/src/spatialcubeservice && ant
RUN cp /var/src/spatialcubeservice/dist/spatialcubeservice_local_rN-dev.war /var/lib/tomcat6/webapps/spatialcubeservice.war

export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
export CLASSPATH=/usr/local/lib/gdal.jar

# RUN service tomcat6 start

# Expose ports.
#   - 27017: process
#   - 80: http
EXPOSE 80
EXPOSE 8080
>>>>>>> origin
