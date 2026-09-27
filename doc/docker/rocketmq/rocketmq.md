# Local RocketMQ for bootcamp-framework

RocketMQ **5.3.1** (single master, no proxy) plus the dashboard, in Docker Compose. The server
version is pinned to the `rocketmq-client` that `rocketmq-spring-boot-starter 2.3.5` resolves —
check with `mvn dependency:tree -pl bootcamp-framework-producer | grep rocketmq-client`. Same
minor, newer patch (5.3.4) is a drop-in tag.

## Files

| File | Purpose |
|---|---|
| `docker-compose.yml` | `namesrv`, `broker` (+ one-shot `broker-init` that fixes volume ownership), `dashboard` |
| `broker.conf` | single-master broker config, mounted over the image's `conf/broker.conf` |
| `init-rocketmq.sh` | idempotent: waits for the broker, creates the two topics the services use |

## First start

```shell
cd doc/docker/rocketmq
docker compose up -d
./init-rocketmq.sh
```

- Name server: `127.0.0.1:9876` — what `rocketmq.name-server` in `application.yml` points at
- Broker: advertised as `127.0.0.1:10911` (`brokerIP1` in `broker.conf`)
- Dashboard: http://127.0.0.1:8890 (no login)

## Day to day

```shell
docker compose up -d            # start; messages and topics live in the broker-store volume
docker compose stop             # stop, keep data
docker compose down -v          # wipe, then repeat "First start"
docker compose logs -f broker
./init-rocketmq.sh              # re-create topics after a wipe (also safe any other time)
```

Send / inspect from the broker container (`mqadmin` is a JVM, ~2 s per call):

```shell
docker compose exec -T broker /home/rocketmq/rocketmq-5.3.1/bin/mqadmin sendMessage -n namesrv:9876 -t bootcamp-framework-topic -p 'hello'
docker compose exec -T broker /home/rocketmq/rocketmq-5.3.1/bin/mqadmin topicStatus -n namesrv:9876 -t bootcamp-framework-topic
docker compose exec -T broker /home/rocketmq/rocketmq-5.3.1/bin/mqadmin consumerProgress -n namesrv:9876 -g bootcamp-framework-consumer   # once BootcampFrameworkConsumer is enabled; the group does not exist before
```

`broker.conf` is bind-mounted from this directory, so keep the checkout the stack was started
from — or run `docker compose up -d` again from the new location to re-point the mount (the
`broker-store` volume, topics and messages survive that).

## What the services expect

| Service | Uses | Topic |
|---|---|---|
| microservice | `MessageApi` → `RocketMQTemplate.send` | `bootcamp-framework-topic` |
| producer | `MessageQueueProducer` → `RocketMQTemplate.send` | `bootcamp-producer` (the value of `rocketmq.producer.customized-trace-topic`, reused as a plain topic; message tracing itself is off — `enableMsgTrace` defaults to false in rocketmq-spring 2.3.5) |
| microservice | `BootcampFrameworkConsumer` (`bootcamp-framework-consumer`) | `bootcamp-framework-topic` — **currently commented out**, nothing consumes |

`autoCreateTopicEnable=true`, so a send to an unknown topic also works; `init-rocketmq.sh` just
makes the two topics exist up front (visible in the dashboard before any traffic).

## Why the dashboard shares the broker's network namespace

The broker must advertise an address the host-side Java clients can reach, i.e. `127.0.0.1:10911`
through the published port. A dashboard in its own container would take that address from the
nameserver and dial its *own* loopback, so every broker-stats page (topic status, consumer
progress) would fail. With `network_mode: "service:broker"` the dashboard sees the broker on
`127.0.0.1:10911` and still resolves `namesrv`; its port (8082 in the 2.1.0 image) is therefore
published on the `broker` service (`8890:8082`).

The same reasoning means a **containerised** service cannot use this stack as-is — it would
also dial its own loopback. Run the services on the host, or change `brokerIP1` to something both
sides can reach.

## Memory

Nameserver 256 MB, broker 512 MB heap (`JAVA_OPT_EXT`, appended last by the run scripts so it
overrides their 1 GB / 2 GB defaults), dashboard 256 MB. The broker additionally maps the commit
log; `-XX:MaxDirectMemorySize=1g` from the defaults stays.

## Disk

`diskMaxUsedSpaceRatio=90` in `broker.conf`: the store sits on the Docker Desktop VM disk, and
with the 75 % default the broker starts refusing sends (`service not available now`) as soon as
that disk fills with unrelated images. Check with `docker system df`.
