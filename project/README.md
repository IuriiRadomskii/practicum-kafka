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