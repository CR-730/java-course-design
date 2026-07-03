# Java课程设计 - 用户行为漏斗分析

本项目用于 Java 程序设计综合实训，选题 D：用户行为漏斗分析。

## 模块

| 模块 | 说明 |
| --- | --- |
| core | 核心实体、DAO、统计服务、配置和日志 |
| benchmark | JMH 性能测试 |
| web | Javalin API 服务 |

## 第一天产物

- `docs/data-dictionary.md`: 数据字典
- `docs/er-diagram.drawio`: E-R 图源文件
- `sql/schema.sql`: MySQL 建表脚本
- `core/src/main/resources/application.properties`: 外部化配置
- `core/src/main/resources/logback.xml`: 日志配置
- `docker/docker-compose.yml`: MySQL 和 Redis 启动配置

## 第二天产物

- `core/src/main/java/edu/gpnu/bigdata/util/DataGenerator.java`: 生成 5 万用户和 10 万行为日志
- `core/src/main/java/edu/gpnu/bigdata/service/StatsService.java`: Stream 统计服务
- `report/user-behavior-report.txt`: 用户行为统计报表

## 环境要求

- JDK 17 或更高版本
- Maven 3.8 或更高版本
- MySQL 8.0
- Redis 7

## 常用命令

启动 MySQL 和 Redis：

```powershell
cd docker
docker compose up -d
```

本项目 MySQL 使用宿主机端口 `3308`，Redis 使用 `6379`。

初始化表结构由 Docker 首次启动时自动执行 `sql/schema.sql`。如果需要手动执行，可进入 MySQL 后运行该 SQL 文件。

运行测试：

```powershell
mvn test
```

生成 5 万用户和 10 万行为日志：

```powershell
mvn -pl core "exec:java" "-Dexec.mainClass=edu.gpnu.bigdata.util.DataGenerator"
```

生成统计报表：

```powershell
mvn -pl core "exec:java" "-Dexec.mainClass=edu.gpnu.bigdata.util.ReportGenerator"
```

数据库验证：

```sql
SELECT COUNT(*) FROM user;
SELECT COUNT(*) FROM user_log;
SELECT event_type, COUNT(*) FROM user_log GROUP BY event_type;
SELECT channel, COUNT(*) FROM user_log GROUP BY channel;
```

Redis 验证：

```powershell
docker exec java-redis redis-cli ping
```

启动 Javalin API：

```powershell
mvn install -DskipTests
mvn -pl web "exec:java" "-Dexec.mainClass=edu.gpnu.bigdata.web.ApiServer"
```

访问地址：

```text
http://localhost:8080/api/health
http://localhost:8080/api/stats
http://localhost:8080/api/stats/event-type
http://localhost:8080/api/stats/channel
http://localhost:8080/api/stats/funnel
```
