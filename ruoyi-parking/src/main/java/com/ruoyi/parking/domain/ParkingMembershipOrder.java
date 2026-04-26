package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

public class ParkingMembershipOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long membershipOrderId;

    private String orderNo;

    private Long customerId;

    private Long vehicleId;

    private Long lotId;

    /** 会员类型 1银卡 2金卡 3白金 */
    private String membershipType;

    private Date validStartTime;

    private Date validEndTime;

    private BigDecimal originalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    /** 支付状态 0未支付 1已支付 2已退款 3已关闭 */
    private String payStatus;

    /** 业务状态 0待生效 1生效中 2已过期 3已取消 */
    private String bizStatus;

    private Date payTime;

    private Date cancelTime;

    private String delFlag;

    /** 非持久化字段：停车场名称 */
    private transient String lotName;

    /** 非持久化字段：客户名称 */
    private String customerName;

    /** 非持久化字段：客户编号 */
    private String customerCode;

    /** 非持久化字段：客户手机号 */
    private String mobile;

    public Long getMembershipOrderId()
    {
        return membershipOrderId;
    }

    public void setMembershipOrderId(Long membershipOrderId)
    {
        this.membershipOrderId = membershipOrderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Long getVehicleId()
    {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId)
    {
        this.vehicleId = vehicleId;
    }

    public Long getLotId()
    {
        return lotId;
    }

    public void setLotId(Long lotId)
    {
        this.lotId = lotId;
    }

    public String getMembershipType()
    {
        return membershipType;
    }

    public void setMembershipType(String membershipType)
    {
        this.membershipType = membershipType;
    }

    public Date getValidStartTime()
    {
        return validStartTime;
    }

    public void setValidStartTime(Date validStartTime)
    {
        this.validStartTime = validStartTime;
    }

    public Date getValidEndTime()
    {
        return validEndTime;
    }

    public void setValidEndTime(Date validEndTime)
    {
        this.validEndTime = validEndTime;
    }

    public BigDecimal getOriginalAmount()
    {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount)
    {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getDiscountAmount()
    {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount)
    {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getPayAmount()
    {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount)
    {
        this.payAmount = payAmount;
    }

    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public String getBizStatus()
    {
        return bizStatus;
    }

    public void setBizStatus(String bizStatus)
    {
        this.bizStatus = bizStatus;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public Date getCancelTime()
    {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime)
    {
        this.cancelTime = cancelTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getLotName()
    {
        return lotName;
    }

    public void setLotName(String lotName)
    {
        this.lotName = lotName;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
}
