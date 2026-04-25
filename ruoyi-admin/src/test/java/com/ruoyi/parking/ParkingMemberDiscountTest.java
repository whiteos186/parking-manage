package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.util.ParkingMemberDiscountUtils;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ParkingMemberDiscountTest
{
    private static final BigDecimal AMOUNT_100 = new BigDecimal("100.00");

    @Test
    void silverMemberGetsConfiguredDiscount()
    {
        ParkingCustomer customer = activeMember("1");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer, enabledDiscount());
        assertEquals(new BigDecimal("5.00"), discount);
    }

    @Test
    void goldMemberGetsConfiguredDiscount()
    {
        ParkingCustomer customer = activeMember("2");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer, enabledDiscount());
        assertEquals(new BigDecimal("10.00"), discount);
    }

    @Test
    void platinumMemberGetsConfiguredDiscount()
    {
        ParkingCustomer customer = activeMember("3");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer, enabledDiscount());
        assertEquals(new BigDecimal("15.00"), discount);
    }

    @Test
    void nonMemberGetsNoDiscount()
    {
        ParkingCustomer customer = new ParkingCustomer();
        customer.setIsMember("0");
        customer.setMemberType("1");
        customer.setMemberExpireTime(futureDate());
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer, enabledDiscount());
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void expiredMemberGetsNoDiscount()
    {
        ParkingCustomer customer = new ParkingCustomer();
        customer.setIsMember("1");
        customer.setMemberType("2");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        customer.setMemberExpireTime(cal.getTime());
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer, enabledDiscount());
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void nullCustomerGetsNoDiscount()
    {
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, null, enabledDiscount());
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void zeroAmountGetsNoDiscount()
    {
        ParkingCustomer customer = activeMember("1");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(BigDecimal.ZERO, customer, enabledDiscount());
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void disabledMemberDiscountReturnsZero()
    {
        ParkingCustomer customer = activeMember("2");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer, disabledDiscount());
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void missingConfiguredRateThrowsException()
    {
        ParkingCustomer customer = activeMember("2");
        ParkingSettingsDto.MemberDiscount memberDiscount = new ParkingSettingsDto.MemberDiscount();
        memberDiscount.setMemberDiscountEnabled(Boolean.TRUE);
        memberDiscount.setSilverRate(new BigDecimal("0.05"));
        memberDiscount.setPlatinumRate(new BigDecimal("0.15"));

        assertThrows(ServiceException.class,
            () -> ParkingMemberDiscountUtils.calculateDiscount(AMOUNT_100, customer, memberDiscount));
    }

    @Test
    void payAmountCorrectlyReducedByConfiguredDiscount()
    {
        ParkingCustomer customer = activeMember("2");
        BigDecimal original = new BigDecimal("380.00");
        BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(original, customer, enabledDiscount());
        BigDecimal payAmount = original.subtract(discount);
        assertTrue(discount.compareTo(BigDecimal.ZERO) > 0);
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

    private ParkingSettingsDto.MemberDiscount enabledDiscount()
    {
        ParkingSettingsDto.MemberDiscount memberDiscount = new ParkingSettingsDto.MemberDiscount();
        memberDiscount.setMemberDiscountEnabled(Boolean.TRUE);
        memberDiscount.setSilverRate(new BigDecimal("0.05"));
        memberDiscount.setGoldRate(new BigDecimal("0.10"));
        memberDiscount.setPlatinumRate(new BigDecimal("0.15"));
        return memberDiscount;
    }

    private ParkingSettingsDto.MemberDiscount disabledDiscount()
    {
        ParkingSettingsDto.MemberDiscount memberDiscount = enabledDiscount();
        memberDiscount.setMemberDiscountEnabled(Boolean.FALSE);
        return memberDiscount;
    }
}
