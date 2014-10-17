#
# RSA - Master Dockerfile
#
# https://github.com/dockerfile/mongodb
#

# Pull base image.
FROM ubuntu:12.04

# Install postgres.
RUN \
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 && \
  apt-get update && \
  apt-get install -y postgresql-9.1
  
# Install JDK
RUN apt-get install -y openjdk-7-jdk 

# Install build tools
RUN apt-get install -y build-essential ant swig wget

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

# Install libproj
RUN apt-get install -y libproj0

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
#USER postgres
#RUN psql --command "CREATE USER ula WITH SUPERUSER PASSWORD 'password';"
#RUN createdb -O ula uladb
#USER root

# Install Tomcat6
RUN apt-get install -y tomcat6

# Create Directory
RUN mkdir -p /var/lib/ndg/storagepool
RUN mkdir -p /var/spool/ndg/upload
RUN mkdir -p /var/spool/ndg/pickup
RUN mkdir -p /var/tmp/ndg

# Ant build storagemanager
COPY ./build.xml var/src/build.xml
COPY ./storagemanager var/src/storagemanager
COPY ./rsaquery var/src/rsaquery
COPY ./spatialcubeservice var/src/spatialcubeservice

CMD ["cd", "/var/src/spatialcubeservice"]
CMD ["ant"]
# Expose ports.
#   - 27017: process
#   - 80: http
EXPOSE 80