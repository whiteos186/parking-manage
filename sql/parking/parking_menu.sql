-- ----------------------------
-- Parking menu bootstrap
-- ----------------------------

-- Business-key based and idempotent: do not delete by fixed menu_id.
-- 1) Ensure root directory exists.
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车管理', 0, 6, 'parking', null, '', '',
  1, 0, 'M', '0', '0', '', 'guide',
  'admin', sysdate(), '', null, 'Parking module directory'
from dual
where not exists (
  select 1 from sys_menu where path = 'parking' and menu_type = 'M'
);

set @parking_root_id := (
  select menu_id
  from sys_menu
  where path = 'parking' and menu_type = 'M'
  order by menu_id
  limit 1
);

-- Keep root row aligned with expected values without touching role-menu relations.
update sys_menu
set menu_name = '停车管理',
    parent_id = 0,
    order_num = 6,
    path = 'parking',
    component = null,
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'M',
    visible = '0',
    status = '0',
    perms = '',
    icon = 'guide',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking module directory'
where menu_id = @parking_root_id;

-- 2) Ensure landing page exists under root.
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车概览', @parking_root_id, 1, 'index', 'parking/index', '', '',
  1, 0, 'C', '0', '0', 'parking:overview:list', 'build',
  'admin', sysdate(), '', null, 'Parking landing page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/index' and menu_type = 'C'
);

set @parking_overview_id := (
  select menu_id
  from sys_menu
  where component = 'parking/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '停车概览',
    parent_id = @parking_root_id,
    order_num = 1,
    path = 'index',
    component = 'parking/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:overview:list',
    icon = 'build',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking landing page'
where menu_id = @parking_overview_id;

-- 3) Ensure query permission button exists under landing page.
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车概览查询', @parking_overview_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:overview:query', '#',
  'admin', sysdate(), '', null, 'Parking overview query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:overview:query' and menu_type = 'F'
);

set @parking_overview_query_id := (
  select menu_id
  from sys_menu
  where perms = 'parking:overview:query' and menu_type = 'F'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '停车概览查询',
    parent_id = @parking_overview_id,
    order_num = 1,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:overview:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking overview query permission'
where menu_id = @parking_overview_query_id;
