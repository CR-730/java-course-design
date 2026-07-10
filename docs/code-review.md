# 代码审查报告

## 审查范围

- `core` 模块：实体、DTO、DAO、统计服务、Redis 缓存、异步预热、数据生成与报表。
- `benchmark` 模块：JMH 性能基准。
- `web` 模块：Javalin REST API。
- `sql`、`docker`、`scripts`、`docs`、`report` 交付材料。

## 硬性要求核对

| 要求 | 状态 | 证据 |
| --- | --- | --- |
| Java 17+ 与 Maven 多模块 | 通过 | parent `pom.xml` 设置 `maven.compiler.release=17`，包含 `core`、`benchmark`、`web` |
| record DTO/实体 | 通过 | `UserRecord`、`UserLogRecord`、`FunnelStats`、`StatsSnapshot` |
| 至少 2 处 Stream API | 通过 | `StatsService`、`StatsSnapshot`、`CacheService` |
| MySQL 与 Redis | 通过 | `docker/docker-compose.yml`、`sql/schema.sql`、`CacheService` |
| HikariCP 与 JDBC Batch Insert | 通过 | `DataSourceFactory`、`DataGenerator` |
| DataFaker | 通过 | `SyntheticDataFactory` |
| parallelStream 与自定义 Collector | 通过 | `StatsService.countByEventTypeParallel()`、`FunnelCollector` |
| CompletableFuture | 通过 | `CacheWarmupService` |
| Javalin API | 通过 | `web/src/main/java/edu/gpnu/bigdata/web/ApiServer.java` |
| JUnit5、Mockito、JaCoCo | 通过 | 26 个单元测试，Mockito 隔离 DAO/Redis，JaCoCo 行覆盖率 86.81% |
| JMH | 通过 | `StreamBenchmark`、`docs/jmh-report.md` |
| README 与运行脚本 | 通过 | `README.md`、`scripts/*.ps1` |

## 主要设计检查

- 分层结构清晰：`entity`、`dto`、`dao`、`service`、`collector`、`util` 分工明确。
- 数据库访问使用 try-with-resources，`UserLogDao` 不泄漏 `PreparedStatement` 和 `ResultSet`。
- 批量写入使用事务和 batch，适合 10 万日志规模。
- Redis 缓存统一封装在 `CacheService`，缓存命中和刷新有日志。
- 已补充渠道漏斗、设备漏斗、每日事件分布等多维度下钻统计。
- 统计逻辑以不可变输入列表为基础，`parallelStream` 没有共享可变状态。
- JMH 报告如实记录并行流在当前数据规模下更慢，没有伪造性能结论。

## 已发现并修复的问题

| 问题 | 影响 | 处理 |
| --- | --- | --- |
| `StatsApplicationService` 直接 new DAO，不利于 Mockito 隔离测试 | 影响评分点“隔离 DAO” | 增加兼容的 DAO 注入构造器 |
| Mockito 在 JDK 25 下 inline mock maker 不稳定 | 测试无法 mock 部分类 | 配置 subclass mock maker |
| JaCoCo 0.8.12 不支持 JDK 25 class file 69 | 覆盖率数据无法生成 | 升级到 JaCoCo 0.8.14 |
| 项目路径包含中文导致 JaCoCo exec 路径乱码 | report/check 读取不到执行数据 | 将 exec 文件写入用户目录 ASCII 路径 |

## 剩余风险

- API 与 Redis/MySQL 已完成端到端验证，记录位于 `docs/e2e-verification.md`。
- 本机使用 JDK 25，运行时会输出 Maven/JDK 对部分旧 API 的警告，但不影响编译和测试结果。
- 已实现前端数据仪表盘，通过 fetch 调用 `/api/stats` 展示统计结果。
