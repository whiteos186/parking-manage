# 单据状态机设计指南

本文件说明默认状态机的详细规则、常见自定义场景,以及替换时需要同步改动的位置。

---

## 默认状态机(5 态)

```
        ┌──────────────────────────────────────────────────────┐
        │                                                      │
        ▼                                                      │
┌─────────┐    submit     ┌──────────┐    approve   ┌──────────┐    void    ┌──────────┐
│   00    │ ─────────────▶│    10    │ ────────────▶│    20    │ ──────────▶│    90    │
│  草稿   │               │ 待审核   │              │ 审核通过 │            │ 已作废   │
│         │ ◀──────┐      │          │ ────────┐    │          │            │  (终态)  │
└─────────┘        │      └──────────┘  reject │    └──────────┘            └──────────┘
     │             │           ▲                │
     │  edit       │           │                ▼
     │  remove     │           │           ┌──────────┐
     ▼             │           │           │    30    │
   (deleted)       │           │           │ 审核拒绝 │
                   │           └───────────│          │
                   │              re-submit└──────────┘
                   │                            │
                   │                edit 改完自动回到草稿
                   └────────────────────────────┘
```

### 状态值与中文标签

| 值 | 中文 | tagType(前端 el-tag 颜色) |
|---|---|---|
| `'00'` | 草稿 | `info`(灰) |
| `'10'` | 待审核 | `warning`(橙) |
| `'20'` | 审核通过 | `success`(绿) |
| `'30'` | 审核拒绝 | `danger`(红) |
| `'90'` | 已作废 | `info`(灰) |

为什么用 `'00'` 而不是 `'0'`?预留扩展空间 — 后续可能需要 `'01'` 待补充资料、`'05'` 内部预审等中间态,固定 2 位避免数据库迁移。

### 转换允许表

| 当前状态 | 允许动作 | 触发后状态 | 触发权限 |
|---|---|---|---|
| `00` 草稿 | 编辑 (`update`) | `00` | `parking:<r>:edit` |
| `00` 草稿 | 删除 (`delete`) | (deleted) | `parking:<r>:remove` |
| `00` 草稿 | 提交审核 (`submit`) | `10` | `parking:<r>:submit` |
| `10` 待审核 | 审核通过 (`approve`) | `20` | `parking:<r>:approve` |
| `10` 待审核 | 审核拒绝 (`reject`) | `30`(必填意见) | `parking:<r>:reject` |
| `20` 审核通过 | 作废 (`void`) | `90` | `parking:<r>:void` |
| `30` 审核拒绝 | 编辑 (`update`) | `00`(自动回到草稿) | `parking:<r>:edit` |
| `30` 审核拒绝 | 删除 (`delete`) | (deleted) | `parking:<r>:remove` |
| `30` 审核拒绝 | 重新提交 (`submit`) | `10` | `parking:<r>:submit` |
| `90` 已作废 | (无) | 终态 | - |

**关键设计**:
- `30 → 00`(编辑后自动回草稿)是 `Parking{{Entity}}ServiceImpl.updateParking{{Entity}}` 里的硬逻辑,前端不需要做任何额外处理
- `'30' 重新提交`不需要先编辑 — 用户只是点"重新提交"也能从拒绝直接进入待审核(允许"申诉")
- `'20' → '90'` 作废前,Service 应该校验该单据是否已被下游使用(如果有下游单据/支付,要么级联作废,要么禁止作废) — 在 ServiceImpl 的 `voidParking{{Entity}}` 里加 BIZ 块

### 状态防护清单(Service 层)

| 方法 | 守卫条件 | 失败时抛错 |
|---|---|---|
| `updateParking{{Entity}}` | 当前状态 ∈ {`00`, `30`} | "当前状态不允许编辑" |
| `deleteParking{{Entity}}ByIds` | 每条记录状态 ∈ {`00`, `30`} | "仅草稿或审核拒绝状态可删除(单号 X)" |
| `submitParking{{Entity}}` | 当前状态 ∈ {`00`, `30`} | "当前状态不允许提交" |
| `approveParking{{Entity}}` | 当前状态 = `10` | "仅待审核状态可审核通过" |
| `rejectParking{{Entity}}` | 当前状态 = `10`,且 `auditRemark` 非空 | "仅待审核状态可审核拒绝" / "审核拒绝必须填写意见" |
| `voidParking{{Entity}}` | 当前状态 = `20` | "仅审核通过状态可作废" |

防护代码模板见 `backend-template.md` 的 ServiceImpl 模板。**任何状态推进方法都必须先 select 当前记录、检查实际状态,而不是相信前端传上来的旧状态**(避免并发覆盖)。

---

## 常见自定义场景

### 场景 A:不需要审核(只有草稿和已生效)

适用:配置类单据(如"维护计划"),只要保存就是有效的。

```
草稿 (00)  ──── publish ───▶  生效中 (20)  ──── void ───▶  已作废 (90)
```

改动:
- 状态值: 仍用 `00 / 20 / 90`,跳过 `10/30`
- 删 Service 的 `submit/approve/reject` 方法,加一个 `publishParking{{Entity}}`
- 删菜单 SQL 里的 submit/approve/reject 三个权限块,加一个 `publish`
- 前端列表页 `canSubmit/canAudit` 判断改成 `canPublish: row.billStatus === '00'`
- options.js 的 `BILL_STATUS_OPTIONS` 删掉 `10/30` 项

### 场景 B:多级审核(初审 + 复审)

适用:大额订单、跨部门协作。

```
草稿 (00) ──submit──▶ 待初审 (10) ──firstApprove──▶ 待复审 (15) ──finalApprove──▶ 通过 (20) ──void──▶ 作废 (90)
                              │                            │
                              └──reject──▶ 拒绝 (30) ◀────┘
```

改动:
- 状态值: `00 / 10 / 15 / 20 / 30 / 90`,加 `'15'` 待复审
- Service: 拆 `approveParking{{Entity}}` 为 `firstApprove` + `finalApprove`,各自检查源态分别是 `10` / `15`
- Domain 加 `firstAuditUser / firstAuditTime / finalAuditUser / finalAuditTime` 字段
- 菜单加 `parking:<r>:first-approve` / `parking:<r>:final-approve` 两个权限码
- 前端 options.js 的 BILL_STATUS_OPTIONS 加 `'15'` 待复审项
- 列表页操作列改写按钮显示规则:`row.billStatus === '10'` 显示初审按钮,`row.billStatus === '15'` 显示复审按钮

### 场景 C:订单类单据(含支付状态 + 业务状态双轨)

适用:`ParkingMembershipOrder` 这种已经存在的订单。**直接参考现有 ParkingMembershipOrder 实现,不要套用本 skill**。本 skill 默认是单审核流的"工单/单据"型,不是"支付订单"型。

如果用户的需求其实是订单(有支付),应当在 Phase 0 反问清楚,然后:
- 选项 1: 拒绝用本 skill,引导用户参考现有 `ParkingMembershipOrder` 写
- 选项 2: 用本 skill + 在状态机里增加 `payStatus` 字段(独立于 `billStatus`),做"双轨"

双轨方案的字段:
```
billStatus  char(2)   00草稿 10待审核 20审核通过 90已作废       — 业务流转
payStatus   char(1)   0未支付 1已支付 2已退款                   — 支付流转
```

业务方法增加: `payParking{{Entity}}` / `refundParking{{Entity}}`,配合现有 `ParkingPaymentRecordService.createPaymentForOrder(...)`。这个属于**变体**,不是默认。

### 场景 D:暂存草稿要支持自动保存(防丢失)

适用:字段非常多的长表单。

改动只在前端:
- form.vue 的 `data()` 里加 `autoSaveTimer: null`
- `mounted()` 启动 `setInterval(this.autoSave, 30000)`(30 秒)
- `autoSave` 只在 `isEdit && form.billStatus === '00'` 时触发,调用 `updateParking{{Entity}}` 静默保存(不弹消息)
- `beforeDestroy` 清除 timer

后端不需要改。

---

## 替换状态机时要同步改的位置

如果决定走自定义状态机,以下文件必须一起改,**漏一处都会让 skill 产出的代码不自洽**:

| 位置 | 改什么 |
|---|---|
| `Parking{{Entity}}.java` Domain | `billStatus` 字段注释里的状态值清单 |
| `Parking{{Entity}}ServiceImpl.java` 顶端常量 | `STATUS_*` 常量值 |
| `Parking{{Entity}}ServiceImpl.java` 各业务方法 | 守卫条件 + 实际转换的目标值 |
| `Parking{{Entity}}Controller.java` | 端点数量(增删 submit/approve/reject/void) |
| `IParking{{Entity}}Service.java` | 业务方法签名 |
| `<resource>.js` API 文件 | 业务动作的 export function |
| `index.vue` 列表页 | 操作列按钮 + `canXxx()` 判断 |
| `options.js` 的 `<RESOURCE>_BILL_STATUS_OPTIONS` | 选项数组 |
| `parking_init.sql` | 权限按钮的菜单 insert 块 |
| `ParkingMenuScriptTest` | block count + 权限码集合 |
| `Parking{{Entity}}ControllerTest` | 增删对应的 endpoint 测试 |

建议做法:在 Phase 0 拍板状态机后,**立刻在工作区写一个小的"状态机决议"清单**(用 TaskCreate 或者一段口述),贯穿后续每个 phase 都对照这个清单替换,避免漏改。

---

## void 时的级联策略(快速参考)

如果模块的 `approve` 已经在下游表(如 `parking_temp_order`、`parking_payment_record`)创建过记录,那么 `voidParking<Entity>` 时必须显式决定怎么处理这些下游。三种策略:

| 策略 | 何时用 | 怎么写 |
|---|---|---|
| **(i) 级联作废** | 下游还没生效,作废上游意味着"整笔业务取消" | 在 `voidParking<Entity>` 内调用下游 service 的 void 方法 |
| **(ii) 保守不动**(默认) | 下游可能已被实际使用(车辆已入场、客户已收通知) | 仅在 remark 字段记录"下游 X 未联动作废" |
| **(iii) 阻断作废** | 下游已产生不可逆副作用(已收款、已开发票) | 开头查下游状态,不允许作废就抛 `ServiceException("下游 X 已生效,请走退款流程")` |

**判断顺序**:有支付/收款 → (iii);有实物/对外动作 → (ii);否则 → (i)。

不管选哪个,**都必须在 `voidParking<Entity>` 上方写注释说明决策依据**。完整代码模板和飞行前检查见 `cascade-guide.md`。

---

## 不要做的事

- ❌ 不要用 Java enum 表达状态。用 `String` + 类常量 + 注释。原因:RuoYi 的 MyBatis 体系对 enum 不友好,且数据库直接存字符值便于运维查询
- ❌ 不要在状态值用纯数字(`1, 2, 3`)。用 `'00', '10', '20'` 类带前导 0 的字符,留扩展空间且和已有项目风格一致
- ❌ 不要在前端做"状态计算"(比如把 `00` 当 `'草稿'` 显示)。统一通过 `findLabel(BILL_STATUS_OPTIONS, value)`,这样新增状态只改 options.js
- ❌ 不要在 Controller 层做状态防护。所有 guard 都在 ServiceImpl,Controller 只透传
- ❌ 不要在 Mapper XML 里做状态判断(`<if test="billStatus == '00'">`)。状态判断属于业务逻辑,放 Service
- ❌ 不要把 `auditRemark` 的"必填"校验放 Domain 的 `@NotBlank`,因为新建/编辑时它确实可以为空。在 `rejectParking{{Entity}}` 方法里手动校验
