# Local Nacos for bootcamp-framework

Nacos **3.1.1** (standalone, MySQL-backed) in Docker Compose. The server version is pinned to the
`nacos-client` that `spring-cloud-alibaba-dependencies` resolves for this project — check with
`mvn dependency:tree -pl bootcamp-framework-microservice | grep nacos-client` and bump both together.

## Files

| File | Purpose |
|---|---|
| `docker-compose.yml` | `nacos` (3.1.1-slim) + `nacos-mysql` (8.4) with named volumes |
| `.env.example` → `.env` | credentials, auth token, namespace/group names — read by compose **and** the init script. `.env` is git-ignored |
| `mysql-schema.sql` | Nacos 3.1.1 schema, applied by MySQL's `docker-entrypoint-initdb.d` on the first start only |
| `init-nacos.sh` | idempotent bootstrap: admin user → login → namespace → publish seeds |
| `seed/*.yml` | bootstrap defaults, one file per service, published as `dataId=<file name>` in `bootcamp-dev` / `BOOTCAMP` |
| `seed-local/*.yml` | git-ignored overrides with the real values; same file name wins over `seed/` |

## First start

```shell
cd doc/docker/nacos
cp .env.example .env
sed -i '' "s|^NACOS_AUTH_TOKEN=.*|NACOS_AUTH_TOKEN=$(openssl rand -base64 48 | tr -d '\n')|" .env
# optionally: mkdir seed-local && cp seed/bootcamp-framework-microservice.yml seed-local/ && edit the real values
docker compose up -d --wait     # ~1 min: MySQL init + Nacos boot
./init-nacos.sh                 # admin user, namespace, seed configs
```

Then:

- API: `http://localhost:8848/nacos` — what `spring.cloud.nacos.server-addr: 127.0.0.1:8848` talks to
- gRPC: `9848` (server port + 1000; the client derives it, do not remap)
- Console: `http://localhost:8880` — login `nacos` / `nacos`. Host port 8080 belongs to the gateway.

## Day to day

```shell
docker compose up -d            # start (data is in the volumes, survives container removal)
docker compose stop             # stop, keep data
docker compose down -v          # wipe everything, then repeat "First start"
./init-nacos.sh                 # publishes only dataIds that do not exist yet
FORCE=1 ./init-nacos.sh         # overwrite from seed-local/ or seed/ after editing a file
docker compose logs -f nacos
```

Once seeded, Nacos itself is the source of truth — edit in the console at will; a plain
`./init-nacos.sh` never overwrites what is there.

`MYSQL_ROOT_PASSWORD` / `MYSQL_NACOS_PASSWORD` are baked into the MySQL volume on first start, so
`.env` must keep the values the volume was created with; change them only together with
`docker compose down -v`. Likewise a lost `.env` cannot be rebuilt from `.env.example` against an
existing volume.

## What the services expect

Every service registers in namespace **`bootcamp-dev`**, group **`BOOTCAMP`** (see each
`application.yml`). Nacos discovery is namespace-scoped, so the gateway's `lb://` routes and the
microservice→producer Feign call only work when all of them share this namespace.

Config is per service through `spring.config.import: optional:nacos:<app-name>.yml?group=BOOTCAMP`.
`seed/` is the source of truth for what a service needs there:

- `bootcamp-framework-microservice.yml` — **required**: `microservice.ttl/app-key/secret` have no
  defaults, the service fails to start without them. Also sets `server.port: 20000`. The committed
  seed carries `change-me` placeholders; put the real pair in `seed-local/`.
- `bootcamp-framework-gateway.yml` — optional, `bootcamp.gateway.circuit-breaker.timeout` (default 5s).
- `bootcamp-framework-producer.yml` — `server.port: 8081`; the producer has no port of its own and
  Boot's 8080 default collides with the gateway.

Add a new service's config by dropping `seed/<app-name>.yml` and re-running `init-nacos.sh`.

## Auth

`NACOS_AUTH_ENABLE=true`: clients must log in, which is why every `application.yml` carries
`username: nacos / password: nacos`. `NACOS_AUTH_TOKEN` (base64, ≥32 bytes) signs the JWTs and
`NACOS_AUTH_IDENTITY_KEY/VALUE` is the server-to-server identity — Nacos ≥2.2.1 refuses to start
without them. All three live in the git-ignored `.env`; `.env.example` only shows the shape.

## 3.x API notes (for scripting)

- `/nacos/v1/console/**` and `/nacos/v2/console/**` answer **410 Gone**; readiness is
  `GET http://localhost:8880/v3/console/health/readiness` (console port).
- Admin bootstrap: `POST /nacos/v3/auth/user/admin` with `password=…` — 409 once an admin exists.
- Login: `POST /nacos/v3/auth/user/login` (`username`, `password`) → `accessToken`, pass it as a
  form/query parameter to the admin API.
- Namespace: `POST /nacos/v3/admin/core/namespace` (`namespaceId`, `namespaceName`, `namespaceDesc`).
- Config: `POST /nacos/v3/admin/cs/config` (`dataId`, `groupName`, `namespaceId`, `type`, `content`).

## Running a service inside Docker

A containerised service cannot reach `127.0.0.1:8848`. Either join the compose network
(`--network bootcamp-nacos_default`, server-addr `nacos:8848`) or use `host.docker.internal:8848`.
Override with `-e SPRING_CLOUD_NACOS_SERVER_ADDR=...`.
