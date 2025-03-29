```bash
docker compose down -v
docker compose build --no-cache
docker compose -p project -f ./infra/docker-compose.yaml up -d
```