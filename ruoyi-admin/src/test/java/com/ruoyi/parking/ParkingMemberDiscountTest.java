package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.util.ParkingMemberDiscountUtils;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ParkingMemberDiscountTest
{
    private static final BigDecimal AMOUNT_100 = new BigDecimal("100.00");

    @Test
    void silverMemberGets5PercentDiscount()
    {
        ParkingCustomer customer = activeMember("1");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer);
        assertEquals(new BigDecimal("5.00"), discount);
    }

    @Test
    void goldMemberGets10PercentDiscount()
    {
        ParkingCustomer customer = activeMember("2");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer);
        assertEquals(new BigDecimal("10.00"), discount);
    }

    @Test
    void platinumMemberGets15PercentDiscount()
    {
        ParkingCustomer customer = activeMember("3");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer);
        assertEquals(new BigDecimal("15.00"), discount);
    }

    @Test
    void nonMemberGetsNoDiscount()
    {
        ParkingCustomer customer = new ParkingCustomer();
        customer.setIsMember("0");
        customer.setMemberType("1");
        customer.setMemberExpireTime(futureDate());
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer);
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void expiredMemberGetsNoDiscount()
    {
        ParkingCustomer customer = new ParkingCustomer();
        customer.setIsMember("1");
        customer.setMemberType("2");
        // 到期时间设为昨天
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        customer.setMemberExpireTime(cal.getTime());
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer);
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void nullCustomerGetsNoDiscount()
    {
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, null);
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void zeroAmountGetsNoDiscount()
    {
        ParkingCustomer customer = activeMember("1");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(BigDecimal.ZERO, customer);
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void payAmountCorrectlyReducedByDiscount()
    {
        ParkingCustomer customer = activeMember("2"); // 金卡 90折
        BigDecimal original = new BigDecimal("380.00");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(original, customer);
        BigDecimal payAmount = original.subtract(discount);
        assertTrue(discount.compareTo(BigDecimal.ZERO) > 0, "Expected discount > 0 for active member");
        assertEquals(new BigDecimal("342.00"), payAmount);
    }

    private ParkingCustomer activeMember(String memberType)
    {
        ParkingCustomer customer = new ParkingCustomer();
        customer.setIsMember("1");
        customer.setMemberType(memberType);
        customer.setMemberExpireTime(futureDate());
        return customer;
    }

    private Date futureDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 365);
        return cal.getTime();
    }
}
