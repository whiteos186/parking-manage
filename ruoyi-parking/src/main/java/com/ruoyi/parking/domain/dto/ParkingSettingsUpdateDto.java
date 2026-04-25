package com.ruoyi.parking.domain.dto;

import java.math.BigDecimal;

public class ParkingSettingsUpdateDto
{
    private BigDecimal monthlyPrice;

    private BigDecimal tempHourPrice;

    private Integer tempFreeMinutes;

    private Integer tempBillingStepMinutes;

    private Boolean tempRoundUpEnabled;

    private BigDecimal tempDailyCapAmount;

    private Boolean memberDiscountEnabled;

    private BigDecimal silverDiscountRate;

    private BigDecimal goldDiscountRate;

    private BigDecimal platinumDiscountRate;

    private String defaultPaymentChannel;

    private Boolean cashEnabled;

    private Boolean cancelAutoRefundEnabled;

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

    public Integer getTempFreeMinutes()
    {
        return tempFreeMinutes;
    }

    public void setTempFreeMinutes(Integer tempFreeMinutes)
    {
        this.tempFreeMinutes = tempFreeMinutes;
    }

    public Integer getTempBillingStepMinutes()
    {
        return tempBillingStepMinutes;
    }

    public void setTempBillingStepMinutes(Integer tempBillingStepMinutes)
    {
        this.tempBillingStepMinutes = tempBillingStepMinutes;
    }

    public Boolean getTempRoundUpEnabled()
    {
        return tempRoundUpEnabled;
    }

    public void setTempRoundUpEnabled(Boolean tempRoundUpEnabled)
    {
        this.tempRoundUpEnabled = tempRoundUpEnabled;
    }

    public BigDecimal getTempDailyCapAmount()
    {
        return tempDailyCapAmount;
    }

    public void setTempDailyCapAmount(BigDecimal tempDailyCapAmount)
    {
        this.tempDailyCapAmount = tempDailyCapAmount;
    }

    public Boolean getMemberDiscountEnabled()
    {
        return memberDiscountEnabled;
    }

    public void setMemberDiscountEnabled(Boolean memberDiscountEnabled)
    {
        this.memberDiscountEnabled = memberDiscountEnabled;
    }

    public BigDecimal getSilverDiscountRate()
    {
        return silverDiscountRate;
    }

    public void setSilverDiscountRate(BigDecimal silverDiscountRate)
    {
        this.silverDiscountRate = silverDiscountRate;
    }

    public BigDecimal getGoldDiscountRate()
    {
        return goldDiscountRate;
    }

    public void setGoldDiscountRate(BigDecimal goldDiscountRate)
    {
        this.goldDiscountRate = goldDiscountRate;
    }

    public BigDecimal getPlatinumDiscountRate()
    {
        return platinumDiscountRate;
    }

    public void setPlatinumDiscountRate(BigDecimal platinumDiscountRate)
    {
        this.platinumDiscountRate = platinumDiscountRate;
    }

    public String getDefaultPaymentChannel()
    {
        return defaultPaymentChannel;
    }

    public void setDefaultPaymentChannel(String defaultPaymentChannel)
    {
        this.defaultPaymentChannel = defaultPaymentChannel;
    }

    public Boolean getCashEnabled()
    {
        return cashEnabled;
    }

    public void setCashEnabled(Boolean cashEnabled)
    {
        this.cashEnabled = cashEnabled;
    }

    public Boolean getCancelAutoRefundEnabled()
    {
        return cancelAutoRefundEnabled;
    }

    public void setCancelAutoRefundEnabled(Boolean cancelAutoRefundEnabled)
    {
        this.cancelAutoRefundEnabled = cancelAutoRefundEnabled;
    }
}
