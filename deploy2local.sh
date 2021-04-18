#!/bin/bash

echo "deploy tms to tomcat start"

DEST=/Users/xiweicheng/Applications/apache-tomcat-8.5.59
SRC=/Users/xiweicheng/tms

echo "DEST DIR: $DEST"
echo "SRC DIR: $SRC"

echo "mvn clean compile"
mvn clean compile

DT=`date +"%Y%m%d%H%M%S"`
mkdir -p $DEST/backup/tms/$DT
cp -rf $DEST/webapps/ROOT/WEB-INF/classes/com $DEST/backup/tms/$DT
cp -rf $DEST/webapps/ROOT/WEB-INF/classes/static $DEST/backup/tms/$DT
cp -rf $DEST/webapps/ROOT/WEB-INF/classes/templates $DEST/backup/tms/$DT

echo "backup path: $DEST/backup/tms/$DT"

echo "rm com & static & templates"

rm -rf $DEST/webapps/ROOT/WEB-INF/classes/com
rm -rf $DEST/webapps/ROOT/WEB-INF/classes/static
rm -rf $DEST/webapps/ROOT/WEB-INF/classes/templates

echo "cp tms to local tomcat"

cp -rf $SRC/target/classes/com $DEST/webapps/ROOT/WEB-INF/classes
cp -rf $SRC/src/main/resources/static $DEST/webapps/ROOT/WEB-INF/classes
cp -rf $SRC/src/main/resources/templates $DEST/webapps/ROOT/WEB-INF/classes

sh $DEST/bin/shutdown.sh
sleep 5
sh $DEST/bin/startup.sh

echo "deploy tms to tomcat end"