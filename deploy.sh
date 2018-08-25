#!/bin/sh

sed -i 's/=dev/=prod/' src/main/resources/application.properties

mvn clean package -Dmaven.test.skip=true

sed -i 's/=prod/=dev/' src/main/resources/application.properties

docker build -t xiwc/tms .

cd ./db
docker build -t xiwc/tms-mysql .
cd ../

docker-compose up -d