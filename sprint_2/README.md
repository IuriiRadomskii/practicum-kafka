### Build java app image
```bash
docker build -f .docker/Dockerfile -t sprint_2_app:latest .
```

### Create network
```bash
docker network create sprint_2_kafka_net
```

### Up cluster
```bash
docker compose -f ./.docker/docker-compose.yaml up -d
```

### Create topic
```bash
docker exec -it 1dccd49e00b9 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic trx_statuses --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 2
```