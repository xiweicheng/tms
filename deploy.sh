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

# docker-compose start
#docker-compose up -d --build
docker-compose up --build