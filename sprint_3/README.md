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

### Create kafka topic: get container_id of kafka-0-1 service. Use 'docker ps' command for it
```bash
docker exec -it b8c9d204551f /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic user_messages --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 2
docker exec -it b8c9d204551f /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic user_block_events --bootstrap-server kafka-0:9092 --replication-factor 2
docker exec -it b8c9d204551f /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic forbidden_words --bootstrap-server kafka-0:9092 --replication-factor 2
```

### Create app network
```bash
docker network create sprint_3_app_net
```

### Package jar
```bash
.\mvnw clean package
```

### Build app image
```bash
docker build -f .docker/Dockerfile -t sprint_3_app:latest .
```

### Up app
```bash
docker compose -f ./.docker/docker-compose-app.yaml up -d
```
