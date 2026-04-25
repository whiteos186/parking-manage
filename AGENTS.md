# AGENTS.md — 给 Codex 在本仓库写代码用的工作手册

本文件是 Codex（或任何遵循 `AGENTS.md` 约定的 AI 编码代理）在本仓库工作时的硬性规范。读完整篇后再动键盘。仓库内任何与本文件冲突的注释、历史代码或注释皆以本文件为准。

## 1. 项目是什么

- **名字**：基于 Spring Boot + Vue 的停车场管理系统（毕业设计项目）
- **底座**：RuoYi Vue 3.9.2 脚手架
- **后端**：Spring Boot 3 / Jakarta EE / MyBatis (XML) / Spring Security / JWT
- **前端**：Vue 2 + Element UI + Axios + Vue Router（**不是 Vue 3**）
- **数据库**：MySQL 8（`ry-cloud` 库）
- **当前分支**：`feat/parking-management-system`
- **业务模块根目录**：`ruoyi-parking/`（后端）、`ruoyi-ui/src/views/parking/` + `ruoyi-ui/src/api/parking/`（前端）
- **业务 SQL**：`sql/parking/parking_init.sql`（建表 + 种子 + 菜单 + 权限 + 角色绑定）

毕业论文 `D:\code\基于SpringBoot+Vue+的停车场管理系统的设计与实.docx` 是功能规格的最终来源，遇到模糊需求以论文为准。

## 2. 三套账户体系（**本仓库最重要的不变量，违反必崩**）

| 表 | 谁 | 登录入口 |
|---|---|---|
| `sys_user` | 平台员工：超管 + 停车场管理员 | 后台 `/login` |
| `parking_customer` | 停车客户（车主、月租、会员） | 客户端登录（独立流程） |
| `parking_lot_admin` | 把 `sys_user.user_id` 绑定到 `lot_id` 的关联表 | 不直接登录 |

**铁律**：

1. 所有业务表的客户外键叫 `customer_id`（FK → `parking_customer.customer_id`），**绝不**叫 `user_id`、**绝不**指向 `sys_user`
2. `customer_id` 字段类型一律 `bigint`，删除走逻辑删除 (`del_flag char(1)`)
3. `parking_lot_admin` 是 `sys_user` 唯一允许 join 的关联点
4. 数据隔离用 `ParkingAuthUtils`（`ruoyi-parking` 模块内）做：admin 看全量、`lot_admin` 按所绑 lot 过滤、`customer` 只看自己的数据

三个角色的 role_key：`admin` / `lot_admin` / `customer`。前端登录后路由根据角色重定向（见 `ruoyi-ui/src/permission.js` 的 `resolveFirstRoute`）。

## 3. 全局编码硬约束

下面这 15 条违反任何一条都会被现有测试或 code review 打回。**不要发明新风格**。

| # | 约束 | 为什么 |
|---|---|---|
| 1 | 客户外键用 `customer_id`，不用 `user_id` | 见第 2 节 |
| 2 | 权限码格式 `parking:<resource>:<action>`；`<action>` 仅限 `list / query / add / edit / remove / export / <business_action>` | `ParkingMenuScriptTest` 校验 |
| 3 | URL 前缀 `/parking/<resource>`；业务动作挂 `PUT /parking/<resource>/<action>` | Controller 测试校验 |
| 4 | 逻辑删除 `del_flag char(1) default '0'`；删除 = `set del_flag = '2'`；查询永远 `and del_flag = '0'` | 业务回溯 |
| 5 | 状态字段用 `String`（char(1) 或 char(2)），**不要 enum**，注释里写清每个码值含义 | 兼容 MySQL CHAR + 配合前端 options.js 字典 |
| 6 | 金额用 `BigDecimal` + DB `decimal(10,2) default 0.00` | double 精度坑 |
| 7 | Domain 继承 `BaseEntity`，普通 getter/setter，**不要 Lombok** | 仓库未引入 Lombok |
| 8 | Service 用构造注入 + `final` 字段（不要 `@Autowired` 字段注入） | Spring 推荐 |
| 9 | `@Transactional(rollbackFor = Exception.class)` 只加在多步写入的业务方法上，CRUD 不加 | 缩小锁范围 |
| 10 | 字段多的新增/编辑用**独立路由页面** `/parking/<resource>/form`，**不要 el-dialog**；只有"输入审核意见"这种一两行的小动作才用 dialog | 体验 + 用户明确要求 |
| 11 | 菜单 SQL 用 `set @parking_<resource>_id := (...)` 变量 + `insert ... select ... from dual where not exists (...)` 幂等插入；**严禁** hardcode `menu_id`；**严禁** `delete from sys_menu` | `ParkingMenuScriptTest` 校验 |
| 12 | 建表、种子、菜单、权限 SQL **统一追加**到 `sql/parking/parking_init.sql`；不要新建 `<module>.sql`，不要恢复旧的拆分脚本 | `ParkingSchemaScriptTest` / `ParkingMenuScriptTest` 只扫这一个入口 |
| 13 | 数据库**不加 FK 约束**；外键存在性校验放 Service 层 | 性能 + 配合逻辑删除 |
| 14 | 中文文案直接硬编码，没有 i18n | 项目没装 vue-i18n |
| 15 | 代码优先精简易读、逻辑直观、向下兼容；不要为了“架构感”过度封装、抽象或引入新层 | 毕设项目要稳定可讲、可维护，旧数据和旧接口不能被无意打断 |

## 4. 模块结构速查

### 后端 (`ruoyi-parking/src/main/java/com/ruoyi/parking/`)

```
controller/   *Controller.java        — REST 入口，extends BaseController
service/      IParking*Service.java   — 接口
service/impl/ Parking*ServiceImpl.java— 实现，构造注入，业务流转
mapper/       Parking*Mapper.java     — MyBatis 接口
domain/       Parking*.java           — 实体，extends BaseEntity
```

XML 在 `ruoyi-parking/src/main/resources/mapper/parking/Parking*Mapper.xml`。

### 前端 (`ruoyi-ui/src/`)

```
views/parking/<resource>/index.vue   — 列表页（含搜索 + 表格 + 分页 + 操作按钮）
views/parking/<resource>/form.vue    — 独立编辑页（新增 / 修改）
views/parking/options.js             — 全部业务字典常量集中在这里
api/parking/<resource>.js            — 每个方法 export function（不要 default export）
```

### SQL (`sql/parking/`)

```
parking_init.sql        — drop + create + 种子数据 + 菜单权限 + 角色授权 + 测试账户
```

### 测试 (`ruoyi-admin/src/test/java/com/ruoyi/parking/`)

- `ParkingSchemaScriptTest` — 校验建表 SQL 中表名 + 必需字段
- `ParkingMenuScriptTest` — 校验菜单 SQL 中插入数 + 变量名 + 权限码
- `ParkingModuleSmokeTest` — 模块装配冒烟
- 各业务模块的 `Parking*ControllerTest` — `MockMvcBuilders.standaloneSetup`，不起 Spring 上下文

新增模块务必同步更新前两个回归测试，否则必红。

## 5. 写新业务模块的"配方"（最高优先级）

Codex 专属深度规范放在 `.codex/skills/`，按主题分文件，按需 Read：

| 文件 | 何时读 |
|---|---|
| `.codex/skills/parking-bill-module.md` | **入口文件**。用户要求做"XX 单 / XX 订单 / XX 工单 / 任何带状态流转的业务模块"时，先整篇读完再动键盘。包含 Phase 0~13 完整流程 + 默认 5 态状态机 + 反模式清单 |
| `.claude/skills/parking-bill-module/references/backend-template.md` | 写 Domain / Mapper / XML / Service / Controller 时，复制对应代码模板 |
| `.claude/skills/parking-bill-module/references/frontend-template.md` | 写列表页 / 编辑页 / api.js 时 |
| `.claude/skills/parking-bill-module/references/sql-template.md` | 追加建表 SQL / 菜单 SQL 时 |
| `.claude/skills/parking-bill-module/references/test-template.md` | 写 Controller 测试 / 更新 Schema / Menu 回归测试时 |
| `.claude/skills/parking-bill-module/references/state-machine-guide.md` | 自定义状态机时 |
| `.claude/skills/parking-bill-module/references/cascade-guide.md` | **有级联（业务方法写其他业务表）必读**：4 种范式 + 飞行前检查 + Service 单测模板 |

> 引用的 `references/*.md` 与 Claude Code 共用，单一源地避免漂移。Codex 直接 Read 路径即可，跟目录名（`.claude` vs `.codex`）无关。

入口文件的几个核心要点（违反后修不完）：

- **Phase 0**：先把 6 项问完（模块名 / 挂载位置 / 字段清单 / 关联 / 状态机 / 业务动作）再动代码
- **Phase 5 / 6**：Mapper XML 排序、Service 层状态防护、`updateXxx` 时 `billStatus = null` 让 `<if>` 跳过
- **Phase 12**：级联模块必须额外写 Service 级测试，覆盖"快乐 / 守卫 / 回滚"三条路径

## 6. 状态机默认值（单据型业务）

```
00 草稿        → 10 待审核                       （submit）
10 待审核      → 20 审核通过 / 30 审核拒绝       （approve / reject）
20 审核通过    → 90 已作废                       （void）
30 审核拒绝    → 00 草稿（编辑后） / 10 待审核   （重新提交）
90 已作废      终态
```

Service 层防护规则（写在 `Parking*ServiceImpl` 里）：

- `update`：仅允许 `bill_status in ('00','30')`
- `submit`：`'00'/'30'` → `'10'`
- `approve`：`'10'` → `'20'`
- `reject`：`'10'` → `'30'`，`audit_remark` 必填
- `void`：`'20'` → `'90'`
- `remove`（逻辑删）：仅允许 `bill_status in ('00','30')`

非法转移一律 `throw new ServiceException("...")`。

## 7. 反模式（出现在 diff 里立刻改）

| 反模式 | 应该怎么做 |
|---|---|
| `el-dialog` 装多字段表单 | 独立路由页 `form.vue` |
| `@Data` / `@Getter`（Lombok） | 普通 getter/setter |
| 物理 `delete from parking_*` | `update ... set del_flag = '2'` |
| `enum BillStatus { ... }` | `String billStatus` + 注释 + options.js |
| DB 层 FK 约束 / `@ForeignKey` | Service 层做存在性校验 |
| Domain 重复声明 `createBy / createTime / updateBy / updateTime / remark` | BaseEntity 已有 |
| 测试用 `@SpringBootTest` 起整个上下文 | `MockMvcBuilders.standaloneSetup(controller).build()` |
| 菜单 SQL hardcode `menu_id` 或 `delete from sys_menu` | `set @var := (...)` + `insert ... where not exists` |
| API 文件 `export default { ... }` | 每个方法 `export function xxx()` |
| 列表页用 `this.queryParams = {}` 重置 | `this.resetForm('queryForm')` |
| 给某模块新建 `sql/parking/<module>.sql` | 追加到 `parking_init.sql` |
| 业务表多一个 `user_id` 关联 sys_user | 用 `customer_id` 关联 `parking_customer` |

## 8. 本地构建 / 测试命令

> Windows + bash（git-bash）。路径统一用正斜杠。

```bash
# 后端：单跑某模块测试 + 回归
cd D:/code/parking-management-system
mvn -pl ruoyi-admin -am \
    "-Dtest=ParkingSchemaScriptTest,ParkingMenuScriptTest,ParkingModuleSmokeTest,Parking<Entity>ControllerTest" \
    "-Dsurefire.failIfNoSpecifiedTests=false" \
    test

# 后端：跑全部 parking 测试
mvn -pl ruoyi-admin -am "-Dtest=Parking*Test" "-Dsurefire.failIfNoSpecifiedTests=false" test

# 前端：开发模式
cd D:/code/parking-management-system/ruoyi-ui && npm run dev

# 前端：生产构建（提交前必须过）
cd D:/code/parking-management-system/ruoyi-ui && npm run build:prod

# 启动后端（开发）
cd D:/code/parking-management-system && mvn -pl ruoyi-admin -am spring-boot:run
```

测试账户（密码全是 `admin123`）：

- `admin` — 系统管理员
- `lotadmin1` — 停车场管理员，绑定 `lot_id=1`
- `customer1` — 客户，绑定 `parking_customer.customer_id=1`

## 9. 提交前自检清单

提交前 Codex 必须自己过一遍：

- [ ] 客户外键叫 `customer_id` 不叫 `user_id`
- [ ] `del_flag` 字段齐全，删除是 update 不是 delete
- [ ] 权限码符合 `parking:<resource>:<action>` 形式
- [ ] 菜单 SQL 用 `@parking_<resource>_id` 变量 + `where not exists`，没 hardcode `menu_id`
- [ ] SQL 追加在 `parking_init.sql` 对应区域，drop / create / seed / menu 顺序正确
- [ ] `ParkingSchemaScriptTest` / `ParkingMenuScriptTest` 已同步更新
- [ ] Controller 测试用 standaloneSetup，不依赖 Spring 上下文
- [ ] 有级联：写了 Service 级测试覆盖快乐/守卫/回滚
- [ ] 编辑页是独立路由 `form.vue`，不是 el-dialog
- [ ] options.js 字典命名为 `<MODULE>_BILL_STATUS_OPTIONS`
- [ ] `mvn ... test` 全绿、`npm run build:prod` 通过
- [ ] commit message 用项目历史的 `feat/fix/docs/refactor: ...` 前缀

## 10. 提问与对齐

需求模糊时**先问再写**。需要对齐的最常见 6 项（来自 `parking-bill-module` 的 Phase 0）：

1. 模块中文名 + 英文 resource 代号
2. 菜单挂载在 `工作台 / 基础档案 / 运营中心 / 客户中心` 哪个根目录下
3. 字段清单（中文名 / 英文字段名 / 类型 / 必填 / 唯一）
4. 是否需要关联 `lot_id` / `customer_id` / `vehicle_id`
5. 用默认状态机还是自定义
6. CRUD 之外还要哪些业务按钮

把 6 个问题一次性问完，不要挤牙膏。

## 11. 不要做的事

- 不要在未经用户允许时执行 `git push` / `git reset --hard` / 强删分支
- 不要 `--no-verify` 跳过钩子
- 不要为了绕过测试失败而修改测试断言（除非确认是预期的回归更新）
- 不要在 `node_modules/`、`*.class`、`replay_pid*.log`、`hs_err_pid*.log` 下做任何变更
- 不要新建 README.md / *.md 文档（除非用户明确要求），文档放 `docs/`
- 不要给 Domain 类加 Lombok / 给前端加 i18n / 给业务表加数据库 FK 约束

---

任何与本文件冲突的旧约定都按本文件覆盖。文档落后于代码时，回头更新这个文件而不是绕过它。
