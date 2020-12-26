#!/bin/sh

# mac
sed -i '' 's/=dev/=prod/' src/main/resources/application.properties

# linux
#sed -i 's/=dev/=prod/' src/main/resources/application.properties

mvn clean package -Dmaven.test.skip=true
# mac
sed -i '' 's/=prod/=dev/' src/main/resources/application.properties

# linux
#sed -i 's/=prod/=dev/' src/main/resources/application.properties

# bulid tms tomcat image
docker build -t xiwc/tms .

# buile tms mysql image
cd ./db
docker build -t xiwc/tms-mysql .
cd ../

# docker-compose start
#docker-compose up -d --build
docker-compose up --build