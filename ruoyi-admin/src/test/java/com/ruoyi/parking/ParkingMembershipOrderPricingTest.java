package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingMembershipOrder;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingMembershipOrderMapper;
import com.ruoyi.parking.mapper.ParkingUserVehicleMapper;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import com.ruoyi.parking.service.impl.ParkingMembershipOrderServiceImpl;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkingMembershipOrderPricingTest
{
    private ParkingMembershipOrderMapper membershipOrderMapper;
    private ParkingLotMapper lotMapper;
    private ParkingCustomerMapper customerMapper;
    private ParkingUserVehicleMapper vehicleMapper;
    private IParkingPaymentRecordService paymentRecordService;
    private IParkingGlobalRuleService parkingGlobalRuleService;
    private ParkingMembershipOrderServiceImpl service;

    @BeforeEach
    void setUp()
    {
        membershipOrderMapper = mock(ParkingMembershipOrderMapper.class);
        lotMapper = mock(ParkingLotMapper.class);
        customerMapper = mock(ParkingCustomerMapper.class);
        vehicleMapper = mock(ParkingUserVehicleMapper.class);
        paymentRecordService = mock(IParkingPaymentRecordService.class);
        parkingGlobalRuleService = mock(IParkingGlobalRuleService.class);

        service = new ParkingMembershipOrderServiceImpl(
            membershipOrderMapper,
            lotMapper,
            customerMapper,
            vehicleMapper,
            paymentRecordService,
            parkingGlobalRuleService
        );

        when(membershipOrderMapper.insertParkingMembershipOrder(any(ParkingMembershipOrder.class))).thenReturn(1);
        when(membershipOrderMapper.updateParkingMembershipOrder(any(ParkingMembershipOrder.class))).thenReturn(1);
        when(parkingGlobalRuleService.getValidatedMemberDiscount())
            .thenReturn(buildMemberDiscount(true, "0.05", "0.10", "0.15"));
    }

    @Test
    void insertWithDefaultGoldMembershipRecalculatesAmountAndDiscount()
    {
        when(lotMapper.selectParkingLotById(1L)).thenReturn(new ParkingLot());
        when(customerMapper.selectParkingCustomerById(1L)).thenReturn(new ParkingCustomer());

        ParkingMembershipOrder order = new ParkingMembershipOrder();
        order.setLotId(1L);
        order.setCustomerId(1L);
        order.setMembershipType("2");
        order.setOriginalAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);

        service.insertParkingMembershipOrder(order);

        assertEquals(0, new BigDecimal("300.00").compareTo(order.getOriginalAmount()));
        assertEquals(0, new BigDecimal("30.00").compareTo(order.getDiscountAmount()));
        assertEquals(0, new BigDecimal("270.00").compareTo(order.getPayAmount()));
    }

    @Test
    void payWithLegacyZeroAmountRecalculatesAndCreatesPaymentRecord()
    {
        ParkingMembershipOrder existing = new ParkingMembershipOrder();
        existing.setMembershipOrderId(2L);
        existing.setOrderNo("PO202604260001");
        existing.setCustomerId(1L);
        existing.setLotId(1L);
        existing.setMembershipType("3");
        existing.setValidEndTime(buildFutureDate(365));
        existing.setPayStatus("0");
        existing.setBizStatus("0");
        existing.setOriginalAmount(BigDecimal.ZERO);
        existing.setDiscountAmount(BigDecimal.ZERO);
        existing.setPayAmount(BigDecimal.ZERO);
        when(membershipOrderMapper.selectParkingMembershipOrderById(2L)).thenReturn(existing);

        ParkingCustomer customer = new ParkingCustomer();
        customer.setCustomerId(1L);
        when(customerMapper.selectParkingCustomerById(1L)).thenReturn(customer);

        ParkingMembershipOrder form = new ParkingMembershipOrder();
        form.setMembershipOrderId(2L);
        form.setUpdateBy("tester");

        service.payMembershipOrder(form);

        assertEquals("1", existing.getPayStatus());
        assertEquals("1", existing.getBizStatus());
        assertEquals(0, new BigDecimal("600.00").compareTo(existing.getOriginalAmount()));
        assertEquals(0, new BigDecimal("90.00").compareTo(existing.getDiscountAmount()));
        assertEquals(0, new BigDecimal("510.00").compareTo(existing.getPayAmount()));
        verify(paymentRecordService).createPaymentForOrder(
            eq("PO202604260001"),
            eq("1"),
            eq(1L),
            eq(1L),
            eq(new BigDecimal("510.00")),
            isNull(),
            eq("tester")
        );
    }

    private Date buildFutureDate(int daysFromNow)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysFromNow);
        return cal.getTime();
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
