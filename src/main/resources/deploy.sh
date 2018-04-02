#!/bin/sh

TOMCAT=/usr/local/tomcat
echo "TOMCAT PATH: $TOMCAT"
ROOT=$TOMCAT/webapps/ROOT
echo "ROOT PATH: $ROOT"
CLASS=$ROOT/WEB-INF/classes
echo "CLASS PATH: $CLASS"

NOW_DATE=`date +"%Y%m%d-%H%M%S-%N"`

#shutdown tomcat
echo "shutdown tomcat..."
#sh $TOMCAT/bin/shutdown.sh
ps -ef | grep tomcat | grep -v grep | awk '{print $2}' | xargs kill -9

#backup
echo "backup webapps/ROOT"
zip -r $TOMCAT/webapps/tms-deploy-backup-$NOW_DATE.zip $ROOT

echo "rm $CLASS/com"
rm -rf $CLASS/com
echo "rm $CLASS/static"
rm -rf $CLASS/static

echo "cp com to $CLASS"
cp -rf com $CLASS
echo "cp static to $CLASS"
cp -rf static $CLASS

#startup tomcat
echo "startup tomcat..."
sh $TOMCAT/bin/startup.sh