package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;

public class ParkingPaymentRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long paymentId;

    @NotBlank(message = "业务订单号不能为空")
    private String bizOrderNo;

    @NotBlank(message = "业务订单类型不能为空")
    private String bizOrderType;

    private Long customerId;

    private Long lotId;

    private String payChannel;

    private BigDecimal payAmount;

    private String payStatus;

    private String tradeNo;

    private Date payTime;

    private Date refundTime;

    /** transient: joined from parking_lot */
    private String lotName;

    /** transient: joined from parking_customer */
    private String customerName;

    /** transient: joined from parking_customer */
    private String customerCode;

    /** transient: joined from parking_customer */
    private String mobile;

    public Long getPaymentId()
    {
        return paymentId;
    }

    public void setPaymentId(Long paymentId)
    {
        this.paymentId = paymentId;
    }

    public String getBizOrderNo()
    {
        return bizOrderNo;
    }

    public void setBizOrderNo(String bizOrderNo)
    {
        this.bizOrderNo = bizOrderNo;
    }

    public String getBizOrderType()
    {
        return bizOrderType;
    }

    public void setBizOrderType(String bizOrderType)
    {
        this.bizOrderType = bizOrderType;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Long getLotId()
    {
        return lotId;
    }

    public void setLotId(Long lotId)
    {
        this.lotId = lotId;
    }

    public String getPayChannel()
    {
        return payChannel;
    }

    public void setPayChannel(String payChannel)
    {
        this.payChannel = payChannel;
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

    public String getTradeNo()
    {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo)
    {
        this.tradeNo = tradeNo;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public Date getRefundTime()
    {
        return refundTime;
    }

    public void setRefundTime(Date refundTime)
    {
        this.refundTime = refundTime;
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
