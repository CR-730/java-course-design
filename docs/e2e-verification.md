# 端到端验收记录

验收时间：2026-07-03

## Docker 服务

```text
java-mysql: Up, localhost:3308 -> 3306
java-redis: Up, localhost:6379 -> 6379
```

## 数据规模

```sql
SELECT COUNT(*) AS users FROM user;
-- 50000

SELECT COUNT(*) AS logs FROM user_log;
-- 100000
```

事件类型分布：

```text
view  55000
cart  25000
order 13000
pay   7000
```

渠道分布：

```text
app          33333
miniprogram 33333
web          33334
```

## 报表

运行命令：

```powershell
.\scripts\generate-report.ps1
```

输出文件：

```text
report/user-behavior-report.txt
```

报表已包含事件类型、渠道、设备、每日 PV/UV、每日事件分布、总体漏斗、渠道漏斗下钻、设备漏斗下钻、商品类别 Top 5。

## API 验证

```text
GET /api/health -> OK
GET /api/stats -> fromCache=True
GET /api/stats/drilldown/channel-funnel -> app.payUsers=2334
GET /api/stats/drilldown/device-funnel -> android.orderToPayRate=25.0
GET /api/stats/daily-event-type -> 30 days
```

核心统计：

```text
eventType.view=55000
eventType.cart=25000
eventType.order=13000
eventType.pay=7000
device.android=25000
device.ios=25000
```

## Redis 验证

```text
stats:channel
stats:channelFunnel
stats:dailyEventType
stats:dailyPv
stats:dailyUv
stats:device
stats:deviceFunnel
stats:eventType
stats:funnel
stats:topCategory
```

TTL：

```text
stats:channelFunnel TTL = 1778
stats:dailyEventType TTL = 1778
```

结论：MySQL 数据规模、Redis 缓存、REST API、下钻统计和文本报表均已完成端到端验证。
