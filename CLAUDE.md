# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands
### Commands
| Task              | Command                                   |
| ----------------- |-------------------------------------------|
| Build             | `./mvnw clean package  `                  |
| Build (skip tests)| `./mvnw clean package -DskipTests`        |
| Run tests         | `./mvnw test`                             |
| Run single test   | `./mvnw test -Dtest=<ClassName>`          |
| Run single method | `./mvnw test -Dtest=<ClassName>#<method>` |
| Run locally       | `./mvnw spring-boot:run`                  |
| Format            | `./mvnw spotless:apply`                   |
| Lint check        | `./mvnw verify`                           |
| Build container   | `./mvnw spring-boot:build-image`          |
| Dependency check  | `./mvnw dependency-check:check`           |
| Show dep tree     | `./mvnw dependency:tree`                  |
Always run `./mvnw verify` before declaring a task done.

## Architecture

**xg-tsla-svc** is a minimal Spring Boot 4.0.x REST microservice (Java 25) that currently exposes health/version endpoints. It is a template/baseline service designed to be extended.

### Package Structure

```
xuyang.dev.xgtslasvc/
├── XgTslaSvcApplication.java   # Entry point; @EnableScheduling enabled
├── config/
│   └── OpenApiConfig.java      # Springdoc/Swagger config (server URLs currently commented out)
└── web/
    └── WebController.java      # All REST endpoints; @CrossOrigin enabled globally
```

### Key Endpoints (context path: `/xg-tsla-svc`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/sayhello` | Returns greeting string |
| GET | `/health` | Returns current time in LA timezone |
| GET | `/version` | Returns `env:version` string from ConfigMap + build info |
| GET | `/swagger-ui` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI JSON spec |

### Configuration

- **`application.yaml`** — sets Spring context path, default timezone, OpenAPI settings, and `APP_ENV`/`APP_VERSION` placeholders that are injected from Kubernetes ConfigMaps at runtime.
- **`k8s/dev/ConfigMap.yaml`** and **`k8s/prod/ConfigMap.yaml`** — supply environment-specific values (`APP_ENV`, `APP_VERSION`, `ARGO_API_TOKEN`).
- **`Dockerfile`** — multi-stage build; final image uses `eclipse-temurin:25`, runs as non-root user `spring` (UID 70501), timezone set to `America/Los_Angeles`.

### CI/CD

Defined in `.github/workflows/deploy.yaml`, triggered on push to `development`:

1. **Semantic Release** — parses conventional commits, bumps `pom.xml` version, updates `k8s/` deployment image tags, generates `CHANGELOG.md`, creates Git tag.
2. **Maven build** — `mvn clean package -Dmaven.test.skip=true`
3. **Docker build & push** — to `ghcr.io/lookmail88/xg-tsla-svc` (tagged `latest` + versioned).

Use [conventional commit](https://www.conventionalcommits.org/) format (`fix:`, `feat:`, `chore:`, etc.) — semantic-release drives versioning from commit types.

## Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | REST API |
| `springdoc-openapi-starter-webmvc-ui` 3.0.1 | Swagger UI + OpenAPI spec |
| `spring-boot-starter-actuator` | Health/metrics endpoints |
