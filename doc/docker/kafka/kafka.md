# Local Kafka for bootcamp-framework

Apache Kafka **4.3.1** in KRaft mode (no ZooKeeper) plus [kafka-ui](https://github.com/kafbat/kafka-ui),
in Docker Compose. Two shapes of the same thing:

| File | What runs | Use it for |
|---|---|---|
| `docker-compose.yml` | one node that is broker and controller at once, kafka-ui | everyday development |
| `docker-compose.cluster.yml` | three broker+controller nodes, kafka-ui | replication: ISR, `min.insync.replicas`, `acks=all`, leader failover, reassignment |
| `init-kafka.sh` | idempotent: waits for the broker, creates `bootcamp-framework-kafka` | after a first start or a wipe |

Both files publish the same host ports, so run one at a time.

No service uses Kafka yet. `bootcamp-framework-kafka` is reserved for the Spring Kafka path,
the Kafka counterpart of `bootcamp-framework-topic` on RocketMQ. The services' client comes from
Spring Boot's dependency management: spring-kafka 4.1.1, kafka-clients 4.2.1. A 4.2 client talks
to a 4.3 broker with no extra setting.

## First start

```shell
cd doc/docker/kafka
docker compose up -d --wait
./init-kafka.sh
```

- Bootstrap for processes on the Mac: `127.0.0.1:9092`
- kafka-ui: http://127.0.0.1:8895 (no login)
- Inside the compose network (kafka-ui, the CLI tools): `kafka:19092`

## Day to day

```shell
docker compose up -d --wait     # start; topics and data live in the kafka-data volume
docker compose stop             # stop, keep data
docker compose down -v          # wipe, then repeat "First start"
docker compose logs -f kafka
./init-kafka.sh                 # safe any time
```

The CLI tools ship in the image. They are JVMs, so give them a small heap of their own; the
container's `KAFKA_HEAP_OPTS` is the broker's:

```shell
k() { docker compose exec -T -e KAFKA_HEAP_OPTS=-Xmx128m kafka "/opt/kafka/bin/$1" "${@:2}"; }

k kafka-topics.sh --bootstrap-server kafka:19092 --describe --topic bootcamp-framework-kafka
k kafka-console-producer.sh --bootstrap-server kafka:19092 --topic bootcamp-framework-kafka
k kafka-console-consumer.sh --bootstrap-server kafka:19092 --topic bootcamp-framework-kafka --from-beginning \
  --formatter-property print.partition=true --formatter-property print.offset=true
k kafka-consumer-groups.sh --bootstrap-server kafka:19092 --describe --all-groups
k kafka-dump-log.sh --files /var/lib/kafka/data/bootcamp-framework-kafka-0/00000000000000000000.log
k kafka-metadata-quorum.sh --bootstrap-server kafka:19092 describe --status
```

Kafka 4.2+ renamed several CLI options (KIP-1147): `--command-property` instead of
`--producer-property`/`--consumer-property`, `--reader-property` and `--formatter-property`
instead of `--property`. The old names still work; `--help` marks them deprecated.

## Three-node cluster

```shell
docker compose down                                   # the single node and the cluster share ports
docker compose -f docker-compose.cluster.yml up -d --wait
./init-kafka.sh                                       # detects the cluster, creates topics with 3 replicas
docker compose -f docker-compose.cluster.yml exec -T -e KAFKA_HEAP_OPTS=-Xmx128m kafka-1 \
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --describe --topic bootcamp-framework-kafka
docker compose -f docker-compose.cluster.yml stop kafka-2   # watch Isr shrink; acks=all still works with 2 of 3
docker compose -f docker-compose.cluster.yml start kafka-2  # Isr is back to 1,2,3 within seconds
```

After a restart the replicas rejoin the ISR within seconds, but leadership stays where it moved:
the partitions kafka-2 led keep their new leader until a preferred-leader election (automatic,
checked every 5 minutes, or `kafka-leader-election.sh --election-type preferred --all-topic-partitions`).

Clients still bootstrap on `127.0.0.1:9092` and are then sent to the partition leader at
`127.0.0.1:9092`, `9093` or `9094` (kafka-1, 2, 3). The cluster defaults are production-shaped:
replication factor 3 and `min.insync.replicas=2`. The volumes `kafka-1-data` to `kafka-3-data`
are separate from the single node's `kafka-data`.

## Listeners: why there are two client addresses

A Kafka client asks any broker for metadata and then connects to the address the broker
*advertises* for the listener it came in on. A container sees its own `127.0.0.1`, the Mac sees
the published port, so one address cannot serve both. Each broker therefore has two client
listeners:

| Listener | Port in the container | Advertised as | Used by |
|---|---|---|---|
| `HOST` | 9092 | `127.0.0.1:9092` (cluster: 9092 / 9093 / 9094) | Java processes on the Mac |
| `INTERNAL` | 19092 | `kafka:19092` (cluster: `kafka-N:19092`) | kafka-ui, CLI tools, brokers among themselves |
| `CONTROLLER` | 9093 | not advertised | the KRaft quorum |

The RocketMQ stack solves the same problem by sharing the broker's network namespace with its
dashboard. Kafka can advertise per listener, so it doesn't have to. A containerised service
would bootstrap on `kafka:19092`.

## Settings that differ from production defaults

| Setting | Here | Why |
|---|---|---|
| internal topics' replication factor and min ISR | 1 (single node), 3 / 2 (cluster) | a single node cannot hold three replicas |
| `group.initial.rebalance.delay.ms` | 0 | no 3 s wait before a new group's first assignment |
| `log.retention.check.interval.ms` | 30 s (default 5 min) | retention deletes become observable in a couple of minutes |
| `num.partitions` | 3 | auto-created topics get more than one partition |

## Memory

Single node: broker 512 MB heap, kafka-ui 256 MB heap, about 650 MB resident together. Cluster:
three brokers at 384 MB each. The Docker Desktop VM also holds Nacos, its MySQL and RocketMQ.
Stop RocketMQ while running the cluster if the VM is short of memory.

## Data

`/var/lib/kafka/data` in the `kafka-data` volume. The image runs as uid 1000 and the directory
already exists in the image, so a fresh named volume is created with the right owner and no
init container is needed, unlike the RocketMQ stack.
