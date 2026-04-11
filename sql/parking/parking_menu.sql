-- ----------------------------
-- Parking menu bootstrap
-- ----------------------------

-- Force UTF-8 client charset so importing this file via a latin1-default mysql client
-- does not double-encode Chinese menu names. Safe to run with any client.
set names utf8mb4;

-- Hide generic platform menus that do not belong in the dedicated parking system shell.
update sys_menu
set visible = '1',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Hidden for parking platform shell'
where path = 'monitor'
  and menu_type = 'M';

update sys_menu
set visible = '1',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Hidden for parking platform shell'
where path = 'http://ruoyi.vip'
  and menu_type = 'M';

-- Business-key based and idempotent: do not delete by fixed menu_id.
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '基础档案', 0, -3, 'archives', null, '', '',
  1, 0, 'M', '0', '0', '', 'tree-table',
  'admin', sysdate(), '', null, 'Parking archive root directory'
from dual
where not exists (
  select 1
  from sys_menu
  where (path = 'archives' or path = 'parking')
    and menu_type = 'M'
);

set @parking_archive_root_id := (
  select menu_id
  from sys_menu
  where (path = 'archives' or path = 'parking')
    and menu_type = 'M'
  order by case when path = 'archives' then 0 else 1 end, menu_id
  limit 1
);

update sys_menu
set menu_name = '基础档案',
    parent_id = 0,
    order_num = -3,
    path = 'archives',
    component = null,
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'M',
    visible = '0',
    status = '0',
    perms = '',
    icon = 'tree-table',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking archive root directory'
where menu_id = @parking_archive_root_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '工作台', 0, -4, 'parking', 'parking/index', '', '',
  1, 0, 'C', '0', '0', 'parking:overview:list', 'dashboard',
  'admin', sysdate(), '', null, 'Parking workbench page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/index' and menu_type = 'C'
);

set @parking_workbench_id := (
  select menu_id
  from sys_menu
  where component = 'parking/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '工作台',
    parent_id = 0,
    order_num = -4,
    path = 'parking',
    component = 'parking/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:overview:list',
    icon = 'dashboard',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking workbench page'
where menu_id = @parking_workbench_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车概览查询', @parking_workbench_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:overview:query', '#',
  'admin', sysdate(), '', null, 'Parking overview query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:overview:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '停车概览查询',
    parent_id = @parking_workbench_id,
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
where perms = 'parking:overview:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车场管理', @parking_archive_root_id, 1, 'lot', 'parking/lot/index', '', '',
  1, 0, 'C', '0', '0', 'parking:lot:list', 'table',
  'admin', sysdate(), '', null, 'Parking lot management page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/lot/index' and menu_type = 'C'
);

set @parking_lot_id := (
  select menu_id
  from sys_menu
  where component = 'parking/lot/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '停车场管理',
    parent_id = @parking_archive_root_id,
    order_num = 1,
    path = 'lot',
    component = 'parking/lot/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:lot:list',
    icon = 'table',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot management page'
where menu_id = @parking_lot_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车场查询', @parking_lot_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lot:query', '#',
  'admin', sysdate(), '', null, 'Parking lot query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lot:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '停车场查询',
    parent_id = @parking_lot_id,
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
    perms = 'parking:lot:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot query permission'
where perms = 'parking:lot:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车场新增', @parking_lot_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lot:add', '#',
  'admin', sysdate(), '', null, 'Parking lot add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lot:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '停车场新增',
    parent_id = @parking_lot_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:lot:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot add permission'
where perms = 'parking:lot:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车场修改', @parking_lot_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lot:edit', '#',
  'admin', sysdate(), '', null, 'Parking lot edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lot:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '停车场修改',
    parent_id = @parking_lot_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:lot:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot edit permission'
where perms = 'parking:lot:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车场删除', @parking_lot_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lot:remove', '#',
  'admin', sysdate(), '', null, 'Parking lot remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lot:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '停车场删除',
    parent_id = @parking_lot_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:lot:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot remove permission'
where perms = 'parking:lot:remove'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '车位管理', @parking_archive_root_id, 2, 'space', 'parking/space/index', '', '',
  1, 0, 'C', '0', '0', 'parking:space:list', 'edit',
  'admin', sysdate(), '', null, 'Parking space management page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/space/index' and menu_type = 'C'
);

set @parking_space_id := (
  select menu_id
  from sys_menu
  where component = 'parking/space/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '车位管理',
    parent_id = @parking_archive_root_id,
    order_num = 2,
    path = 'space',
    component = 'parking/space/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:space:list',
    icon = 'edit',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking space management page'
where menu_id = @parking_space_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '车位查询', @parking_space_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:space:query', '#',
  'admin', sysdate(), '', null, 'Parking space query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:space:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '车位查询',
    parent_id = @parking_space_id,
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
    perms = 'parking:space:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking space query permission'
where perms = 'parking:space:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '车位新增', @parking_space_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:space:add', '#',
  'admin', sysdate(), '', null, 'Parking space add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:space:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '车位新增',
    parent_id = @parking_space_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:space:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking space add permission'
where perms = 'parking:space:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '车位修改', @parking_space_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:space:edit', '#',
  'admin', sysdate(), '', null, 'Parking space edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:space:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '车位修改',
    parent_id = @parking_space_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:space:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking space edit permission'
where perms = 'parking:space:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '车位删除', @parking_space_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:space:remove', '#',
  'admin', sysdate(), '', null, 'Parking space remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:space:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '车位删除',
    parent_id = @parking_space_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:space:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking space remove permission'
where perms = 'parking:space:remove'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '运营中心', 0, -2, 'operations', null, '', '',
  1, 0, 'M', '0', '0', '', 'chart',
  'admin', sysdate(), '', null, 'Parking operations root directory'
from dual
where not exists (
  select 1 from sys_menu where path = 'operations' and menu_type = 'M'
);

set @parking_operations_root_id := (
  select menu_id
  from sys_menu
  where path = 'operations' and menu_type = 'M'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '运营中心',
    parent_id = 0,
    order_num = -2,
    path = 'operations',
    component = null,
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'M',
    visible = '0',
    status = '0',
    perms = '',
    icon = 'chart',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking operations root directory'
where menu_id = @parking_operations_root_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '客户中心', 0, -1, 'customers', null, '', '',
  1, 0, 'M', '0', '0', '', 'user',
  'admin', sysdate(), '', null, 'Parking customer root directory'
from dual
where not exists (
  select 1 from sys_menu where path = 'customers' and menu_type = 'M'
);

set @parking_customers_root_id := (
  select menu_id
  from sys_menu
  where path = 'customers' and menu_type = 'M'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '客户中心',
    parent_id = 0,
    order_num = -1,
    path = 'customers',
    component = null,
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'M',
    visible = '0',
    status = '0',
    perms = '',
    icon = 'user',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking customer root directory'
where menu_id = @parking_customers_root_id;

-- ----------------------------
-- 临停订单 (parking_temp_order)
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单', @parking_operations_root_id, 1, 'temp', 'parking/temp/index', '', '',
  1, 0, 'C', '0', '0', 'parking:temp:list', 'money',
  'admin', sysdate(), '', null, 'Parking temp order management page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/temp/index' and menu_type = 'C'
);

set @parking_temp_id := (
  select menu_id
  from sys_menu
  where component = 'parking/temp/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '临停订单',
    parent_id = @parking_operations_root_id,
    order_num = 1,
    path = 'temp',
    component = 'parking/temp/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:temp:list',
    icon = 'money',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order management page'
where menu_id = @parking_temp_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单查询', @parking_temp_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:temp:query', '#',
  'admin', sysdate(), '', null, 'Parking temp order query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:temp:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '临停订单查询',
    parent_id = @parking_temp_id,
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
    perms = 'parking:temp:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order query permission'
where perms = 'parking:temp:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单新增', @parking_temp_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:temp:add', '#',
  'admin', sysdate(), '', null, 'Parking temp order add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:temp:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '临停订单新增',
    parent_id = @parking_temp_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:temp:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order add permission'
where perms = 'parking:temp:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单修改', @parking_temp_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:temp:edit', '#',
  'admin', sysdate(), '', null, 'Parking temp order edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:temp:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '临停订单修改',
    parent_id = @parking_temp_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:temp:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order edit permission'
where perms = 'parking:temp:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单删除', @parking_temp_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:temp:remove', '#',
  'admin', sysdate(), '', null, 'Parking temp order remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:temp:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '临停订单删除',
    parent_id = @parking_temp_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:temp:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order remove permission'
where perms = 'parking:temp:remove'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单结算', @parking_temp_id, 5, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:temp:settle', '#',
  'admin', sysdate(), '', null, 'Parking temp order settle permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:temp:settle' and menu_type = 'F'
);

update sys_menu
set menu_name = '临停订单结算',
    parent_id = @parking_temp_id,
    order_num = 5,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:temp:settle',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order settle permission'
where perms = 'parking:temp:settle'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单入场', @parking_temp_id, 6, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:temp:entry', '#',
  'admin', sysdate(), '', null, 'Parking temp order entry permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:temp:entry' and menu_type = 'F'
);

update sys_menu
set menu_name = '临停订单入场',
    parent_id = @parking_temp_id,
    order_num = 6,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:temp:entry',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order entry permission'
where perms = 'parking:temp:entry'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单出场', @parking_temp_id, 7, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:temp:exit', '#',
  'admin', sysdate(), '', null, 'Parking temp order exit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:temp:exit' and menu_type = 'F'
);

update sys_menu
set menu_name = '临停订单出场',
    parent_id = @parking_temp_id,
    order_num = 7,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:temp:exit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking temp order exit permission'
where perms = 'parking:temp:exit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '临停订单表单', @parking_temp_id, 0, 'form', 'parking/temp/form', '', '',
  1, 0, 'C', '1', '0', '', '#',
  'admin', sysdate(), '', null, 'Temp order add/edit form page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/temp/form' and menu_type = 'C'
);

update sys_menu
set menu_name = '临停订单表单',
    parent_id = @parking_temp_id,
    order_num = 0,
    path = 'form',
    component = 'parking/temp/form',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = '',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Temp order add/edit form page'
where component = 'parking/temp/form'
  and menu_type = 'C';

-- ----------------------------
-- 月卡订单 (parking_monthly_order)
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单', @parking_operations_root_id, 2, 'monthly', 'parking/monthly/index', '', '',
  1, 0, 'C', '0', '0', 'parking:monthly:list', 'time-range',
  'admin', sysdate(), '', null, 'Parking monthly order management page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/monthly/index' and menu_type = 'C'
);

set @parking_monthly_id := (
  select menu_id
  from sys_menu
  where component = 'parking/monthly/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '月卡订单',
    parent_id = @parking_operations_root_id,
    order_num = 2,
    path = 'monthly',
    component = 'parking/monthly/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:monthly:list',
    icon = 'time-range',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking monthly order management page'
where menu_id = @parking_monthly_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单查询', @parking_monthly_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:monthly:query', '#',
  'admin', sysdate(), '', null, 'Parking monthly order query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:monthly:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '月卡订单查询',
    parent_id = @parking_monthly_id,
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
    perms = 'parking:monthly:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking monthly order query permission'
where perms = 'parking:monthly:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单新增', @parking_monthly_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:monthly:add', '#',
  'admin', sysdate(), '', null, 'Parking monthly order add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:monthly:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '月卡订单新增',
    parent_id = @parking_monthly_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:monthly:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking monthly order add permission'
where perms = 'parking:monthly:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单修改', @parking_monthly_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:monthly:edit', '#',
  'admin', sysdate(), '', null, 'Parking monthly order edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:monthly:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '月卡订单修改',
    parent_id = @parking_monthly_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:monthly:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking monthly order edit permission'
where perms = 'parking:monthly:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单删除', @parking_monthly_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:monthly:remove', '#',
  'admin', sysdate(), '', null, 'Parking monthly order remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:monthly:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '月卡订单删除',
    parent_id = @parking_monthly_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:monthly:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking monthly order remove permission'
where perms = 'parking:monthly:remove'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单支付', @parking_monthly_id, 5, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:monthly:pay', '#',
  'admin', sysdate(), '', null, 'Parking monthly order pay permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:monthly:pay' and menu_type = 'F'
);

update sys_menu
set menu_name = '月卡订单支付',
    parent_id = @parking_monthly_id,
    order_num = 5,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:monthly:pay',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking monthly order pay permission'
where perms = 'parking:monthly:pay'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单取消', @parking_monthly_id, 6, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:monthly:cancel', '#',
  'admin', sysdate(), '', null, 'Parking monthly order cancel permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:monthly:cancel' and menu_type = 'F'
);

update sys_menu
set menu_name = '月卡订单取消',
    parent_id = @parking_monthly_id,
    order_num = 6,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:monthly:cancel',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking monthly order cancel permission'
where perms = 'parking:monthly:cancel'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '月卡订单表单', @parking_monthly_id, 0, 'form', 'parking/monthly/form', '', '',
  1, 0, 'C', '1', '0', '', '#',
  'admin', sysdate(), '', null, 'Monthly order add/edit form page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/monthly/form' and menu_type = 'C'
);

update sys_menu
set menu_name = '月卡订单表单',
    parent_id = @parking_monthly_id,
    order_num = 0,
    path = 'form',
    component = 'parking/monthly/form',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = '',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Monthly order add/edit form page'
where component = 'parking/monthly/form'
  and menu_type = 'C';

-- ----------------------------
-- 会员订单 (parking_membership_order)
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单', @parking_operations_root_id, 3, 'membership', 'parking/membership/index', '', '',
  1, 0, 'C', '0', '0', 'parking:membership:list', 'peoples',
  'admin', sysdate(), '', null, 'Parking membership order management page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/membership/index' and menu_type = 'C'
);

set @parking_membership_id := (
  select menu_id
  from sys_menu
  where component = 'parking/membership/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '会员订单',
    parent_id = @parking_operations_root_id,
    order_num = 3,
    path = 'membership',
    component = 'parking/membership/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:membership:list',
    icon = 'peoples',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking membership order management page'
where menu_id = @parking_membership_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单查询', @parking_membership_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:membership:query', '#',
  'admin', sysdate(), '', null, 'Parking membership order query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:membership:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '会员订单查询',
    parent_id = @parking_membership_id,
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
    perms = 'parking:membership:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking membership order query permission'
where perms = 'parking:membership:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单新增', @parking_membership_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:membership:add', '#',
  'admin', sysdate(), '', null, 'Parking membership order add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:membership:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '会员订单新增',
    parent_id = @parking_membership_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:membership:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking membership order add permission'
where perms = 'parking:membership:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单修改', @parking_membership_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:membership:edit', '#',
  'admin', sysdate(), '', null, 'Parking membership order edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:membership:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '会员订单修改',
    parent_id = @parking_membership_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:membership:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking membership order edit permission'
where perms = 'parking:membership:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单删除', @parking_membership_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:membership:remove', '#',
  'admin', sysdate(), '', null, 'Parking membership order remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:membership:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '会员订单删除',
    parent_id = @parking_membership_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:membership:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking membership order remove permission'
where perms = 'parking:membership:remove'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单支付', @parking_membership_id, 5, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:membership:pay', '#',
  'admin', sysdate(), '', null, 'Parking membership order pay permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:membership:pay' and menu_type = 'F'
);

update sys_menu
set menu_name = '会员订单支付',
    parent_id = @parking_membership_id,
    order_num = 5,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:membership:pay',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking membership order pay permission'
where perms = 'parking:membership:pay'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单取消', @parking_membership_id, 6, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:membership:cancel', '#',
  'admin', sysdate(), '', null, 'Parking membership order cancel permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:membership:cancel' and menu_type = 'F'
);

update sys_menu
set menu_name = '会员订单取消',
    parent_id = @parking_membership_id,
    order_num = 6,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:membership:cancel',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking membership order cancel permission'
where perms = 'parking:membership:cancel'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '会员订单表单', @parking_membership_id, 0, 'form', 'parking/membership/form', '', '',
  1, 0, 'C', '1', '0', '', '#',
  'admin', sysdate(), '', null, 'Membership order add/edit form page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/membership/form' and menu_type = 'C'
);

update sys_menu
set menu_name = '会员订单表单',
    parent_id = @parking_membership_id,
    order_num = 0,
    path = 'form',
    component = 'parking/membership/form',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = '',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Membership order add/edit form page'
where component = 'parking/membership/form'
  and menu_type = 'C';

-- ----------------------------
-- 支付流水 (parking_payment_record)
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '支付流水', @parking_operations_root_id, 4, 'payment', 'parking/payment/index', '', '',
  1, 0, 'C', '0', '0', 'parking:payment:list', 'money',
  'admin', sysdate(), '', null, 'Parking payment record page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/payment/index' and menu_type = 'C'
);

set @parking_payment_id := (
  select menu_id
  from sys_menu
  where component = 'parking/payment/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '支付流水',
    parent_id = @parking_operations_root_id,
    order_num = 4,
    path = 'payment',
    component = 'parking/payment/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:payment:list',
    icon = 'money',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking payment record page'
where menu_id = @parking_payment_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '支付流水查询', @parking_payment_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:payment:query', '#',
  'admin', sysdate(), '', null, 'Parking payment query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:payment:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '支付流水查询',
    parent_id = @parking_payment_id,
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
    perms = 'parking:payment:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking payment query permission'
where perms = 'parking:payment:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '支付流水新增', @parking_payment_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:payment:add', '#',
  'admin', sysdate(), '', null, 'Parking payment add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:payment:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '支付流水新增',
    parent_id = @parking_payment_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:payment:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking payment add permission'
where perms = 'parking:payment:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '支付流水修改', @parking_payment_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:payment:edit', '#',
  'admin', sysdate(), '', null, 'Parking payment edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:payment:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '支付流水修改',
    parent_id = @parking_payment_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:payment:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking payment edit permission'
where perms = 'parking:payment:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '支付流水删除', @parking_payment_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:payment:remove', '#',
  'admin', sysdate(), '', null, 'Parking payment remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:payment:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '支付流水删除',
    parent_id = @parking_payment_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:payment:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking payment remove permission'
where perms = 'parking:payment:remove'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '支付流水退款', @parking_payment_id, 5, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:payment:refund', '#',
  'admin', sysdate(), '', null, 'Parking payment refund permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:payment:refund' and menu_type = 'F'
);

update sys_menu
set menu_name = '支付流水退款',
    parent_id = @parking_payment_id,
    order_num = 5,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:payment:refund',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking payment refund permission'
where perms = 'parking:payment:refund'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '支付流水表单', @parking_payment_id, 0, 'form', 'parking/payment/form', '', '',
  1, 0, 'C', '1', '0', '', '#',
  'admin', sysdate(), '', null, 'Payment add/edit form page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/payment/form' and menu_type = 'C'
);

update sys_menu
set menu_name = '支付流水表单',
    parent_id = @parking_payment_id,
    order_num = 0,
    path = 'form',
    component = 'parking/payment/form',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = '',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Payment add/edit form page'
where component = 'parking/payment/form'
  and menu_type = 'C';

-- ----------------------------
-- 客户档案 (parking_customer)
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '客户档案', @parking_customers_root_id, 1, 'customer', 'parking/customer/index', '', '',
  1, 0, 'C', '0', '0', 'parking:customer:list', 'peoples',
  'admin', sysdate(), '', null, 'Parking customer archive page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/customer/index' and menu_type = 'C'
);

set @parking_customer_id := (
  select menu_id
  from sys_menu
  where component = 'parking/customer/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '客户档案',
    parent_id = @parking_customers_root_id,
    order_num = 1,
    path = 'customer',
    component = 'parking/customer/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:customer:list',
    icon = 'peoples',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking customer archive page'
where menu_id = @parking_customer_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '客户档案查询', @parking_customer_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:customer:query', '#',
  'admin', sysdate(), '', null, 'Parking customer query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:customer:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '客户档案查询',
    parent_id = @parking_customer_id,
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
    perms = 'parking:customer:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking customer query permission'
where perms = 'parking:customer:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '客户档案新增', @parking_customer_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:customer:add', '#',
  'admin', sysdate(), '', null, 'Parking customer add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:customer:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '客户档案新增',
    parent_id = @parking_customer_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:customer:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking customer add permission'
where perms = 'parking:customer:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '客户档案修改', @parking_customer_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:customer:edit', '#',
  'admin', sysdate(), '', null, 'Parking customer edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:customer:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '客户档案修改',
    parent_id = @parking_customer_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:customer:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking customer edit permission'
where perms = 'parking:customer:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '客户档案删除', @parking_customer_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:customer:remove', '#',
  'admin', sysdate(), '', null, 'Parking customer remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:customer:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '客户档案删除',
    parent_id = @parking_customer_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:customer:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking customer remove permission'
where perms = 'parking:customer:remove'
  and menu_type = 'F';

-- ----------------------------
-- 用户车辆 (parking_user_vehicle)
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '用户车辆', @parking_customers_root_id, 2, 'vehicle', 'parking/vehicle/index', '', '',
  1, 0, 'C', '0', '0', 'parking:vehicle:list', 'dict',
  'admin', sysdate(), '', null, 'Parking user vehicle management page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/vehicle/index' and menu_type = 'C'
);

set @parking_vehicle_id := (
  select menu_id
  from sys_menu
  where component = 'parking/vehicle/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '用户车辆',
    parent_id = @parking_customers_root_id,
    order_num = 2,
    path = 'vehicle',
    component = 'parking/vehicle/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:vehicle:list',
    icon = 'dict',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking user vehicle management page'
where menu_id = @parking_vehicle_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '用户车辆查询', @parking_vehicle_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:vehicle:query', '#',
  'admin', sysdate(), '', null, 'Parking vehicle query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:vehicle:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '用户车辆查询',
    parent_id = @parking_vehicle_id,
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
    perms = 'parking:vehicle:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking vehicle query permission'
where perms = 'parking:vehicle:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '用户车辆新增', @parking_vehicle_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:vehicle:add', '#',
  'admin', sysdate(), '', null, 'Parking vehicle add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:vehicle:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '用户车辆新增',
    parent_id = @parking_vehicle_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:vehicle:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking vehicle add permission'
where perms = 'parking:vehicle:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '用户车辆修改', @parking_vehicle_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:vehicle:edit', '#',
  'admin', sysdate(), '', null, 'Parking vehicle edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:vehicle:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '用户车辆修改',
    parent_id = @parking_vehicle_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:vehicle:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking vehicle edit permission'
where perms = 'parking:vehicle:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '用户车辆删除', @parking_vehicle_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:vehicle:remove', '#',
  'admin', sysdate(), '', null, 'Parking vehicle remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:vehicle:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '用户车辆删除',
    parent_id = @parking_vehicle_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:vehicle:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking vehicle remove permission'
where perms = 'parking:vehicle:remove'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '设为默认车辆', @parking_vehicle_id, 5, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:vehicle:setDefault', '#',
  'admin', sysdate(), '', null, 'Parking vehicle set default permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:vehicle:setDefault' and menu_type = 'F'
);

update sys_menu
set menu_name = '设为默认车辆',
    parent_id = @parking_vehicle_id,
    order_num = 5,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:vehicle:setDefault',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking vehicle set default permission'
where perms = 'parking:vehicle:setDefault'
  and menu_type = 'F';

-- ----------------------------
-- 管理员绑定 (parking_lot_admin)
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '管理员绑定', @parking_archive_root_id, 3, 'lotadmin', 'parking/lotadmin/index', '', '',
  1, 0, 'C', '0', '0', 'parking:lotadmin:list', 'peoples',
  'admin', sysdate(), '', null, 'Parking lot admin binding page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/lotadmin/index' and menu_type = 'C'
);

set @parking_lotadmin_id := (
  select menu_id
  from sys_menu
  where component = 'parking/lotadmin/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '管理员绑定',
    parent_id = @parking_archive_root_id,
    order_num = 3,
    path = 'lotadmin',
    component = 'parking/lotadmin/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:lotadmin:list',
    icon = 'peoples',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot admin binding page'
where menu_id = @parking_lotadmin_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '管理员查询', @parking_lotadmin_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lotadmin:query', '#',
  'admin', sysdate(), '', null, 'Parking lot admin query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lotadmin:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '管理员查询',
    parent_id = @parking_lotadmin_id,
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
    perms = 'parking:lotadmin:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot admin query permission'
where perms = 'parking:lotadmin:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '管理员新增', @parking_lotadmin_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lotadmin:add', '#',
  'admin', sysdate(), '', null, 'Parking lot admin add permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lotadmin:add' and menu_type = 'F'
);

update sys_menu
set menu_name = '管理员新增',
    parent_id = @parking_lotadmin_id,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:lotadmin:add',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot admin add permission'
where perms = 'parking:lotadmin:add'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '管理员修改', @parking_lotadmin_id, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lotadmin:edit', '#',
  'admin', sysdate(), '', null, 'Parking lot admin edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lotadmin:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '管理员修改',
    parent_id = @parking_lotadmin_id,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:lotadmin:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot admin edit permission'
where perms = 'parking:lotadmin:edit'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '管理员删除', @parking_lotadmin_id, 4, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:lotadmin:remove', '#',
  'admin', sysdate(), '', null, 'Parking lot admin remove permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:lotadmin:remove' and menu_type = 'F'
);

update sys_menu
set menu_name = '管理员删除',
    parent_id = @parking_lotadmin_id,
    order_num = 4,
    path = '',
    component = '',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'parking:lotadmin:remove',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking lot admin remove permission'
where perms = 'parking:lotadmin:remove'
  and menu_type = 'F';
