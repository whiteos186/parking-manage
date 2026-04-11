-- ----------------------------
-- Remove monitor menus from existing databases
-- Safe to run multiple times (idempotent DELETE by menu_id)
-- ----------------------------
set names utf8mb4;

-- Button permissions under monitor menus
delete from sys_menu where menu_id in (
  1039, 1040, 1041,  -- 操作日志按钮
  1042, 1043, 1044, 1045,  -- 登录日志按钮
  1046, 1047, 1048,  -- 在线用户按钮
  1049, 1050, 1051, 1052, 1053, 1054  -- 定时任务按钮
);

-- Leaf menus under 系统监控 (id=2) and 日志管理 (id=108)
delete from sys_menu where menu_id in (
  109,  -- 在线用户
  110,  -- 定时任务
  111,  -- 数据监控 (druid)
  112,  -- 服务监控
  113,  -- 缓存监控
  114,  -- 缓存列表
  500,  -- 操作日志
  501   -- 登录日志
);

-- Parent menus
delete from sys_menu where menu_id in (
  108,  -- 日志管理 (under 系统管理)
  2     -- 系统监控 (top-level)
);
