#
# RSA - Master Dockerfile
#
# https://github.com/dockerfile/mongodb
#

# Pull base image.
FROM ubuntu:12.10

# Install postgres.
RUN \
  apt-get update && \
  apt-get install -y postgresql-9.1

#COPY ./ /data/dump/

# Expose ports.
#   - 80: http
EXPOSE 80