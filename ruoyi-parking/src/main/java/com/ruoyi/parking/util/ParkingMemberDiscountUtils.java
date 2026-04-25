package com.ruoyi.parking.util;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class ParkingMemberDiscountUtils
{
    private ParkingMemberDiscountUtils()
    {
    }

    public static BigDecimal calculateDiscount(
        BigDecimal originalAmount,
        ParkingCustomer customer,
        ParkingSettingsDto.MemberDiscount memberDiscount)
    {
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        if (customer == null || !"1".equals(customer.getIsMember()))
        {
            return BigDecimal.ZERO;
        }
        Date expireTime = customer.getMemberExpireTime();
        if (expireTime == null || expireTime.before(new Date()))
        {
            return BigDecimal.ZERO;
        }
        if (memberDiscount == null || !Boolean.TRUE.equals(memberDiscount.getMemberDiscountEnabled()))
        {
            return BigDecimal.ZERO;
        }

        BigDecimal discountRate = resolveDiscountRate(customer.getMemberType(), memberDiscount);
        if (discountRate == null)
        {
            return BigDecimal.ZERO;
        }
        return originalAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal resolveDiscountRate(
        String memberType,
        ParkingSettingsDto.MemberDiscount memberDiscount)
    {
        if ("1".equals(memberType))
        {
            return requireRate(memberDiscount.getSilverRate(), "parking.rule.memberDiscount.silver");
        }
        if ("2".equals(memberType))
        {
            return requireRate(memberDiscount.getGoldRate(), "parking.rule.memberDiscount.gold");
        }
        if ("3".equals(memberType))
        {
            return requireRate(memberDiscount.getPlatinumRate(), "parking.rule.memberDiscount.platinum");
        }
        return null;
    }

    private static BigDecimal requireRate(BigDecimal rate, String configKey)
    {
        if (rate == null)
        {
            throw new ServiceException("缺少停车场全局配置: " + configKey);
        }
        return rate;
    }
}
