```bash
docker compose down -v
docker compose build --no-cache
docker compose -p project -f ./infra/docker-compose.yaml up -d
```

# DEV
### Create kafka network
```bash
docker network create proxynet
```

### Up kafka cluster
```bash
docker compose -p dev-cluster -f ./infra/docker-compose-dev-cluster.yaml up -d
```
### Create kafka topics
```bash
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic data-products-topic --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 1
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic data-products-topic-raw --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 1
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic filter-names-topic --bootstrap-server kafka-0:9092 --partitions 3 --replication-factor 1
```