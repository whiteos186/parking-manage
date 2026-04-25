# 级联场景指南

单据模块的"业务流转"很少是孤岛。审核通过、提交、作废这些动作,通常会触发对**其他业务表**的写操作:生成下游订单、创建支付记录、改副表状态、发外部消息。

这一类"级联写入"代码最容易出 bug,因为:
- 跨表事务边界容易漏(`@Transactional` 没加 / rollbackFor 没配)
- 下游表字段语义和上游不对齐(下游有 `in_time`,上游有 "有效期开始")
- 下游写入失败时,上游的状态推进没回滚
- 写入下游后,**忘记把下游 ID 回写到上游**(导致后续追溯不到)
- `void` / `reject` 时不知道要不要把已经创建的下游也处理掉

本文件给出 4 种最常见的级联范式 + 飞行前检查 + Service 级测试模板。

---

## 飞行前检查(必做,不要跳过)

在 ServiceImpl 里写任何级联代码之前,**必须先 Read 下游目标的 Domain 类和 Mapper.xml**,把以下信息列清楚:

| 项 | 检查方法 | 为什么重要 |
|---|---|---|
| 下游 Domain 全字段表 | Read `Parking<Downstream>.java` | 防止字段名拼错(`vehicle_plate_no` vs `plateNo`) |
| 下游 Mapper insert 的必填字段 | Read `Parking<Downstream>Mapper.xml` 的 `<insert>` 段 | 防止漏 not-null 字段导致 SQL 报错 |
| 下游字段的默认值约定 | 看 insert XML 里哪些字段写死了(`'0'`, `sysdate()` 等) | 知道哪些字段应用层不用填 |
| 下游 `useGeneratedKeys` 是否开启 | 看 insert XML 的 `useGeneratedKeys="true" keyProperty="xxx"` | 决定 ID 回写是不是免费的 |
| 下游 Service 是否有更高级的方法 | 列 `IParking<Downstream>Service.java` | 优先调 Service 的业务方法(可能内含校验/默认值)而非直接调 mapper |
| 下游表的字段语义和上游需求是否对齐 | 对照需求文档 + 字段 comment | 不对齐时要决定:复用既有字段(写注释解释映射) vs 加新字段(扩表) |
| 下游本身有没有"删除/作废"语义 | 搜 `delete` / `void` 方法 | 决定上游 `voidParking<Entity>` 时怎么处理已生成的下游 |

把这些信息以一段注释的形式写在你打算调用的级联代码上方,作为决策记录。例子:

```java
// CASCADE: 调用 ParkingTempOrderMapper.insertParkingTempOrder
//   下游字段映射:
//     vehiclePlateNo  ← current.vehiclePlateNo  (上游同名)
//     lotId           ← current.lotId
//     inTime          ← auditTime               (语义映射:有效期开始 = 审核时间)
//     outTime         ← auditTime + validDays 天 (语义映射:有效期结束)
//     payAmount/feeAmount/discountAmount ← BigDecimal.ZERO (待车辆出场时再算)
//     bizStatus '0' / payStatus '0'             (XML insert 默认值,实际由 service 兜底)
//   ID 回写:useGeneratedKeys 开启,直接读 tempOrder.getTempOrderId()
//   void 策略:保守不动(临停可能已被实际使用)
```

这个动作每次只花 30 秒,但救你于上线后才发现"字段写错位"。

---

## 范式 A:生成下游订单

**适用**: 审核通过 → 创建下游业务单据(临停订单 / 派工单 / 出库单 / ...)

**关键点**:
- 整个方法 `@Transactional(rollbackFor = Exception.class)`,失败一起回滚
- 先推进上游状态 → 再创建下游 → 再回写下游 ID 到上游(三步同事务)
- 用 `useGeneratedKeys` 自动取回下游 ID
- 在 `voidParking<Entity>` 里**显式决定**是否级联作废下游(默认保守,不动)

```java
@Override
@Transactional(rollbackFor = Exception.class)
public int approveParking<Entity>(Parking<Entity> form)
{
    Parking<Entity> current = <entity>Mapper.selectParking<Entity>ById(form.get<Entity>Id());
    if (current == null) {
        throw new ServiceException("<模块中文>不存在");
    }
    if (!STATUS_PENDING.equals(current.getBillStatus())) {
        throw new ServiceException("仅待审核状态可审核通过");
    }
    // 数据完整性预检(级联依赖的字段必须有)
    if (current.getLotId() == null) {
        throw new ServiceException("停车场缺失,无法生成下游单据");
    }

    Date auditTime = new Date();

    // 1. 推进上游状态
    Parking<Entity> update = new Parking<Entity>();
    update.set<Entity>Id(form.get<Entity>Id());
    update.setBillStatus(STATUS_APPROVED);
    update.setAuditUser(form.getUpdateBy());
    update.setAuditTime(auditTime);
    update.setAuditRemark(form.getAuditRemark());
    update.setUpdateBy(form.getUpdateBy());
    int rows = <entity>Mapper.updateParking<Entity>(update);

    // 2. 级联:创建下游单据
    // CASCADE: 见上方"飞行前检查"中记录的字段映射
    Parking<Downstream> downstream = new Parking<Downstream>();
    downstream.setLotId(current.getLotId());
    downstream.setVehiclePlateNo(current.getVehiclePlateNo());
    downstream.setInTime(auditTime);                                    // 语义映射
    downstream.setOutTime(addDays(auditTime, current.getValidDays()));  // 语义映射
    downstream.setCreateBy(form.getUpdateBy());
    downstream.setRemark("由 " + current.getBillNo() + " 审核通过自动生成");
    parking<Downstream>Mapper.insertParking<Downstream>(downstream);

    // 3. 回写下游 ID 到上游(便于追溯)
    Parking<Entity> backFill = new Parking<Entity>();
    backFill.set<Entity>Id(form.get<Entity>Id());
    backFill.set<Downstream>Id(downstream.get<Downstream>Id());
    backFill.setUpdateBy(form.getUpdateBy());
    <entity>Mapper.updateParking<Entity>(backFill);

    return rows;
}
```

**Domain 必须加一个外键字段**保存下游 ID(如 `Long tempOrderId`),且对应 schema 加列 `<downstream>_id bigint(20) default null comment '...'`。

---

## 范式 B:创建支付记录

**适用**: 审核通过 / 支付动作 → 写一条 `parking_payment_record`(项目里现成的 service)

**关键点**:
- 用现成的 `IParkingPaymentRecordService.createPaymentForOrder(...)` 而不是直接 insert mapper
- 金额为 0 时跳过(常见于免单单据)

```java
@Override
@Transactional(rollbackFor = Exception.class)
public int approveParking<Entity>(Parking<Entity> form)
{
    Parking<Entity> current = ... // 状态防护省略

    // 1. 上游状态推进
    Parking<Entity> update = new Parking<Entity>();
    update.set<Entity>Id(form.get<Entity>Id());
    update.setBillStatus(STATUS_APPROVED);
    update.setAuditTime(new Date());
    update.setUpdateBy(form.getUpdateBy());
    int rows = <entity>Mapper.updateParking<Entity>(update);

    // 2. 创建支付记录(免单跳过)
    BigDecimal cascadeAmount = current.getPayAmount() != null ? current.getPayAmount() : BigDecimal.ZERO;
    if (cascadeAmount.compareTo(BigDecimal.ZERO) > 0)
    {
        parkingPaymentRecordService.createPaymentForOrder(
            current.getBillNo(),               // 关联业务单号
            "<bizType>",                        // 业务类型字符常量(见 ParkingPaymentRecord 里的 biz_order_type 字典)
            current.getCustomerId(),            // 客户 ID(可空)
            current.getLotId(),
            cascadeAmount,
            null,                               // 支付方式,null 由后端兜底
            form.getUpdateBy()
        );
    }
    return rows;
}
```

注入: 在构造函数加 `IParkingPaymentRecordService parkingPaymentRecordService`。这个 service 已经存在,不需要新建。**先 Read** `IParkingPaymentRecordService.java` 确认 `createPaymentForOrder` 的当前签名。

---

## 范式 C:多表状态联动

**适用**: 主单审核通过后,把多张关联从表的状态一起推进(典型:批量结算 → 把所有子明细标记为 "已结算")

**关键点**:
- 用 mapper 的批量 update,不要循环 update
- 子表也要走逻辑删除过滤(`del_flag = '0'`)

```java
@Override
@Transactional(rollbackFor = Exception.class)
public int approveParking<Entity>(Parking<Entity> form)
{
    Parking<Entity> current = ... // 状态防护省略

    Parking<Entity> update = new Parking<Entity>();
    update.set<Entity>Id(form.get<Entity>Id());
    update.setBillStatus(STATUS_APPROVED);
    update.setAuditTime(new Date());
    update.setUpdateBy(form.getUpdateBy());
    int rows = <entity>Mapper.updateParking<Entity>(update);

    // 级联:把所有子明细推进到"已结算"
    parkingItemMapper.batchUpdateStatusByMasterId(
        current.get<Entity>Id(),
        "0",   // from
        "1",   // to
        form.getUpdateBy()
    );

    return rows;
}
```

子 mapper 需要加一个新方法:

```xml
<update id="batchUpdateStatusByMasterId">
    update parking_<item> set
        item_status = #{toStatus},
        update_by = #{updateBy},
        update_time = sysdate()
    where master_id = #{masterId}
      and item_status = #{fromStatus}
      and del_flag = '0'
</update>
```

---

## 范式 D:外部通知

**适用**: 审核通过后通知外部系统(发短信 / 推 webhook / 写 MQ)

**关键点**:
- **不要**让外部通知失败影响事务
- 通知动作放在事务**外**,即 `@Transactional` 的方法 return 后再发(或者用 `TransactionSynchronizationManager.registerSynchronization` 在 commit 后触发)
- 通知本身做 try-catch,失败只记日志,不抛

```java
@Override
@Transactional(rollbackFor = Exception.class)
public int approveParking<Entity>(Parking<Entity> form)
{
    Parking<Entity> current = ... // 状态防护省略

    // 事务内只动 DB
    Parking<Entity> update = new Parking<Entity>();
    update.set<Entity>Id(form.get<Entity>Id());
    update.setBillStatus(STATUS_APPROVED);
    int rows = <entity>Mapper.updateParking<Entity>(update);

    // 通知:注册成"事务提交后才执行"
    final Long entityId = current.get<Entity>Id();
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
            try {
                notificationService.sendApproved(entityId);
            } catch (Exception ex) {
                log.warn("notify approved failed for id={}", entityId, ex);
            }
        }
    });

    return rows;
}
```

如果项目里没有 `notificationService`,优先用 `ApplicationEventPublisher` + 一个 `@TransactionalEventListener(phase = AFTER_COMMIT)` 的监听器,而不是直接调 webhook。

---

## void 时的级联策略

`voidParking<Entity>` 在作废上游时,要决定**已经创建的下游**怎么办。三种选项:

| 策略 | 何时用 | 代码 |
|---|---|---|
| **(i) 级联作废** | 下游还未生效,且作废上游意味着"整笔业务取消" | 在 `voidParking<Entity>` 内调用 `parking<Downstream>Service.voidParking<Downstream>(downstreamId, ...)` |
| **(ii) 保守不动**(默认) | 下游可能已经被实际使用(如临停订单车辆已入场)、有支付流水、有实际业务影响 | 仅在 `void_remark` / `remark` 字段记录"作废,但下游 X 未联动" |
| **(iii) 阻断作废** | 下游已经产生了不可逆的副作用(已收款、已发票) | 在 `voidParking<Entity>` 开头查下游状态,如果不允许作废就抛 `ServiceException("下游单据 X 已生效,本单不能作废,请走退款流程")` |

**选哪个?** 写代码时按这个顺序判断:

1. 下游有支付/收款/发票 → **阻断**
2. 下游有任何实物动作(出库/入场/通知客户)→ **保守**
3. 否则 → **级联**

不管选哪种,都必须在 `voidParking<Entity>` 方法上方加注释说明决策依据。**不要静默不动而不写注释** — 后人维护时无法判断是漏了还是有意不写。

---

## Service 级单元测试模板(级联场景必加)

Phase 12 的 Controller 测试用 mockmvc + Mockito,**不能验证级联逻辑**(因为 Service 被 mock 了)。所以涉及级联的模块**必须**额外加一个 ServiceImpl 单元测试,覆盖三件事:

1. 快乐路径:下游确实被调用,字段映射正确
2. 守卫路径:状态非法时抛 ServiceException,下游 mapper **不应被调用**
3. 回滚路径:下游抛异常时,整个方法抛异常(意味着事务会被回滚)

文件路径: `ruoyi-admin/src/test/java/com/ruoyi/parking/Parking<Entity>ServiceImplTest.java`

```java
package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.Parking<Entity>;
import com.ruoyi.parking.domain.Parking<Downstream>;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.Parking<Entity>Mapper;
import com.ruoyi.parking.mapper.Parking<Downstream>Mapper;
import com.ruoyi.parking.service.impl.Parking<Entity>ServiceImpl;

class Parking<Entity>ServiceImplTest
{
    private Parking<Entity>Mapper entityMapper;
    private ParkingLotMapper lotMapper;
    private Parking<Downstream>Mapper downstreamMapper;
    private Parking<Entity>ServiceImpl service;

    @BeforeEach
    void setUp()
    {
        entityMapper = mock(Parking<Entity>Mapper.class);
        lotMapper = mock(ParkingLotMapper.class);
        downstreamMapper = mock(Parking<Downstream>Mapper.class);
        service = new Parking<Entity>ServiceImpl(entityMapper, lotMapper, downstreamMapper);
    }

    private Parking<Entity> pendingBill()
    {
        Parking<Entity> bill = new Parking<Entity>();
        bill.set<Entity>Id(100L);
        bill.setBillNo("XX20260101120000001");
        bill.setLotId(1L);
        bill.setVehiclePlateNo("京A12345");
        bill.setValidDays(7);
        bill.setBillStatus("10"); // 待审核
        return bill;
    }

    @Test
    void approveCreatesDownstreamAndWritesBackId()
    {
        when(entityMapper.selectParking<Entity>ById(100L)).thenReturn(pendingBill());
        when(lotMapper.selectParkingLotById(1L)).thenReturn(new ParkingLot());
        when(downstreamMapper.insertParking<Downstream>(any())).thenAnswer(inv -> {
            Parking<Downstream> arg = inv.getArgument(0);
            arg.set<Downstream>Id(999L); // 模拟自增 ID
            return 1;
        });

        Parking<Entity> form = new Parking<Entity>();
        form.set<Entity>Id(100L);
        form.setUpdateBy("auditor");
        service.approveParking<Entity>(form);

        // 验证下游被调用,且字段映射正确
        ArgumentCaptor<Parking<Downstream>> captor = ArgumentCaptor.forClass(Parking<Downstream>.class);
        verify(downstreamMapper).insertParking<Downstream>(captor.capture());
        Parking<Downstream> created = captor.getValue();
        assertEquals(1L, created.getLotId());
        assertEquals("京A12345", created.getVehiclePlateNo());
        assertNotNull(created.getInTime());
        assertNotNull(created.getOutTime());
        assertTrue(created.getOutTime().after(created.getInTime()));

        // 验证 ID 回写发生(至少 2 次 update:状态推进 + 回写)
        verify(entityMapper, atLeast(2)).updateParking<Entity>(any());
    }

    @Test
    void approveRejectsIfNotPending()
    {
        Parking<Entity> draft = pendingBill();
        draft.setBillStatus("00"); // 草稿
        when(entityMapper.selectParking<Entity>ById(100L)).thenReturn(draft);

        Parking<Entity> form = new Parking<Entity>();
        form.set<Entity>Id(100L);
        form.setUpdateBy("auditor");

        ServiceException ex = assertThrows(ServiceException.class,
            () -> service.approveParking<Entity>(form));
        assertTrue(ex.getMessage().contains("仅待审核"));

        // 状态非法时,下游绝对不能被调用
        verify(downstreamMapper, never()).insertParking<Downstream>(any());
    }

    @Test
    void approvePropagatesDownstreamFailure()
    {
        when(entityMapper.selectParking<Entity>ById(100L)).thenReturn(pendingBill());
        when(lotMapper.selectParkingLotById(1L)).thenReturn(new ParkingLot());
        when(downstreamMapper.insertParking<Downstream>(any()))
            .thenThrow(new RuntimeException("simulated DB error"));

        Parking<Entity> form = new Parking<Entity>();
        form.set<Entity>Id(100L);
        form.setUpdateBy("auditor");

        // 必须抛出来,这样外层 @Transactional 才会回滚
        assertThrows(RuntimeException.class, () -> service.approveParking<Entity>(form));
    }
}
```

**注意**:
- `@Transactional` 在单元测试里**不会真正生效**(没起 Spring 容器),所以"回滚路径"测试只能验证**异常被抛出来**,不能验证 DB 真的回滚了。要验证后者必须用 `@SpringBootTest + @Sql + 真数据库`,代价大,不在此 skill 范围内。
- 上面的"快乐路径"测试中,验证下游 ID 回写时用 `verify(entityMapper, atLeast(2)).updateParking<Entity>(any())` 而不是精确等于 2,因为不同实现的 update 次数可能不同(2 次或 3 次)。

---

## Phase 检查点(写完级联代码后必须自查)

回到 SKILL.md 的 Phase 12 之前,请确保:

- [ ] 飞行前检查注释已写在 ServiceImpl 顶部
- [ ] `@Transactional(rollbackFor = Exception.class)` 加在级联方法上
- [ ] 下游 ID 已回写到上游 Domain(对应 schema 已加列)
- [ ] `voidParking<Entity>` 注释里写明了三种 cascade 策略选了哪一个、为什么
- [ ] 已经在 Phase 12 加 ServiceImpl 单元测试,覆盖快乐 / 守卫 / 回滚三条路径
- [ ] 如果用了范式 B(支付记录),已确认 `IParkingPaymentRecordService.createPaymentForOrder` 的当前签名
- [ ] 如果用了范式 D(外部通知),通知动作在事务**外**或 `afterCommit`
