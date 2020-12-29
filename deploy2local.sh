#!/bin/bash

echo "deploy to tomcat start"

DEST=/Users/xiweicheng/Applications/apache-tomcat-8.5.59
SRC=/Users/xiweicheng/tms

echo "DEST DIR: $DEST"
echo "SRC DIR: $SRC"

echo "mvn clean compile"
mvn clean compile

DT=`date +"%Y%m%d%H%M%S"`
mkdir -p $DEST/backup/tms/$DT
mv $DEST/webapps/ROOT/WEB-INF/classes/com $DEST/backup/$DT
echo "backup path: $DEST/backup/tms/$DT"

echo "cp tms to local tomcat"

cp -rf $SRC/target/classes/com $DEST/webapps/ROOT/WEB-INF/classes

sh $DEST/bin/shutdown.sh
sleep 5
sh $DEST/bin/startup.sh

echo "deploy to tomcat end"