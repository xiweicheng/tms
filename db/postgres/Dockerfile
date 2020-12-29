FROM postgres:13

LABEL MAINTAINER="xiweicheng@yeah.net"

# 复制数据库初始化脚本db.sql到/docker-entrypoint-initdb.d文件夹下
COPY db.sql /docker-entrypoint-initdb.d