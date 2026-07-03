-- ============================================================
-- 数据库名称: course_design
-- 说明: Java课程设计实训 - 用户行为漏斗分析
-- ============================================================

CREATE DATABASE IF NOT EXISTS course_design
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE course_design;

DROP TABLE IF EXISTS user_log;
DROP TABLE IF EXISTS user;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    gender VARCHAR(16) DEFAULT 'unknown' COMMENT '性别',
    age INT COMMENT '年龄',
    register_channel VARCHAR(32) NOT NULL COMMENT '注册渠道: app/web/miniprogram',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_register_channel (register_channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 用户行为日志表
-- ============================================================
CREATE TABLE user_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    event_type VARCHAR(16) NOT NULL COMMENT '事件类型: view/cart/order/pay',
    event_time DATETIME NOT NULL COMMENT '事件发生时间',
    channel VARCHAR(32) NOT NULL COMMENT '访问渠道: app/web/miniprogram',
    device VARCHAR(32) NOT NULL COMMENT '设备类型',
    product_category VARCHAR(64) COMMENT '商品类别',
    CONSTRAINT fk_user_log_user_id
        FOREIGN KEY (user_id) REFERENCES user (id)
        ON DELETE CASCADE,
    KEY idx_user_log_user_event (user_id, event_type),
    KEY idx_user_log_event_time (event_time),
    KEY idx_user_log_channel_event (channel, event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为日志表';

