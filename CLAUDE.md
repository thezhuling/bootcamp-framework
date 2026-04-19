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
mvn clean install -pl bootcamp-framework-ai -am

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=Chapter78ApplicationTests -pl bootcamp-framework-microservice

# Run integration tests
mvn verify
```

All modules compile with Java 25 `--enable-preview` (configured in root pom via `maven-compiler-plugin`). Tests require the same flag via `maven-surefire-plugin`.

## Running Services Locally

Infrastructure prerequisites (see `doc/docker/` for setup): **Nacos** (`:8848`), **Redis Stack Server** (`:6379`), **MySQL** (`:3306`), **RocketMQ** (`:9876`), **Sentinel Dashboard** (`:8858`), **OTLP collector** (`:4318`).

Start the auth server first — other services validate JWT against it:

```bash
mvn spring-boot:run -pl bootcamp-framework-auth
mvn spring-boot:run -pl bootcamp-framework-microservice
mvn spring-boot:run -pl bootcamp-framework-producer
mvn spring-boot:run -pl bootcamp-framework-gateway
OPENAI_API_KEY=<your-key> mvn spring-boot:run -pl bootcamp-framework-ai
```

## Architecture Overview

This is a **Spring Boot 4.0.3 / Spring Cloud 2025.1.0 / Java 25** multi-module Maven project.

### Modules

| Module | Role | Port |
|--------|------|------|
| `bootcamp-framework-gateway` | API Gateway (WebFlux/non-blocking) — JWT validation, rate limiting, circuit breaker | 8080 |
| `bootcamp-framework-microservice` | Core service — REST APIs, Redis caching, RocketMQ consumer | 8080 |
| `bootcamp-framework-producer` | Message producer service, Feign client to microservice | 8081 |
| `bootcamp-framework-ai` | Spring AI service — Chat, RAG, Embedding, Redis Vector Store | 8082 |
| `bootcamp-framework-auth` | OAuth2 Authorization Server — issues JWT tokens for all services | 9000 |
| `bootcamp-framework-toolkit` | Shared utility library | — |
| `bootcamp-framework-dto` | Shared DTOs (no framework dependencies) | — |

### Key Technology Choices

- **Service Discovery & Config:** Alibaba Nacos (`:8848`) — services register here; dynamic config (Redis credentials, feature toggles) is pushed from Nacos rather than stored in local `application.yml`. Each service uses a dedicated Nacos namespace (UUID placeholder in `application.yml`).
- **Security:** Spring Authorization Server (`bootcamp-framework-auth`) issues JWTs. All downstream services are OAuth2 Resource Servers that validate tokens against the auth server's JWKS endpoint (`http://localhost:9000/oauth2/jwks`). The Gateway forwards tokens downstream via `TokenRelay` filter.
- **Messaging:** Apache RocketMQ (`:9876`) — producer publishes, microservice consumes.
- **Caching:** Redis via `redis-om-spring` (ORM-style annotations on entities).
- **AI:** Spring AI 1.0.0 with OpenAI backend (`OPENAI_API_KEY` env var required). Redis Vector Store (reuses Redis Stack) for RAG. Chat, streaming, embedding, and RAG endpoints in `AiServiceImpl`.
- **Inter-service calls:** Spring Cloud OpenFeign with Nacos load balancing.
- **Circuit breaking:** Alibaba Sentinel + Resilience4j (Gateway uses reactor-resilience4j). Sentinel dashboard at `:8858`.
- **Virtual threads:** `spring.threads.virtual.enabled: true` in all services.
- **Observability:** Micrometer + OTel tracing (OTLP export to `:4318`), Prometheus metrics at `/actuator/prometheus`.
- **Gateway routing:** Defined as Java `@Bean RouteLocator` in `BootcampFrameworkGatewayApplication`, not in YAML.

### Gateway Route Map

| Path pattern | Downstream service | Notes |
|---|---|---|
| `/api/v1/**` (excl. `/api/v1/ai/**`) | `bootcamp-framework-microservice` | Rate limit 10 req/s, circuit breaker |
| `/api/v1/ai/**` | `bootcamp-framework-ai` | Rate limit 2 req/s |
| `/oauth2/**`, `/.well-known/**` | `bootcamp-framework-auth` | No auth required |
| `/message/**`, `/user/**` | `bootcamp-framework-producer` | TokenRelay |

### Data Flow

```
Client → Gateway (WebFlux, JWT validation) → downstream services
           ├── /api/v1/**   → Microservice (Redis cache, RocketMQ consumer, Feign→Producer)
           ├── /api/v1/ai/** → AI service (OpenAI, Redis Vector Store, RAG)
           ├── /oauth2/**   → Auth server (JWT issuance)
           └── /message/**  → Producer (RocketMQ publisher)
```

### Auth Flow

`bootcamp-framework-auth` uses Spring Authorization Server with an in-memory `RegisteredClient` (`bootcamp-client` / `bootcamp-secret`). RSA key pair is generated in memory on startup — **JWKs are not persisted**, so tokens issued before a restart become invalid. Supported flows: `client_credentials`, `authorization_code`, `refresh_token`.

## Docker Deployment

- Microservice `Dockerfile` uses `azul/zulu-openjdk-alpine:17-latest`, exposes port 8080, JVM heap fixed at 256MB.
- Infrastructure docker-compose/setup scripts are in `doc/docker/` (nacos, redis-stack, mysql subdirectories).
- Redis password and OpenAI API key: configured via Nacos / environment variables, not hardcoded.
