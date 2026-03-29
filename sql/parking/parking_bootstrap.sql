-- ----------------------------
-- Parking business bootstrap schema (without menu SQL)
-- ----------------------------

drop table if exists parking_payment_record;
drop table if exists parking_temp_order;
drop table if exists parking_monthly_order;
drop table if exists parking_membership_order;
drop table if exists parking_user_vehicle;
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
-- 4. parking_user_vehicle
-- ----------------------------
create table parking_user_vehicle (
  vehicle_id            bigint(20)      not null auto_increment comment 'Vehicle ID',
  user_id               bigint(20)      not null                comment 'sys_user.user_id',
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
  key idx_parking_vehicle_user_id (user_id)
) engine=innodb auto_increment=100 comment='User vehicle';

-- ----------------------------
-- 5. parking_membership_order
-- ----------------------------
create table parking_membership_order (
  membership_order_id   bigint(20)      not null auto_increment comment 'Membership order ID',
  order_no              varchar(32)     not null                comment 'Order number',
  user_id               bigint(20)      not null                comment 'sys_user.user_id',
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
  key idx_parking_membership_user_id (user_id),
  key idx_parking_membership_lot_id (lot_id)
) engine=innodb auto_increment=100 comment='Parking membership order';

-- ----------------------------
-- 6. parking_monthly_order
-- ----------------------------
create table parking_monthly_order (
  monthly_order_id      bigint(20)      not null auto_increment comment 'Monthly order ID',
  order_no              varchar(32)     not null                comment 'Order number',
  user_id               bigint(20)      not null                comment 'sys_user.user_id',
  vehicle_id            bigint(20)      default null            comment 'Vehicle ID',
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
  key idx_parking_monthly_user_id (user_id),
  key idx_parking_monthly_lot_id (lot_id)
) engine=innodb auto_increment=100 comment='Parking monthly order';

-- ----------------------------
-- 7. parking_temp_order
-- ----------------------------
create table parking_temp_order (
  temp_order_id         bigint(20)      not null auto_increment comment 'Temporary order ID',
  order_no              varchar(32)     not null                comment 'Order number',
  lot_id                bigint(20)      not null                comment 'Parking lot ID',
  space_id              bigint(20)      default null            comment 'Parking space ID',
  user_id               bigint(20)      default null            comment 'sys_user.user_id',
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
  key idx_parking_temp_user_id (user_id),
  key idx_parking_temp_plate_no (vehicle_plate_no)
) engine=innodb auto_increment=100 comment='Parking temporary order';

-- ----------------------------
-- 8. parking_payment_record
-- ----------------------------
create table parking_payment_record (
  payment_id            bigint(20)      not null auto_increment comment 'Payment record ID',
  biz_order_no          varchar(32)     not null                comment 'Business order number',
  biz_order_type        char(1)         not null                comment 'Business order type (1 membership, 2 monthly, 3 temporary)',
  user_id               bigint(20)      default null            comment 'sys_user.user_id',
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
  key idx_parking_payment_user_id (user_id)
) engine=innodb auto_increment=100 comment='Parking payment record';

-- ----------------------------
-- Demo seed data
-- ----------------------------
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

insert into parking_lot_admin (
  lot_admin_id, lot_id, user_id, status,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 1, 1, '0',
  'admin', sysdate(), 'admin', sysdate(), 'Bind lot admin to sys_user.user_id=1'
);

insert into parking_user_vehicle (
  vehicle_id, user_id, plate_no, vehicle_type, brand_name, vehicle_color, is_default, status,
  bind_time, del_flag, create_by, create_time, update_by, update_time, remark
) values (
  1, 2, 'SZ-A12345', '3', 'BYD', 'Blue', '1', '0',
  sysdate(), '0', 'ry', sysdate(), 'ry', sysdate(), 'Bind demo vehicle to sys_user.user_id=2'
);

insert into parking_membership_order (
  membership_order_id, order_no, user_id, vehicle_id, lot_id, membership_type,
  valid_start_time, valid_end_time, original_amount, discount_amount, pay_amount,
  pay_status, biz_status, pay_time, cancel_time, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'PO202603290001', 2, 1, 1, '2',
  sysdate(), date_add(sysdate(), interval 365 day), 1200.00, 200.00, 1000.00,
  '1', '1', sysdate(), null, '0',
  'ry', sysdate(), 'ry', sysdate(), 'Demo paid membership order'
);

insert into parking_monthly_order (
  monthly_order_id, order_no, user_id, vehicle_id, lot_id, month_count,
  start_time, end_time, original_amount, discount_amount, pay_amount,
  pay_status, biz_status, pay_time, cancel_time, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'MO202603290001', 2, 1, 1, 1,
  sysdate(), date_add(sysdate(), interval 30 day), 380.00, 30.00, 350.00,
  '1', '1', sysdate(), null, '0',
  'ry', sysdate(), 'ry', sysdate(), 'Demo monthly order'
);

insert into parking_temp_order (
  temp_order_id, order_no, lot_id, space_id, user_id, vehicle_plate_no,
  in_time, out_time, parking_duration_min, fee_amount, discount_amount, pay_amount,
  pay_status, biz_status, pay_time, close_time, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'TO202603290001', 1, 3, 2, 'SZ-A12345',
  date_sub(sysdate(), interval 25 minute), null, 0, 0.00, 0.00, 0.00,
  '0', '0', null, null, '0',
  'ry', sysdate(), 'ry', sysdate(), 'Demo active temporary order occupying C2-018'
);

 insert into parking_payment_record (
   payment_id, biz_order_no, biz_order_type, user_id, lot_id, pay_channel, pay_amount, pay_status,
   trade_no, pay_time, refund_time, create_by, create_time, update_by, update_time, remark
 ) values
   (1, 'MO202603290001', '2', 2, 1, '1', 350.00, '1', 'WX-20260329-0001', sysdate(), null, 'ry', sysdate(), 'ry', sysdate(), 'Monthly order payment'),
   (2, 'PO202603290001', '1', 2, 1, '1', 1000.00, '1', 'WX-20260329-0002', sysdate(), null, 'ry', sysdate(), 'ry', sysdate(), 'Membership order payment');
