#!/bin/sh

TOMCAT=/data/apps/tomcat
echo "TOMCAT PATH: $TOMCAT"
ROOT=$TOMCAT/webapps/ROOT
echo "ROOT PATH: $ROOT"
CLASS=$ROOT/WEB-INF/classes
echo "CLASS PATH: $CLASS"
WEB=$ROOT/WEB-INF
echo "WEB-INF PATH: $WEB"

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
echo "rm $WEB/lib"
rm -rf $LIB/lib

echo "cp com to $CLASS"
cp -rf com $CLASS
echo "cp static to $CLASS"
cp -rf static $CLASS
echo "cp ../lib to $WEB"
cp -rf ../lib $WEB

#startup tomcat
echo "startup tomcat..."
echo "请手动执行【$TOMCAT/bin/startup.sh】脚本启动tomcat"
#sh $TOMCAT/bin/startup.sh