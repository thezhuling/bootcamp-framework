#!/usr/bin/env bash
# Create the topics the services publish to. updateTopic is create-or-update, so re-running is
# harmless. Topics persist in the broker-store volume (store/config/topics.json).
set -euo pipefail

cd "$(dirname "$0")"

MQADMIN=/home/rocketmq/rocketmq-5.3.1/bin/mqadmin
NAMESRV=namesrv:9876
CLUSTER=DefaultCluster
# bootcamp-framework-topic: microservice MessageApi -> (consumer bootcamp-framework-consumer)
# bootcamp-producer:        producer MessageQueueProducer (the name comes from
#                           rocketmq.producer.customized-trace-topic; tracing itself is off)
TOPICS=(bootcamp-framework-topic bootcamp-producer)

mqadmin() { docker compose exec -T broker "${MQADMIN}" "$@"; }

echo "waiting for broker-a to register with ${NAMESRV} ..."
for _ in $(seq 1 30); do
  if mqadmin clusterList -n "${NAMESRV}" 2>/dev/null | grep -q "broker-a"; then
    break
  fi
  sleep 3
done
mqadmin clusterList -n "${NAMESRV}" 2>/dev/null | grep -q "broker-a" || { echo "broker not registered"; exit 1; }

for t in "${TOPICS[@]}"; do
  echo "topic ${t}"
  mqadmin updateTopic -n "${NAMESRV}" -c "${CLUSTER}" -t "${t}" -a +message.type=NORMAL 2>/dev/null | grep -E "success|already" || true
done

echo
echo "topics on ${CLUSTER}:"
# topicList -c prints "<cluster> <topic> <consumer group>" rows after a header line.
mqadmin topicList -n "${NAMESRV}" -c 2>/dev/null | awk -v c="${CLUSTER}" '$1 == c && $2 ~ /^bootcamp/ { print "  " $2 }' | sort
echo
echo "done. dashboard: http://127.0.0.1:8890  name-server for services: 127.0.0.1:9876"
