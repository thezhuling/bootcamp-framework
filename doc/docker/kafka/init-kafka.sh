#!/usr/bin/env bash
# Create the topic the services will use. --if-not-exists makes re-running harmless. Works
# against whichever stack is up: the single node (docker-compose.yml) or the three-node cluster
# (docker-compose.cluster.yml), which gets three replicas per partition.
set -euo pipefail

cd "$(dirname "$0")"

# bootcamp-framework-kafka: the Spring Kafka path (microservice / producer), the Kafka twin of
# bootcamp-framework-topic on RocketMQ. Format: name:partitions
TOPICS=(bootcamp-framework-kafka:3)

if [ -n "$(docker compose -f docker-compose.cluster.yml ps -q kafka-1 2>/dev/null)" ]; then
  COMPOSE=(docker compose -f docker-compose.cluster.yml)
  SERVICE=kafka-1
  RF=3
else
  COMPOSE=(docker compose)
  SERVICE=kafka
  RF=1
fi

# The CLI tools are JVMs; the container's own KAFKA_HEAP_OPTS (the broker heap) would be
# inherited, so give them a small heap of their own.
kafka_tool() {
  local tool=$1; shift
  "${COMPOSE[@]}" exec -T -e KAFKA_HEAP_OPTS=-Xmx128m "${SERVICE}" \
    "/opt/kafka/bin/${tool}" --bootstrap-server "${SERVICE}:19092" "$@"
}

echo "waiting for ${SERVICE} ..."
for _ in $(seq 1 30); do
  if kafka_tool kafka-broker-api-versions.sh >/dev/null 2>&1; then
    break
  fi
  sleep 3
done
kafka_tool kafka-broker-api-versions.sh >/dev/null 2>&1 || { echo "broker not reachable"; exit 1; }

for spec in "${TOPICS[@]}"; do
  name=${spec%%:*}
  partitions=${spec##*:}
  echo "topic ${name} (${partitions} partitions, replication factor ${RF})"
  kafka_tool kafka-topics.sh --create --if-not-exists --topic "${name}" \
    --partitions "${partitions}" --replication-factor "${RF}" >/dev/null
done

echo
kafka_tool kafka-topics.sh --describe --topic "$(IFS='|'; echo "${TOPICS[*]%%:*}")" 2>/dev/null \
  | grep -E "^Topic:|Partition:" | sed 's/^/  /'
echo
echo "done. kafka-ui: http://127.0.0.1:8895  bootstrap for host-side clients: 127.0.0.1:9092"
