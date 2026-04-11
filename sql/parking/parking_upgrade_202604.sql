-- ============================================================
-- 升级脚本: parking_upgrade_202604.sql
-- 用途: 为已有生产/测试库补充会员字段（上一轮需求补全遗留）
-- 适用环境: 已运行 parking_bootstrap.sql 的库，不适合全新建库（全新建库用 bootstrap.sql 即可）
-- 执行方式: mysql -u<user> -p<pass> <db> < parking_upgrade_202604.sql
-- 幂等性: 可重复执行，检查 information_schema 后再 ALTER，不会重复添加列
-- ============================================================

-- 为 parking_customer 幂等添加三个会员字段
-- is_member / member_type / member_expire_time
drop procedure if exists parking_upgrade_add_customer_membership;

delimiter $$
create procedure parking_upgrade_add_customer_membership()
begin
  -- is_member: 会员标识 (0 否, 1 是)
  if not exists (
    select 1 from information_schema.columns
    where table_schema = database()
      and table_name   = 'parking_customer'
      and column_name  = 'is_member'
  ) then
    alter table parking_customer
      add column is_member char(1) default '0'
        comment '会员标识 (0 否, 1 是)'
        after mobile;
  end if;

  -- member_type: 会员等级 (1 银, 2 金, 3 铂金)
  if not exists (
    select 1 from information_schema.columns
    where table_schema = database()
      and table_name   = 'parking_customer'
      and column_name  = 'member_type'
  ) then
    alter table parking_customer
      add column member_type char(1) default null
        comment '会员等级 (1 银, 2 金, 3 铂金)'
        after is_member;
  end if;

  -- member_expire_time: 会员到期时间
  if not exists (
    select 1 from information_schema.columns
    where table_schema = database()
      and table_name   = 'parking_customer'
      and column_name  = 'member_expire_time'
  ) then
    alter table parking_customer
      add column member_expire_time datetime default null
        comment '会员到期时间'
        after member_type;
  end if;
end$$
delimiter ;

call parking_upgrade_add_customer_membership();
drop procedure if exists parking_upgrade_add_customer_membership;

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
