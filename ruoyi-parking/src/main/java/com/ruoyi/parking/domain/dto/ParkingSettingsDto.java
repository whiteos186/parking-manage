package com.ruoyi.parking.domain.dto;

import java.math.BigDecimal;

public class ParkingSettingsDto
{
    private Pricing pricing;

    private MemberDiscount memberDiscount;

    private Payment payment;

    private Switches switches;

    public Pricing getPricing()
    {
        return pricing;
    }

    public void setPricing(Pricing pricing)
    {
        this.pricing = pricing;
    }

    public MemberDiscount getMemberDiscount()
    {
        return memberDiscount;
    }

    public void setMemberDiscount(MemberDiscount memberDiscount)
    {
        this.memberDiscount = memberDiscount;
    }

    public Payment getPayment()
    {
        return payment;
    }

    public void setPayment(Payment payment)
    {
        this.payment = payment;
    }

    public Switches getSwitches()
    {
        return switches;
    }

    public void setSwitches(Switches switches)
    {
        this.switches = switches;
    }

    public static class Pricing
    {
        private BigDecimal monthlyPrice;

        private BigDecimal tempHourPrice;

        private Integer tempFreeMinutes;

        private Integer tempBillingStepMinutes;

        private Boolean tempRoundUpEnabled;

        private BigDecimal tempDailyCapAmount;

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
    }

    public static class MemberDiscount
    {
        private Boolean memberDiscountEnabled;

        private BigDecimal silverRate;

        private BigDecimal goldRate;

        private BigDecimal platinumRate;

        public Boolean getMemberDiscountEnabled()
        {
            return memberDiscountEnabled;
        }

        public void setMemberDiscountEnabled(Boolean memberDiscountEnabled)
        {
            this.memberDiscountEnabled = memberDiscountEnabled;
        }

        public BigDecimal getSilverRate()
        {
            return silverRate;
        }

        public void setSilverRate(BigDecimal silverRate)
        {
            this.silverRate = silverRate;
        }

        public BigDecimal getGoldRate()
        {
            return goldRate;
        }

        public void setGoldRate(BigDecimal goldRate)
        {
            this.goldRate = goldRate;
        }

        public BigDecimal getPlatinumRate()
        {
            return platinumRate;
        }

        public void setPlatinumRate(BigDecimal platinumRate)
        {
            this.platinumRate = platinumRate;
        }
    }

    public static class Payment
    {
        private String defaultChannel;

        private Boolean cashEnabled;

        public String getDefaultChannel()
        {
            return defaultChannel;
        }

        public void setDefaultChannel(String defaultChannel)
        {
            this.defaultChannel = defaultChannel;
        }

        public Boolean getCashEnabled()
        {
            return cashEnabled;
        }

        public void setCashEnabled(Boolean cashEnabled)
        {
            this.cashEnabled = cashEnabled;
        }
    }

    public static class Switches
    {
        private Boolean cancelAutoRefundEnabled;

        public Boolean getCancelAutoRefundEnabled()
        {
            return cancelAutoRefundEnabled;
        }

        public void setCancelAutoRefundEnabled(Boolean cancelAutoRefundEnabled)
        {
            this.cancelAutoRefundEnabled = cancelAutoRefundEnabled;
        }
    }
}
