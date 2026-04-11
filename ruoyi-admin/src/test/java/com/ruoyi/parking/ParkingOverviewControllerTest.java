package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.PermissionService;
import com.ruoyi.parking.controller.ParkingOverviewController;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.service.IParkingOverviewService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringJUnitConfig
@ContextConfiguration(classes = ParkingOverviewControllerTest.TestConfig.class)
class ParkingOverviewControllerTest
{
    @Autowired
    private ParkingOverviewController securedController;

    @Autowired
    private IParkingOverviewService securedOverviewService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedOverviewService);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void statsEndpointReturnsSuccessPayloadWithBusinessMetrics() throws Exception
    {
        IParkingOverviewService overviewService = Mockito.mock(IParkingOverviewService.class);
        ParkingOverviewStats stats = new ParkingOverviewStats();
        stats.setLotCount(3L);
        stats.setTotalSpaceCount(100L);
        stats.setAvailableSpaceCount(60L);
        stats.setOccupiedSpaceCount(30L);
        stats.setDisabledSpaceCount(10L);
        stats.setLockedSpaceCount(5L);
        stats.setTodayEntryCount(42L);
        stats.setTodayExitCount(38L);
        stats.setTodayRevenue(new BigDecimal("1350.00"));
        stats.setActiveTempOrderCount(4L);
        when(overviewService.selectOverviewStats()).thenReturn(stats);

        ParkingOverviewController controller = new ParkingOverviewController(overviewService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/overview/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.lotCount").value(3))
            .andExpect(jsonPath("$.data.totalSpaceCount").value(100))
            .andExpect(jsonPath("$.data.availableSpaceCount").value(60))
            .andExpect(jsonPath("$.data.occupiedSpaceCount").value(30))
            .andExpect(jsonPath("$.data.disabledSpaceCount").value(10))
            .andExpect(jsonPath("$.data.lockedSpaceCount").value(5))
            .andExpect(jsonPath("$.data.todayEntryCount").value(42))
            .andExpect(jsonPath("$.data.todayExitCount").value(38))
            .andExpect(jsonPath("$.data.todayRevenue").value(1350.00))
            .andExpect(jsonPath("$.data.activeTempOrderCount").value(4));

        verify(overviewService).selectOverviewStats();
    }

    @Test
    void listPermissionAllowsStatsEndpoint()
    {
        setLoginUserPermissions("parking:overview:list");
        ParkingOverviewStats stats = new ParkingOverviewStats();
        stats.setLotCount(1L);
        when(securedOverviewService.selectOverviewStats()).thenReturn(stats);

        AjaxResult result = securedController.stats();

        assertEquals(200, result.get("code"));
        verify(securedOverviewService).selectOverviewStats();
    }

    @Test
    void missingListPermissionDeniesStatsEndpoint()
    {
        setLoginUserPermissions("parking:overview:query");

        assertThrows(AccessDeniedException.class, () -> securedController.stats());
        verify(securedOverviewService, never()).selectOverviewStats();
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
        IParkingOverviewService parkingOverviewService()
        {
            return Mockito.mock(IParkingOverviewService.class);
        }

        @Bean
        ParkingOverviewController parkingOverviewController(IParkingOverviewService parkingOverviewService)
        {
            return new ParkingOverviewController(parkingOverviewService);
        }
    }
}
