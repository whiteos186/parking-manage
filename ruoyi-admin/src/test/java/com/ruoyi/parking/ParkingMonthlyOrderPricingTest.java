package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingMonthlyOrderMapper;
import com.ruoyi.parking.mapper.ParkingUserVehicleMapper;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import com.ruoyi.parking.service.impl.ParkingMonthlyOrderServiceImpl;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkingMonthlyOrderPricingTest
{
    private ParkingMonthlyOrderMapper monthlyOrderMapper;
    private ParkingLotMapper lotMapper;
    private ParkingCustomerMapper customerMapper;
    private ParkingUserVehicleMapper vehicleMapper;
    private IParkingGlobalRuleService parkingGlobalRuleService;
    private ParkingMonthlyOrderServiceImpl service;

    @BeforeEach
    void setUp()
    {
        monthlyOrderMapper = mock(ParkingMonthlyOrderMapper.class);
        lotMapper = mock(ParkingLotMapper.class);
        customerMapper = mock(ParkingCustomerMapper.class);
        vehicleMapper = mock(ParkingUserVehicleMapper.class);
        parkingGlobalRuleService = mock(IParkingGlobalRuleService.class);
        IParkingPaymentRecordService paymentRecordService = mock(IParkingPaymentRecordService.class);

        service = new ParkingMonthlyOrderServiceImpl(
            monthlyOrderMapper,
            lotMapper,
            customerMapper,
            vehicleMapper,
            paymentRecordService,
            parkingGlobalRuleService
        );

        when(monthlyOrderMapper.updateParkingMonthlyOrder(any())).thenReturn(1);
        when(parkingGlobalRuleService.getValidatedPricing()).thenReturn(buildPricing("420.00"));
        when(parkingGlobalRuleService.getValidatedMemberDiscount()).thenReturn(buildMemberDiscount("0.05", "0.10", "0.15"));
    }

    @Test
    void updateWithChangedMonthCountRecalculatesPayAmountFromGlobalMonthlyPrice()
    {
        ParkingMonthlyOrder existing = buildExistingOrder(1L, 1L, 1, 1L, null, "380.00", "0.00", "380.00");
        when(monthlyOrderMapper.selectParkingMonthlyOrderById(1L)).thenReturn(existing);
        when(lotMapper.selectParkingLotById(1L)).thenReturn(new ParkingLot());

        ParkingCustomer customer = buildCustomer(1L, "0", null, null);
        when(customerMapper.selectParkingCustomerById(1L)).thenReturn(customer);

        ParkingMonthlyOrder update = new ParkingMonthlyOrder();
        update.setMonthlyOrderId(1L);
        update.setCustomerId(1L);
        update.setLotId(1L);
        update.setMonthCount(3);

        service.updateParkingMonthlyOrder(update);

        assertNotNull(update.getOriginalAmount());
        assertEquals(0, new BigDecimal("1260.00").compareTo(update.getOriginalAmount()));
        assertNotNull(update.getDiscountAmount());
        assertEquals(0, BigDecimal.ZERO.compareTo(update.getDiscountAmount()));
        assertNotNull(update.getPayAmount());
        assertEquals(0, new BigDecimal("1260.00").compareTo(update.getPayAmount()));
    }

    @Test
    void updateWithMemberCustomerAppliesConfiguredDiscount()
    {
        ParkingMonthlyOrder existing = buildExistingOrder(2L, 1L, 1, 1L, null, "380.00", "0.00", "380.00");
        when(monthlyOrderMapper.selectParkingMonthlyOrderById(2L)).thenReturn(existing);
        when(lotMapper.selectParkingLotById(1L)).thenReturn(new ParkingLot());

        Date futureExpire = buildFutureDate(365);
        ParkingCustomer memberCustomer = buildCustomer(2L, "1", "2", futureExpire);
        when(customerMapper.selectParkingCustomerById(2L)).thenReturn(memberCustomer);

        ParkingMonthlyOrder update = new ParkingMonthlyOrder();
        update.setMonthlyOrderId(2L);
        update.setCustomerId(2L);
        update.setLotId(1L);
        update.setMonthCount(1);

        service.updateParkingMonthlyOrder(update);

        assertNotNull(update.getOriginalAmount());
        assertEquals(0, new BigDecimal("420.00").compareTo(update.getOriginalAmount()));
        assertNotNull(update.getDiscountAmount());
        assertEquals(0, new BigDecimal("42.00").compareTo(update.getDiscountAmount()));
        assertNotNull(update.getPayAmount());
        assertEquals(0, new BigDecimal("378.00").compareTo(update.getPayAmount()));
    }

    @Test
    void updateWithOnlyRemarkChangedPreservesOriginalAmounts()
    {
        ParkingMonthlyOrder existing = buildExistingOrder(3L, 1L, 1, 1L, 1L, "380.00", "0.00", "300.00");
        when(monthlyOrderMapper.selectParkingMonthlyOrderById(3L)).thenReturn(existing);

        ParkingMonthlyOrder update = new ParkingMonthlyOrder();
        update.setMonthlyOrderId(3L);
        update.setCustomerId(1L);
        update.setLotId(1L);
        update.setMonthCount(1);
        update.setRemark("更新备注");

        service.updateParkingMonthlyOrder(update);

        assertEquals(null, update.getOriginalAmount());
        assertEquals(null, update.getDiscountAmount());
        assertEquals(null, update.getPayAmount());
    }

    private ParkingMonthlyOrder buildExistingOrder(
        Long orderId,
        Long lotId,
        Integer monthCount,
        Long customerId,
        Long vehicleId,
        String original,
        String discount,
        String pay)
    {
        ParkingMonthlyOrder order = new ParkingMonthlyOrder();
        order.setMonthlyOrderId(orderId);
        order.setLotId(lotId);
        order.setMonthCount(monthCount);
        order.setCustomerId(customerId);
        order.setVehicleId(vehicleId);
        order.setOriginalAmount(new BigDecimal(original));
        order.setDiscountAmount(new BigDecimal(discount));
        order.setPayAmount(new BigDecimal(pay));
        order.setPayStatus("0");
        order.setBizStatus("0");
        return order;
    }

    private ParkingCustomer buildCustomer(Long customerId, String isMember, String memberType, Date expireTime)
    {
        ParkingCustomer customer = new ParkingCustomer();
        customer.setCustomerId(customerId);
        customer.setIsMember(isMember);
        customer.setMemberType(memberType);
        customer.setMemberExpireTime(expireTime);
        return customer;
    }

    private Date buildFutureDate(int daysFromNow)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysFromNow);
        return cal.getTime();
    }

    private ParkingSettingsDto.Pricing buildPricing(String monthlyPrice)
    {
        ParkingSettingsDto.Pricing pricing = new ParkingSettingsDto.Pricing();
        pricing.setMonthlyPrice(new BigDecimal(monthlyPrice));
        pricing.setTempHourPrice(new BigDecimal("8.00"));
        pricing.setTempFreeMinutes(15);
        pricing.setTempBillingStepMinutes(30);
        pricing.setTempRoundUpEnabled(Boolean.TRUE);
        pricing.setTempDailyCapAmount(new BigDecimal("60.00"));
        return pricing;
    }

    private ParkingSettingsDto.MemberDiscount buildMemberDiscount(String silverRate, String goldRate, String platinumRate)
    {
        ParkingSettingsDto.MemberDiscount memberDiscount = new ParkingSettingsDto.MemberDiscount();
        memberDiscount.setMemberDiscountEnabled(Boolean.TRUE);
        memberDiscount.setSilverRate(new BigDecimal(silverRate));
        memberDiscount.setGoldRate(new BigDecimal(goldRate));
        memberDiscount.setPlatinumRate(new BigDecimal(platinumRate));
        return memberDiscount;
    }
}
