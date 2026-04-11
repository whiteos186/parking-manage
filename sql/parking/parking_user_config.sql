-- ----------------------------
-- 用户个人配置表
-- ----------------------------
create table if not exists parking_user_config (
  id           bigint(20)  not null auto_increment comment '主键',
  user_id      bigint(20)  not null               comment '用户ID',
  config_key   varchar(64) not null               comment '配置键',
  config_value text                               comment '配置值（JSON字符串）',
  update_time  datetime    default null           comment '更新时间',
  primary key (id),
  unique key uk_user_config (user_id, config_key)
) engine=InnoDB default charset=utf8mb4 comment='用户个人配置表';
