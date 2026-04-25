# SQL Template

本文件提供 Schema DDL 和菜单 SQL 的填空式模板。占位符参考 `backend-template.md` 的命名速查表。

**关键原则**:
- **追加,不要另建文件**。Schema、种子、菜单统一追加到 `sql/parking/parking_init.sql`。
- **幂等**。菜单 SQL 必须能重复执行不出错,永远用 `insert ... select ... where not exists` + `@variable_id`。

---

## Schema 模板

追加到 `sql/parking/parking_init.sql`。两个地方要改:

### Step 1: 顶部 drop table 区加一行

**先 Read** `parking_init.sql` 顶部,理解当前 drop 顺序。当前的逆依赖顺序大致是(从上到下):

```
parking_user_config           ← 最上,被其他表引用最少,先 drop
parking_payment_record
parking_temp_order
parking_monthly_order
parking_membership_order
parking_user_vehicle
parking_customer
parking_lot_admin
parking_space
parking_lot                   ← 最下,被几乎所有表引用,最后 drop
```

把你的新表 drop 行加到这个区里,位置由下面规则决定:

- 你的表**不被任何现有表引用**(纯新加,大多数情况) → 放在**最顶部**(`parking_user_config` 前面或紧随其后)
- 你的表**被某个现有表引用** → 放在那个引用方下面
- 你的表**引用了某些现有表** → 放在被引用方上面(比如你 `lot_id` → `parking_lot`,所以你必须在 `parking_lot` 之上)

```sql
drop table if exists {{table}};
```

### Step 2: 其他业务表之后加 create table

```sql
-- ----------------------------
-- {{N}}. {{table}}
-- ----------------------------
create table {{table}} (
  {{pk_col}}          bigint(20)      not null auto_increment                    comment '主键',
  bill_no             varchar(32)     not null default ''                        comment '单据编号',
  lot_id              bigint(20)      default null                               comment '所属停车场ID',
  -- BIZ: 业务字段,按 Phase 0 字段清单填
  -- customer_id      bigint(20)      default null                               comment '客户ID',
  -- vehicle_id       bigint(20)      default null                               comment '车辆ID',
  -- equipment_code   varchar(64)     default ''                                 comment '设备编码',
  -- estimated_amount decimal(10,2)   default 0.00                               comment '预估金额',
  -- schedule_time    datetime                                                    comment '计划时间',
  bill_status         char(2)         default '00'                               comment '业务状态(00草稿 10待审核 20审核通过 30审核拒绝 90已作废)',
  submit_time         datetime                                                    comment '提交时间',
  audit_user          varchar(64)     default null                               comment '审核人',
  audit_time          datetime                                                    comment '审核时间',
  audit_remark        varchar(500)    default null                               comment '审核意见',
  void_time           datetime                                                    comment '作废时间',
  del_flag            char(1)         default '0'                                comment '逻辑删除(0存在 2已删)',
  create_by           varchar(64)     default ''                                 comment '创建人',
  create_time         datetime                                                    comment '创建时间',
  update_by           varchar(64)     default ''                                 comment '更新人',
  update_time         datetime                                                    comment '更新时间',
  remark              varchar(500)    default null                               comment '备注',
  primary key ({{pk_col}}),
  unique key uk_parking_{{resource}}_no (bill_no),
  key idx_parking_{{resource}}_lot_id (lot_id),
  key idx_parking_{{resource}}_status (bill_status)
  -- BIZ: 其他索引,如 customer_id / vehicle_id
) engine=innodb auto_increment=100 comment='{{模块中文}}';
```

**字段规范再强调**:
- 字符串字段用 `varchar(N) default ''` 或 `default null`,不要 `not null` 除非真的不允许空
- `char(1)` 用于 `del_flag` / 单字符状态;`char(2)` 用于带前导 0 的状态码如 `'00'`
- `datetime` 不写 `default`,由 MyBatis 的 `sysdate()` 填
- 索引命名:`uk_parking_<resource>_<col>` / `idx_parking_<resource>_<col>`
- 表注释 `comment='<中文模块名>'`

### Step 3: 文件底部 demo 数据区加示例

```sql
insert into {{table}} (
  {{pk_col}}, bill_no, lot_id,
  -- BIZ: 业务字段,
  bill_status, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, '{{BillPrefix}}20260101120000001', 1,
  -- BIZ: 业务字段示例值,
  '00', '0',
  'admin', sysdate(), 'admin', sysdate(), 'Bootstrap demo {{resource}} bill'
);
```

1-2 条就够了,其中至少一条 `bill_status = '00'`(草稿),可选一条 `'20'`(已通过) 便于演示"作废"按钮。

### Step 4: 同步 ParkingSchemaScriptTest

打开 `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingSchemaScriptTest.java`。这个测试用一个 `REQUIRED_TABLE_COLUMNS` 的 `Map<String, List<String>>`(由 `Map.ofEntries(Map.entry(...), ...)` 构造),**只有这一个常量**,没有单独的 `REQUIRED_TABLES`。每个 entry 表达"这个表必须有这几列"。

在 Map 末尾(最后一个 `Map.entry(...)` 后面)追加一行:

```java
Map.entry("{{table}}", List.of("{{pk_col}}", "bill_no", "lot_id", "bill_status", "del_flag"))
```

注意:Map 末尾不要留逗号。如果原本最后一行有逗号,现在最后一行(你新增的)就没逗号;如果原本最后一行没逗号,你需要先把它末尾加个逗号,再加你的新行。

**先 Read 一次再改**,因为这个测试 N 个月后字段断言列表可能会增加。

---

## 菜单 SQL 模板

追加到 `sql/parking/parking_init.sql` 的菜单区末尾(或者 `-- Module menu blocks` 区段内)。

### 结构说明

一个单据模块需要插入 **约 11 个菜单行**,分三层:

```
{{父菜单根}}                          ← 已经存在的根(如 @parking_operations_root_id)
├── {{模块中文}} (C, visible='0')     ← 列表页
│   ├── {{模块中文}}表单 (C, visible='1', 隐藏的子页)    ← form.vue 路由注册
│   ├── 查询 (F, parking:{{resource}}:query)
│   ├── 新增 (F, parking:{{resource}}:add)
│   ├── 修改 (F, parking:{{resource}}:edit)
│   ├── 删除 (F, parking:{{resource}}:remove)
│   ├── 提交审核 (F, parking:{{resource}}:submit)
│   ├── 审核通过 (F, parking:{{resource}}:approve)
│   ├── 审核拒绝 (F, parking:{{resource}}:reject)
│   └── 作废 (F, parking:{{resource}}:void)
```

### 模板代码

```sql
-- ========================================
-- {{模块中文}} Module
-- ========================================

-- 1. 列表页(C, 可见)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}', {{parent_id_var}}, {{order_num}}, '{{resource}}', 'parking/{{resource}}/index', '', '',
       1, 0, 'C', '0', '0', 'parking:{{resource}}:list', 'list',
       'admin', sysdate(), '', null, '{{模块中文}} list page'
from dual
where not exists (
  select 1 from sys_menu
  where component = 'parking/{{resource}}/index' and menu_type = 'C'
);

set @parking_{{resource}}_id := (
  select menu_id from sys_menu
  where component = 'parking/{{resource}}/index' and menu_type = 'C'
  order by menu_id limit 1
);

update sys_menu
set menu_name = '{{模块中文}}',
    parent_id = {{parent_id_var}},
    order_num = {{order_num}},
    path = '{{resource}}',
    component = 'parking/{{resource}}/index',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'parking:{{resource}}:list',
    icon = 'list',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '{{模块中文}} list page'
where menu_id = @parking_{{resource}}_id;

-- 2. 表单页(C, 隐藏子菜单,用于路由注册)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}表单', @parking_{{resource}}_id, 1, 'form', 'parking/{{resource}}/form', '', '',
       1, 0, 'C', '1', '0', '', '#',
       'admin', sysdate(), '', null, '{{模块中文}} form page (hidden)'
from dual
where not exists (
  select 1 from sys_menu
  where component = 'parking/{{resource}}/form' and menu_type = 'C'
);

-- 3. 查询权限(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}查询', @parking_{{resource}}_id, 2, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:query', '#',
       'admin', sysdate(), '', null, '{{模块中文}} query permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:query' and menu_type = 'F'
);

-- 4. 新增权限(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}新增', @parking_{{resource}}_id, 3, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:add', '#',
       'admin', sysdate(), '', null, '{{模块中文}} add permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:add' and menu_type = 'F'
);

-- 5. 修改权限(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}修改', @parking_{{resource}}_id, 4, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:edit', '#',
       'admin', sysdate(), '', null, '{{模块中文}} edit permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:edit' and menu_type = 'F'
);

-- 6. 删除权限(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}删除', @parking_{{resource}}_id, 5, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:remove', '#',
       'admin', sysdate(), '', null, '{{模块中文}} remove permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:remove' and menu_type = 'F'
);

-- 7. 提交审核(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}提交', @parking_{{resource}}_id, 6, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:submit', '#',
       'admin', sysdate(), '', null, '{{模块中文}} submit permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:submit' and menu_type = 'F'
);

-- 8. 审核通过(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}通过', @parking_{{resource}}_id, 7, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:approve', '#',
       'admin', sysdate(), '', null, '{{模块中文}} approve permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:approve' and menu_type = 'F'
);

-- 9. 审核拒绝(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}拒绝', @parking_{{resource}}_id, 8, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:reject', '#',
       'admin', sysdate(), '', null, '{{模块中文}} reject permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:reject' and menu_type = 'F'
);

-- 10. 作废(F)
insert into sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select '{{模块中文}}作废', @parking_{{resource}}_id, 9, '', '', '', '',
       1, 0, 'F', '0', '0', 'parking:{{resource}}:void', '#',
       'admin', sysdate(), '', null, '{{模块中文}} void permission'
from dual
where not exists (
  select 1 from sys_menu
  where perms = 'parking:{{resource}}:void' and menu_type = 'F'
);
```

### 参数说明

- `{{parent_id_var}}`: 父菜单的 SQL 变量名,根据 `parking-developer.md` 的 IA 树决定:
  - `@parking_archive_root_id` — 基础档案
  - `@parking_operations_root_id` — 运营中心
  - `@parking_customers_root_id` — 客户中心
  - 新模块不能自己造新根,只能挂到这三个根下
- `{{order_num}}`: 在父菜单下的排序,参考现有同根兄弟的 `order_num` 依次递增

### Step 5: 同步 ParkingMenuScriptTest

打开 `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java`。这个测试**没有用常量**,所有断言都是 inline 写死的。要改三个地方:

1. **block count**: 找到 `assertEquals(N, inserts.size(), "Expected N insert-if-not-exists blocks");`,把 `N` 加上你新增的块数。**先 Read 一次 N 的当前值** — 不要凭记忆,因为别的模块可能刚刚也加过。一个标准单据加 10。
2. **变量名断言**: 找到一片 `assertTrue(normalizedSql.contains("@parking_xxx_id"), ...)` 的断言,在末尾追加一行:
   ```java
   assertTrue(normalizedSql.contains("@parking_{{resource}}_id"), "Expected SQL variable @parking_{{resource}}_id");
   ```
3. **页面 + 按钮断言**(可选但推荐): 仿照测试里 membership / monthly 模块的断言段,在末尾追加你模块的断言块,使用 `findInsertByComponent` / `assertFunctionButton` 这两个 helper:
   ```java
   MenuInsertSpec page = findInsertByComponent(inserts, "'parking/{{resource}}/index'");
   assertNotNull(page, "Expected parking {{resource}} page row");
   assertEquals("'parking:{{resource}}:list'", page.valuesByColumn().get("perms"));
   assertFunctionButton(inserts, "'parking:{{resource}}:query'",   "@parking_{{resource}}_id");
   assertFunctionButton(inserts, "'parking:{{resource}}:add'",     "@parking_{{resource}}_id");
   assertFunctionButton(inserts, "'parking:{{resource}}:edit'",    "@parking_{{resource}}_id");
   assertFunctionButton(inserts, "'parking:{{resource}}:remove'",  "@parking_{{resource}}_id");
   assertFunctionButton(inserts, "'parking:{{resource}}:submit'",  "@parking_{{resource}}_id");
   assertFunctionButton(inserts, "'parking:{{resource}}:approve'", "@parking_{{resource}}_id");
   assertFunctionButton(inserts, "'parking:{{resource}}:reject'",  "@parking_{{resource}}_id");
   assertFunctionButton(inserts, "'parking:{{resource}}:void'",    "@parking_{{resource}}_id");
   ```

**先 Read 当前 ParkingMenuScriptTest.java**,不要凭记忆改 N 或断言风格。

---

## 验证

改完 SQL 后必须跑测试确认:

```bash
cd D:/code/parking-management-system
mvn -pl ruoyi-admin -am "-Dtest=ParkingSchemaScriptTest,ParkingMenuScriptTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

两个都得绿。如果红,通常是:
- `ParkingSchemaScriptTest` 红:忘了在 `REQUIRED_TABLE_COLUMNS` Map 末尾加你的 entry,或者 create table 里漏了必需字段
- `ParkingMenuScriptTest` 红:block count 没更新,或变量名拼错,或新 block 里出现了 `delete from sys_menu` / hardcoded `menu_id`
