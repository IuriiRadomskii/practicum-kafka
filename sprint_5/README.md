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

# TASK_2: SSL/TLS and ACL
#### Delete task_1 related containers and volumes
#### see file ./infra/task_2/make_certs.sh
#### make_certs.sh should be executed on local Ubuntu wsl subsystem. openssl utility and jre21 are required
#### Generated dirs should be copied to ./infra/task_2 directory

### Up kafka cluster
```bash
docker compose -p sprint_5_task_2 -f ./infra/docker-compose-task-2.yaml up -d
```

### Create kafka topics: topic-1, topic-2
```bash
docker exec -it kafka-1 /usr/bin/kafka-topics --create --topic topic-1 --bootstrap-server localhost:9093 --command-config /etc/kafka/secrets/utility.properties --partitions 1 --replication-factor 3 
docker exec -it kafka-1 /usr/bin/kafka-topics --create --topic topic-2 --bootstrap-server localhost:9093 --command-config /etc/kafka/secrets/utility.properties --partitions 1 --replication-factor 3 
```

### Add entries to ACL
```bash
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:producer --operation Write           --topic topic-1
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:producer --operation Describe        --topic topic-1
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:producer --operation DescribeConfigs --topic topic-1
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:producer --operation All --cluster
```
```bash
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:consumer --operation Read     --topic topic-1
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:consumer --operation Describe --topic topic-1
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:consumer --operation Read     --group group-consumer
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:consumer --operation Describe --group group-consumer
```
```bash
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:producer --operation Write           --topic topic-2
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:producer --operation Describe        --topic topic-2
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --add --allow-principal User:producer --operation DescribeConfigs --topic topic-2
```

### Wait a little and execute command to show list of privileges 
```bash
docker exec -it kafka-1 /usr/bin/kafka-acls --bootstrap-server=localhost:9093 --command-config /etc/kafka/secrets/utility.properties --list
```

### Cluster operates nice and fine. It means that SASL_PLAIN authentication mechanism works inside cluster
