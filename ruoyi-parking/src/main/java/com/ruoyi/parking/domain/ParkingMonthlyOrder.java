package com.ruoyi.parking.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingMonthlyOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long monthlyOrderId;

    private String orderNo;

    private Long customerId;

    private Long vehicleId;

    private String vehiclePlateNo;

    private Long lotId;

    private Integer monthCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private BigDecimal originalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private String payStatus;

    private String bizStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    private String delFlag;

    /** Transient — joined from parking_lot for list view */
    private String lotName;

    /** Transient — joined from parking_customer for list view */
    private String customerName;

    /** Transient — joined from parking_customer for list view */
    private String customerCode;

    /** Transient — joined from parking_customer for list view */
    private String mobile;

    public Long getMonthlyOrderId()
    {
        return monthlyOrderId;
    }

    public void setMonthlyOrderId(Long monthlyOrderId)
    {
        this.monthlyOrderId = monthlyOrderId;
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

    public String getVehiclePlateNo()
    {
        return vehiclePlateNo;
    }

    public void setVehiclePlateNo(String vehiclePlateNo)
    {
        this.vehiclePlateNo = vehiclePlateNo;
    }

    public Long getLotId()
    {
        return lotId;
    }

    public void setLotId(Long lotId)
    {
        this.lotId = lotId;
    }

    public Integer getMonthCount()
    {
        return monthCount;
    }

    public void setMonthCount(Integer monthCount)
    {
        this.monthCount = monthCount;
    }

    public LocalDateTime getStartTime()
    {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime)
    {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime()
    {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime)
    {
        this.endTime = endTime;
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

    public LocalDateTime getPayTime()
    {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime)
    {
        this.payTime = payTime;
    }

    public LocalDateTime getCancelTime()
    {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime)
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
