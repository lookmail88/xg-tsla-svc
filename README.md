# xg-tsla-svc

[English](#english) | [中文](#中文)

---

## English

A Spring Boot 4.0 REST microservice (Java 25) that tracks TSLA stock prices in real time. It polls the Polygon.io API every minute, persists OHLCV data to PostgreSQL, and exposes REST endpoints for querying price history.

### Tech Stack

- **Java 25** / **Spring Boot 4.0.5**
- **Spring Data JPA** + **PostgreSQL** — price quote persistence
- **Polygon.io API** — real-time and previous-day OHLCV data
- **Springdoc OpenAPI 3.0.1** — Swagger UI
- **Spring Boot Actuator** — health and metrics
- **Docker** (eclipse-temurin:25, non-root)
- **Kubernetes** manifests for dev and prod
- **GitHub Actions** CI/CD with semantic-release

### Getting Started

#### Prerequisites

- Java 25+
- Maven 3.9+
- PostgreSQL (or use the K8s manifests)
- Polygon.io API token
- Docker (optional)

#### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_HOST` | PostgreSQL host | *(required)* |
| `POSTGRES_PORT` | PostgreSQL port | `30432` |
| `POSTGRES_DB` | Database name | `tsla_db` |
| `POSTGRES_USER` | Database user | `tsla_user` |
| `POSTGRES_PASSWORD` | Database password | *(required)* |
| `MASSIVE_API_TOKEN` | Polygon.io API token | *(required)* |
| `APP_ENV` | Deployment environment | `local` |
| `APP_VERSION` | Application version | injected by K8s |
| `ARGO_API_TOKEN` | Argo CD API token | injected by K8s |

#### Build and Run

```bash
# Build the JAR (skipping tests)
mvn clean package -Dmaven.test.skip=true

# Run locally (requires env vars above)
java -jar target/app.jar

# Build Docker image
docker build -t xg-tsla-svc .

# Run in Docker
docker run -p 8080:8080 \
  -e POSTGRES_HOST=localhost \
  -e POSTGRES_PASSWORD=secret \
  -e MASSIVE_API_TOKEN=your_token \
  xg-tsla-svc
```

### API Endpoints

All endpoints are served under the context path `/xg-tsla-svc`.

#### Price Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/xg-tsla-svc/api/v1/price/latest` | Most recent TSLA price quotes (default: last 60) |
| GET | `/xg-tsla-svc/api/v1/price/today` | All TSLA quotes from the last 24 hours |
| GET | `/xg-tsla-svc/api/v1/price/fetch` | Manually trigger a Polygon.io price fetch |

**Query parameters:**

- `GET /api/v1/price/latest?limit=N` — override the default record limit (default `60`)

#### System Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/xg-tsla-svc/sayhello` | Returns a greeting string |
| GET | `/xg-tsla-svc/health` | Returns current time (America/Los_Angeles) |
| GET | `/xg-tsla-svc/version` | Returns environment and version info |
| GET | `/xg-tsla-svc/db/status` | Returns PostgreSQL connection status |
| GET | `/xg-tsla-svc/swagger-ui` | Swagger UI |
| GET | `/xg-tsla-svc/v3/api-docs` | OpenAPI JSON spec |

### Schedulers

| Scheduler | Cron | Description |
|-----------|------|-------------|
| `PriceScheduler` | Every minute (`0 * * * * *`) | Fetches TSLA OHLCV snapshot from Polygon.io and persists to DB |
| `DividendScheduler` | Daily at 09:00 (`0 0 9 * * *`) | Fetches TSLA dividend data from Polygon.io |

The price fetch attempts the real-time snapshot endpoint first; if that returns 403 (free-tier plan), it falls back to the previous-day bar. Duplicate quotes (same symbol + timestamp) are silently skipped.

Cron schedules are configurable via properties:

```yaml
massive:
  api:
    price-cron: "0 * * * * *"   # default: every minute
    cron: "0 0 9 * * *"         # default: 09:00 daily
```

### Data Model

**`price_quotes` table**

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-generated |
| `symbol` | VARCHAR | Ticker symbol (e.g. `TSLA`) |
| `timestamp` | TIMESTAMPTZ | Bar timestamp (UTC) |
| `open` | DECIMAL(12,4) | Opening price |
| `high` | DECIMAL(12,4) | High price |
| `low` | DECIMAL(12,4) | Low price |
| `close_price` | DECIMAL(12,4) | Closing price |
| `volume` | BIGINT | Volume |
| `vwap` | DECIMAL(12,4) | Volume-weighted average price |

### Project Structure

```
xg-tsla-svc/
├── src/main/java/xuyang/dev/xgtslasvc/
│   ├── XgTslaSvcApplication.java       # Entry point (@EnableScheduling)
│   ├── config/
│   │   └── OpenApiConfig.java          # Springdoc/Swagger configuration
│   ├── dto/
│   │   ├── PolygonSnapshotResponse.java # Polygon snapshot API response
│   │   └── PolygonPrevDayResponse.java  # Polygon prev-day bar response
│   ├── entity/
│   │   └── PriceQuote.java             # JPA entity for price_quotes table
│   ├── repository/
│   │   └── PriceQuoteRepository.java   # Spring Data JPA repository
│   ├── scheduler/
│   │   ├── PriceScheduler.java         # Minutely TSLA price fetch
│   │   └── DividendScheduler.java      # Daily dividend fetch
│   ├── service/
│   │   ├── PriceService.java           # Fetch + deduplicate + persist logic
│   │   └── MassiveApiService.java      # Polygon.io REST client
│   └── web/
│       ├── PriceController.java        # /api/v1/price/* endpoints
│       └── WebController.java          # Health/version/db endpoints
├── k8s/
│   ├── dev/                            # Dev Kubernetes manifests
│   └── prod/                           # Prod Kubernetes manifests
├── .github/workflows/
│   └── deploy.yaml                     # CI/CD pipeline
├── Dockerfile                          # Multi-stage Docker build
└── pom.xml
```

### CI/CD

The GitHub Actions pipeline (`.github/workflows/deploy.yaml`) triggers on push to `development`:

1. **Semantic Release** -- parses conventional commits, bumps the version in `pom.xml`, updates K8s deployment tags, generates `CHANGELOG.md`, and creates a Git tag.
2. **Maven Build** -- compiles and packages the JAR.
3. **Docker Build & Push** -- pushes to `ghcr.io/lookmail88/xg-tsla-svc` with `latest` and versioned tags.

#### Commit Convention

This project uses [semantic-release](https://github.com/semantic-release/semantic-release) to automate versioning and releases. All commits must follow the [Conventional Commits](https://www.conventionalcommits.org/) format:

- `feat: ...` -- triggers a **minor** version bump (e.g. 0.2.0 -> 0.3.0)
- `fix: ...` -- triggers a **patch** version bump (e.g. 0.2.0 -> 0.2.1)
- `docs:`, `chore:`, `refactor:`, `test:`, `style:`, `ci:` -- no version bump
- `feat!: ...` or `fix!: ...` (with `!`) -- triggers a **major** version bump (e.g. 0.2.0 -> 1.0.0)
- `BREAKING CHANGE:` in the commit footer -- also triggers a **major** version bump

### Docker

The image runs as a non-root user (`spring`, UID 70501) with the timezone set to `America/Los_Angeles`.

```bash
# Build
docker build -t xg-tsla-svc .

# Run
docker run -p 8080:8080 \
  -e POSTGRES_HOST=localhost \
  -e POSTGRES_PASSWORD=secret \
  -e MASSIVE_API_TOKEN=your_token \
  xg-tsla-svc
```

### License

See [LICENSE](LICENSE) for details.

---

## 中文

一个基于 Spring Boot 4.0 的 REST 微服务（Java 25），用于实时追踪 TSLA 股票价格。每分钟从 Polygon.io API 拉取数据，将 OHLCV 数据持久化到 PostgreSQL，并提供 REST 接口查询历史价格。

### 技术栈

- **Java 25** / **Spring Boot 4.0.5**
- **Spring Data JPA** + **PostgreSQL** — 价格数据持久化
- **Polygon.io API** — 实时及前一日 OHLCV 数据
- **Springdoc OpenAPI 3.0.1** — Swagger UI 接口文档
- **Spring Boot Actuator** — 健康检查和监控指标
- **Docker**（eclipse-temurin:25，非 root 用户运行）
- **Kubernetes** — 包含 dev 和 prod 环境部署清单
- **GitHub Actions** — CI/CD 流水线，集成 semantic-release 自动版本管理

### 快速开始

#### 环境要求

- Java 25+
- Maven 3.9+
- PostgreSQL（或使用 K8s 部署清单）
- Polygon.io API Token
- Docker（可选）

#### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `POSTGRES_HOST` | PostgreSQL 地址 | *(必填)* |
| `POSTGRES_PORT` | PostgreSQL 端口 | `30432` |
| `POSTGRES_DB` | 数据库名 | `tsla_db` |
| `POSTGRES_USER` | 数据库用户名 | `tsla_user` |
| `POSTGRES_PASSWORD` | 数据库密码 | *(必填)* |
| `MASSIVE_API_TOKEN` | Polygon.io API Token | *(必填)* |
| `APP_ENV` | 部署环境 | `local` |
| `APP_VERSION` | 应用版本号 | 由 K8s 注入 |
| `ARGO_API_TOKEN` | Argo CD API Token | 由 K8s 注入 |

#### 构建与运行

```bash
# 构建 JAR 包（跳过测试）
mvn clean package -Dmaven.test.skip=true

# 本地运行（需提前配置环境变量）
java -jar target/app.jar

# 构建 Docker 镜像
docker build -t xg-tsla-svc .

# 使用 Docker 运行
docker run -p 8080:8080 \
  -e POSTGRES_HOST=localhost \
  -e POSTGRES_PASSWORD=secret \
  -e MASSIVE_API_TOKEN=your_token \
  xg-tsla-svc
```

### API 接口

所有接口的上下文路径为 `/xg-tsla-svc`。

#### 价格接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/xg-tsla-svc/api/v1/price/latest` | 最新 TSLA 价格记录（默认最近 60 条） |
| GET | `/xg-tsla-svc/api/v1/price/today` | 过去 24 小时内所有 TSLA 价格记录 |
| GET | `/xg-tsla-svc/api/v1/price/fetch` | 手动触发一次 Polygon.io 价格拉取 |

**查询参数：**

- `GET /api/v1/price/latest?limit=N` — 指定返回条数（默认 `60`）

#### 系统接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/xg-tsla-svc/sayhello` | 返回问候字符串 |
| GET | `/xg-tsla-svc/health` | 返回当前时间（美西时区） |
| GET | `/xg-tsla-svc/version` | 返回环境和版本信息 |
| GET | `/xg-tsla-svc/db/status` | 返回 PostgreSQL 连接状态 |
| GET | `/xg-tsla-svc/swagger-ui` | Swagger UI 接口文档 |
| GET | `/xg-tsla-svc/v3/api-docs` | OpenAPI JSON 规范 |

### 定时任务

| 调度器 | Cron 表达式 | 说明 |
|--------|-------------|------|
| `PriceScheduler` | 每分钟（`0 * * * * *`） | 从 Polygon.io 拉取 TSLA OHLCV 快照并持久化 |
| `DividendScheduler` | 每日 09:00（`0 0 9 * * *`） | 从 Polygon.io 拉取 TSLA 分红数据 |

价格拉取优先调用实时快照接口；若返回 403（免费套餐不支持），则自动降级为前一日 K 线数据。相同 symbol + 时间戳的重复记录会被静默跳过。

Cron 表达式可通过配置项覆盖：

```yaml
massive:
  api:
    price-cron: "0 * * * * *"   # 默认：每分钟
    cron: "0 0 9 * * *"         # 默认：每日 09:00
```

### 数据模型

**`price_quotes` 表**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT（主键） | 自增 |
| `symbol` | VARCHAR | 股票代码（如 `TSLA`） |
| `timestamp` | TIMESTAMPTZ | K 线时间戳（UTC） |
| `open` | DECIMAL(12,4) | 开盘价 |
| `high` | DECIMAL(12,4) | 最高价 |
| `low` | DECIMAL(12,4) | 最低价 |
| `close_price` | DECIMAL(12,4) | 收盘价 |
| `volume` | BIGINT | 成交量 |
| `vwap` | DECIMAL(12,4) | 成交量加权平均价 |

### 项目结构

```
xg-tsla-svc/
├── src/main/java/xuyang/dev/xgtslasvc/
│   ├── XgTslaSvcApplication.java       # 应用入口（@EnableScheduling）
│   ├── config/
│   │   └── OpenApiConfig.java          # Springdoc/Swagger 配置
│   ├── dto/
│   │   ├── PolygonSnapshotResponse.java # Polygon 快照接口响应体
│   │   └── PolygonPrevDayResponse.java  # Polygon 前一日 K 线响应体
│   ├── entity/
│   │   └── PriceQuote.java             # JPA 实体（price_quotes 表）
│   ├── repository/
│   │   └── PriceQuoteRepository.java   # Spring Data JPA Repository
│   ├── scheduler/
│   │   ├── PriceScheduler.java         # 分钟级 TSLA 价格拉取
│   │   └── DividendScheduler.java      # 每日分红数据拉取
│   ├── service/
│   │   ├── PriceService.java           # 拉取、去重、持久化逻辑
│   │   └── MassiveApiService.java      # Polygon.io REST 客户端
│   └── web/
│       ├── PriceController.java        # /api/v1/price/* 接口
│       └── WebController.java          # 健康/版本/数据库接口
├── k8s/
│   ├── dev/                            # 开发环境 Kubernetes 部署清单
│   └── prod/                           # 生产环境 Kubernetes 部署清单
├── .github/workflows/
│   └── deploy.yaml                     # CI/CD 流水线
├── Dockerfile                          # 多阶段 Docker 构建
└── pom.xml
```

### CI/CD 流水线

GitHub Actions 流水线定义在 `.github/workflows/deploy.yaml`，在推送到 `development` 分支时触发：

1. **Semantic Release** — 解析约定式提交信息，自动更新 `pom.xml` 版本号，更新 K8s 部署镜像标签，生成 `CHANGELOG.md`，并创建 Git 标签。
2. **Maven 构建** — 编译并打包 JAR。
3. **Docker 构建与推送** — 推送到 `ghcr.io/lookmail88/xg-tsla-svc`，包含 `latest` 和版本号标签。

#### 提交规范

本项目使用 [semantic-release](https://github.com/semantic-release/semantic-release) 自动管理版本号和发布流程。所有提交必须遵循[约定式提交](https://www.conventionalcommits.org/)格式：

- `feat: ...` — 触发**次版本号**升级（如 0.2.0 -> 0.3.0）
- `fix: ...` — 触发**修订版本号**升级（如 0.2.0 -> 0.2.1）
- `docs:`、`chore:`、`refactor:`、`test:`、`style:`、`ci:` — 不触发版本升级
- `feat!: ...` 或 `fix!: ...`（带 `!`）— 触发**主版本号**升级（如 0.2.0 -> 1.0.0）
- 提交信息底部包含 `BREAKING CHANGE:` — 同样触发**主版本号**升级

### Docker

镜像以非 root 用户（`spring`，UID 70501）运行，时区设置为 `America/Los_Angeles`（美西时间）。

```bash
# 构建
docker build -t xg-tsla-svc .

# 运行
docker run -p 8080:8080 \
  -e POSTGRES_HOST=localhost \
  -e POSTGRES_PASSWORD=secret \
  -e MASSIVE_API_TOKEN=your_token \
  xg-tsla-svc
```

### 许可证

详见 [LICENSE](LICENSE)。
