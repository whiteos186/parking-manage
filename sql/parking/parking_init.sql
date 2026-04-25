-- ============================================================
-- parking_init.sql
-- ONE file to run for a fresh database installation.
-- Execution order:
--   1. DDL          (10 tables, updated schema)
--   2. Seed data    (1 lot, 3 spaces, 1 customer, 1 vehicle, demo orders/payments)
--   3. Menus        (parking_menu.sql verbatim)
--   4. Global config seeds (sys_config)
--   5. Dict seeds   (sys_dict_type + sys_dict_data)
--   6. Roles and test users
-- ============================================================

set names utf8mb4;

-- ============================================================
-- Section 1: DDL
-- ============================================================

drop table if exists parking_user_config;
drop table if exists parking_payment_record;
drop table if exists parking_temp_order;
drop table if exists parking_monthly_order;
drop table if exists parking_membership_order;
drop table if exists parking_user_vehicle;
drop table if exists parking_customer;
drop table if exists parking_lot_admin;
drop table if exists parking_space;
drop table if exists parking_lot;

-- ----------------------------
-- 1. parking_lot
-- ----------------------------
create table parking_lot (
  lot_id                bigint(20)      not null auto_increment comment 'Parking lot ID',
  lot_name              varchar(100)    not null default ''     comment 'Parking lot name',
  lot_address           varchar(255)    not null default ''     comment 'Parking lot address',
  total_space_count     int(11)         default 0               comment 'Total space count',
  available_space_count int(11)         default 0               comment 'Available space count',
  monthly_price         decimal(10,2)   default 0.00            comment 'Monthly package price',
  temp_hour_price       decimal(10,2)   default 0.00            comment 'Temporary parking hourly price',
  status                char(1)         default '0'             comment 'Status (0 normal, 1 disabled)',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (lot_id),
  key idx_parking_lot_status (status)
) engine=innodb auto_increment=100 comment='Parking lot';

-- ----------------------------
-- 2. parking_space
-- ----------------------------
create table parking_space (
  space_id              bigint(20)      not null auto_increment comment 'Parking space ID',
  lot_id                bigint(20)      not null                comment 'Parking lot ID',
  space_code            varchar(32)     not null                comment 'Parking space code',
  area_name             varchar(64)     default ''              comment 'Area name',
  floor_no              varchar(20)     default ''              comment 'Floor number',
  space_type            char(1)         default '1'             comment 'Space type (1 standard, 2 charging, 3 accessible, 4 vip)',
  status                char(1)         default '0'             comment 'Space status (0 free, 1 occupied, 2 disabled, 3 locked)',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (space_id),
  unique key uk_parking_space_lot_code (lot_id, space_code),
  key idx_parking_space_lot_id (lot_id),
  key idx_parking_space_status (status)
) engine=innodb auto_increment=1000 comment='Parking space';

-- ----------------------------
-- 3. parking_lot_admin
-- ----------------------------
create table parking_lot_admin (
  lot_admin_id          bigint(20)      not null auto_increment comment 'Lot admin binding ID',
  lot_id                bigint(20)      not null                comment 'Parking lot ID',
  user_id               bigint(20)      not null                comment 'sys_user.user_id',
  status                char(1)         default '0'             comment 'Binding status (0 active, 1 disabled)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (lot_admin_id),
  unique key uk_parking_lot_admin (lot_id, user_id),
  key idx_parking_lot_admin_user_id (user_id)
) engine=innodb auto_increment=100 comment='Parking lot and admin binding';

-- ----------------------------
-- 4. parking_customer  (user_id added for sys_user linkage)
-- ----------------------------
create table parking_customer (
  customer_id           bigint(20)      not null auto_increment comment 'Customer ID',
  user_id               bigint(20)      default null            comment 'sys_user.user_id',
  customer_code         varchar(32)     not null                comment 'Customer code',
  customer_name         varchar(50)     not null default ''     comment 'Customer name',
  mobile                varchar(20)     not null default ''     comment 'Mobile number',
  customer_type         char(1)         default '1'             comment 'Customer type (1 owner, 2 member, 3 enterprise, 4 visitor)',
  is_member             char(1)         default '0'             comment 'Member flag (0 no, 1 yes)',
  member_type           char(1)         default null            comment 'Member level (1 silver, 2 gold, 3 platinum)',
  member_expire_time    datetime        default null            comment 'Member expiry time',
  status                char(1)         default '0'             comment 'Status (0 active, 1 disabled)',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (customer_id),
  unique key uk_parking_customer_user_id (user_id),
  unique key uk_parking_customer_code (customer_code),
  key idx_parking_customer_mobile (mobile)
) engine=innodb auto_increment=100 comment='Parking customer archive';

-- ----------------------------
-- 5. parking_user_vehicle
-- ----------------------------
create table parking_user_vehicle (
  vehicle_id            bigint(20)      not null auto_increment comment 'Vehicle ID',
  customer_id           bigint(20)      not null                comment 'parking_customer.customer_id',
  plate_no              varchar(20)     not null                comment 'Plate number',
  vehicle_type          char(1)         default '1'             comment 'Vehicle type (1 small car, 2 suv, 3 new energy, 4 other)',
  brand_name            varchar(50)     default ''              comment 'Vehicle brand',
  vehicle_color         varchar(20)     default ''              comment 'Vehicle color',
  is_default            char(1)         default '0'             comment 'Default flag (0 no, 1 yes)',
  status                char(1)         default '0'             comment 'Vehicle status (0 active, 1 disabled)',
  bind_time             datetime                                   comment 'Bind time',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (vehicle_id),
  unique key uk_parking_vehicle_plate_no (plate_no),
  key idx_parking_vehicle_customer_id (customer_id)
) engine=innodb auto_increment=100 comment='User vehicle';

-- ----------------------------
-- 6. parking_membership_order
-- ----------------------------
create table parking_membership_order (
  membership_order_id   bigint(20)      not null auto_increment comment 'Membership order ID',
  order_no              varchar(32)     not null                comment 'Order number',
  customer_id           bigint(20)      not null                comment 'parking_customer.customer_id',
  vehicle_id            bigint(20)      default null            comment 'Vehicle ID',
  lot_id                bigint(20)      not null                comment 'Parking lot ID',
  membership_type       char(1)         default '1'             comment 'Membership type (1 silver, 2 gold, 3 platinum)',
  valid_start_time      datetime                                   comment 'Validity start time',
  valid_end_time        datetime                                   comment 'Validity end time',
  original_amount       decimal(10,2)   default 0.00            comment 'Original amount',
  discount_amount       decimal(10,2)   default 0.00            comment 'Discount amount',
  pay_amount            decimal(10,2)   default 0.00            comment 'Paid amount',
  pay_status            char(1)         default '0'             comment 'Payment status (0 unpaid, 1 paid, 2 refunded, 3 closed)',
  biz_status            char(1)         default '0'             comment 'Business status (0 pending, 1 active, 2 expired, 3 canceled)',
  pay_time              datetime                                   comment 'Payment time',
  cancel_time           datetime                                   comment 'Cancel time',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (membership_order_id),
  unique key uk_parking_membership_order_no (order_no),
  key idx_parking_membership_customer_id (customer_id),
  key idx_parking_membership_lot_id (lot_id)
) engine=innodb auto_increment=100 comment='Parking membership order';

-- ----------------------------
-- 7. parking_monthly_order  (vehicle_plate_no added)
-- ----------------------------
create table parking_monthly_order (
  monthly_order_id      bigint(20)      not null auto_increment comment 'Monthly order ID',
  order_no              varchar(32)     not null                comment 'Order number',
  customer_id           bigint(20)      not null                comment 'parking_customer.customer_id',
  vehicle_id            bigint(20)      default null            comment 'Vehicle ID',
  vehicle_plate_no      varchar(20)     default ''              comment 'Vehicle plate number',
  lot_id                bigint(20)      not null                comment 'Parking lot ID',
  month_count           int(11)         default 1               comment 'Purchased month count',
  start_time            datetime                                   comment 'Start time',
  end_time              datetime                                   comment 'End time',
  original_amount       decimal(10,2)   default 0.00            comment 'Original amount',
  discount_amount       decimal(10,2)   default 0.00            comment 'Discount amount',
  pay_amount            decimal(10,2)   default 0.00            comment 'Paid amount',
  pay_status            char(1)         default '0'             comment 'Payment status (0 unpaid, 1 paid, 2 refunded, 3 closed)',
  biz_status            char(1)         default '0'             comment 'Business status (0 pending, 1 active, 2 expired, 3 canceled)',
  pay_time              datetime                                   comment 'Payment time',
  cancel_time           datetime                                   comment 'Cancel time',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (monthly_order_id),
  unique key uk_parking_monthly_order_no (order_no),
  key idx_parking_monthly_customer_id (customer_id),
  key idx_parking_monthly_lot_id (lot_id)
) engine=innodb auto_increment=100 comment='Parking monthly order';

-- ----------------------------
-- 8. parking_temp_order
-- ----------------------------
create table parking_temp_order (
  temp_order_id         bigint(20)      not null auto_increment comment 'Temporary order ID',
  order_no              varchar(32)     not null                comment 'Order number',
  lot_id                bigint(20)      not null                comment 'Parking lot ID',
  space_id              bigint(20)      default null            comment 'Parking space ID',
  customer_id           bigint(20)      default null            comment 'parking_customer.customer_id',
  vehicle_plate_no      varchar(20)     not null                comment 'Vehicle plate number',
  in_time               datetime        not null                comment 'Entry time',
  out_time              datetime                                   comment 'Exit time',
  parking_duration_min  int(11)         default 0               comment 'Parking duration (minutes)',
  fee_amount            decimal(10,2)   default 0.00            comment 'Calculated fee amount',
  discount_amount       decimal(10,2)   default 0.00            comment 'Discount amount',
  pay_amount            decimal(10,2)   default 0.00            comment 'Paid amount',
  pay_status            char(1)         default '0'             comment 'Payment status (0 unpaid, 1 paid, 2 refunded, 3 closed)',
  biz_status            char(1)         default '0'             comment 'Business status (0 parking, 1 waiting payment, 2 finished, 3 canceled)',
  pay_time              datetime                                   comment 'Payment time',
  close_time            datetime                                   comment 'Close time',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (temp_order_id),
  unique key uk_parking_temp_order_no (order_no),
  key idx_parking_temp_lot_id (lot_id),
  key idx_parking_temp_customer_id (customer_id),
  key idx_parking_temp_plate_no (vehicle_plate_no)
) engine=innodb auto_increment=100 comment='Parking temporary order';

-- ----------------------------
-- 9. parking_payment_record
-- ----------------------------
create table parking_payment_record (
  payment_id            bigint(20)      not null auto_increment comment 'Payment record ID',
  biz_order_no          varchar(32)     not null                comment 'Business order number',
  biz_order_type        char(1)         not null                comment 'Business order type (1 membership, 2 monthly, 3 temporary)',
  customer_id           bigint(20)      default null            comment 'parking_customer.customer_id',
  lot_id                bigint(20)      default null            comment 'Parking lot ID',
  pay_channel           char(1)         default '1'             comment 'Pay channel (1 wechat, 2 alipay, 3 cash, 4 other)',
  pay_amount            decimal(10,2)   default 0.00            comment 'Payment amount',
  pay_status            char(1)         default '0'             comment 'Payment status (0 pending, 1 success, 2 failed, 3 refunded)',
  trade_no              varchar(64)     default null            comment 'External trade number',
  pay_time              datetime                                   comment 'Payment time',
  refund_time           datetime                                   comment 'Refund time',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (payment_id),
  unique key uk_parking_payment_trade_no (trade_no),
  key idx_parking_payment_biz_order (biz_order_type, biz_order_no),
  key idx_parking_payment_customer_id (customer_id)
) engine=innodb auto_increment=100 comment='Parking payment record';

-- ----------------------------
-- 10. parking_user_config
-- ----------------------------
create table parking_user_config (
  id          bigint(20)   not null auto_increment comment 'Primary key',
  user_id     bigint(20)   not null               comment 'User ID',
  config_key  varchar(64)  not null               comment 'Config key',
  config_value text                               comment 'Config value (JSON string)',
  update_time datetime     default null           comment 'Update time',
  primary key (id),
  unique key uk_user_config (user_id, config_key)
) engine=InnoDB default charset=utf8mb4 comment='User personal config';

-- ============================================================
-- Section 2: Minimal seed data
-- ============================================================

insert into parking_lot (
  lot_id, lot_name, lot_address, total_space_count, available_space_count,
  monthly_price, temp_hour_price, status, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'RuoYi Science Park Lot', 'No. 88 Innovation Road, Shenzhen', 3, 2,
  380.00, 8.00, '0', '0',
  'admin', sysdate(), 'admin', sysdate(), 'Bootstrap demo lot'
);

insert into parking_space (
  space_id, lot_id, space_code, area_name, floor_no, space_type, status, del_flag,
  create_by, create_time, update_by, update_time, remark
) values
  (1, 1, 'A1-001', 'A', 'B1', '1', '0', '0', 'admin', sysdate(), 'admin', sysdate(), 'Near elevator'),
  (2, 1, 'A1-002', 'A', 'B1', '1', '0', '0', 'admin', sysdate(), 'admin', sysdate(), 'Standard parking space'),
  (3, 1, 'C2-018', 'C', 'B2', '2', '1', '0', 'admin', sysdate(), 'admin', sysdate(), 'Charging space currently occupied');

insert into parking_customer (
  customer_id, customer_code, customer_name, mobile, customer_type, is_member, member_type, member_expire_time, status, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'CUST-0001', '陈晓', '13800138000', '1', '0', null, null, '0', '0',
  'admin', sysdate(), 'admin', sysdate(), 'Demo parking customer'
);

insert into parking_user_vehicle (
  vehicle_id, customer_id, plate_no, vehicle_type, brand_name, vehicle_color, is_default, status, bind_time, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 1, 'DEMO-A123', '1', 'Demo', 'White', '1', '0', sysdate(), '0',
  'admin', sysdate(), 'admin', sysdate(), 'Demo vehicle for customer1'
);

insert into parking_membership_order (
  membership_order_id, order_no, customer_id, vehicle_id, lot_id, membership_type,
  valid_start_time, valid_end_time, original_amount, discount_amount, pay_amount,
  pay_status, biz_status, pay_time, cancel_time, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'PO-DEMO-0001', 1, 1, 1, '2',
  sysdate(), date_add(sysdate(), interval 1 year), 300.00, 30.00, 270.00,
  '1', '1', sysdate(), null, '0',
  'admin', sysdate(), 'admin', sysdate(), 'Paid demo membership order'
);

insert into parking_monthly_order (
  monthly_order_id, order_no, customer_id, vehicle_id, vehicle_plate_no, lot_id,
  month_count, start_time, end_time, original_amount, discount_amount, pay_amount,
  pay_status, biz_status, pay_time, cancel_time, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'MO-DEMO-0001', 1, 1, 'DEMO-A123', 1,
  1, sysdate(), date_add(sysdate(), interval 1 month), 380.00, 0.00, 380.00,
  '0', '0', null, null, '0',
  'admin', sysdate(), 'admin', sysdate(), 'Pending demo monthly order'
);

insert into parking_temp_order (
  temp_order_id, order_no, lot_id, space_id, customer_id, vehicle_plate_no,
  in_time, out_time, parking_duration_min, fee_amount, discount_amount, pay_amount,
  pay_status, biz_status, pay_time, close_time, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'TO-DEMO-0001', 1, null, 1, 'DEMO-A123',
  date_sub(sysdate(), interval 2 hour), sysdate(), 120, 16.00, 0.00, 16.00,
  '1', '3', sysdate(), sysdate(), '0',
  'admin', sysdate(), 'admin', sysdate(), 'Paid demo temporary parking order'
);

insert into parking_payment_record (
  payment_id, biz_order_no, biz_order_type, customer_id, lot_id, pay_channel,
  pay_amount, pay_status, trade_no, pay_time, refund_time,
  create_by, create_time, update_by, update_time, remark
) values
  (1, 'PO-DEMO-0001', '1', 1, 1, '3', 270.00, '1', 'TR-DEMO-MEMBER-0001', sysdate(), null,
   'admin', sysdate(), 'admin', sysdate(), 'Demo membership payment'),
  (2, 'TO-DEMO-0001', '3', 1, 1, '3', 16.00, '1', 'TR-DEMO-TEMP-0001', sysdate(), null,
   'admin', sysdate(), 'admin', sysdate(), 'Demo temporary parking payment');

-- ============================================================
-- Section 3: Menus
-- ============================================================

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

-- ----------------------------
-- Parking settings
-- ----------------------------

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置', @parking_archive_root_id, 4, 'settings', 'parking/settings/index', '', '',
  1, 0, 'C', '0', '0', 'parking:settings:list', 'setting',
  'admin', sysdate(), '', null, 'Parking global settings page'
from dual
where not exists (
  select 1 from sys_menu where component = 'parking/settings/index' and menu_type = 'C'
);

set @parking_settings_id := (
  select menu_id
  from sys_menu
  where component = 'parking/settings/index' and menu_type = 'C'
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '停车设置',
    parent_id = @parking_archive_root_id,
    order_num = 4,
    path = 'settings',
    component = 'parking/settings/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:settings:list',
    icon = 'setting',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking global settings page'
where menu_id = @parking_settings_id;

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置查询', @parking_settings_id, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:settings:query', '#',
  'admin', sysdate(), '', null, 'Parking settings query permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:settings:query' and menu_type = 'F'
);

update sys_menu
set menu_name = '停车设置查询',
    parent_id = @parking_settings_id,
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
    perms = 'parking:settings:query',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking settings query permission'
where perms = 'parking:settings:query'
  and menu_type = 'F';

insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置修改', @parking_settings_id, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:settings:edit', '#',
  'admin', sysdate(), '', null, 'Parking settings edit permission'
from dual
where not exists (
  select 1 from sys_menu where perms = 'parking:settings:edit' and menu_type = 'F'
);

update sys_menu
set menu_name = '停车设置修改',
    parent_id = @parking_settings_id,
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
    perms = 'parking:settings:edit',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = 'Parking settings edit permission'
where perms = 'parking:settings:edit'
  and menu_type = 'F';

-- ============================================================
-- Section 4: Global config seeds
-- ============================================================

-- Parking global config seeds
-- ============================================================

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-月卡全局价格', 'parking.rule.monthlyPrice', '380.00', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.monthlyPrice'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-临停全局小时单价', 'parking.rule.tempHourPrice', '8.00', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.tempHourPrice'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-临停免费分钟数', 'parking.rule.tempFreeMinutes', '0', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.tempFreeMinutes'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-临停计费步长分钟数', 'parking.rule.tempBillingStepMinutes', '30', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.tempBillingStepMinutes'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-临停向上取整开关', 'parking.rule.tempRoundUpEnabled', 'true', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.tempRoundUpEnabled'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-临停封顶金额', 'parking.rule.tempDailyCapAmount', '0.00', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.tempDailyCapAmount'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-会员折扣开关', 'parking.rule.memberDiscountEnabled', 'true', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.memberDiscountEnabled'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-银卡折扣率', 'parking.rule.memberDiscount.silver', '0.05', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.memberDiscount.silver'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-金卡折扣率', 'parking.rule.memberDiscount.gold', '0.10', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.memberDiscount.gold'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-铂金折扣率', 'parking.rule.memberDiscount.platinum', '0.15', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.memberDiscount.platinum'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-默认支付渠道', 'parking.rule.payment.defaultChannel', '1', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.payment.defaultChannel'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-现金支付开关', 'parking.rule.payment.cashEnabled', 'true', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.payment.cashEnabled'
);

insert into sys_config (
  config_name, config_key, config_value, config_type,
  create_by, create_time, update_by, update_time, remark
)
select
  '停车设置-取消自动退款开关', 'parking.rule.order.cancelAutoRefundEnabled', 'false', 'N',
  'admin', sysdate(), '', null, 'parking global rule'
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.order.cancelAutoRefundEnabled'
);

-- ============================================================

-- ============================================================
-- Section 5: Dict seeds
-- ============================================================

-- Parking dict seeds (phase 1)
-- ============================================================

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '停车场状态', 'parking_lot_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_lot_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '支付业务类型', 'parking_payment_biz_type', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_payment_biz_type');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '支付渠道', 'parking_payment_channel', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_payment_channel');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '支付状态', 'parking_payment_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_payment_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '会员类型', 'parking_membership_type', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_membership_type');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '正常', '0', 'parking_lot_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_lot_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '停用', '1', 'parking_lot_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_lot_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '会员', '1', 'parking_payment_biz_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_biz_type' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '月卡', '2', 'parking_payment_biz_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_biz_type' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '临停', '3', 'parking_payment_biz_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_biz_type' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '微信', '1', 'parking_payment_channel', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_channel' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '支付宝', '2', 'parking_payment_channel', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_channel' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '现金', '3', 'parking_payment_channel', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_channel' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '其他', '4', 'parking_payment_channel', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_channel' and dict_value = '4');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '处理中', '0', 'parking_payment_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '成功', '1', 'parking_payment_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '失败', '2', 'parking_payment_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已退款', '3', 'parking_payment_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_payment_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '银卡', '1', 'parking_membership_type', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_type' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '金卡', '2', 'parking_membership_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_type' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '铂金', '3', 'parking_membership_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_type' and dict_value = '3');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '车位状态', 'parking_space_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_space_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '车位类型', 'parking_space_type', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_space_type');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '车辆类型', 'parking_vehicle_type', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_vehicle_type');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '车辆状态', 'parking_vehicle_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_vehicle_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '默认车辆标识', 'parking_vehicle_default', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_vehicle_default');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '客户类型', 'parking_customer_type', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_customer_type');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '客户状态', 'parking_customer_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_customer_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '临停订单支付状态', 'parking_temp_pay_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_temp_pay_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '临停订单业务状态', 'parking_temp_biz_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_temp_biz_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '月卡订单支付状态', 'parking_monthly_pay_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_monthly_pay_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '月卡订单业务状态', 'parking_monthly_biz_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_monthly_biz_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '会员订单支付状态', 'parking_membership_pay_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_membership_pay_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '会员订单业务状态', 'parking_membership_biz_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_membership_biz_status');

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '停车场管理员绑定状态', 'parking_lot_admin_status', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'parking_lot_admin_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '空闲', '0', 'parking_space_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '占用', '1', 'parking_space_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '停用', '2', 'parking_space_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '锁定', '3', 'parking_space_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '标准车位', '1', 'parking_space_type', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_type' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '充电车位', '2', 'parking_space_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_type' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '无障碍车位', '3', 'parking_space_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_type' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, 'VIP车位', '4', 'parking_space_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_space_type' and dict_value = '4');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '小型车', '1', 'parking_vehicle_type', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_type' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, 'SUV', '2', 'parking_vehicle_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_type' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '新能源', '3', 'parking_vehicle_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_type' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '其他', '4', 'parking_vehicle_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_type' and dict_value = '4');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '启用', '0', 'parking_vehicle_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '停用', '1', 'parking_vehicle_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '否', '0', 'parking_vehicle_default', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_default' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '默认', '1', 'parking_vehicle_default', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_vehicle_default' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '业主车主', '1', 'parking_customer_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_customer_type' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '会员客户', '2', 'parking_customer_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_customer_type' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '企业客户', '3', 'parking_customer_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_customer_type' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '访客客户', '4', 'parking_customer_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_customer_type' and dict_value = '4');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '启用', '0', 'parking_customer_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_customer_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '停用', '1', 'parking_customer_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_customer_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '未支付', '0', 'parking_temp_pay_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_pay_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '已支付', '1', 'parking_temp_pay_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_pay_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '已退款', '2', 'parking_temp_pay_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_pay_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已关闭', '3', 'parking_temp_pay_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_pay_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '待入场', '0', 'parking_temp_biz_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_biz_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '停车中', '1', 'parking_temp_biz_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_biz_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '待支付', '2', 'parking_temp_biz_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_biz_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已完成', '3', 'parking_temp_biz_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_temp_biz_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '未支付', '0', 'parking_monthly_pay_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_pay_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '已支付', '1', 'parking_monthly_pay_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_pay_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '已退款', '2', 'parking_monthly_pay_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_pay_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已关闭', '3', 'parking_monthly_pay_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_pay_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '待生效', '0', 'parking_monthly_biz_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_biz_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '生效中', '1', 'parking_monthly_biz_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_biz_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '已过期', '2', 'parking_monthly_biz_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_biz_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已取消', '3', 'parking_monthly_biz_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_monthly_biz_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '未支付', '0', 'parking_membership_pay_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_pay_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '已支付', '1', 'parking_membership_pay_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_pay_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '已退款', '2', 'parking_membership_pay_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_pay_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已关闭', '3', 'parking_membership_pay_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_pay_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '待生效', '0', 'parking_membership_biz_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_biz_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '生效中', '1', 'parking_membership_biz_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_biz_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '已过期', '2', 'parking_membership_biz_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_biz_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已取消', '3', 'parking_membership_biz_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_membership_biz_status' and dict_value = '3');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '启用', '0', 'parking_lot_admin_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_lot_admin_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '停用', '1', 'parking_lot_admin_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, 'parking dict'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'parking_lot_admin_status' and dict_value = '1');

-- ============================================================
-- Section 6: Roles and test users
-- ============================================================

set names utf8mb4;

-- ============================================================
-- 2. 新增两个角色（幂等，按 role_key 判断）
-- ============================================================

-- 停车场管理员
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select 100, '停车场管理员', 'lot_admin', 3, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '停车场管理员角色'
from dual where not exists (select 1 from sys_role where role_key = 'lot_admin');

-- 普通用户（车主）
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select 101, '普通用户', 'customer', 4, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通用户（车主）角色'
from dual where not exists (select 1 from sys_role where role_key = 'customer');

-- 锚定两个角色 ID（支持 role_id 与预设值不同的情况）
set @lot_admin_role_id := (select role_id from sys_role where role_key = 'lot_admin' limit 1);
set @customer_role_id  := (select role_id from sys_role where role_key = 'customer'  limit 1);

-- ============================================================
-- 3. 菜单分配辅助存储过程
--    根据 perms 字符串查找菜单，insert ignore 到 sys_role_menu，
--    并沿 parent_id 链路向上补全所有目录节点
-- ============================================================

drop procedure if exists parking_assign_menu;

delimiter $$
create procedure parking_assign_menu(in p_role_id bigint, in p_perms varchar(200))
begin
  declare v_menu_id   bigint default null;
  declare v_parent_id bigint default 0;

  -- 按 perms 查找菜单（优先取 menu_id 最小的那条）
  select menu_id into v_menu_id
  from sys_menu
  where perms = p_perms
  order by menu_id
  limit 1;

  if v_menu_id is not null then
    -- 插入当前节点
    insert ignore into sys_role_menu (role_id, menu_id) values (p_role_id, v_menu_id);

    -- 沿 parent_id 链向上插入所有祖先目录节点
    select parent_id into v_parent_id from sys_menu where menu_id = v_menu_id;
    while v_parent_id > 0 do
      insert ignore into sys_role_menu (role_id, menu_id) values (p_role_id, v_parent_id);
      set v_menu_id = v_parent_id;
      select parent_id into v_parent_id from sys_menu where menu_id = v_menu_id;
    end while;
  end if;
end$$
delimiter ;

-- ============================================================
-- 4. 为 lot_admin（停车场管理员）分配菜单
-- ============================================================

-- 工作台（概览）
call parking_assign_menu(@lot_admin_role_id, 'parking:overview:list');
call parking_assign_menu(@lot_admin_role_id, 'parking:overview:query');

-- 停车场管理
call parking_assign_menu(@lot_admin_role_id, 'parking:lot:list');
call parking_assign_menu(@lot_admin_role_id, 'parking:lot:query');
call parking_assign_menu(@lot_admin_role_id, 'parking:lot:edit');

-- 车位管理
call parking_assign_menu(@lot_admin_role_id, 'parking:space:list');
call parking_assign_menu(@lot_admin_role_id, 'parking:space:query');
call parking_assign_menu(@lot_admin_role_id, 'parking:space:add');
call parking_assign_menu(@lot_admin_role_id, 'parking:space:edit');
call parking_assign_menu(@lot_admin_role_id, 'parking:space:remove');

-- 临停订单
call parking_assign_menu(@lot_admin_role_id, 'parking:temp:list');
call parking_assign_menu(@lot_admin_role_id, 'parking:temp:query');
call parking_assign_menu(@lot_admin_role_id, 'parking:temp:add');
call parking_assign_menu(@lot_admin_role_id, 'parking:temp:edit');
call parking_assign_menu(@lot_admin_role_id, 'parking:temp:settle');
call parking_assign_menu(@lot_admin_role_id, 'parking:temp:entry');
call parking_assign_menu(@lot_admin_role_id, 'parking:temp:exit');

-- 月卡订单
call parking_assign_menu(@lot_admin_role_id, 'parking:monthly:list');
call parking_assign_menu(@lot_admin_role_id, 'parking:monthly:query');
call parking_assign_menu(@lot_admin_role_id, 'parking:monthly:edit');

-- 客户档案
call parking_assign_menu(@lot_admin_role_id, 'parking:customer:list');
call parking_assign_menu(@lot_admin_role_id, 'parking:customer:query');

-- 用户车辆
call parking_assign_menu(@lot_admin_role_id, 'parking:vehicle:list');
call parking_assign_menu(@lot_admin_role_id, 'parking:vehicle:query');

-- ============================================================
-- 5. 为 customer（普通用户/车主）分配菜单
-- ============================================================

-- 月卡订单
call parking_assign_menu(@customer_role_id, 'parking:monthly:list');
call parking_assign_menu(@customer_role_id, 'parking:monthly:query');
call parking_assign_menu(@customer_role_id, 'parking:monthly:add');
call parking_assign_menu(@customer_role_id, 'parking:monthly:pay');
call parking_assign_menu(@customer_role_id, 'parking:monthly:cancel');

-- 临停订单（仅查看）
call parking_assign_menu(@customer_role_id, 'parking:temp:list');
call parking_assign_menu(@customer_role_id, 'parking:temp:query');

-- 会员订单
call parking_assign_menu(@customer_role_id, 'parking:membership:list');
call parking_assign_menu(@customer_role_id, 'parking:membership:query');
call parking_assign_menu(@customer_role_id, 'parking:membership:add');
call parking_assign_menu(@customer_role_id, 'parking:membership:pay');
call parking_assign_menu(@customer_role_id, 'parking:membership:cancel');

-- 支付流水
call parking_assign_menu(@customer_role_id, 'parking:payment:list');
call parking_assign_menu(@customer_role_id, 'parking:payment:query');

-- 用户车辆（完整自助管理）
call parking_assign_menu(@customer_role_id, 'parking:vehicle:list');
call parking_assign_menu(@customer_role_id, 'parking:vehicle:query');
call parking_assign_menu(@customer_role_id, 'parking:vehicle:add');
call parking_assign_menu(@customer_role_id, 'parking:vehicle:edit');
call parking_assign_menu(@customer_role_id, 'parking:vehicle:remove');
call parking_assign_menu(@customer_role_id, 'parking:vehicle:setDefault');

-- ============================================================
-- 6. 创建测试用户
--    密码均为 admin123，BCrypt hash:
--    $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
-- ============================================================

-- 停车场管理员测试账号 lotadmin1
insert into sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
select 100, 103, 'lotadmin1', '停车场管理员', '00', 'lotadmin@test.com', '13900001111', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试停车场管理员'
from dual where not exists (select 1 from sys_user where user_name = 'lotadmin1');

-- 普通车主测试账号 customer1
insert into sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
select 101, 103, 'customer1', '车主用户', '00', 'customer@test.com', '13800138001', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试普通车主用户'
from dual where not exists (select 1 from sys_user where user_name = 'customer1');

-- 锚定测试用户 ID
set @lotadmin1_id := (select user_id from sys_user where user_name = 'lotadmin1' limit 1);
set @customer1_id := (select user_id from sys_user where user_name = 'customer1' limit 1);

-- 绑定用户到角色
insert ignore into sys_user_role (user_id, role_id) values (@lotadmin1_id, @lot_admin_role_id);
insert ignore into sys_user_role (user_id, role_id) values (@customer1_id, @customer_role_id);

-- 将 lotadmin1 绑定到演示停车场（lot_id=1）
insert into parking_lot_admin (lot_id, user_id, status, create_by, create_time, update_by, update_time, remark)
select 1, @lotadmin1_id, '0', 'admin', sysdate(), 'admin', sysdate(), 'Test lot admin binding'
from dual where not exists (
  select 1 from parking_lot_admin where lot_id = 1 and user_id = @lotadmin1_id
);

-- 将演示 parking_customer 记录（customer_id=1）关联到 customer1 的 sys_user 账号
update parking_customer
set user_id = @customer1_id
where customer_id = 1
  and (user_id is null or user_id = 0);

-- ============================================================
-- 7. 清理辅助存储过程
-- ============================================================

drop procedure if exists parking_assign_menu;
