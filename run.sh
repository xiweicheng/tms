#!/bin/sh

#替换环境变量
sed -i '' 's/=dev/=prod/' src/main/resources/application.properties
#执行maven命令打tomcat war包
mvn clean package -Dmaven.test.skip=true
#恢复前面针对环境变量的替换
sed -i '' 's/=prod/=dev/' src/main/resources/application.properties

docker-compose up -d