# TASK_1: Cluster topic reassignment
### Create kafka network
```bash
docker network create proxynet_task_1
```

### Up kafka cluster
```bash
docker compose -p sprint_5_task_1 -f ./infra/docker-compose-task-1.yaml up -d
```

### !Wait for kafka cluster is up and running!

### Create kafka topic: balanced-topic
```bash
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic balanced-topic --bootstrap-server kafka-0:9092 --partitions 8 --replication-factor 3
```
### Describe topic
```bash 
docker exec -t kafka-0 /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic balanced-topic
```
### Copy json file with reassignment plan to broker container
```bash
docker cp ./infra/task_1/reassignment.json kafka-0:/
```
### Generate reassignment plan
```bash
docker exec -t kafka-0 /opt/bitnami/kafka/bin/kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --broker-list "0,1,2" --topics-to-move-json-file "/reassignment.json" --generate
```
### Execute reassignment plan: describe topic, configuration has changed
```bash
docker exec -t kafka-0 /opt/bitnami/kafka/bin/kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --reassignment-json-file /reassignment.json --execute
```
### Stopped broker is not in ISR list, use describe topic
```bash
docker stop kafka-2
```
### After some time kafka-2 broker will appear in ISR list for all partitions, use describe topic
```bash
docker start kafka-2
```

# TASK_2: SSL/TLS
#### Delete task_1 related containers and volumes
#### All commands are executed on local Ubuntu wsl subsystem. openssl utility and jre21 are required
#### see file ./infra/task_2/make_certs.sh
#### Copy generated dirs to ./infra/task_2 directory

### Create kafka network
```bash
docker network create proxynet_task_2
```

### Up kafka cluster
```bash
docker compose -p sprint_5_task_2 -f ./infra/docker-compose-task-2.yaml up -d
```

### Smoke-test: kafka-ui can collect metadata about cluster

### Create kafka topics: topic-1, topic-2
```bash
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topic-1 --bootstrap-server kafka-0:9091 --partitions 3 --replication-factor 3
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topic-2 --bootstrap-server kafka-0:9091 --partitions 3 --replication-factor 3
```

### Add entries to ACL
```bash
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-acls.sh --bootstrap-server=kafka-0:9091 --add --allow-principal User:PRODUCER --operation Write --topic topic-1
```
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-acls.sh --bootstrap-server=kafka-0:9091 --add --allow-principal User:PRODUCER --operation Describe --topic topic-1
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-acls.sh --bootstrap-server=kafka-0:9091 --add --allow-principal User:CONSUMER --operation Read --topic topic-1
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-acls.sh --bootstrap-server=kafka-0:9091 --add --allow-principal User:CONSUMER --operation Describe --topic topic-1

docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-acls.sh --bootstrap-server=kafka-0:9091 --add --allow-principal User:PRODUCER --operation Write --topic topic-2
docker exec -it kafka-0 /opt/bitnami/kafka/bin/kafka-acls.sh --bootstrap-server=kafka-0:9091 --add --allow-principal User:PRODUCER --operation Describe --topic topic-2

