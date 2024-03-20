# mysql-server build for docker

## step

#### pull image

```` shell
docker pull mysql
````

#### build container

```` shell
mkdir -p /var/local/mysql/data/
mkdir -p /var/local/mysql/logs/
mkdir -p /var/local/mysql/conf/
vi /var/local/mysql/conf/my.cnf
````

#### build my.cnf

see more detail for https://hub.docker.com/_/mysql

#### docker run

```` shell
docker run -d \ 
--name mysql-server \
--restart=always \ 
--privileged=true \
-e MYSQL_ROOT_PASSWORD=Rong710821 \
-e TZ=Asia/Shanghai \
-v /var/local/mysql/data/:/var/lib/mysql \
-v /var/local/mysql/logs/:/logs \
-v /var/local/mysql/conf:/etc/mysql/conf.d \
-p 3306:3306 mysql:latest \
--character-set-server=utf8mb4 \ 
--collation-server=utf8mb4_unicode_ci
````

congratulate.

