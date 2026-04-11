package com.ruoyi.parking.util;

import com.ruoyi.parking.domain.ParkingCustomer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class ParkingMemberDiscountUtils
{
    private ParkingMemberDiscountUtils() {}

    /**
     * 计算会员折扣金额。客户必须是有效会员且未到期，否则返回 ZERO。
     * 银卡 0.95、金卡 0.90、铂金 0.85
     */
    public static BigDecimal calculateDiscount(BigDecimal originalAmount, ParkingCustomer customer)
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
        BigDecimal rate = resolveDiscountRate(customer.getMemberType());
        if (rate == null)
        {
            return BigDecimal.ZERO;
        }
        return originalAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /** 返回折扣额外比例（即原价 × rate = 优惠金额），非折后价比例 */
    private static BigDecimal resolveDiscountRate(String memberType)
    {
        if ("1".equals(memberType))
        {
            return new BigDecimal("0.05"); // 银卡 95折，优惠5%
        }
        if ("2".equals(memberType))
        {
            return new BigDecimal("0.10"); // 金卡 90折，优惠10%
        }
        if ("3".equals(memberType))
        {
            return new BigDecimal("0.15"); // 铂金 85折，优惠15%
        }
        return null;
    }
}
