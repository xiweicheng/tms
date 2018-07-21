#!/bin/sh

mvn clean package -Dmaven.test.skip=true

docker build -t xiwc/tms .

# cd ./db
# docker build -t xiwc/tms-mysql .
# cd ../

docker-compose up -d