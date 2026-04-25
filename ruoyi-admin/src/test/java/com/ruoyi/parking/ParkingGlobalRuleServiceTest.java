package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.domain.dto.ParkingSettingsUpdateDto;
import com.ruoyi.parking.service.impl.ParkingGlobalRuleServiceImpl;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDictTypeService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ParkingGlobalRuleServiceTest
{
    private ISysConfigService sysConfigService;
    private ISysDictTypeService sysDictTypeService;
    private ParkingGlobalRuleServiceImpl service;

    @BeforeEach
    void setUp()
    {
        sysConfigService = mock(ISysConfigService.class);
        sysDictTypeService = mock(ISysDictTypeService.class);
        service = new ParkingGlobalRuleServiceImpl(sysConfigService, sysDictTypeService);
    }

    @Test
    void getSettingsReturnsTypedParkingRules()
    {
        when(sysConfigService.selectConfigByKey("parking.rule.monthlyPrice")).thenReturn("380.00");
        when(sysConfigService.selectConfigByKey("parking.rule.tempHourPrice")).thenReturn("8.00");
        when(sysConfigService.selectConfigByKey("parking.rule.tempFreeMinutes")).thenReturn("15");
        when(sysConfigService.selectConfigByKey("parking.rule.tempBillingStepMinutes")).thenReturn("30");
        when(sysConfigService.selectConfigByKey("parking.rule.tempRoundUpEnabled")).thenReturn("true");
        when(sysConfigService.selectConfigByKey("parking.rule.tempDailyCapAmount")).thenReturn("60.00");
        when(sysConfigService.selectConfigByKey("parking.rule.memberDiscountEnabled")).thenReturn("true");
        when(sysConfigService.selectConfigByKey("parking.rule.memberDiscount.silver")).thenReturn("0.05");
        when(sysConfigService.selectConfigByKey("parking.rule.memberDiscount.gold")).thenReturn("0.10");
        when(sysConfigService.selectConfigByKey("parking.rule.memberDiscount.platinum")).thenReturn("0.15");
        when(sysConfigService.selectConfigByKey("parking.rule.payment.defaultChannel")).thenReturn("1");
        when(sysConfigService.selectConfigByKey("parking.rule.payment.cashEnabled")).thenReturn("true");
        when(sysConfigService.selectConfigByKey("parking.rule.order.cancelAutoRefundEnabled")).thenReturn("false");

        ParkingSettingsDto settings = service.getSettings();

        assertEquals(new BigDecimal("380.00"), settings.getPricing().getMonthlyPrice());
        assertEquals(new BigDecimal("8.00"), settings.getPricing().getTempHourPrice());
        assertEquals(Integer.valueOf(15), settings.getPricing().getTempFreeMinutes());
        assertEquals(Integer.valueOf(30), settings.getPricing().getTempBillingStepMinutes());
        assertEquals(Boolean.TRUE, settings.getPricing().getTempRoundUpEnabled());
        assertEquals(new BigDecimal("60.00"), settings.getPricing().getTempDailyCapAmount());
        assertEquals(new BigDecimal("0.10"), settings.getMemberDiscount().getGoldRate());
        assertEquals("1", settings.getPayment().getDefaultChannel());
        assertEquals(Boolean.TRUE, settings.getPayment().getCashEnabled());
        assertEquals(Boolean.FALSE, settings.getSwitches().getCancelAutoRefundEnabled());
    }

    @Test
    void updateSettingsPersistsConfigValues()
    {
        ParkingSettingsUpdateDto updateDto = new ParkingSettingsUpdateDto();
        updateDto.setMonthlyPrice(new BigDecimal("420.00"));
        updateDto.setTempHourPrice(new BigDecimal("10.00"));
        updateDto.setTempFreeMinutes(20);
        updateDto.setTempBillingStepMinutes(30);
        updateDto.setTempRoundUpEnabled(Boolean.TRUE);
        updateDto.setTempDailyCapAmount(new BigDecimal("88.00"));
        updateDto.setMemberDiscountEnabled(Boolean.TRUE);
        updateDto.setSilverDiscountRate(new BigDecimal("0.05"));
        updateDto.setGoldDiscountRate(new BigDecimal("0.10"));
        updateDto.setPlatinumDiscountRate(new BigDecimal("0.15"));
        updateDto.setDefaultPaymentChannel("1");
        updateDto.setCashEnabled(Boolean.TRUE);
        updateDto.setCancelAutoRefundEnabled(Boolean.FALSE);

        SysDictData channel = new SysDictData();
        channel.setDictValue("1");
        when(sysDictTypeService.selectDictDataByType("parking_payment_channel")).thenReturn(List.of(channel));
        when(sysConfigService.selectConfigList(any(SysConfig.class))).thenReturn(List.of());

        service.updateSettings(updateDto, "tester");

        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(sysConfigService, org.mockito.Mockito.atLeastOnce()).insertConfig(captor.capture());
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void updateSettingsRejectsUnknownDefaultPaymentChannel()
    {
        ParkingSettingsUpdateDto updateDto = new ParkingSettingsUpdateDto();
        updateDto.setMonthlyPrice(new BigDecimal("420.00"));
        updateDto.setTempHourPrice(new BigDecimal("10.00"));
        updateDto.setTempFreeMinutes(0);
        updateDto.setTempBillingStepMinutes(30);
        updateDto.setTempRoundUpEnabled(Boolean.TRUE);
        updateDto.setTempDailyCapAmount(new BigDecimal("0.00"));
        updateDto.setMemberDiscountEnabled(Boolean.TRUE);
        updateDto.setSilverDiscountRate(new BigDecimal("0.05"));
        updateDto.setGoldDiscountRate(new BigDecimal("0.10"));
        updateDto.setPlatinumDiscountRate(new BigDecimal("0.15"));
        updateDto.setDefaultPaymentChannel("9");
        updateDto.setCashEnabled(Boolean.TRUE);
        updateDto.setCancelAutoRefundEnabled(Boolean.FALSE);

        when(sysDictTypeService.selectDictDataByType("parking_payment_channel")).thenReturn(List.of());

        assertThrows(ServiceException.class, () -> service.updateSettings(updateDto, "tester"));
    }
}
