# nacos-server-build for docker + mysql

## step

#### pull image
```` shell
docker pull nacos/nacos-server:v2.3.1-slim
````

#### build container
```` shell
mkdir -p /var/nacos/logs/
mkdir -p /var/nacos/conf/
vi /var/nacos/conf/application.properties
````
#### build application.properties
see more detail for project ../nacos/application.properties

#### If you use a custom database, you need to initialize the database script yourself for the first time.
see more detail for project ../nacos/mysql-schema.sql

#### docker run
```` shell
docker run -d \
--name nacos-server \
--restart=always \
--privileged=true \
-e MODE=standalone \
-e PREFER_HOST_MODE=hostname \
-v /var/nacos/logs:/home/nacos/logs \
-v /var/nacos/conf/application.properties:/home/nacos/init.d \
-p 8848:8848 -p 9848:9848 -p 9849:9849 \
nacos/nacos-server:v2.3.1-slim
````

congratulate.

