package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.service.IParkingOverviewService;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockHttpServletRequest;
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
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
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
            .andExpect(jsonPath("$.data.disabledSpaceCount").value(10));

        verify(overviewService).selectOverviewStats();
    }

    @Test
    void lotListEndpointReturnsTableDataAndBindsFilters() throws Exception
    {
        IParkingOverviewService overviewService = Mockito.mock(IParkingOverviewService.class);
        ParkingLot lot = new ParkingLot();
        lot.setLotId(1L);
        lot.setLotName("Demo Lot");
        lot.setStatus("0");
        when(overviewService.selectParkingLotList(any(ParkingLot.class))).thenReturn(List.of(lot));

        ParkingOverviewController controller = new ParkingOverviewController(overviewService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lot/list")
                .param("lotName", "Demo")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].lotId").value(1))
            .andExpect(jsonPath("$.rows[0].lotName").value("Demo Lot"))
            .andExpect(jsonPath("$.rows[0].status").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingLot> captor = ArgumentCaptor.forClass(ParkingLot.class);
        verify(overviewService).selectParkingLotList(captor.capture());
        assertEquals("Demo", captor.getValue().getLotName());
        assertEquals("0", captor.getValue().getStatus());
    }

    @Test
    void lotOptionsEndpointReturnsAllLotOptionsPayload() throws Exception
    {
        IParkingOverviewService overviewService = Mockito.mock(IParkingOverviewService.class);
        ParkingLot lot = new ParkingLot();
        lot.setLotId(88L);
        lot.setLotName("North Campus Lot");
        when(overviewService.selectParkingLotOptions()).thenReturn(List.of(lot));

        ParkingOverviewController controller = new ParkingOverviewController(overviewService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lot/options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].lotId").value(88))
            .andExpect(jsonPath("$.data[0].lotName").value("North Campus Lot"));

        verify(overviewService).selectParkingLotOptions();
    }

    @Test
    void spaceListEndpointReturnsTableDataAndBindsFilters() throws Exception
    {
        IParkingOverviewService overviewService = Mockito.mock(IParkingOverviewService.class);
        ParkingSpace space = new ParkingSpace();
        space.setSpaceId(11L);
        space.setLotId(1L);
        space.setSpaceCode("A1-001");
        space.setStatus("0");
        when(overviewService.selectParkingSpaceList(any(ParkingSpace.class))).thenReturn(Collections.singletonList(space));

        ParkingOverviewController controller = new ParkingOverviewController(overviewService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/space/list")
                .param("lotId", "1")
                .param("spaceCode", "A1")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].spaceId").value(11))
            .andExpect(jsonPath("$.rows[0].lotId").value(1))
            .andExpect(jsonPath("$.rows[0].spaceCode").value("A1-001"))
            .andExpect(jsonPath("$.rows[0].status").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingSpace> captor = ArgumentCaptor.forClass(ParkingSpace.class);
        verify(overviewService).selectParkingSpaceList(captor.capture());
        assertEquals(1L, captor.getValue().getLotId());
        assertEquals("A1", captor.getValue().getSpaceCode());
        assertEquals("0", captor.getValue().getStatus());
    }

    @Test
    void listPermissionAllowsStatsEndpoint() throws Exception
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
    void listPermissionAllowsLotListEndpoint() throws Exception
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedOverviewService.selectParkingLotList(any(ParkingLot.class)))
            .thenReturn(Collections.emptyList());

        securedController.lotList(new ParkingLot());

        verify(securedOverviewService).selectParkingLotList(any(ParkingLot.class));
    }

    @Test
    void listPermissionAllowsLotOptionsEndpoint() throws Exception
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedOverviewService.selectParkingLotOptions()).thenReturn(Collections.emptyList());

        securedController.lotOptions();

        verify(securedOverviewService).selectParkingLotOptions();
    }

    @Test
    void listPermissionAllowsSpaceListEndpoint() throws Exception
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedOverviewService.selectParkingSpaceList(any(ParkingSpace.class)))
            .thenReturn(Collections.emptyList());

        securedController.spaceList(new ParkingSpace());

        verify(securedOverviewService).selectParkingSpaceList(any(ParkingSpace.class));
    }

    @Test
    void missingListPermissionDeniesStatsEndpoint()
    {
        setLoginUserPermissions("parking:overview:query");

        assertThrows(AccessDeniedException.class, () -> securedController.stats());
        verify(securedOverviewService, never()).selectOverviewStats();
    }

    @Test
    void missingListPermissionDeniesLotListEndpoint()
    {
        setLoginUserPermissions("parking:overview:query");

        assertThrows(AccessDeniedException.class, () -> securedController.lotList(new ParkingLot()));
        verify(securedOverviewService, never()).selectParkingLotList(any(ParkingLot.class));
    }

    @Test
    void missingListPermissionDeniesLotOptionsEndpoint()
    {
        setLoginUserPermissions("parking:overview:query");

        assertThrows(AccessDeniedException.class, () -> securedController.lotOptions());
        verify(securedOverviewService, never()).selectParkingLotOptions();
    }

    @Test
    void missingListPermissionDeniesSpaceListEndpoint()
    {
        setLoginUserPermissions("parking:overview:query");

        assertThrows(AccessDeniedException.class, () -> securedController.spaceList(new ParkingSpace()));
        verify(securedOverviewService, never()).selectParkingSpaceList(any(ParkingSpace.class));
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
