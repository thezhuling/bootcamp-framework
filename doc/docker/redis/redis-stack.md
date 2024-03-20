# redis-stack-server build for docker

## step

#### pull image

```` shell
docker pull redis/redis-stack-server:latest
````

#### build container

```` shell
mkdir -p /var/local/redis/data/
mkdir -p /var/local/redis/conf/
vi /var/local/redis/conf/redis-stack.conf
````

#### build my.cnf

see more detail for https://hub.docker.com/r/redis/redis-stack-server

#### docker run

```` shell
docker run -d \
--privileged=true \
--name redis-stack-server \
-e REDIS_ARGS="--requirepass Rong710821" \
-e REDIS_ARGS="--appendonly yes" \
-v /var/local/redis/data/:/data \
-v /var/local/redis/conf/redis-stack.conf:/redis-stack.conf \
-p 6379:6379 -p 8001:8001 \
redis/redis-stack-server:latest
````

congratulate.

