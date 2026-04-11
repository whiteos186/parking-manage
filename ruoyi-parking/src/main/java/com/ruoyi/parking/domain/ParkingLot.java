package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;

public class ParkingLot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long lotId;

    @NotBlank(message = "停车场名称不能为空")
    private String lotName;

    @NotBlank(message = "停车场地址不能为空")
    private String lotAddress;

    private Integer totalSpaceCount;

    private Integer availableSpaceCount;

    private BigDecimal monthlyPrice;

    private BigDecimal tempHourPrice;

    private String status;

    private String delFlag;

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

    public String getLotAddress()
    {
        return lotAddress;
    }

    public void setLotAddress(String lotAddress)
    {
        this.lotAddress = lotAddress;
    }

    public Integer getTotalSpaceCount()
    {
        return totalSpaceCount;
    }

    public void setTotalSpaceCount(Integer totalSpaceCount)
    {
        this.totalSpaceCount = totalSpaceCount;
    }

    public Integer getAvailableSpaceCount()
    {
        return availableSpaceCount;
    }

    public void setAvailableSpaceCount(Integer availableSpaceCount)
    {
        this.availableSpaceCount = availableSpaceCount;
    }

    public BigDecimal getMonthlyPrice()
    {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice)
    {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getTempHourPrice()
    {
        return tempHourPrice;
    }

    public void setTempHourPrice(BigDecimal tempHourPrice)
    {
        this.tempHourPrice = tempHourPrice;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
