Go to sprint_3 dir and execute these command one by one

### Create kafka network
```bash
docker network create sprint_3_kafka_net
```

### Up kafka cluster
```bash
docker compose -p sprint_3_cluster -f ./.docker/docker-compose.yaml up -d
```

### !Wait for kafka cluster is up and running!

### Create kafka topic: get container_id of kafka-0 service. Use docker ps for it
```bash
docker exec -it 0d312407a805 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic input_topic --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 2
docker exec -it 0d312407a805 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic output_topic --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 2
```
