package com.ruoyi.parking.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Lightweight projection used by the overview dashboard "recent orders" table.
 * Rows come from a UNION over parking_temp_order, parking_monthly_order and
 * parking_membership_order, so the entity is read-only.
 */
public class ParkingRecentOrder implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** temp / monthly / membership */
    private String orderType;

    private String orderNo;

    private Long lotId;

    private String lotName;

    private BigDecimal payAmount;

    private String payStatus;

    private Date createTime;

    public String getOrderType()
    {
        return orderType;
    }

    public void setOrderType(String orderType)
    {
        this.orderType = orderType;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getLotId()
    {
        return lotId;
    }

    public void setLotId(Long lotId)
    {
        this.lotId = lotId;
    }

    public String getLotName()
    {
        return lotName;
    }

    public void setLotName(String lotName)
    {
        this.lotName = lotName;
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

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
