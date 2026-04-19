# Tesla Stock Analysis — Design Document

> **Project:** xg-tsla-svc
> **Status:** Active
> **Description:** AI-powered TSLA analysis — trend detection, sentiment analysis, and price prediction.

---

[English](#english) | [中文](#中文)

---

## English

### 1. Overview

xg-tsla-svc is an AI-powered Tesla (TSLA) stock analysis platform built as a Spring Boot 4.0 microservice (Java 25). The service provides real-time market data ingestion, technical and fundamental analysis, screening, mock trading, and money flow tracking — all focused on TSLA.

### 2. System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        xg-tsla-svc                              │
│                    (Spring Boot 4.0 / Java 25)                  │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  REST Controllers │  │  Schedulers  │  │  Service Layer   │  │
│  │  WebController    │  │  PriceSched. │  │  PriceService    │  │
│  │  PriceController  │  │  DividendSch.│  │  MassiveApiSvc   │  │
│  └────────┬──────────┘  └──────┬───────┘  └────────┬─────────┘  │
│           │                    │                   │            │
│           └────────────────────┴───────────────────┘            │
│                                │                                │
│           ┌────────────────────┼────────────────────┐           │
│           ▼                    ▼                    ▼           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐   │
│  │  TimescaleDB    │  │   Polygon.io    │  │  Polygon.io  │   │
│  │  (price_quotes) │  │  Snapshot API   │  │  Prev-Day /  │   │
│  │  PostgreSQL 17  │  │  (real-time)    │  │  Dividends   │   │
│  └─────────────────┘  └─────────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────────────┘

Infrastructure:
  Docker (eclipse-temurin:25) → Kubernetes (dev / prod) → GitHub Actions CI/CD
  GHCR: ghcr.io/lookmail88/xg-tsla-svc
```

### 3. Modules

The platform is organized into six functional modules:

#### 3.1 Real-Time Data

Ingests live and near-real-time TSLA market data from external APIs.

- **Data source:** Polygon.io API (accessed via `MassiveApiService` with `Bearer` token auth)
- **Delivery method:** Scheduled polling via Spring `@Scheduled` cron jobs (configurable per-scheduler)
- **Current implementation:**
  - `PriceScheduler` — fetches TSLA OHLCV snapshot every minute (`price-cron: "0 * * * * *"`). Tries the real-time snapshot endpoint first (`/v2/snapshot/locale/us/markets/stocks/tickers/TSLA`); falls back to the previous-day bar (`/v2/aggs/ticker/TSLA/prev`) if the plan doesn't support snapshots (HTTP 403). Duplicate quotes (same symbol + timestamp) are silently skipped.
  - `DividendScheduler` — fetches dividend data daily at 09:00 PT (`/v3/reference/dividends`) via `massive.api.cron`
  - `MassiveApiService` — Spring `RestClient`-based HTTP client; handles both raw and typed responses
  - `PriceService` — orchestrates fetch, deduplication, and persistence to `price_quotes` via `PriceQuoteRepository`
- **Planned enhancements:**
  - WebSocket streaming for tick-level price data
  - Pre-market and after-hours data feeds
  - Data caching layer (Redis or in-memory)

**Key data points:**

| Data Type | Source | Frequency | Status |
|-----------|--------|-----------|--------|
| Price quotes (OHLCV) | Polygon.io snapshot | Every minute | **Implemented** |
| Price quotes (prev day) | Polygon.io prev-day bar | Every minute (fallback) | **Implemented** |
| Dividends | Polygon.io dividends | Daily 09:00 PT | **Implemented** |
| Market depth | TBD | Real-time | Planned |
| News/Events | TBD | Event-driven | Planned |

#### 3.2 Technical Analysis

Computes technical indicators and chart patterns to identify trends and trading signals.

- **Indicators (planned):**
  - Moving Averages: SMA, EMA (5, 10, 20, 50, 200-day)
  - Momentum: RSI, MACD, Stochastic Oscillator
  - Volatility: Bollinger Bands, ATR (Average True Range)
  - Volume: OBV (On-Balance Volume), VWAP
- **Pattern detection (planned):**
  - Candlestick patterns (Doji, Hammer, Engulfing, etc.)
  - Chart patterns (Head & Shoulders, Double Top/Bottom, Triangles)
  - Support and resistance levels
- **Signal generation:**
  - Buy/Sell/Hold signals based on indicator crossovers
  - Configurable thresholds and timeframes
  - Multi-indicator confluence scoring

#### 3.3 Fundamental Analysis

Evaluates TSLA's financial health, valuation, and growth metrics.

- **Financial metrics (planned):**
  - Income statement: Revenue, EPS, Net Income, Gross Margin
  - Balance sheet: Total Debt, Cash Position, Book Value
  - Cash flow: Free Cash Flow, Operating Cash Flow
  - Valuation ratios: P/E, P/S, P/B, EV/EBITDA, PEG
- **Comparative analysis:**
  - Peer comparison (EV sector)
  - Historical valuation trends
  - Analyst estimate tracking (consensus EPS, revenue)
- **Earnings analysis:**
  - Earnings surprise history
  - Earnings call sentiment (NLP-based)
  - Forward guidance tracking

#### 3.4 Screener

Filters and ranks TSLA-related data based on user-defined criteria.

- **Screening dimensions (planned):**
  - Price-based: 52-week high/low proximity, price change %, gap detection
  - Volume-based: unusual volume spikes, volume trend
  - Technical: RSI overbought/oversold, MACD crossovers, moving average crossovers
  - Fundamental: P/E range, revenue growth rate, margin thresholds
- **Alert system:**
  - Configurable threshold alerts
  - Notification via REST callback or scheduled report
  - Historical alert log

#### 3.5 Mock System

A paper trading simulator for testing strategies without risking real capital.

- **Features (planned):**
  - Virtual portfolio with configurable starting balance
  - Market/Limit/Stop order types
  - Position tracking: entry price, current P&L, holding period
  - Transaction history and audit log
- **Performance metrics:**
  - Total return, annualized return
  - Sharpe ratio, max drawdown
  - Win rate, average win/loss ratio
  - Benchmark comparison (vs. SPY, QQQ)
- **Strategy backtesting:**
  - Historical data replay
  - Strategy parameter optimization
  - Monte Carlo simulation

#### 3.6 Money Flow Analysis

Tracks institutional and retail capital movements in TSLA.

- **Metrics (planned):**
  - Net money flow (inflow vs. outflow by trade size)
  - Large-order detection (block trades)
  - Institutional vs. retail flow estimation
  - Sector rotation signals
- **Visualization data:**
  - Time-series money flow charts
  - Flow heatmaps by time of day
  - Cumulative flow trend lines

### 4. API Design

All endpoints are served under the context path `/xg-tsla-svc`.

#### 4.1 Existing Endpoints

All paths below are relative to the context path `/xg-tsla-svc`.

**Price endpoints (`PriceController`)**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/price/latest` | Most recent TSLA price quotes. `?limit=N` (default: 60) |
| GET | `/api/v1/price/today` | All TSLA quotes from the last 24 hours |
| GET | `/api/v1/price/fetch` | Manually trigger a Polygon.io price fetch |

**System endpoints (`WebController`)**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/sayhello` | Health greeting |
| GET | `/health` | Current time (America/Los_Angeles) |
| GET | `/version` | Environment + build version |
| GET | `/db/status` | PostgreSQL connection status |
| GET | `/swagger-ui` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI JSON spec |

#### 4.2 Planned Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/realtime/dividends` | Latest dividend data |
| GET | `/api/v1/technical/indicators` | Technical indicator values |
| GET | `/api/v1/technical/signals` | Buy/Sell/Hold signals |
| GET | `/api/v1/fundamental/financials` | Financial statements summary |
| GET | `/api/v1/fundamental/valuation` | Valuation ratios |
| GET | `/api/v1/screener/scan` | Run screener with criteria |
| POST | `/api/v1/mock/order` | Place a mock trade |
| GET | `/api/v1/mock/portfolio` | Current mock portfolio |
| GET | `/api/v1/mock/history` | Trade history |
| GET | `/api/v1/moneyflow/summary` | Money flow summary |
| GET | `/api/v1/moneyflow/trend` | Money flow time series |

### 5. Data Model

#### 5.1 Core Entities (Planned)

```
┌──────────────┐     ┌──────────────────┐     ┌────────────────┐
│  PriceQuote  │     │ TechnicalSignal  │     │  Financials    │
├──────────────┤     ├──────────────────┤     ├────────────────┤
│ timestamp    │     │ timestamp        │     │ period         │
│ open         │     │ indicator        │     │ revenue        │
│ high         │     │ value            │     │ eps            │
│ low          │     │ signal (BUY/     │     │ netIncome      │
│ close        │     │   SELL/HOLD)     │     │ freeCashFlow   │
│ volume       │     │ confidence       │     │ peRatio        │
│ vwap         │     │ timeframe        │     │ psRatio        │
└──────────────┘     └──────────────────┘     └────────────────┘

┌──────────────┐     ┌──────────────────┐     ┌────────────────┐
│  MockOrder   │     │  MockPortfolio   │     │  MoneyFlow     │
├──────────────┤     ├──────────────────┤     ├────────────────┤
│ orderId      │     │ portfolioId      │     │ timestamp      │
│ type (BUY/   │     │ cash             │     │ inflow         │
│   SELL)      │     │ positions[]      │     │ outflow        │
│ orderType    │     │ totalValue       │     │ netFlow        │
│ quantity     │     │ totalReturn      │     │ largeOrderFlow │
│ price        │     │ createdAt        │     │ tradeSize      │
│ status       │     └──────────────────┘     └────────────────┘
│ filledAt     │
└──────────────┘
```

### 6. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 25 |
| Framework | Spring Boot | 4.0.5 |
| HTTP Client | Spring RestClient | (Spring Boot built-in) |
| API Docs | Springdoc OpenAPI | 3.0.1 |
| Monitoring | Spring Boot Actuator | (Spring Boot built-in) |
| Container | Docker (eclipse-temurin:25) | — |
| Orchestration | Kubernetes | — |
| CI/CD | GitHub Actions + semantic-release | — |
| Registry | GHCR (ghcr.io/lookmail88/xg-tsla-svc) | — |
| External API | Massive API | v3 |
| Database | PostgreSQL + TimescaleDB | PG 17 |

### 7. Database

#### 7.1 TimescaleDB (PostgreSQL + Time-Series Extension)

The project uses TimescaleDB (PostgreSQL 17 with the TimescaleDB extension) deployed as a containerized service in Kubernetes. TimescaleDB provides hypertables for efficient time-series storage and querying — ideal for price quotes, technical indicators, and money flow data.

**Image:** `timescale/timescaledb:latest-pg17`

**Connection details (internal ClusterIP):**

| Environment | Host | Port | Database | Namespace |
|-------------|------|------|----------|-----------|
| Dev | `timescaledb.xgao-dev.svc.cluster.local` | 5432 | tsla_db | xgao-dev |
| Prod | `timescaledb.xgao-prod.svc.cluster.local` | 5432 | tsla_db | xgao-prod |

**Spring Boot JDBC URL:**
```
jdbc:postgresql://timescaledb:5432/tsla_db
```

**Storage:**

| Environment | PVC Size | StorageClass |
|-------------|----------|--------------|
| Dev | 5Gi | local-path |
| Prod | 20Gi | local-path |

**Resource allocation:**

| Environment | Memory Request | Memory Limit | CPU Request |
|-------------|---------------|-------------|-------------|
| Dev | 256Mi | 512Mi | 0.25 |
| Prod | 512Mi | 1024Mi | 0.5 |

**K8s manifests per environment:**
- `timescaledb-secret.yaml` — database credentials (user, password, db name)
- `timescaledb-pvc.yaml` — persistent volume claim
- `timescaledb-deployment.yaml` — pod spec with health probes
- `timescaledb-service.yaml` — ClusterIP service on port 5432

**Health probes:** Both liveness and readiness probes use `pg_isready` to verify the database is accepting connections.

### 8. Infrastructure

#### 8.1 Docker

The container image runs as non-root user `spring` (UID 70501) with timezone `America/Los_Angeles`. Built on `eclipse-temurin:25`.

#### 8.2 Kubernetes

Two environments with separate ConfigMaps and Secrets:

| Environment | Namespace | API Gateway |
|-------------|-----------|-------------|
| Dev | xgao-dev | `https://api-dev.xuyang.dev/xg-tsla-svc` |
| Prod | xgao-prod | `https://api.xuyang.dev/xg-tsla-svc` (planned) |

Resource requests: 512Mi memory, 0.5 CPU, 2Gi ephemeral storage. Memory limit: 1024Mi.

#### 8.3 CI/CD Pipeline

Triggered on push to `development` branch:

1. **semantic-release** — version bump, changelog, Git tag
2. **Maven build** — compile and package JAR
3. **Docker build & push** — push to GHCR with `latest` and versioned tags

### 9. Security Considerations

- Container runs as non-root user
- API tokens stored in Kubernetes Secrets (not ConfigMaps)
- `@CrossOrigin` enabled on controllers — should be scoped to specific origins in production
- External API tokens injected via environment variables
- HTTPS enforced at the gateway level

### 10. Roadmap

| Phase | Milestone | Modules |
|-------|-----------|---------|
| Phase 1 (Current) | Foundation | Health/Version endpoints, Dividend scheduler, CI/CD pipeline |
| Phase 2 | Real-Time Data | Price quote ingestion, WebSocket streaming, data caching |
| Phase 3 | Technical Analysis | Indicator engine, signal generation, pattern detection |
| Phase 4 | Fundamental Analysis | Financial data ingestion, valuation model, earnings tracking |
| Phase 5 | Screener + Alerts | Screening engine, configurable alerts, notification system |
| Phase 6 | Mock System | Paper trading, backtesting, performance analytics |
| Phase 7 | Money Flow | Flow tracking, large-order detection, institutional flow |

---

## 中文

### 1. 概述

xg-tsla-svc 是一个基于 AI 的特斯拉（TSLA）股票分析平台，以 Spring Boot 4.0 微服务（Java 25）构建。该服务提供实时行情数据接入、技术分析、基本面分析、筛选器、模拟交易和资金流向追踪，专注于 TSLA 股票。

### 2. 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      xg-tsla-svc                            │
│                  (Spring Boot 4.0 / Java 25)                │
│                                                             │
│  ┌───────────┐  ┌───────────┐  ┌────────────────────────┐  │
│  │ WebController │  │ 定时任务   │  │ 服务层                │  │
│  │ (REST API) │  │ (Cron)    │  │ (MassiveApiService...) │  │
│  └─────┬─────┘  └─────┬─────┘  └───────────┬────────────┘  │
│        │               │                    │               │
│        └───────────────┴────────────────────┘               │
│                         │                                   │
│              ┌──────────┴──────────┐                        │
│              │   外部 API          │                        │
│              │  (Massive API 等)   │                        │
│              └─────────────────────┘                        │
└─────────────────────────────────────────────────────────────┘

基础设施：
  Docker (eclipse-temurin:25) → Kubernetes (dev / prod) → GitHub Actions CI/CD
  镜像仓库：ghcr.io/lookmail88/xg-tsla-svc
```

### 3. 功能模块

平台包含六个功能模块：

#### 3.1 实时数据（Real-Time Data）

从外部 API 获取 TSLA 实时和准实时行情数据。

- **数据源：** Massive API（`/v3/reference/dividends`），后续将接入更多数据提供商
- **获取方式：** 通过 Spring `@Scheduled` 定时任务轮询（通过 `massive.api.cron` 配置）
- **当前实现：** `DividendScheduler` 按 cron 定时获取分红数据；`MassiveApiService` 通过 Spring `RestClient` 处理 HTTP 请求
- **规划增强：**
  - WebSocket 推送逐笔价格数据
  - 日内 OHLCV（开盘价、最高价、最低价、收盘价、成交量）采集
  - 盘前和盘后数据源
  - 数据缓存层（Redis 或内存缓存）

#### 3.2 技术分析（Technical Analysis）

计算技术指标和图表形态，识别趋势和交易信号。

- **技术指标（规划中）：**
  - 移动平均线：SMA、EMA（5、10、20、50、200 日）
  - 动量指标：RSI、MACD、随机振荡器
  - 波动率：布林带、ATR（平均真实波幅）
  - 成交量：OBV（能量潮）、VWAP
- **形态识别（规划中）：**
  - K 线形态（十字星、锤子线、吞没形态等）
  - 图表形态（头肩顶/底、双顶/底、三角形）
  - 支撑位和阻力位
- **信号生成：**
  - 基于指标交叉的买入/卖出/持有信号
  - 可配置的阈值和时间周期
  - 多指标共振评分

#### 3.3 基本面分析（Fundamental Analysis）

评估 TSLA 的财务健康状况、估值和增长指标。

- **财务指标（规划中）：**
  - 利润表：营收、每股收益、净利润、毛利率
  - 资产负债表：总负债、现金头寸、账面价值
  - 现金流量表：自由现金流、经营现金流
  - 估值比率：市盈率、市销率、市净率、EV/EBITDA、PEG
- **对比分析：**
  - 同行业对比（电动车板块）
  - 历史估值趋势
  - 分析师预期跟踪（一致预期 EPS、营收）
- **财报分析：**
  - 财报超预期历史
  - 财报电话会议情绪分析（基于 NLP）
  - 前瞻指引追踪

#### 3.4 筛选器（Screener）

根据用户自定义条件筛选和排序 TSLA 相关数据。

- **筛选维度（规划中）：**
  - 价格类：52 周高/低点距离、涨跌幅、跳空缺口
  - 成交量类：异常放量、量能趋势
  - 技术类：RSI 超买/超卖、MACD 金叉/死叉、均线交叉
  - 基本面类：市盈率区间、营收增速、利润率阈值
- **告警系统：**
  - 可配置阈值告警
  - 通过 REST 回调或定时报告通知
  - 历史告警记录

#### 3.5 模拟交易（Mock System）

无需真实资金的模拟交易系统，用于策略测试。

- **功能（规划中）：**
  - 虚拟投资组合，可配置初始资金
  - 市价单/限价单/止损单
  - 持仓追踪：建仓价格、当前盈亏、持有时间
  - 交易历史和审计日志
- **绩效指标：**
  - 总收益率、年化收益率
  - 夏普比率、最大回撤
  - 胜率、平均盈亏比
  - 基准对比（对比 SPY、QQQ）
- **策略回测：**
  - 历史数据回放
  - 策略参数优化
  - 蒙特卡洛模拟

#### 3.6 资金流向分析（Money Flow Analysis）

追踪 TSLA 的机构和散户资金流动。

- **指标（规划中）：**
  - 净资金流（按交易规模划分的流入 vs 流出）
  - 大单检测（大宗交易）
  - 机构 vs 散户资金流估算
  - 板块轮动信号
- **可视化数据：**
  - 时间序列资金流图表
  - 按时段分布的流量热力图
  - 累计资金流趋势线

### 4. API 设计

所有接口的上下文路径为 `/xg-tsla-svc`。

#### 4.1 已有接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/sayhello` | 健康问候 |
| GET | `/health` | 当前时间（美西时区） |
| GET | `/version` | 环境 + 构建版本 |
| GET | `/swagger-ui` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI JSON 规范 |

#### 4.2 规划接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/v1/realtime/quote` | 当前 TSLA 报价 |
| GET | `/api/v1/realtime/dividends` | 最新分红数据 |
| GET | `/api/v1/technical/indicators` | 技术指标值 |
| GET | `/api/v1/technical/signals` | 买入/卖出/持有信号 |
| GET | `/api/v1/fundamental/financials` | 财务报表摘要 |
| GET | `/api/v1/fundamental/valuation` | 估值比率 |
| GET | `/api/v1/screener/scan` | 按条件执行筛选 |
| POST | `/api/v1/mock/order` | 提交模拟交易 |
| GET | `/api/v1/mock/portfolio` | 当前模拟投资组合 |
| GET | `/api/v1/mock/history` | 交易历史 |
| GET | `/api/v1/moneyflow/summary` | 资金流向摘要 |
| GET | `/api/v1/moneyflow/trend` | 资金流向时间序列 |

### 5. 数据模型

#### 5.1 核心实体（规划中）

```
┌──────────────┐     ┌──────────────────┐     ┌────────────────┐
│  PriceQuote  │     │ TechnicalSignal  │     │  Financials    │
│  (行情报价)   │     │ (技术信号)        │     │  (财务数据)     │
├──────────────┤     ├──────────────────┤     ├────────────────┤
│ timestamp    │     │ timestamp        │     │ period         │
│ open         │     │ indicator        │     │ revenue        │
│ high         │     │ value            │     │ eps            │
│ low          │     │ signal (BUY/     │     │ netIncome      │
│ close        │     │   SELL/HOLD)     │     │ freeCashFlow   │
│ volume       │     │ confidence       │     │ peRatio        │
│ vwap         │     │ timeframe        │     │ psRatio        │
└──────────────┘     └──────────────────┘     └────────────────┘

┌──────────────┐     ┌──────────────────┐     ┌────────────────┐
│  MockOrder   │     │  MockPortfolio   │     │  MoneyFlow     │
│  (模拟订单)   │     │  (模拟组合)       │     │  (资金流向)     │
├──────────────┤     ├──────────────────┤     ├────────────────┤
│ orderId      │     │ portfolioId      │     │ timestamp      │
│ type (BUY/   │     │ cash             │     │ inflow         │
│   SELL)      │     │ positions[]      │     │ outflow        │
│ orderType    │     │ totalValue       │     │ netFlow        │
│ quantity     │     │ totalReturn      │     │ largeOrderFlow │
│ price        │     │ createdAt        │     │ tradeSize      │
│ status       │     └──────────────────┘     └────────────────┘
│ filledAt     │
└──────────────┘
```

### 6. 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 编程语言 | Java | 25 |
| 框架 | Spring Boot | 4.0.5 |
| HTTP 客户端 | Spring RestClient | （Spring Boot 内置） |
| API 文档 | Springdoc OpenAPI | 3.0.1 |
| 监控 | Spring Boot Actuator | （Spring Boot 内置） |
| 容器 | Docker (eclipse-temurin:25) | — |
| 编排 | Kubernetes | — |
| CI/CD | GitHub Actions + semantic-release | — |
| 镜像仓库 | GHCR (ghcr.io/lookmail88/xg-tsla-svc) | — |
| 外部 API | Massive API | v3 |
| 数据库 | PostgreSQL + TimescaleDB | PG 17 |

### 7. 数据库

#### 7.1 TimescaleDB（PostgreSQL + 时序扩展）

项目使用 TimescaleDB（PostgreSQL 17 + TimescaleDB 扩展），以容器化方式部署在 Kubernetes 中。TimescaleDB 提供 hypertable 实现高效的时序数据存储和查询，非常适合行情报价、技术指标和资金流向数据。

**镜像：** `timescale/timescaledb:latest-pg17`

**连接信息（内部 ClusterIP）：**

| 环境 | 主机 | 端口 | 数据库 | 命名空间 |
|------|------|------|--------|---------|
| Dev | `timescaledb.xgao-dev.svc.cluster.local` | 5432 | tsla_db | xgao-dev |
| Prod | `timescaledb.xgao-prod.svc.cluster.local` | 5432 | tsla_db | xgao-prod |

**Spring Boot JDBC URL：**
```
jdbc:postgresql://timescaledb:5432/tsla_db
```

**持久化存储：**

| 环境 | PVC 大小 | StorageClass |
|------|---------|--------------|
| Dev | 5Gi | local-path |
| Prod | 20Gi | local-path |

**资源分配：**

| 环境 | 内存请求 | 内存上限 | CPU 请求 |
|------|---------|---------|---------|
| Dev | 256Mi | 512Mi | 0.25 |
| Prod | 512Mi | 1024Mi | 0.5 |

**每个环境的 K8s 清单文件：**
- `timescaledb-secret.yaml` — 数据库凭证（用户名、密码、数据库名）
- `timescaledb-pvc.yaml` — 持久化存储卷声明
- `timescaledb-deployment.yaml` — Pod 规格及健康探针
- `timescaledb-service.yaml` — ClusterIP 服务（端口 5432）

**健康探针：** 存活和就绪探针均使用 `pg_isready` 验证数据库是否接受连接。

### 8. 基础设施

#### 8.1 Docker

容器镜像以非 root 用户 `spring`（UID 70501）运行，时区为 `America/Los_Angeles`。基础镜像为 `eclipse-temurin:25`。

#### 8.2 Kubernetes

两套环境，各有独立的 ConfigMap 和 Secret：

| 环境 | 命名空间 | API 网关 |
|------|---------|---------|
| Dev | xgao-dev | `https://api-dev.xuyang.dev/xg-tsla-svc` |
| Prod | xgao-prod | `https://api.xuyang.dev/xg-tsla-svc`（规划中） |

资源请求：512Mi 内存、0.5 CPU、2Gi 临时存储。内存上限：1024Mi。

#### 8.3 CI/CD 流水线

推送到 `development` 分支时触发：

1. **semantic-release** — 版本升级、变更日志、Git 标签
2. **Maven 构建** — 编译并打包 JAR
3. **Docker 构建与推送** — 推送到 GHCR，包含 `latest` 和版本号标签

### 9. 安全考量

- 容器以非 root 用户运行
- API 令牌存储在 Kubernetes Secret 中（非 ConfigMap）
- 控制器已启用 `@CrossOrigin` — 生产环境应限制为特定域名
- 外部 API 令牌通过环境变量注入
- 在网关层强制使用 HTTPS

### 10. 路线图

| 阶段 | 里程碑 | 模块 |
|------|--------|------|
| 第一阶段（当前） | 基础框架 | 健康/版本接口、分红定时任务、CI/CD 流水线 |
| 第二阶段 | 实时数据 | 报价采集、WebSocket 推送、数据缓存 |
| 第三阶段 | 技术分析 | 指标引擎、信号生成、形态识别 |
| 第四阶段 | 基本面分析 | 财务数据采集、估值模型、财报追踪 |
| 第五阶段 | 筛选器 + 告警 | 筛选引擎、可配置告警、通知系统 |
| 第六阶段 | 模拟交易 | 模拟交易、策略回测、绩效分析 |
| 第七阶段 | 资金流向 | 流向追踪、大单检测、机构资金流 |
