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
