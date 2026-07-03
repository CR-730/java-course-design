# Java 课程设计：用户行为漏斗分析

本项目是 Java 程序设计综合实训项目，固定选题 D：用户行为漏斗分析。项目生成 5 万用户和 10 万条行为日志，围绕 `view -> cart -> order -> pay` 路径完成统计、缓存、报表、REST API 与性能基准测试。

## 技术栈

- Java 17+，Maven 多模块
- MySQL，Redis，Docker Compose
- HikariCP，JDBC Batch Insert，DataFaker
- Stream API，parallelStream，自定义 Collector
- Jedis，CompletableFuture，Javalin
- SLF4J + Logback
- JUnit5，Mockito，JaCoCo
- JMH，Git

## 项目结构

```text
java-course-design/
├─ pom.xml
├─ core/
│  └─ src/main/java/edu/gpnu/bigdata/
│     ├─ entity/
│     ├─ dto/
│     ├─ dao/
│     ├─ service/
│     ├─ collector/
│     └─ util/
├─ benchmark/
├─ web/
├─ sql/schema.sql
├─ docker/docker-compose.yml
├─ docs/
├─ report/
├─ scripts/
└─ README.md
```

## 环境要求

- JDK 17 或更高版本
- Maven 3.8 或更高版本
- Docker Desktop
- MySQL 8.0 和 Redis 7 由 Docker Compose 启动

当前 Docker 配置使用：

- MySQL：`localhost:3308`
- Redis：`localhost:6379`
- 数据库：`course_design`
- 应用账号：`app_user / app_pass`

## 启动 MySQL / Redis

```powershell
.\scripts\start-docker.ps1
```

首次启动会通过 `sql/schema.sql` 自动建表。表结构至少包含 `user` 和 `user_log`，`user_log` 包含 `id`、`user_id`、`event_type`、`event_time`、`channel`、`device`、`product_category`，并配置主键、外键、索引、字段注释和表注释。

## 生成 10 万条数据

```powershell
.\scripts\generate-data.ps1
```

验证 SQL：

```sql
SELECT COUNT(*) FROM user;
SELECT COUNT(*) FROM user_log;
SELECT event_type, COUNT(*) FROM user_log GROUP BY event_type;
SELECT channel, COUNT(*) FROM user_log GROUP BY channel;
```

预期规模：

- `user`：50,000 行
- `user_log`：100,000 行
- `event_type`：`view / cart / order / pay`
- `channel`：`app / web / miniprogram`

## 生成统计报表

```powershell
.\scripts\generate-report.ps1
```

输出文件：

```text
report/user-behavior-report.txt
```

统计内容包括事件类型、渠道、每日 PV/UV、漏斗转化率、商品类别 TopN。

## 运行测试

```powershell
.\scripts\run-tests.ps1
```

等价命令：

```powershell
mvn clean test
```

## 查看 JaCoCo 覆盖率

```powershell
.\scripts\run-jacoco.ps1
```

等价命令：

```powershell
mvn -pl core clean verify
```

覆盖率报告：

```text
core/target/site/jacoco/index.html
```

当前 core 模块 JaCoCo 行覆盖率为 61.76%，满足 60% 要求。

## 运行 JMH

```powershell
.\scripts\run-jmh.ps1
```

已生成的 JMH 材料：

- `benchmark/src/main/java/edu/gpnu/bigdata/benchmark/StreamBenchmark.java`
- `docs/jmh-output.txt`
- `docs/jmh-report.md`

当前结果显示，在 10 万条内存日志的四分类统计场景下，普通循环和顺序 Stream 接近，parallelStream 因并行开销更慢。

## 启动 Javalin API

```powershell
.\scripts\start-api.ps1
```

访问地址：

```text
http://localhost:8080/api/health
http://localhost:8080/api/stats
http://localhost:8080/api/stats/event-type
http://localhost:8080/api/stats/channel
http://localhost:8080/api/stats/funnel
```

`/api/stats` 会返回 JSON 统计结果，并通过 Redis 缓存统计快照。启动时会使用 `CompletableFuture` 异步预热缓存。

## Redis 验证

```powershell
docker exec java-redis redis-cli ping
docker exec java-redis redis-cli KEYS stats:*
docker exec java-redis redis-cli TTL stats:eventType
```

## 过程文档

- 选题设计说明书：`docs/project-design.md`
- Day1：`docs/er-diagram.drawio`、`docs/er-diagram.png`、`docs/data-dictionary.md`、`sql/schema.sql`
- Day2：`core/src/main/java/edu/gpnu/bigdata/service/StatsService.java`、`report/user-behavior-report.txt`
- Day3：`benchmark/src/main/java/edu/gpnu/bigdata/benchmark/StreamBenchmark.java`、`docs/jmh-report.md`
- Day4：`docs/code-review.md`

## Git 仓库

私有仓库地址：

```text
https://github.com/CR-730/java-course-design
```

说明：仓库名称为 `java-course-design`。
