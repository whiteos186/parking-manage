package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.PermissionService;
import com.ruoyi.parking.controller.ParkingSettingsController;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.domain.dto.ParkingSettingsUpdateDto;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringJUnitConfig
@ContextConfiguration(classes = ParkingSettingsControllerTest.TestConfig.class)
class ParkingSettingsControllerTest
{
    @Autowired
    private ParkingSettingsController controller;

    @Autowired
    private IParkingGlobalRuleService parkingGlobalRuleService;

    @BeforeEach
    void setUp()
    {
        reset(parkingGlobalRuleService);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getSettingsReturnsPayload()
    {
        setLoginUserPermissions("parking:settings:query");
        ParkingSettingsDto dto = new ParkingSettingsDto();
        ParkingSettingsDto.Pricing pricing = new ParkingSettingsDto.Pricing();
        pricing.setMonthlyPrice(new BigDecimal("380.00"));
        dto.setPricing(pricing);
        when(parkingGlobalRuleService.getSettings()).thenReturn(dto);

        AjaxResult result = controller.getSettings();

        assertEquals(200, result.get("code"));
        verify(parkingGlobalRuleService).getSettings();
    }

    @Test
    void updateSettingsDelegatesWithOperator()
    {
        setLoginUserPermissions("parking:settings:edit");
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
        updateDto.setDefaultPaymentChannel("1");
        updateDto.setCashEnabled(Boolean.TRUE);
        updateDto.setCancelAutoRefundEnabled(Boolean.FALSE);

        AjaxResult result = controller.updateSettings(updateDto);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingSettingsUpdateDto> captor = ArgumentCaptor.forClass(ParkingSettingsUpdateDto.class);
        verify(parkingGlobalRuleService).updateSettings(captor.capture(), org.mockito.Mockito.eq("tester"));
        assertEquals(new BigDecimal("420.00"), captor.getValue().getMonthlyPrice());
    }

    @Test
    void missingQueryPermissionDeniesGetSettings()
    {
        setLoginUserPermissions("parking:settings:list");

        assertThrows(AccessDeniedException.class, () -> controller.getSettings());
        verify(parkingGlobalRuleService, never()).getSettings();
    }

    @Test
    void missingEditPermissionDeniesUpdateSettings()
    {
        setLoginUserPermissions("parking:settings:query");

        assertThrows(AccessDeniedException.class, () -> controller.updateSettings(new ParkingSettingsUpdateDto()));
        verify(parkingGlobalRuleService, never()).updateSettings(any(), any());
    }

    private void setLoginUserPermissions(String... permissions)
    {
        SysUser user = new SysUser();
        user.setUserName("tester");
        user.setPassword("N/A");

        Set<String> permissionSet = new LinkedHashSet<>(Arrays.asList(permissions));
        LoginUser loginUser = new LoginUser(1L, 1L, user, permissionSet);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            loginUser,
            null,
            loginUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Configuration
    @EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
    static class TestConfig
    {
        @Bean("ss")
        PermissionService permissionService()
        {
            return new PermissionService();
        }

        @Bean
        IParkingGlobalRuleService parkingGlobalRuleService()
        {
            return Mockito.mock(IParkingGlobalRuleService.class);
        }

        @Bean
        ParkingSettingsController parkingSettingsController(IParkingGlobalRuleService parkingGlobalRuleService)
        {
            return new ParkingSettingsController(parkingGlobalRuleService);
        }
    }
}
