# Scala 3 Typelevel Examples

## [Geolocation Service](geolocation)

Geolocation service for operations with addresses and GPS coordinates. There is a docker compose for both the geolocation service and Grafana Loki for logs.

### Start grafana loki

```shell
docker compose -f docker/loki/docker-compose.yml up -d
```

Once the Grafana stack is fully running, you can access the web UI at http://localhost:3000. The default username/password is admin/admin.

### Start geolocation service

```shell
docker compose -f docker/geolocation/docker-compose.yml up -d
```

### Stop geolocation service and grafana loki:

```shell
docker compose -f docker/geolocation/docker-compose.yml down
docker compose -f docker/loki/docker-compose.yml down
```

### Curl examples

Request GPS coordinates for an address:

```shell
curl -v -X POST localhost:8080/coords -d '{"street": "123 Anywhere St.", "city": "New York", "state": "NY"}'
```

Create a new address:

```shell
curl -v -X POST localhost:8080/coords/new -d '{"id": 3, "street": "123 Anywhere St.", "city": "New York", "state": "NY", "coords": { "lat": 10, "lon": 10 } }'
```

## [cats-examples](cats-examples)

Various examples with cats and cats-effect.
