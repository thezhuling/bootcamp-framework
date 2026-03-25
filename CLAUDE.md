# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build all modules
mvn clean install

# Build all modules, skip tests
mvn clean install -DskipTests

# Build a single module (with dependencies)
mvn clean install -pl bootcamp-framework-microservice -am
mvn clean install -pl bootcamp-framework-producer -am

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=Chapter78ApplicationTests -pl bootcamp-framework-microservice

# Run integration tests
mvn verify
```

## Running Services Locally

Infrastructure prerequisites (see `doc/docker/` for setup): **Nacos** (`:8848`), **Redis Stack Server** (`:6379`), **MySQL** (`:3306`), **RocketMQ** (`:9876`).

```bash
# Start a service
mvn spring-boot:run -pl bootcamp-framework-microservice
mvn spring-boot:run -pl bootcamp-framework-producer
mvn spring-boot:run -pl bootcamp-framework-gateway
```

## Architecture Overview

This is a **Spring Boot 4.0.3 / Spring Cloud 2025.1.0** multi-module Maven project structured as microservices.

### Modules

| Module | Role | Port |
|--------|------|------|
| `bootcamp-framework-gateway` | API Gateway (WebFlux/non-blocking) | 8080 |
| `bootcamp-framework-microservice` | Core service — REST APIs, Redis caching, RocketMQ consumer | 8080 |
| `bootcamp-framework-producer` | Message producer service, Feign client to microservice | 8081 |
| `bootcamp-framework-toolkit` | Shared utility library |  |
| `bootcamp-framework-dto` | Shared DTOs (no framework dependencies) |  |

### Key Technology Choices

- **Service Discovery & Config:** Alibaba Nacos (`:8848`) — services register here; dynamic config (Redis credentials, feature toggles) is pushed from Nacos rather than stored in local `application.yml`.
- **Messaging:** Apache RocketMQ (`:9876`) — producer publishes, microservice consumes.
- **Caching:** Redis via `redis-om-spring` (ORM-style annotations on entities).
- **Inter-service calls:** Spring Cloud OpenFeign with Nacos load balancing.
- **Circuit breaking:** Alibaba Sentinel + Resilience4j (Gateway uses reactor-resilience4j).
- **Gateway routing:** Defined as Java `@Bean RouteLocator` in `BootcampFrameworkGatewayApplication`, not in YAML.

### Data Flow

```
Client → Gateway (WebFlux) → Microservice REST APIs
                                  ├── TrendingNewsService → RSS feeds (Google/BBC News) → Redis cache
                                  ├── Feign client ↔ Producer service
                                  └── RocketMQ consumer ← Producer publishes messages
```

### Nacos Namespaces

Each service uses a dedicated Nacos namespace (UUID) configured in `application.yml` under `spring.cloud.nacos`. Sensitive config like Redis passwords is managed via Nacos config center, not committed to source.

### Scheduled Tasks

`TrendingNewsTask` in the microservice runs every 30 minutes to refresh trending news from RSS feeds and store results in Redis.

## Docker Deployment

- Microservice `Dockerfile` uses `azul/zulu-openjdk-alpine:17-latest`, exposes port 8080, JVM heap fixed at 256MB.
- Infrastructure docker-compose/setup scripts are in `doc/docker/` (nacos, redis-stack, mysql subdirectories).
- Redis password: configured in Nacos (not hardcoded in app config).