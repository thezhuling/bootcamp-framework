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
mvn test -Dtest=GatewayApplicationContextTest -pl bootcamp-framework-gateway

# Run integration tests
mvn verify
```

All modules compile with `maven.compiler.release=25`. No preview features are used, so no `--enable-preview` flag is needed anywhere — do not reintroduce it without an actual preview API to justify it.

Each service module has a `*ApplicationContextTest` that refreshes the full Spring context with Nacos and RocketMQ switched off or mocked. These are the regression guard for framework upgrades — a moved autoconfiguration class or an ambiguous bean surfaces here rather than at deploy time. Keep them passing.

## Running Services Locally

Infrastructure prerequisites (see `doc/docker/` for setup): **Nacos** (`:8848`), **MySQL** (`:3306`), **RocketMQ** (`:9876`), **Sentinel Dashboard** (`:8858`), **OTLP collector** (`:4318`).

Start the auth server first — other services validate JWT against it:

```bash
mvn spring-boot:run -pl bootcamp-framework-auth
mvn spring-boot:run -pl bootcamp-framework-microservice
mvn spring-boot:run -pl bootcamp-framework-producer
mvn spring-boot:run -pl bootcamp-framework-gateway
OPENAI_API_KEY=<your-key> mvn spring-boot:run -pl bootcamp-framework-ai
```

## Architecture Overview

This is a **Spring Boot 4.1.1 / Spring Cloud 2025.1.3 / Java 25** multi-module Maven project.

Spring Cloud and Boot versions are coupled: Spring Cloud's compatibility verifier is on by default and fails startup on a mismatch. 2025.1.2 is the oldest train that accepts Boot 4.1.x. Keep `spring-cloud-dependencies` imported ahead of `spring-cloud-alibaba-dependencies` so `spring-cloud-commons` resolves from the newer train rather than SCA's older parent.

### Modules

| Module | Role | Port |
|--------|------|------|
| `bootcamp-framework-gateway` | API Gateway (WebFlux/non-blocking) — JWT validation, circuit breaker | 8080 |
| `bootcamp-framework-microservice` | Core service — REST APIs, RocketMQ consumer | 8080 |
| `bootcamp-framework-producer` | Message producer service, Feign client to microservice | 8081 |
| `bootcamp-framework-ai` | Spring AI service — Chat, Embedding | 8082 |
| `bootcamp-framework-auth` | OAuth2 Authorization Server — issues JWT tokens for all services | 9000 |
| `bootcamp-framework-toolkit` | Shared utility library | — |
| `bootcamp-framework-dto` | Shared DTOs (no framework dependencies) | — |

### Key Technology Choices

- **Service Discovery & Config:** Alibaba Nacos (`:8848`) — services register here; dynamic config (feature toggles) is pushed from Nacos rather than stored in local `application.yml`. Each service uses a dedicated Nacos namespace (UUID placeholder in `application.yml`).
- **Security:** Spring Authorization Server (`bootcamp-framework-auth`) issues JWTs. All downstream services are OAuth2 Resource Servers that validate tokens against the auth server's JWKS endpoint (`http://localhost:9000/oauth2/jwks`). The Gateway forwards tokens downstream via `TokenRelay` filter.
- **Messaging:** Apache RocketMQ (`:9876`) — producer publishes, microservice consumes.
- **AI:** Spring AI 2.0.1 with OpenAI backend (`OPENAI_API_KEY` env var required). Chat, streaming and embedding endpoints in `AiServiceImpl`.
- **Inter-service calls:** Spring Cloud OpenFeign with Nacos load balancing.
- **Circuit breaking:** Alibaba Sentinel + Resilience4j. The Gateway uses reactor-resilience4j for circuit breaking and excludes Sentinel's circuit breaker autoconfiguration in `application.yml` — with both present, Sentinel's factory wins and the gateway's `.circuitBreaker()` filter cannot be created. Sentinel still does flow control there. The gateway's breakers time out after `bootcamp.gateway.circuit-breaker.timeout` (default 5s, set in `GatewayCircuitBreakerConfig`); without it Resilience4j's 1s default silently sends slower healthy calls to `/fallback`. Sentinel dashboard at `:8858`.
- **Virtual threads:** `spring.threads.virtual.enabled: true` in all services.
- **Observability:** Micrometer + OTel tracing (OTLP export to `:4318`), Prometheus metrics at `/actuator/prometheus`.
- **Gateway routing:** Defined as Java `@Bean RouteLocator` in `BootcampFrameworkGatewayApplication`, not in YAML.

### Gateway Route Map

| Path pattern | Downstream service | Notes |
|---|---|---|
| `/api/v1/**` (excl. `/api/v1/ai/**`) | `bootcamp-framework-microservice` | TokenRelay, circuit breaker |
| `/api/v1/ai/**` | `bootcamp-framework-ai` | TokenRelay |
| `/oauth2/**`, `/.well-known/**` | `bootcamp-framework-auth` | No auth required |
| `/message/**`, `/user/**` | `bootcamp-framework-producer` | TokenRelay |

### Data Flow

```
Client → Gateway (WebFlux, JWT validation) → downstream services
           ├── /api/v1/**   → Microservice (RocketMQ consumer, Feign→Producer)
           ├── /api/v1/ai/** → AI service (OpenAI chat, embedding)
           ├── /oauth2/**   → Auth server (JWT issuance)
           └── /message/**  → Producer (RocketMQ publisher)
```

### Auth Flow

`bootcamp-framework-auth` uses Spring Authorization Server with an in-memory `RegisteredClient` (`bootcamp-client` / `bootcamp-secret`). RSA key pair is generated in memory on startup — **JWKs are not persisted**, so tokens issued before a restart become invalid. Supported flows: `client_credentials`, `authorization_code`, `refresh_token`.

Both security filter chains live in `auth/config/SecurityConfig`: `@Order(1)` for the protocol endpoints, `@Order(2)` for form login. Boot's own authorization server chain backs off as soon as any `SecurityFilterChain` bean exists, so the `@Order(1)` chain must stay — without it `/oauth2/token`, `/oauth2/jwks` and `/.well-known/openid-configuration` all redirect to `/login` while the context still loads cleanly. `AuthApplicationContextTest` calls those endpoints over HTTP to guard this.

### Actuator

Every service permits only `/actuator/health/**`, `/actuator/info` and `/actuator/prometheus` anonymously; everything else under `/actuator` needs a token (or a login, on auth). `shutdown` is not exposed anywhere. The resource servers and the gateway run with CSRF disabled, so do not widen the anonymous matcher back to `/actuator/**` — with `shutdown` exposed that let a single anonymous POST stop the service. Each `*ApplicationContextTest` asserts this over HTTP.

## Docker Deployment

- Microservice `Dockerfile` uses `azul/zulu-openjdk-alpine:25-jre`, exposes port 8080, JVM heap fixed at 256MB. The base image must stay on the same major as `maven.compiler.release`, and the image copies the repackaged jar, so `mvn package` has to have run the `repackage` goal (bound in the root pom).
- Infrastructure docker-compose/setup scripts are in `doc/docker/` (nacos, mysql subdirectories).
- OpenAI API key: configured via Nacos / environment variables, not hardcoded.
