# Backend Template

本文件提供 Java 后端五大件(Domain / Mapper / XML / Service / Controller)的填空式模板。
占位符用 `{{}}` 标记,替换规则:

- `{{Entity}}` = PascalCase 实体名,如 `MaintenanceOrder`
- `{{entity}}` = camelCase 实体名,如 `maintenanceOrder`
- `{{ENTITY}}` = UPPER_SNAKE,如 `MAINTENANCE_ORDER`
- `{{resource}}` = URL 段,如 `maintenance`
- `{{table}}` = DB 表名,如 `parking_maintenance_order`
- `{{pk}}` = 主键字段 camelCase,如 `maintenanceOrderId`
- `{{pk_col}}` = 主键字段 snake,如 `maintenance_order_id`
- `{{BillPrefix}}` = 业务编号前缀(2-3 字母),如 `MO`(MaintenanceOrder)
- `{{模块中文}}` = 菜单标题,如 `维修工单`

生成代码时逐处替换,替换完后把占位符全部删掉。业务字段用 `// BIZ` 注释块标记,从 Phase 0 的字段清单填进去。

---

## Domain 模板

文件路径: `ruoyi-parking/src/main/java/com/ruoyi/parking/domain/Parking{{Entity}}.java`

```java
package com.ruoyi.parking.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * {{模块中文}} parking_{{resource}}_order
 */
public class Parking{{Entity}} extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long {{pk}};

    /** 单据编号 */
    @NotBlank(message = "单据编号不能为空")
    private String billNo;

    /** 所属停车场ID */
    @NotNull(message = "所属停车场不能为空")
    private Long lotId;

    // BIZ: 业务字段 - 从 Phase 0 字段清单复制到此处
    // 例子:
    // private Long customerId;
    // private Long vehicleId;
    // private String equipmentCode;
    // private BigDecimal estimatedAmount;
    // private Date scheduleTime;

    /** 业务状态 00草稿 10待审核 20审核通过 30审核拒绝 90已作废 */
    private String billStatus;

    /** 提交时间 */
    private Date submitTime;

    /** 审核人 */
    private String auditUser;

    /** 审核时间 */
    private Date auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 作废时间 */
    private Date voidTime;

    /** 逻辑删除 0存在 2已删 */
    private String delFlag;

    // ------ 非持久化字段(关联查询回填) ------
    /** 停车场名称(join from parking_lot) */
    private transient String lotName;

    // BIZ: 其他 join 回填字段(如 customerName, vehiclePlateNo)

    // ====== getters / setters ======
    public Long get{{Entity}}Id() { return {{pk}}; }
    public void set{{Entity}}Id(Long {{pk}}) { this.{{pk}} = {{pk}}; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public Long getLotId() { return lotId; }
    public void setLotId(Long lotId) { this.lotId = lotId; }

    // BIZ: 对应业务字段的 get/set

    public String getBillStatus() { return billStatus; }
    public void setBillStatus(String billStatus) { this.billStatus = billStatus; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }

    public String getAuditUser() { return auditUser; }
    public void setAuditUser(String auditUser) { this.auditUser = auditUser; }

    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }

    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    public Date getVoidTime() { return voidTime; }
    public void setVoidTime(Date voidTime) { this.voidTime = voidTime; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getLotName() { return lotName; }
    public void setLotName(String lotName) { this.lotName = lotName; }
}
```

---

## Mapper 接口模板

文件路径: `ruoyi-parking/src/main/java/com/ruoyi/parking/mapper/Parking{{Entity}}Mapper.java`

```java
package com.ruoyi.parking.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ruoyi.parking.domain.Parking{{Entity}};

public interface Parking{{Entity}}Mapper
{
    Parking{{Entity}} selectParking{{Entity}}ById(Long {{pk}});

    List<Parking{{Entity}}> selectParking{{Entity}}List(Parking{{Entity}} query);

    int insertParking{{Entity}}(Parking{{Entity}} entity);

    int updateParking{{Entity}}(Parking{{Entity}} entity);

    int deleteParking{{Entity}}ByIds(@Param("{{entity}}Ids") Long[] {{entity}}Ids);
}
```

---

## Mapper XML 模板

文件路径: `ruoyi-parking/src/main/resources/mapper/parking/Parking{{Entity}}Mapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.parking.mapper.Parking{{Entity}}Mapper">

    <resultMap type="com.ruoyi.parking.domain.Parking{{Entity}}" id="Parking{{Entity}}Result">
        <id     property="{{pk}}"      column="{{pk_col}}"/>
        <result property="billNo"       column="bill_no"/>
        <result property="lotId"        column="lot_id"/>
        <!-- BIZ: 业务字段列映射 -->
        <result property="billStatus"   column="bill_status"/>
        <result property="submitTime"   column="submit_time"/>
        <result property="auditUser"    column="audit_user"/>
        <result property="auditTime"    column="audit_time"/>
        <result property="auditRemark"  column="audit_remark"/>
        <result property="voidTime"     column="void_time"/>
        <result property="delFlag"      column="del_flag"/>
        <result property="createBy"     column="create_by"/>
        <result property="createTime"   column="create_time"/>
        <result property="updateBy"     column="update_by"/>
        <result property="updateTime"   column="update_time"/>
        <result property="remark"       column="remark"/>
        <result property="lotName"      column="lot_name"/>
    </resultMap>

    <sql id="selectParking{{Entity}}Columns">
        select
            o.{{pk_col}},
            o.bill_no,
            o.lot_id,
            <!-- BIZ: o.biz_field_1, o.biz_field_2, -->
            o.bill_status,
            o.submit_time,
            o.audit_user,
            o.audit_time,
            o.audit_remark,
            o.void_time,
            o.del_flag,
            o.create_by,
            o.create_time,
            o.update_by,
            o.update_time,
            o.remark,
            l.lot_name
        from {{table}} o
        left join parking_lot l on l.lot_id = o.lot_id and l.del_flag = '0'
    </sql>

    <select id="selectParking{{Entity}}List" parameterType="com.ruoyi.parking.domain.Parking{{Entity}}" resultMap="Parking{{Entity}}Result">
        <include refid="selectParking{{Entity}}Columns"/>
        where o.del_flag = '0'
        <if test="billNo != null and billNo != ''">
            and o.bill_no like concat('%', #{billNo}, '%')
        </if>
        <if test="lotId != null">
            and o.lot_id = #{lotId}
        </if>
        <!-- BIZ: 其他过滤条件 -->
        <if test="billStatus != null and billStatus != ''">
            and o.bill_status = #{billStatus}
        </if>
        order by o.{{pk_col}} desc
    </select>

    <select id="selectParking{{Entity}}ById" parameterType="java.lang.Long" resultMap="Parking{{Entity}}Result">
        <include refid="selectParking{{Entity}}Columns"/>
        where o.{{pk_col}} = #{ {{pk}} } and o.del_flag = '0'
    </select>

    <insert id="insertParking{{Entity}}" parameterType="com.ruoyi.parking.domain.Parking{{Entity}}" useGeneratedKeys="true" keyProperty="{{pk}}">
        insert into {{table}} (
            bill_no,
            lot_id,
            <!-- BIZ: biz_field_1, biz_field_2, -->
            bill_status,
            del_flag,
            create_by,
            create_time,
            remark
        ) values (
            #{billNo},
            #{lotId},
            <!-- BIZ: #{bizField1}, #{bizField2}, -->
            #{billStatus},
            '0',
            #{createBy},
            sysdate(),
            #{remark}
        )
    </insert>

    <update id="updateParking{{Entity}}" parameterType="com.ruoyi.parking.domain.Parking{{Entity}}">
        update {{table}}
        <set>
            <if test="billNo != null and billNo != ''">bill_no = #{billNo},</if>
            <if test="lotId != null">lot_id = #{lotId},</if>
            <!-- BIZ: <if test="bizField1 != null">biz_field_1 = #{bizField1},</if> -->
            <if test="billStatus != null and billStatus != ''">bill_status = #{billStatus},</if>
            <if test="submitTime != null">submit_time = #{submitTime},</if>
            <if test="auditUser != null">audit_user = #{auditUser},</if>
            <if test="auditTime != null">audit_time = #{auditTime},</if>
            <if test="auditRemark != null">audit_remark = #{auditRemark},</if>
            <if test="voidTime != null">void_time = #{voidTime},</if>
            <if test="remark != null">remark = #{remark},</if>
            <if test="updateBy != null and updateBy != ''">update_by = #{updateBy},</if>
            update_time = sysdate()
        </set>
        where {{pk_col}} = #{ {{pk}} }
          and del_flag = '0'
    </update>

    <update id="deleteParking{{Entity}}ByIds" parameterType="java.lang.Long">
        update {{table}}
        set del_flag = '2',
            update_time = sysdate()
        where del_flag = '0'
          and {{pk_col}} in
        <foreach collection="{{entity}}Ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </update>

</mapper>
```

**注意**:
- `#{ {{pk}} }` 中 `{{pk}}` 是 camelCase,因为它是 Java 属性名
- XML 里 `update_time = sysdate()` 无条件更新,不用包在 `<if>` 里
- 不要加 `update_by` 到初始 insert — 初始只填 `create_by`

---

## Service 接口模板

文件路径: `ruoyi-parking/src/main/java/com/ruoyi/parking/service/IParking{{Entity}}Service.java`

```java
package com.ruoyi.parking.service;

import java.util.List;

import com.ruoyi.parking.domain.Parking{{Entity}};

public interface IParking{{Entity}}Service
{
    // 基础 CRUD
    Parking{{Entity}} selectParking{{Entity}}ById(Long {{pk}});

    List<Parking{{Entity}}> selectParking{{Entity}}List(Parking{{Entity}} query);

    int insertParking{{Entity}}(Parking{{Entity}} entity);

    int updateParking{{Entity}}(Parking{{Entity}} entity);

    int deleteParking{{Entity}}ByIds(Long[] {{entity}}Ids);

    // 业务流转
    int submitParking{{Entity}}(Parking{{Entity}} form);

    int approveParking{{Entity}}(Parking{{Entity}} form);

    int rejectParking{{Entity}}(Parking{{Entity}} form);

    int voidParking{{Entity}}(Parking{{Entity}} form);
}
```

---

## ServiceImpl 模板

文件路径: `ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/Parking{{Entity}}ServiceImpl.java`

```java
package com.ruoyi.parking.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.Parking{{Entity}};
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.mapper.Parking{{Entity}}Mapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.service.IParking{{Entity}}Service;

@Service
public class Parking{{Entity}}ServiceImpl implements IParking{{Entity}}Service
{
    private static final String STATUS_DRAFT    = "00";
    private static final String STATUS_PENDING  = "10";
    private static final String STATUS_APPROVED = "20";
    private static final String STATUS_REJECTED = "30";
    private static final String STATUS_VOID     = "90";

    private final Parking{{Entity}}Mapper {{entity}}Mapper;
    private final ParkingLotMapper parkingLotMapper;

    public Parking{{Entity}}ServiceImpl(
            Parking{{Entity}}Mapper {{entity}}Mapper,
            ParkingLotMapper parkingLotMapper)
    {
        this.{{entity}}Mapper = {{entity}}Mapper;
        this.parkingLotMapper = parkingLotMapper;
    }

    @Override
    public Parking{{Entity}} selectParking{{Entity}}ById(Long {{pk}})
    {
        return {{entity}}Mapper.selectParking{{Entity}}ById({{pk}});
    }

    @Override
    public List<Parking{{Entity}}> selectParking{{Entity}}List(Parking{{Entity}} query)
    {
        return {{entity}}Mapper.selectParking{{Entity}}List(query);
    }

    @Override
    public int insertParking{{Entity}}(Parking{{Entity}} entity)
    {
        // 1. 生成单据编号
        if (StringUtils.isEmpty(entity.getBillNo()))
        {
            String datePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            int rand = new Random().nextInt(9000) + 1000;
            entity.setBillNo("{{BillPrefix}}" + datePart + rand);
        }

        // 2. 默认值
        if (StringUtils.isEmpty(entity.getBillStatus()))
        {
            entity.setBillStatus(STATUS_DRAFT);
        }

        // 3. 业务校验:停车场存在
        if (entity.getLotId() != null)
        {
            ParkingLot lot = parkingLotMapper.selectParkingLotById(entity.getLotId());
            if (lot == null)
            {
                throw new ServiceException("停车场不存在");
            }
        }

        // BIZ: 其他外键存在性校验(customer / vehicle / ...)
        // BIZ: 衍生字段计算

        return {{entity}}Mapper.insertParking{{Entity}}(entity);
    }

    @Override
    public int updateParking{{Entity}}(Parking{{Entity}} entity)
    {
        Parking{{Entity}} current = {{entity}}Mapper.selectParking{{Entity}}ById(entity.get{{Entity}}Id());
        if (current == null)
        {
            throw new ServiceException("{{模块中文}}不存在");
        }

        // 状态防护:仅草稿或审核拒绝可编辑
        if (!STATUS_DRAFT.equals(current.getBillStatus())
                && !STATUS_REJECTED.equals(current.getBillStatus()))
        {
            throw new ServiceException("当前状态不允许编辑");
        }

        // 外键变更时校验
        if (entity.getLotId() != null && !entity.getLotId().equals(current.getLotId()))
        {
            ParkingLot lot = parkingLotMapper.selectParkingLotById(entity.getLotId());
            if (lot == null)
            {
                throw new ServiceException("停车场不存在");
            }
        }

        // 关键字段保护:普通 update 不能改这些,由专用业务方法负责
        entity.setBillStatus(null);
        entity.setSubmitTime(null);
        entity.setAuditUser(null);
        entity.setAuditTime(null);
        entity.setAuditRemark(null);
        entity.setVoidTime(null);

        // 被拒绝的单据改后自动回到草稿
        if (STATUS_REJECTED.equals(current.getBillStatus()))
        {
            entity.setBillStatus(STATUS_DRAFT);
        }

        return {{entity}}Mapper.updateParking{{Entity}}(entity);
    }

    @Override
    public int deleteParking{{Entity}}ByIds(Long[] {{entity}}Ids)
    {
        for (Long id : {{entity}}Ids)
        {
            Parking{{Entity}} current = {{entity}}Mapper.selectParking{{Entity}}ById(id);
            if (current == null)
            {
                continue;
            }
            if (!STATUS_DRAFT.equals(current.getBillStatus())
                    && !STATUS_REJECTED.equals(current.getBillStatus()))
            {
                throw new ServiceException("仅草稿或审核拒绝状态可删除(单号 " + current.getBillNo() + ")");
            }
        }
        return {{entity}}Mapper.deleteParking{{Entity}}ByIds({{entity}}Ids);
    }

    // ===================== 业务流转 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitParking{{Entity}}(Parking{{Entity}} form)
    {
        Parking{{Entity}} current = {{entity}}Mapper.selectParking{{Entity}}ById(form.get{{Entity}}Id());
        if (current == null)
        {
            throw new ServiceException("{{模块中文}}不存在");
        }
        if (!STATUS_DRAFT.equals(current.getBillStatus())
                && !STATUS_REJECTED.equals(current.getBillStatus()))
        {
            throw new ServiceException("当前状态不允许提交");
        }

        Parking{{Entity}} update = new Parking{{Entity}}();
        update.set{{Entity}}Id(form.get{{Entity}}Id());
        update.setBillStatus(STATUS_PENDING);
        update.setSubmitTime(new Date());
        update.setUpdateBy(form.getUpdateBy());
        return {{entity}}Mapper.updateParking{{Entity}}(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveParking{{Entity}}(Parking{{Entity}} form)
    {
        Parking{{Entity}} current = {{entity}}Mapper.selectParking{{Entity}}ById(form.get{{Entity}}Id());
        if (current == null)
        {
            throw new ServiceException("{{模块中文}}不存在");
        }
        if (!STATUS_PENDING.equals(current.getBillStatus()))
        {
            throw new ServiceException("仅待审核状态可审核通过");
        }

        Parking{{Entity}} update = new Parking{{Entity}}();
        update.set{{Entity}}Id(form.get{{Entity}}Id());
        update.setBillStatus(STATUS_APPROVED);
        update.setAuditUser(form.getUpdateBy());
        update.setAuditTime(new Date());
        update.setAuditRemark(form.getAuditRemark());
        update.setUpdateBy(form.getUpdateBy());

        int rows = {{entity}}Mapper.updateParking{{Entity}}(update);

        // BIZ: 审核通过后的级联动作(创建支付记录、生成下游订单、发外部通知等)
        // 使用 current(审核前的快照) + form(审核人传入的参数)
        //
        // 如果本模块需要级联,**先停下来**读 references/cascade-guide.md,做飞行前检查:
        //   1. Read 下游目标的 Domain + Mapper.xml,把字段映射列清楚
        //   2. 选择四种范式之一(A 生成下游 / B 创建支付 / C 状态联动 / D 外部通知)
        //   3. 决定 voidParking{{Entity}} 时怎么处理已经生成的下游(级联/保守/阻断)
        //   4. Phase 12 里加一份 Service 级单元测试(快乐 + 守卫 + 回滚)
        // 否则极易出现"字段拼错"、"事务漏配"、"忘回写下游 ID" 这三类典型 bug

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectParking{{Entity}}(Parking{{Entity}} form)
    {
        if (StringUtils.isEmpty(form.getAuditRemark()))
        {
            throw new ServiceException("审核拒绝必须填写意见");
        }

        Parking{{Entity}} current = {{entity}}Mapper.selectParking{{Entity}}ById(form.get{{Entity}}Id());
        if (current == null)
        {
            throw new ServiceException("{{模块中文}}不存在");
        }
        if (!STATUS_PENDING.equals(current.getBillStatus()))
        {
            throw new ServiceException("仅待审核状态可审核拒绝");
        }

        Parking{{Entity}} update = new Parking{{Entity}}();
        update.set{{Entity}}Id(form.get{{Entity}}Id());
        update.setBillStatus(STATUS_REJECTED);
        update.setAuditUser(form.getUpdateBy());
        update.setAuditTime(new Date());
        update.setAuditRemark(form.getAuditRemark());
        update.setUpdateBy(form.getUpdateBy());
        return {{entity}}Mapper.updateParking{{Entity}}(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidParking{{Entity}}(Parking{{Entity}} form)
    {
        Parking{{Entity}} current = {{entity}}Mapper.selectParking{{Entity}}ById(form.get{{Entity}}Id());
        if (current == null)
        {
            throw new ServiceException("{{模块中文}}不存在");
        }
        if (!STATUS_APPROVED.equals(current.getBillStatus()))
        {
            throw new ServiceException("仅审核通过状态可作废");
        }

        Parking{{Entity}} update = new Parking{{Entity}}();
        update.set{{Entity}}Id(form.get{{Entity}}Id());
        update.setBillStatus(STATUS_VOID);
        update.setVoidTime(new Date());
        update.setUpdateBy(form.getUpdateBy());

        int rows = {{entity}}Mapper.updateParking{{Entity}}(update);

        // BIZ: 作废后的级联动作 — 三选一,在此处实现并加注释说明依据。
        // 见 references/state-machine-guide.md 和 references/cascade-guide.md "void 时的级联策略" 一节:
        //   (i)  级联作废下游(下游还未生效)
        //   (ii) 保守不动 — 默认 — (下游可能已被实际使用,如临停车辆已入场)
        //   (iii) 阻断作废(下游已收款/已开票/已不可逆)
        // 不管选哪一种,都必须在这里写一行注释说明决策依据,不要静默。

        return rows;
    }
}
```

**关键点**:
- `STATUS_*` 常量写在类顶端,方便集中查看和维护
- 业务方法里**构造新的 update 对象**,只 set 要改的字段,避免误覆盖
- `@Transactional` 只加在多步写入的业务方法上,不要加在 select/ 简单 insert 上
- 抛错都用 `ServiceException`,带具体业务原因

---

## Controller 模板

文件路径: `ruoyi-parking/src/main/java/com/ruoyi/parking/controller/Parking{{Entity}}Controller.java`

```java
package com.ruoyi.parking.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.parking.domain.Parking{{Entity}};
import com.ruoyi.parking.service.IParking{{Entity}}Service;

@RestController
@RequestMapping("/parking/{{resource}}")
public class Parking{{Entity}}Controller extends BaseController
{
    private final IParking{{Entity}}Service {{entity}}Service;

    public Parking{{Entity}}Controller(IParking{{Entity}}Service {{entity}}Service)
    {
        this.{{entity}}Service = {{entity}}Service;
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:list')")
    @GetMapping("/list")
    public TableDataInfo list(Parking{{Entity}} query)
    {
        startPage();
        return getDataTable({{entity}}Service.selectParking{{Entity}}List(query));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:query')")
    @GetMapping("/{ {{pk}} }")
    public AjaxResult getInfo(@PathVariable Long {{pk}})
    {
        return success({{entity}}Service.selectParking{{Entity}}ById({{pk}}));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:add')")
    @Log(title = "{{模块中文}}", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Parking{{Entity}} entity)
    {
        entity.setCreateBy(getUsername());
        return toAjax({{entity}}Service.insertParking{{Entity}}(entity));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:edit')")
    @Log(title = "{{模块中文}}", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Parking{{Entity}} entity)
    {
        entity.setUpdateBy(getUsername());
        return toAjax({{entity}}Service.updateParking{{Entity}}(entity));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:submit')")
    @Log(title = "{{模块中文}}", businessType = BusinessType.UPDATE)
    @PutMapping("/submit")
    public AjaxResult submit(@RequestBody Parking{{Entity}} form)
    {
        form.setUpdateBy(getUsername());
        return toAjax({{entity}}Service.submitParking{{Entity}}(form));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:approve')")
    @Log(title = "{{模块中文}}", businessType = BusinessType.UPDATE)
    @PutMapping("/approve")
    public AjaxResult approve(@RequestBody Parking{{Entity}} form)
    {
        form.setUpdateBy(getUsername());
        return toAjax({{entity}}Service.approveParking{{Entity}}(form));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:reject')")
    @Log(title = "{{模块中文}}", businessType = BusinessType.UPDATE)
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody Parking{{Entity}} form)
    {
        form.setUpdateBy(getUsername());
        return toAjax({{entity}}Service.rejectParking{{Entity}}(form));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:void')")
    @Log(title = "{{模块中文}}", businessType = BusinessType.UPDATE)
    @PutMapping("/void")
    public AjaxResult voidBill(@RequestBody Parking{{Entity}} form)
    {
        form.setUpdateBy(getUsername());
        return toAjax({{entity}}Service.voidParking{{Entity}}(form));
    }

    @PreAuthorize("@ss.hasPermi('parking:{{resource}}:remove')")
    @Log(title = "{{模块中文}}", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax({{entity}}Service.deleteParking{{Entity}}ByIds(ids));
    }
}
```

**注意**:
- `@PutMapping("/void")` 对应的方法名必须叫 `voidBill`(或其它非保留字),`void` 是 Java 保留字
- `@DeleteMapping("/{ids}")` 参数名直接叫 `ids`,对应的 Service 方法签名里才用 `{{entity}}Ids`

---

## 命名速查

| 维度 | 示例值(以 MaintenanceOrder 为例) |
|---|---|
| 中文模块名 | 维修工单 |
| resource | maintenance |
| table | parking_maintenance_order |
| pk_col | maintenance_order_id |
| Java 类前缀 | ParkingMaintenanceOrder |
| pk (camelCase) | maintenanceOrderId |
| URL 前缀 | /parking/maintenance |
| 权限前缀 | parking:maintenance: |
| BillPrefix | MO |
| options.js 常量名 | MAINTENANCE_BILL_STATUS_OPTIONS |
| 前端路由路径 | /parking/maintenance/form |
| Vue 目录 | src/views/parking/maintenance/ |
| API 文件 | src/api/parking/maintenance.js |

表名、类名、路径的一致性是整个项目最重要的约定,替换时不能错位。
