# E-R 图说明

本图采用传统 Chen E-R 图表示法：矩形表示实体，椭圆表示属性，菱形表示联系，主键属性以下划线标识。

本项目包含两个核心实体：

- `user`：用户基础信息表。
- `user_log`：用户行为日志实体。

关系：

```text
用户（user） 1 ── 产生 ── N 行为日志（user_log）
```

转换为关系模型后，`user_log.user_id` 作为外键关联 `user.id`。

可编辑源文件：

```text
docs/er-diagram.drawio
```

导出图片：

```text
docs/er-diagram.png
```

![ER Diagram](er-diagram.png)
