# FROM tomcat:8.5
FROM tomcat:8.5.73-jdk8

LABEL MAINTAINER="xiweicheng@yeah.net"

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/tms-1.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war