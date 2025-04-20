### Create network
```bash
docker network create proxynet
```

### Up hadoop and kafka-connect services. Spark is commented because it just doesn't run on win(((
```bash
docker compose down -v
docker compose build --no-cache
docker compose -p project -f ./infra/docker-compose.yaml up -d
```
### Replace local jdk cacerts with .truststore/cacerts file
### Run Application, go to localhost:8080.
### Then click buttons in any order you want.

No grafana, because cloud cluster has metrics and doesn't expose jmx port
No Spark because this thing doesn't run on win. I have only win11.
Отсылаю как есть. Уже просто нет сил ковырять эту таску.


