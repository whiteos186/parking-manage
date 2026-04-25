package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingTempOrder;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingSpaceMapper;
import com.ruoyi.parking.mapper.ParkingTempOrderMapper;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import com.ruoyi.parking.service.impl.ParkingTempOrderServiceImpl;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ParkingTempOrderPricingTest
{
    private ParkingTempOrderMapper tempOrderMapper;
    private ParkingLotMapper lotMapper;
    private ParkingSpaceMapper spaceMapper;
    private ParkingCustomerMapper customerMapper;
    private IParkingGlobalRuleService parkingGlobalRuleService;
    private ParkingTempOrderServiceImpl service;

    @BeforeEach
    void setUp()
    {
        tempOrderMapper = mock(ParkingTempOrderMapper.class);
        lotMapper = mock(ParkingLotMapper.class);
        spaceMapper = mock(ParkingSpaceMapper.class);
        customerMapper = mock(ParkingCustomerMapper.class);
        parkingGlobalRuleService = mock(IParkingGlobalRuleService.class);
        IParkingPaymentRecordService paymentRecordService = mock(IParkingPaymentRecordService.class);

        service = new ParkingTempOrderServiceImpl(
            tempOrderMapper,
            lotMapper,
            spaceMapper,
            customerMapper,
            paymentRecordService,
            parkingGlobalRuleService
        );

        when(tempOrderMapper.updateParkingTempOrder(any(ParkingTempOrder.class))).thenReturn(1);
    }

    @Test
    void exitUsesGlobalPricingRulesAndMemberDiscount()
    {
        ParkingTempOrder current = new ParkingTempOrder();
        current.setTempOrderId(1L);
        current.setLotId(1L);
        current.setCustomerId(10L);
        current.setBizStatus("1");
        current.setInTime(minutesAgoRoundedUp(80));
        when(tempOrderMapper.selectParkingTempOrderById(1L)).thenReturn(current);

        ParkingCustomer customer = activeMember("2");
        when(customerMapper.selectParkingCustomerById(10L)).thenReturn(customer);
        when(parkingGlobalRuleService.getValidatedPricing()).thenReturn(buildPricing("8.00", 15, 30, true, "60.00"));
        when(parkingGlobalRuleService.getValidatedMemberDiscount()).thenReturn(buildMemberDiscount(true, "0.05", "0.10", "0.15"));

        service.exitParkingTempOrder(1L);

        ArgumentCaptor<ParkingTempOrder> captor = ArgumentCaptor.forClass(ParkingTempOrder.class);
        verify(tempOrderMapper).updateParkingTempOrder(captor.capture());
        ParkingTempOrder update = captor.getValue();
        assertNotNull(update.getOutTime());
        assertEquals(Integer.valueOf(80), update.getParkingDurationMin());
        assertEquals(0, new BigDecimal("12.00").compareTo(update.getFeeAmount()));
        assertEquals(0, new BigDecimal("1.20").compareTo(update.getDiscountAmount()));
        assertEquals(0, new BigDecimal("10.80").compareTo(update.getPayAmount()));
        assertEquals("2", update.getBizStatus());
    }

    @Test
    void exitAppliesDailyCapAndSkipsDiscountWhenDisabled()
    {
        ParkingTempOrder current = new ParkingTempOrder();
        current.setTempOrderId(2L);
        current.setLotId(1L);
        current.setCustomerId(11L);
        current.setBizStatus("1");
        current.setInTime(minutesAgoRoundedUp(301));
        when(tempOrderMapper.selectParkingTempOrderById(2L)).thenReturn(current);

        ParkingCustomer customer = activeMember("3");
        when(customerMapper.selectParkingCustomerById(11L)).thenReturn(customer);
        when(parkingGlobalRuleService.getValidatedPricing()).thenReturn(buildPricing("12.00", 0, 30, false, "20.00"));
        when(parkingGlobalRuleService.getValidatedMemberDiscount()).thenReturn(buildMemberDiscount(false, "0.05", "0.10", "0.15"));

        service.exitParkingTempOrder(2L);

        ArgumentCaptor<ParkingTempOrder> captor = ArgumentCaptor.forClass(ParkingTempOrder.class);
        verify(tempOrderMapper).updateParkingTempOrder(captor.capture());
        ParkingTempOrder update = captor.getValue();
        assertEquals(Integer.valueOf(301), update.getParkingDurationMin());
        assertEquals(0, new BigDecimal("20.00").compareTo(update.getFeeAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(update.getDiscountAmount()));
        assertEquals(0, new BigDecimal("20.00").compareTo(update.getPayAmount()));
    }

    private Date minutesAgoRoundedUp(int durationMinutes)
    {
        return new Date(System.currentTimeMillis() - ((durationMinutes - 1L) * 60_000L) - 1_000L);
    }

    private ParkingCustomer activeMember(String memberType)
    {
        ParkingCustomer customer = new ParkingCustomer();
        customer.setCustomerId(1L);
        customer.setIsMember("1");
        customer.setMemberType(memberType);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        customer.setMemberExpireTime(cal.getTime());
        return customer;
    }

    private ParkingSettingsDto.Pricing buildPricing(
        String tempHourPrice,
        int tempFreeMinutes,
        int tempBillingStepMinutes,
        boolean tempRoundUpEnabled,
        String tempDailyCapAmount)
    {
        ParkingSettingsDto.Pricing pricing = new ParkingSettingsDto.Pricing();
        pricing.setMonthlyPrice(new BigDecimal("420.00"));
        pricing.setTempHourPrice(new BigDecimal(tempHourPrice));
        pricing.setTempFreeMinutes(tempFreeMinutes);
        pricing.setTempBillingStepMinutes(tempBillingStepMinutes);
        pricing.setTempRoundUpEnabled(tempRoundUpEnabled);
        pricing.setTempDailyCapAmount(new BigDecimal(tempDailyCapAmount));
        return pricing;
    }

    private ParkingSettingsDto.MemberDiscount buildMemberDiscount(
        boolean enabled,
        String silverRate,
        String goldRate,
        String platinumRate)
    {
        ParkingSettingsDto.MemberDiscount memberDiscount = new ParkingSettingsDto.MemberDiscount();
        memberDiscount.setMemberDiscountEnabled(enabled);
        memberDiscount.setSilverRate(new BigDecimal(silverRate));
        memberDiscount.setGoldRate(new BigDecimal(goldRate));
        memberDiscount.setPlatinumRate(new BigDecimal(platinumRate));
        return memberDiscount;
    }
}
