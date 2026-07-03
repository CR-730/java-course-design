# 数据字典

## 表: user

| 字段名 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(64) | NOT NULL, UNIQUE | 用户名 |
| gender | VARCHAR(16) | DEFAULT unknown | 性别 |
| age | INT |  | 年龄 |
| register_channel | VARCHAR(32) | NOT NULL, INDEX | 注册渠道: app/web/miniprogram |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 表: user_log

| 字段名 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK, AUTO_INCREMENT | 日志ID |
| user_id | BIGINT | NOT NULL, FK | 用户ID，关联 user.id |
| event_type | VARCHAR(16) | NOT NULL, INDEX | 事件类型: view/cart/order/pay |
| event_time | DATETIME | NOT NULL, INDEX | 事件发生时间 |
| channel | VARCHAR(32) | NOT NULL, INDEX | 访问渠道: app/web/miniprogram |
| device | VARCHAR(32) | NOT NULL | 设备类型 |
| product_category | VARCHAR(64) |  | 商品类别 |

## 表关系

| 父表 | 子表 | 关系 | 说明 |
| --- | --- | --- | --- |
| user | user_log | 一对多 | 一个用户可以产生多条行为日志 |

## 设计说明

- 选题 D 的最小可行设计只需要 `user` 和 `user_log` 两张表。
- 漏斗事件按 `event_type` 区分，统计顺序为 `view -> cart -> order -> pay`。
- 渠道统计按 `channel` 字段区分，支持 `app`、`web`、`miniprogram`。
- 商品维度先用 `product_category` 字段承载，避免第一天过度建模。

