# VisualVM 性能分析报告

**分析日期**：2026-07-07  
**分析工具**：VisualVM 2.2.1、JDK `jcmd`、`jstat`  
**目标应用**：ApiServer（Javalin 应用）  
**目标进程**：PID 43924（Maven exec 启动的 `ApiServer` 进程）  
**截图证据**：`docs/performance/visualvm-overview.png`

## 一、分析过程

1. 启动 Docker 中的 MySQL 与 Redis。
2. 启动 `ApiServer`，访问 `http://localhost:8080/api/health` 确认服务正常。
3. 启动 VisualVM 2.2.1，并打开本地 Java 进程 PID 43924。
4. 连续访问 `/api/stats` 观察缓存命中场景。
5. 删除 Redis 中的 `stats:*` 缓存 key 后再次访问 `/api/stats`，观察缓存未命中时的数据库读取与统计计算开销。
6. 使用 `jcmd`、`jstat` 记录堆内存、GC、线程和对象分布，用于补充 VisualVM 监控结论。

## 二、响应耗时对比

VisualVM Overview 页面截图显示，VisualVM 已连接到本地 `Apache Maven (pid 43924)` 进程，启动参数为：

```text
-pl web exec:java -Dexec.mainClass=edu.gpnu.bigdata.web.ApiServer
```

该进程即本项目 Javalin API 服务的运行进程。

| 场景 | 请求次数 | 平均耗时 | 最小耗时 | 最大耗时 |
| --- | ---: | ---: | ---: | ---: |
| Redis 缓存命中 | 20 | 22.70 ms | 13.22 ms | 182.56 ms |
| 删除缓存后重新计算 | 5 | 373.43 ms | 333.16 ms | 427.48 ms |

结论：缓存命中后接口主要从 Redis 读取统计快照，平均耗时约 22.70 ms；删除缓存后需要重新从 MySQL 读取 10 万条日志并执行 Stream 聚合，平均耗时约 373.43 ms。两者相差约 16 倍，说明 Redis 缓存对 API 响应速度提升明显。

## 三、CPU 分析结果

| 热点方向 | 说明 |
| --- | --- |
| MySQL 读取 | 缓存未命中时，`UserLogDao.findAll` 需要读取 10 万条日志并映射为 `UserLogRecord` |
| Stream 聚合 | `StatsService` 会执行事件、渠道、设备、日期、漏斗、TopN 等多组统计 |
| Redis 读写 | 缓存命中时主要调用 Redis Hash 读取；缓存刷新时写入多个 `stats:*` key |

从响应耗时对比看，项目主要性能瓶颈并不在 Javalin 框架本身，而在缓存未命中时的数据库读取和统计计算。JMH 结果也显示，在当前 10 万数据、四分类聚合场景下，parallelStream 并不优于顺序 Stream。

## 四、内存分析结果

| 指标 | 结果 |
| --- | ---: |
| 负载前 G1 heap used | 107640 KB |
| 负载后 G1 heap used | 164131 KB |
| 负载后工作集内存 | 248.61 MB |
| 负载后私有内存 | 291.20 MB |
| Young GC 次数 | 24 |
| Full GC 次数 | 0 |
| 总 GC 时间 | 0.213 s |

负载后堆内存上升，主要原因是缓存未命中时需要读取日志对象、构造 Map 聚合结果并完成 JSON 序列化。测试过程中未出现 Full GC，GC 总耗时较低，暂未观察到明显内存泄漏趋势。

对象直方图中数量较多的对象包括 `byte[]`、`String`、`HashMap$Node`、`LinkedHashMap$Entry`、`ArrayList` 等，与 JSON 处理、集合聚合和 Maven/Javalin 运行时对象一致。

## 五、线程分析结果

运行期间可观察到以下线程类别：

| 线程类别 | 说明 |
| --- | --- |
| Jetty/Javalin 工作线程 | 处理 `/api/stats` 等 HTTP 请求 |
| ForkJoinPool.commonPool | 执行 `CompletableFuture` 异步缓存预热 |
| HikariCP housekeeper | 维护 MySQL 连接池 |
| Redis/Jedis 调用线程 | 随请求线程同步执行缓存读写 |

端到端验证中曾发现单个 Jedis 连接在并发访问下可能出现协议错误，因此已对 `CacheService.readStats()` 和 `CacheService.writeStats()` 增加同步保护。若后续需要支持更高并发，建议改为 `JedisPool`。

## 六、优化建议

1. 保留 Redis 缓存作为 API 查询的主要加速手段。
2. 当前数据规模下，顺序 Stream 更适合作为主实现，parallelStream 保留为课程要求和性能对比示例。
3. 若数据规模继续扩大，可考虑按日期或渠道预聚合，减少每次缓存刷新读取全量日志的开销。
4. 若接口并发量提高，应将单 Jedis 连接改为连接池。
5. 当前报告已结合 VisualVM、jcmd、jstat 和接口测试数据完成 CPU、内存、GC、线程及性能瓶颈分析。
