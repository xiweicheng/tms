#!/bin/sh

# mac
sed -i '' 's/=dev/=prod-pg/' src/main/resources/application.properties

# linux
#sed -i 's/=dev/=prod-pg/' src/main/resources/application.properties

mvn clean package -Dmaven.test.skip=true
# mac
sed -i '' 's/=prod-pg/=dev/' src/main/resources/application.properties

# linux
#sed -i 's/=prod-pg/=dev/' src/main/resources/application.properties

# docker-compose start
#docker-compose -f docker-compose-pg.yml up -d --build
docker-compose -f docker-compose-pg.yml up --build