package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ParkingTempOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long tempOrderId;

    private String orderNo;

    @NotNull(message = "停车场不能为空")
    private Long lotId;

    private Long spaceId;

    private Long customerId;

    @NotBlank(message = "车牌号不能为空")
    private String vehiclePlateNo;

    private Date inTime;

    private Date outTime;

    private Integer parkingDurationMin;

    private BigDecimal feeAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private String payStatus;

    private String bizStatus;

    private Date payTime;

    private Date closeTime;

    private String delFlag;

    /** Transient — joined from parking_lot for list view */
    private transient String lotName;

    /** Transient — date range filter for inTime (start) */
    private transient String inTimeStart;

    /** Transient — date range filter for inTime (end) */
    private transient String inTimeEnd;

    public Long getTempOrderId()
    {
        return tempOrderId;
    }

    public void setTempOrderId(Long tempOrderId)
    {
        this.tempOrderId = tempOrderId;
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

    public Long getSpaceId()
    {
        return spaceId;
    }

    public void setSpaceId(Long spaceId)
    {
        this.spaceId = spaceId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getVehiclePlateNo()
    {
        return vehiclePlateNo;
    }

    public void setVehiclePlateNo(String vehiclePlateNo)
    {
        this.vehiclePlateNo = vehiclePlateNo;
    }

    public Date getInTime()
    {
        return inTime;
    }

    public void setInTime(Date inTime)
    {
        this.inTime = inTime;
    }

    public Date getOutTime()
    {
        return outTime;
    }

    public void setOutTime(Date outTime)
    {
        this.outTime = outTime;
    }

    public Integer getParkingDurationMin()
    {
        return parkingDurationMin;
    }

    public void setParkingDurationMin(Integer parkingDurationMin)
    {
        this.parkingDurationMin = parkingDurationMin;
    }

    public BigDecimal getFeeAmount()
    {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount)
    {
        this.feeAmount = feeAmount;
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

    public Date getCloseTime()
    {
        return closeTime;
    }

    public void setCloseTime(Date closeTime)
    {
        this.closeTime = closeTime;
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

    public String getInTimeStart()
    {
        return inTimeStart;
    }

    public void setInTimeStart(String inTimeStart)
    {
        this.inTimeStart = inTimeStart;
    }

    public String getInTimeEnd()
    {
        return inTimeEnd;
    }

    public void setInTimeEnd(String inTimeEnd)
    {
        this.inTimeEnd = inTimeEnd;
    }
}
