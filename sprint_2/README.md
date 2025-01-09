Go to sprint_2 dir and execute these command one by one

### Create kafka network
```bash
docker network create sprint_2_kafka_net
```

### Up kafka cluster
```bash
docker compose -f ./.docker/docker-compose.yaml up -d
```

### !Wait for kafka cluster is up and running!

### Create kafka topic: get container_id of kafka-0 service. Use docker ps for it
```bash
docker exec -it CONTAINER_ID /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic trx_statuses --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 2
```

### Create app network
```bash
docker network create sprint_2_app_net
```

### Package jar
```bash
.\mvnw clean package
```

### Build app image
```bash
docker build -f .docker/Dockerfile -t sprint_2_app:latest .
```

### Up app
```bash
docker compose -f ./.docker/docker-compose-app.yaml up -d
```