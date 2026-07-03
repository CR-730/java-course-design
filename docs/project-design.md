# 选题设计说明书：用户行为漏斗分析

## 1. 选题背景

本项目选择题目 D：用户行为漏斗分析。电商、内容平台和小程序常通过用户行为日志分析用户从浏览到支付的转化路径。本项目围绕 `view -> cart -> order -> pay` 行为链路，完成数据生成、批量入库、统计分析、缓存、报表、API 和性能测试。

## 2. 项目目标

- 生成 5 万独立用户和 10 万条用户行为日志。
- 使用 MySQL 存储用户和行为日志。
- 使用 Stream API 统计事件类型、渠道、每日 PV/UV、漏斗转化率和商品类别 TopN。
- 使用 Redis 缓存统计结果，并设置过期时间。
- 使用 CompletableFuture 启动时异步预热缓存。
- 使用 Javalin 提供 REST API。
- 使用 JMH 比较循环、顺序 Stream、parallelStream 的性能。
- 使用 JUnit5、Mockito、JaCoCo 验证核心逻辑，行覆盖率达到 60% 以上。

## 3. 技术方案

项目采用 Maven 多模块结构：

| 模块 | 说明 |
| --- | --- |
| `core` | 实体、DTO、DAO、统计服务、Redis 缓存、数据生成、报表 |
| `benchmark` | JMH 性能基准测试 |
| `web` | Javalin REST API |

主要技术：

- Java 17 record 定义不可变数据对象。
- HikariCP 管理 MySQL 连接池。
- JDBC batch insert 批量写入 10 万条日志。
- DataFaker 生成部分模拟商品类别。
- Stream API 完成分组、聚合、排序。
- 自定义 `FunnelCollector` 计算漏斗去重用户数与转化率。
- Redis Hash 缓存统计快照，TTL 为 1800 秒。
- CompletableFuture 异步预热统计缓存。
- Javalin 输出 JSON API。
- JMH 输出性能对比报告。
- Mockito 隔离 DAO 和缓存依赖。
- JaCoCo 对 `core` 模块执行 60% 行覆盖率检查。

## 4. 数据库设计

数据库至少包含两张表：

- `user`：保存用户基础信息。
- `user_log`：保存用户行为日志，并通过 `user_id` 关联 `user.id`。

`user_log` 核心字段：

| 字段 | 含义 |
| --- | --- |
| `id` | 行为日志主键 |
| `user_id` | 用户 ID，外键关联 `user.id` |
| `event_type` | 行为类型：`view`、`cart`、`order`、`pay` |
| `event_time` | 行为时间 |
| `channel` | 渠道：`app`、`web`、`miniprogram` |
| `device` | 设备类型 |
| `product_category` | 商品类别 |

建表脚本位于 `sql/schema.sql`，包含主键、外键、索引、字段注释和表注释。ER 图位于 `docs/er-diagram.drawio` 和 `docs/er-diagram.png`。

## 5. 核心功能

| 功能 | 实现位置 |
| --- | --- |
| 按事件类型统计 | `StatsService.countByEventType()` |
| 按渠道统计 | `StatsService.countByChannel()` |
| 按设备统计 | `StatsService.countByDevice()` |
| 每日 PV | `StatsService.dailyPv()` |
| 每日 UV | `StatsService.dailyUv()` |
| 漏斗转化率 | `FunnelCollector`、`StatsService.funnelConversion()` |
| 商品类别 TopN | `StatsService.topCategories()` |
| Redis 缓存 | `CacheService` |
| 异步预热 | `CacheWarmupService` |
| REST API | `ApiServer` |
| 报表输出 | `ReportGenerator` |
| JMH 性能测试 | `StreamBenchmark` |

## 6. 性能测试结论

JMH 测试基于 10 万条内存日志，对比循环、顺序 Stream 和 parallelStream。

| 实现 | 平均耗时 |
| --- | ---: |
| 循环 | 2.548 ms/op |
| 顺序 Stream | 2.606 ms/op |
| parallelStream | 5.527 ms/op |

结论：当前统计任务只按 4 个事件类型聚合，数据规模为 10 万条，parallelStream 的线程拆分和合并开销高于收益，因此慢于顺序 Stream。项目保留 parallelStream 实现，并用 JMH 真实验证其表现。

## 7. 测试与质量

- 单元测试：25 个。
- Mockito：用于隔离 `UserLogDao`、`CacheService`、`Jedis` 等依赖。
- JaCoCo：`core` 模块行覆盖率 84.59%，超过 60% 要求。
- 资源管理：DAO 使用 try-with-resources，数据生成器使用事务和批处理。
- 日志：关键流程通过 SLF4J + Logback 输出到控制台和文件。

## 8. 演示流程

1. 启动 Docker 服务：`.\scripts\start-docker.ps1`
2. 生成数据：`.\scripts\generate-data.ps1`
3. 生成报表：`.\scripts\generate-report.ps1`
4. 运行测试：`.\scripts\run-tests.ps1`
5. 查看覆盖率：`.\scripts\run-jacoco.ps1`
6. 运行 JMH：`.\scripts\run-jmh.ps1`
7. 启动 API：`.\scripts\start-api.ps1`
8. 访问 `http://localhost:8080/api/stats`

## 9. 交付物

- 源码：`core`、`benchmark`、`web`
- SQL：`sql/schema.sql`
- Docker：`docker/docker-compose.yml`
- ER 图：`docs/er-diagram.drawio`、`docs/er-diagram.png`
- 数据字典：`docs/data-dictionary.md`
- JMH 报告：`docs/jmh-report.md`
- 代码审查报告：`docs/code-review.md`
- 统计报表：`report/user-behavior-report.txt`
- 运行脚本：`scripts/*.ps1`
- README：`README.md`
