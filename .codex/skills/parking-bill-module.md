# Parking Bill Module — Codex 单据类业务模块配方

> **来源**：本文件镜像自 `.claude/skills/parking-bill-module/SKILL.md`（Claude 专用 skill）。引用的深度模板（backend-template / frontend-template / sql-template / test-template / state-machine-guide / cascade-guide）继续指向 `.claude/skills/parking-bill-module/references/*.md`，单一源地不重复。
> **更新策略**：本文件主体内容更新时，同步改 `.claude/skills/parking-bill-module/SKILL.md`，反之亦然。`references/*.md` 不需要镜像。

这份配方是停车管理系统里"单据类业务模块"的唯一标准。Codex 接到"我要做一个新模块/订单/工单"的需求时，按这份配方一次性产出与现有代码一致的全栈实现，包括业务流转、独立编辑页、菜单 SQL、测试，而不是凭印象拼。

## 为什么要有这份配方

项目里 `ParkingMembershipOrder`、`ParkingMonthlyOrder`、`ParkingTempOrder` 已经沉淀出一套稳定的"单据模块"写法。新模块如果不严格按这套来，会在测试、菜单脚本幂等性、权限命名上漏洞百出。凡是用户说"新建一个 XX 单/XX 模块/XX 订单"，先打开本文件再动手。

## 什么算"单据类业务模块"

满足任意一条即按本配方走：

- 实体是一张业务表（有业务编号、归属停车场、创建人，字段 ≥ 6 个）
- 存在生命周期：新建、提交、审核、完成、作废/取消
- 需要独立列表页 + 独立编辑页（字段太多，el-dialog 装不下）
- 需要按钮级权限 + 业务操作端点（不只是 CRUD，还有 approve/void/settle 之类）

如果用户只要一个查询面板、一个统计图、一个配置项，**不是**单据，按普通 CRUD 写，不要套这个配方。

## 核心硬性约束（不可绕过）

这些是项目全局公约，违反任何一条都会被现有测试或 code review 打回：

| # | 约束 | 原因 |
|---|---|---|
| 1 | 客户关联字段用 `customer_id`（FK → `parking_customer`），不用 `user_id` | dual-account 边界，`sys_user` 只给平台员工 |
| 2 | 权限码格式 `parking:<resource>:<action>`，`<action>` 限于 `list / query / add / edit / remove / export / <business_action>` | `ParkingMenuScriptTest` 校验 |
| 3 | URL 前缀 `/parking/<resource>`，业务操作挂 `PUT /parking/<resource>/<action>` | Controller 集成测试校验 |
| 4 | 逻辑删除：`del_flag char(1) default '0'`，删除设 `'2'`，查询永远 `and del_flag = '0'` | 业务回溯 |
| 5 | 状态字段用 `String`（char(1) 或 char(2)），**不要 enum**，注释写明每个值含义 | 兼容 MySQL CHAR + 前端 options.js 字典 |
| 6 | 金额用 `BigDecimal`，DB 用 `decimal(10,2) default 0.00` | double 精度坑 |
| 7 | Domain 继承 `BaseEntity`，普通 getter/setter，**不要 Lombok** | 仓库未引入 Lombok |
| 8 | Service 构造注入 + `final` 字段 | Spring 推荐 |
| 9 | `@Transactional(rollbackFor = Exception.class)` 只加在多步写入的业务方法上，CRUD 不加 | 缩小锁范围 |
| 10 | 新增/编辑是**独立路由页面** `/parking/<resource>/form`，**不要 el-dialog** | 字段多，弹窗体验差 |
| 11 | 菜单 SQL 用 `@variable_id` 变量 + `insert ... where not exists` 幂等插入；**严禁** hardcode `menu_id`；**严禁** `delete from sys_menu` | `ParkingMenuScriptTest` 校验 |
| 12 | 建表、种子、菜单、权限 SQL **统一追加**到 `sql/parking/parking_init.sql`，不要新建 `<module>.sql`，不要恢复旧的拆分脚本 | `ParkingSchemaScriptTest` / `ParkingMenuScriptTest` 只扫这一个入口 |
| 13 | 不加数据库 FK 约束，外键存在性校验放 Service 层 | 性能 + 配合逻辑删除 |
| 14 | 中文文案直接硬编码，没有 i18n | 项目没装 vue-i18n |

## 工作流：14 个 phase（Phase 0 — Phase 13）

严格按顺序做。每个 phase 做完前不要开下一个。如果缺信息，回到 Phase 0 让用户补。

### Phase 0 — 澄清需求（唯一允许追问用户的阶段）

动任何代码前，必须和用户对齐以下 6 项。一次性把 6 个问题问完，不要挤牙膏：

1. **模块名**：中文菜单标题（如"投诉处理单"）+ 英文 resource 代号（小写连字符，如 `complaint`）。表名形如 `parking_complaint_order`，Java 类名 `ParkingComplaintOrder`，路径 `/parking/complaint`，权限前缀 `parking:complaint:*`
2. **挂载位置**：菜单放在哪个根目录下？（`基础档案` / `运营中心` / `客户中心`）
3. **字段清单**：给出字段表（中文名 / 英文字段名 / 类型 / 是否必填 / 是否唯一）。"单据骨架字段"列在下面，用户只补业务字段
4. **关联**：要不要 `lot_id`（停车场）？`customer_id`（客户）？`vehicle_id`（车辆）？其他外键？
5. **业务流转**：用默认 `草稿 → 待审核 → 审核通过/拒绝 → 作废` 状态机？还是自定义？自定义时画清楚状态图
6. **业务动作**：除了 CRUD 还有哪些按钮？例如"提交审核"、"审核通过"、"审核拒绝"、"作废"、"导出"、"结算"。每一个对应一个 Service 方法 + Controller 端点 + 权限码 + 列表页按钮

#### 单据骨架字段（所有单据都必有）

```
<entity>_id       bigint        PK auto_increment      主键
bill_no           varchar(32)   unique                 单据编号（规则 "<前缀><yyyyMMddHHmmss><4 位随机>"）
lot_id            bigint        nullable or not null   所属停车场（若涉及场地）
bill_status       char(2)       default '00'           业务状态（默认状态机的 key）
submit_time       datetime      nullable               提交时间
audit_user        varchar(64)   nullable               审核人
audit_time        datetime      nullable               审核时间
audit_remark      varchar(500)  nullable               审核意见
void_time         datetime      nullable               作废时间
del_flag          char(1)       default '0'            逻辑删除
create_by         varchar(64)                          创建人 (BaseEntity)
create_time       datetime                             创建时间 (BaseEntity)
update_by         varchar(64)                          更新人 (BaseEntity)
update_time       datetime                             更新时间 (BaseEntity)
remark            varchar(500)                         备注 (BaseEntity)
```

BaseEntity 五个字段由继承得到，**不要在 Domain 类里重复声明，不要在建表 SQL 里漏写**。

### Phase 1 — 确定状态机

默认 5 态流转，除非用户明确要改：

| 值 | 中文 | 可编辑？ | 可硬删？ | 下一态（什么动作触发） |
|---|---|---|---|---|
| `00` | 草稿 | ✅ | ✅ | `10`（提交审核） |
| `10` | 待审核 | ❌ | ❌ | `20`（通过） / `30`（拒绝） |
| `20` | 审核通过 | ❌ | ❌ | `90`（作废） |
| `30` | 审核拒绝 | ✅（改完回到 `00`） | ✅ | `00`（重新编辑） / `10`（再次提交） |
| `90` | 已作废 | ❌ | ❌ | 终态 |

**状态推进的防护**（Service 层必须检查）：

- 编辑（update）：仅允许 `bill_status in ('00','30')`，否则 throw ServiceException
- 提交（submit）：仅允许 `'00' / '30'` → `'10'`
- 审核通过（approve）：仅允许 `'10'` → `'20'`
- 审核拒绝（reject）：仅允许 `'10'` → `'30'`，`audit_remark` 必填
- 作废（void）：仅允许 `'20'` → `'90'`
- 删除（remove）：仅允许 `bill_status in ('00','30')`

自定义状态机时，依然保留 `bill_status` 字段名，只改值空间和防护规则，不要改字段名。这样前端 options.js 和测试模板可以直接复用。

详细说明见 `.claude/skills/parking-bill-module/references/state-machine-guide.md`。

### Phase 2 — 写 Schema SQL

追加到 `sql/parking/parking_init.sql`。create table 骨架见 `.claude/skills/parking-bill-module/references/sql-template.md` 的 "Schema 模板" 一节。注意：

- 先 **Read** `parking_init.sql` 顶部 `drop table if exists` 区，看现有表的逆依赖顺序（被引用放下面、引用别人放上面），把新表 drop 行插在正确位置 — 没有下游引用就放在最顶部（紧接 `parking_user_config`）
- create table 写在其他业务表之后
- `engine=innodb auto_increment=100`
- 每一列必须带 `comment '<中文说明>'`
- 必要索引：主键、`unique key uk_parking_<resource>_no (bill_no)`、`key idx_parking_<resource>_lot_id (lot_id)`（若有 lot）、`key idx_parking_<resource>_status (bill_status)`
- 文件底部 demo 数据区加 1-2 条示例数据，`bill_status = '00'`

**别忘了同步 `ParkingSchemaScriptTest`** 里的表名清单和必需字段清单，否则测试红。

### Phase 3 — 写 Menu SQL

追加到 `sql/parking/parking_init.sql` 的菜单区。一个标准单据模块插入 **10 个 insert 块** = 列表页 + 隐藏表单页 + 8 个权限按钮：

1. 列表页（`menu_type='C'`, `component='parking/<resource>/index'`, `perms='parking:<resource>:list'`, `visible='0'`）
2. 隐藏表单页（`menu_type='C'`, `component='parking/<resource>/form'`, `visible='1'` 隐藏, `perms=''`）
3. `parking:<resource>:query` (F)
4. `parking:<resource>:add` (F)
5. `parking:<resource>:edit` (F)
6. `parking:<resource>:remove` (F)
7. `parking:<resource>:submit` (F)
8. `parking:<resource>:approve` (F)
9. `parking:<resource>:reject` (F)
10. `parking:<resource>:void` (F)

需要导出再加 `parking:<resource>:export`（变 11 块）；自定义状态机删了某动作就跟着删对应 F 块。

每行用 `insert ... select ... from dual where not exists (...)` + `set @parking_<resource>_id := (...)` 拿 id。**严禁 hardcode menu_id。严禁 delete from sys_menu。**

骨架代码见 `.claude/skills/parking-bill-module/references/sql-template.md` 的 "Menu 模板" 一节。

**同步更新 `ParkingMenuScriptTest`**：用 inline `assertEquals(N, inserts.size(), ...)`（**不是常量**），N 当前值需先 Read 测试文件确认，再加上新增 block 数。同时该测试断言一组 `@parking_<xxx>_id` 变量名，要补一行 `assertTrue(normalizedSql.contains("@parking_<resource>_id"), ...)`。**先 Read 再改，不要凭记忆改 N。**

### Phase 4 — 后端 Domain

文件：`ruoyi-parking/src/main/java/com/ruoyi/parking/domain/Parking<Entity>.java`

要点：
- `extends BaseEntity`
- `private static final long serialVersionUID = 1L;`
- 主键 `Long <entity>Id`，关联键 `Long lotId` / `Long customerId` / `Long vehicleId`
- 状态字段 `String billStatus`，注释清楚 `00草稿 10待审核 20审核通过 30审核拒绝 90已作废`
- 金额 `BigDecimal`
- 时间 `java.util.Date`
- 关联查询展示字段（如 `lotName`）用 `private transient String lotName;`（Mapper XML 用 `left join` 填，不参与序列化持久化）
- 必填字段加 `@NotBlank(message = "xxx 不能为空")` / `@NotNull(message = "xxx 不能为空")`，导入 `jakarta.validation.constraints.*`
- 全字段 getter/setter，**不要加 Lombok**

模板：`.claude/skills/parking-bill-module/references/backend-template.md` 的 "Domain 模板"。

### Phase 5 — 后端 Mapper + XML

文件：
- `ruoyi-parking/src/main/java/com/ruoyi/parking/mapper/Parking<Entity>Mapper.java`
- `ruoyi-parking/src/main/resources/mapper/parking/Parking<Entity>Mapper.xml`

Mapper 接口必有方法：
```java
Parking<Entity> selectParking<Entity>ById(Long <entity>Id);
List<Parking<Entity>> selectParking<Entity>List(Parking<Entity> query);
int insertParking<Entity>(Parking<Entity> entity);
int updateParking<Entity>(Parking<Entity> entity);
int deleteParking<Entity>ByIds(@Param("<entity>Ids") Long[] ids);
```

XML 要点：
- `resultMap` 列齐所有列（包括 BaseEntity 字段和 transient join 字段）
- `<sql id="selectParking<Entity>Columns">` 片段用主表别名 `o`，所有 `left join` 加 `and x.del_flag = '0'`
- `selectParking<Entity>List` 用 `<where>` + `<if>`，字符串检查 `!= null and != ''`，数字检查 `!= null`
- 排序 `order by o.<pk> desc`
- `insertParking<Entity>` 用 `useGeneratedKeys="true" keyProperty="<entity>Id"`，初始 `del_flag = '0'`，时间用 `sysdate()`
- `updateParking<Entity>` 用 `<set>` + `<if>`，无条件 `update_time = sysdate()`
- `deleteParking<Entity>ByIds` 是 **update 语句**（不是 delete），`set del_flag = '2'`，`where del_flag = '0' and <pk> in <foreach>`

模板：`.claude/skills/parking-bill-module/references/backend-template.md` 的 "Mapper / XML 模板"。

### Phase 6 — 后端 Service 接口 + 实现

文件：
- `ruoyi-parking/src/main/java/com/ruoyi/parking/service/IParking<Entity>Service.java`
- `ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/Parking<Entity>ServiceImpl.java`

接口必有方法：

```java
// 基础 CRUD
Parking<Entity> selectParking<Entity>ById(Long id);
List<Parking<Entity>> selectParking<Entity>List(Parking<Entity> query);
int insertParking<Entity>(Parking<Entity> entity);
int updateParking<Entity>(Parking<Entity> entity);
int deleteParking<Entity>ByIds(Long[] ids);

// 业务流转（默认状态机）— 全部用 Domain 对象作参数，
// 与 ParkingMembershipOrderServiceImpl.payMembershipOrder(ParkingMembershipOrder form) 风格一致；
// auditRemark / updateBy / 业务动作专属字段都从 form 里取
int submitParking<Entity>(Parking<Entity> form);
int approveParking<Entity>(Parking<Entity> form);
int rejectParking<Entity>(Parking<Entity> form);
int voidParking<Entity>(Parking<Entity> form);
```

ServiceImpl 要点：
- `@Service`，字段 `final`，构造注入
- **insert 逻辑**：生成 `bill_no`（格式 `<前缀><yyyyMMddHHmmss><4 位随机>`），默认 `bill_status = '00'`，做所有外键存在性校验（lot/customer/vehicle），计算衍生字段
- **update 逻辑**：先查当前记录做存在性 + 状态防护（仅允许 `'00'/'30'`），外键变更时校验新外键，**把状态字段设为 null** 让 XML `<if>` 跳过（避免普通 update 误改状态）
- **删除逻辑**：查 `bill_status`，非 `'00'/'30'` 拒绝
- **业务方法**：加 `@Transactional(rollbackFor = Exception.class)`，按"取当前 → 状态防护 → 改状态 + 时间戳 + 审核信息 → 同步关联数据（私有 helper）→ 创建下游记录（如 payment）"顺序写
- 抛错用 `throw new ServiceException("xxx")`
- 用 `StringUtils.isEmpty(...)` 检查空串（来自 `com.ruoyi.common.utils.StringUtils`）

**级联硬约束**：业务方法（特别是 `approveXxx`）需要写其他业务表（创建下游订单、写支付记录、推送状态等）时，**必须先 Read 下游目标的 Domain 类 + Mapper.xml**，把字段映射列清楚，再开写。详细范式 + 飞行前清单见 `.claude/skills/parking-bill-module/references/cascade-guide.md`。漏掉这一步是 bug 最多的地方。

模板：`.claude/skills/parking-bill-module/references/backend-template.md` 的 "Service 模板"。

### Phase 7 — 后端 Controller

文件：`ruoyi-parking/src/main/java/com/ruoyi/parking/controller/Parking<Entity>Controller.java`

端点清单：

| 方法 | URL | 权限 | 日志 | 说明 |
|---|---|---|---|---|
| GET | `/parking/<resource>/list` | `parking:<resource>:list` | - | 分页列表 |
| GET | `/parking/<resource>/{id}` | `parking:<resource>:query` | - | 详情 |
| POST | `/parking/<resource>` | `parking:<resource>:add` | INSERT | 新增 |
| PUT | `/parking/<resource>` | `parking:<resource>:edit` | UPDATE | 修改 |
| DELETE | `/parking/<resource>/{ids}` | `parking:<resource>:remove` | DELETE | 批量逻辑删除 |
| PUT | `/parking/<resource>/submit` | `parking:<resource>:submit` | UPDATE | 提交审核 |
| PUT | `/parking/<resource>/approve` | `parking:<resource>:approve` | UPDATE | 审核通过 |
| PUT | `/parking/<resource>/reject` | `parking:<resource>:reject` | UPDATE | 审核拒绝 |
| PUT | `/parking/<resource>/void` | `parking:<resource>:void` | UPDATE | 作废 |

要点：
- `extends BaseController`，构造注入
- 列表用 `startPage()` + `getDataTable(...)`，返回 `TableDataInfo`
- 新增/修改参数用 `@Validated @RequestBody`，set `createBy` / `updateBy` = `getUsername()`
- 业务方法返回 `AjaxResult`，用 `toAjax(int rows)` 包装
- 每个写端点都挂 `@PreAuthorize` + `@Log(title = "<中文模块名>", businessType = BusinessType.XXX)`

模板：`.claude/skills/parking-bill-module/references/backend-template.md` 的 "Controller 模板"。

### Phase 8 — 前端 API 层

文件：`ruoyi-ui/src/api/parking/<resource>.js`

导出函数：
```js
listParking<Entity>s(query)
getParking<Entity>(id)
addParking<Entity>(data)
updateParking<Entity>(data)
delParking<Entity>(ids)
submitParking<Entity>(data)
approveParking<Entity>(data)
rejectParking<Entity>(data)
voidParking<Entity>(data)
```

模板：`.claude/skills/parking-bill-module/references/frontend-template.md` 的 "API 模板"。

### Phase 9 — 前端字典 options.js

追加到 `ruoyi-ui/src/views/parking/options.js`：

```js
export const <RESOURCE>_BILL_STATUS_OPTIONS = [
  { label: '草稿',     value: '00', tagType: 'info'    },
  { label: '待审核',   value: '10', tagType: 'warning' },
  { label: '审核通过', value: '20', tagType: 'success' },
  { label: '审核拒绝', value: '30', tagType: 'danger'  },
  { label: '已作废',   value: '90', tagType: 'info'    }
]
```

**命名风格**：常量名 = `<MODULE>_BILL_STATUS_OPTIONS`，`<MODULE>` 是大写 resource 单段。例如 `COMPLAINT_BILL_STATUS_OPTIONS`、`INSPECTION_BILL_STATUS_OPTIONS`。现有项目里订单类用的 `MEMBERSHIP_BIZ_STATUS_OPTIONS` / `TEMP_ORDER_BIZ_STATUS_OPTIONS` 是订单的双轨状态（`payStatus + bizStatus`）产物；**新单据走单一 `bill_status`，统一用 `_BILL_STATUS_OPTIONS`**。

其他业务字典（类型、等级等）用同样的 `{ label, value, tagType }` 结构追加。不要新建文件。

### Phase 10 — 前端列表页 index.vue

文件：`ruoyi-ui/src/views/parking/<resource>/index.vue`

页面结构（从上到下）：
1. 查询表单 `<el-form :inline="true" v-show="showSearch">`：单据号 / 停车场 / 状态 / 创建时间区间
2. 操作按钮行 `<el-row class="mb8">`：新增 / 修改 / 删除 / 导出（带 `v-hasPermi`）
3. 数据表格 `<el-table>`：selection 列 + 单据号 + 关联名称 + 状态 tag + 创建时间 + 操作列
4. 操作列按钮（按状态条件显示）：
   - 状态 ∈ {草稿, 拒绝}：`修改` / `删除` / `提交审核`
   - 状态 = 待审核：`通过` / `拒绝（弹小框填意见）`
   - 状态 = 通过：`作废`
   - 所有状态：`详情`
5. 分页 `<pagination>`
6. 审核拒绝小弹框（只装一个意见输入框 + 确定/取消） — 这种"一两行输入"的动作可以用 el-dialog；**只有主新增/编辑必须独立路由**

`handleAdd()` → `this.$router.push('/parking/<resource>/form')`
`handleUpdate(row)` → `this.$router.push({ path: '/parking/<resource>/form', query: { id: row.<entity>Id } })`

模板：`.claude/skills/parking-bill-module/references/frontend-template.md` 的 "列表页模板"。

### Phase 11 — 前端编辑页 form.vue（独立路由）

文件：`ruoyi-ui/src/views/parking/<resource>/form.vue`

这是单据 skill 最重要的一块。字段多，必须独立页面。

页面结构：
1. `<el-page-header @back="goBack" :content="isEdit ? '修改<中文模块名>' : '新增<中文模块名>'" />`
2. `<el-form :model="form" :rules="rules" label-width="110px" v-loading="loading">` — 按"逻辑分组 + 分隔标题"组织字段，多时用 `<el-divider content-position="left">基本信息 / 业务信息 / 其他</el-divider>` 分组
3. 底部按钮：`确定` / `取消`（submit + `this.$router.go(-1)`）

脚本要点：
- `computed: { isEdit() { return !!this.$route.query.id } }`
- `created()` 里并发加载字典/下拉（lot/customer），如果 `isEdit` 再调 `loadData()`
- `submitForm()` 里根据 `isEdit` 分发到 `addXxx` / `updateXxx`，成功后 `$modal.msgSuccess` 再 `goBack()`
- **禁用状态**：`isEdit` 且 `form.billStatus` 不在 `{'00', '30'}` 时禁用所有输入 + 隐藏"确定"按钮，只显示"返回"

模板：`.claude/skills/parking-bill-module/references/frontend-template.md` 的 "编辑页模板"。

### Phase 12 — 测试

至少加两类测试，否则 CI 飘红；**有级联的模块再加一类（Service 级测试）**：

1. **Controller 单元测试**：`ruoyi-admin/src/test/java/com/ruoyi/parking/Parking<Entity>ControllerTest.java`
   - 用 `MockMvcBuilders.standaloneSetup(controller).build()`，不起 Spring 上下文
   - Mock `IParking<Entity>Service`，准备一条数据，覆盖 list / getInfo / add / edit / remove / submit / approve / reject / void 端点
   - 用 `ArgumentCaptor` 验证参数绑定

2. **Schema / Menu 脚本回归**：
   - `ParkingSchemaScriptTest`：新表加到 `REQUIRED_TABLE_COLUMNS` Map（具体改法见 references/test-template.md）
   - `ParkingMenuScriptTest`：改 inline `assertEquals(N, inserts.size(), ...)` 的 N（**先 Read 当前值**），新增 `@parking_<resource>_id` 变量断言 + 各按钮断言

3. **Service 级单元测试（级联模块必须）**：`ruoyi-admin/src/test/java/com/ruoyi/parking/Parking<Entity>ServiceImplTest.java`
   - 仅当本模块有级联（approve/void 写其他业务表）时才需要
   - Mock 上下游所有 mapper，用 ArgumentCaptor 验证下游收到的字段映射
   - 至少覆盖三条路径：快乐路径（下游被调用，字段对）、守卫路径（状态非法时下游 never 被调用）、回滚路径（下游抛异常时方法整体抛出）
   - 模板见 `.claude/skills/parking-bill-module/references/cascade-guide.md` 末尾

模板：`.claude/skills/parking-bill-module/references/test-template.md`（Controller + Schema/Menu）和 `.claude/skills/parking-bill-module/references/cascade-guide.md`（Service 级）。

### Phase 13 — 本地验证

交付前必须跑通：

```bash
# 后端测试
cd D:/code/parking-management-system
mvn -pl ruoyi-admin -am "-Dtest=ParkingSchemaScriptTest,ParkingMenuScriptTest,ParkingModuleSmokeTest,Parking<Entity>ControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test

# 前端构建
cd D:/code/parking-management-system/ruoyi-ui && npm run build:prod
```

任何红都要回去修，不要交付半成品。

## 输出格式（给用户的最终汇报）

做完后给用户一段简短汇报，不要重复代码内容：

```
已按规范交付 <模块中文名> 单据模块：
- 后端：<新增的 Java 文件 + 1 个 XML>
- 前端：<新增的 Vue 文件 + 1 个 api.js + options.js 追加>
- SQL：已追加到 parking_init.sql（表 + <N> 条菜单行）
- 测试：新增 Parking<Entity>ControllerTest，更新 Schema/Menu 测试断言
- 验证：mvn 测试 X 绿，npm build 通过

状态机：<贴一个小表或一句话描述>
下一步：执行 SQL 脚本后即可访问 /parking/<resource>。
```

## 反模式 — 一旦发现立即改

| 反模式 | 正确做法 |
|---|---|
| 用 `el-dialog` 做新增/编辑主表单 | 独立路由页面 `form.vue` |
| 用 `@Data` / `@Getter` (Lombok) | 普通 getter/setter |
| `delete from parking_<resource> where id = ?` | `update ... set del_flag = '2'` |
| `enum BillStatus { DRAFT, SUBMITTED, ... }` | `String billStatus` + 注释 + options.js |
| `@ForeignKey` 或 DB 层 FK 约束 | Service 层校验外键存在性 |
| 在 Domain 里重复声明 `createBy / createTime` | 由 BaseEntity 继承 |
| 测试用 `@SpringBootTest` 起整个上下文 | `MockMvcBuilders.standaloneSetup` |
| 硬编码 menu_id 或 `delete from sys_menu` | `insert ... where not exists` + `@var_id` |
| API 文件里 default export | 每个方法 `export function xxx()` |
| 列表页 `getList` 里 `this.form = {}` | `this.resetForm('queryForm')` |
| 新建 `sql/parking/<module>.sql` | 追加到 `parking_init.sql` |

## References（深度模板，按需 Read）

这些文件都在 `.claude/skills/parking-bill-module/references/`，Codex 直接 Read 路径即可：

- `.claude/skills/parking-bill-module/references/backend-template.md` — Domain / Mapper / XML / Service / Controller 可直接复制的代码模板
- `.claude/skills/parking-bill-module/references/frontend-template.md` — list 页 / form 页 / api.js 模板
- `.claude/skills/parking-bill-module/references/sql-template.md` — 建表 DDL + 菜单 insert 模板
- `.claude/skills/parking-bill-module/references/test-template.md` — Controller 测试 + Schema/Menu 测试更新模板
- `.claude/skills/parking-bill-module/references/state-machine-guide.md` — 默认状态机详细说明、自定义替换指南、void 时的级联策略
- `.claude/skills/parking-bill-module/references/cascade-guide.md` — **有级联（写其他业务表）的模块必读**：4 种级联范式 + 飞行前检查 + Service 级测试模板
