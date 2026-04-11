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
import com.ruoyi.parking.controller.ParkingLotController;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.service.IParkingLotService;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringJUnitConfig
@ContextConfiguration(classes = ParkingLotControllerTest.TestConfig.class)
class ParkingLotControllerTest
{
    @Autowired
    private ParkingLotController securedController;

    @Autowired
    private IParkingLotService securedParkingLotService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedParkingLotService);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void listEndpointReturnsTableDataAndBindsFilters() throws Exception
    {
        IParkingLotService parkingLotService = Mockito.mock(IParkingLotService.class);
        ParkingLot lot = new ParkingLot();
        lot.setLotId(1L);
        lot.setLotName("Lot-A");
        lot.setStatus("0");
        when(parkingLotService.selectParkingLotList(any(ParkingLot.class))).thenReturn(List.of(lot));

        ParkingLotController controller = new ParkingLotController(parkingLotService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lot/list")
                .param("lotName", "Lot")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].lotId").value(1))
            .andExpect(jsonPath("$.rows[0].lotName").value("Lot-A"))
            .andExpect(jsonPath("$.rows[0].status").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingLot> captor = ArgumentCaptor.forClass(ParkingLot.class);
        verify(parkingLotService).selectParkingLotList(captor.capture());
        assertEquals("Lot", captor.getValue().getLotName());
        assertEquals("0", captor.getValue().getStatus());
    }

    @Test
    void getInfoEndpointReturnsLotPayload() throws Exception
    {
        IParkingLotService parkingLotService = Mockito.mock(IParkingLotService.class);
        ParkingLot lot = new ParkingLot();
        lot.setLotId(6L);
        lot.setLotName("Lot-B");
        when(parkingLotService.selectParkingLotById(6L)).thenReturn(lot);

        ParkingLotController controller = new ParkingLotController(parkingLotService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lot/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.lotId").value(6))
            .andExpect(jsonPath("$.data.lotName").value("Lot-B"));

        verify(parkingLotService).selectParkingLotById(6L);
    }

    @Test
    void lotOptionsEndpointReturnsAllLotOptions() throws Exception
    {
        IParkingLotService parkingLotService = Mockito.mock(IParkingLotService.class);
        ParkingLot lot = new ParkingLot();
        lot.setLotId(88L);
        lot.setLotName("Lot-C");
        when(parkingLotService.selectParkingLotOptions()).thenReturn(List.of(lot));

        ParkingLotController controller = new ParkingLotController(parkingLotService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lot/options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].lotId").value(88))
            .andExpect(jsonPath("$.data[0].lotName").value("Lot-C"));

        verify(parkingLotService).selectParkingLotOptions();
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingLotService parkingLotService = Mockito.mock(IParkingLotService.class);
        when(parkingLotService.insertParkingLot(any(ParkingLot.class))).thenReturn(1);

        setLoginUserPermissions("parking:lot:add");
        ParkingLotController controller = new ParkingLotController(parkingLotService);

        ParkingLot form = new ParkingLot();
        form.setLotName("New Lot");
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingLot> captor = ArgumentCaptor.forClass(ParkingLot.class);
        verify(parkingLotService).insertParkingLot(captor.capture());
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingLotService parkingLotService = Mockito.mock(IParkingLotService.class);
        when(parkingLotService.updateParkingLot(any(ParkingLot.class))).thenReturn(1);

        setLoginUserPermissions("parking:lot:edit");
        ParkingLotController controller = new ParkingLotController(parkingLotService);

        ParkingLot form = new ParkingLot();
        form.setLotId(100L);
        form.setLotName("Updated Lot");
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingLot> captor = ArgumentCaptor.forClass(ParkingLot.class);
        verify(parkingLotService).updateParkingLot(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingLotService parkingLotService = Mockito.mock(IParkingLotService.class);
        when(parkingLotService.deleteParkingLotByIds(new Long[] { 3L, 4L })).thenReturn(2);

        ParkingLotController controller = new ParkingLotController(parkingLotService);
        AjaxResult result = controller.remove(new Long[] { 3L, 4L });

        assertEquals(200, result.get("code"));
        verify(parkingLotService).deleteParkingLotByIds(new Long[] { 3L, 4L });
    }

    @Test
    void listPermissionAllowsLotListEndpoint()
    {
        setLoginUserPermissions("parking:lot:list");
        when(securedParkingLotService.selectParkingLotList(any(ParkingLot.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingLot());

        verify(securedParkingLotService).selectParkingLotList(any(ParkingLot.class));
    }

    @Test
    void listPermissionAllowsLotListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedParkingLotService.selectParkingLotList(any(ParkingLot.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingLot());

        verify(securedParkingLotService).selectParkingLotList(any(ParkingLot.class));
    }

    @Test
    void missingListPermissionDeniesLotListEndpoint()
    {
        setLoginUserPermissions("parking:lot:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingLot()));
        verify(securedParkingLotService, never()).selectParkingLotList(any(ParkingLot.class));
    }

    @Test
    void optionsEndpointAllowsLotListPermission()
    {
        setLoginUserPermissions("parking:lot:list");
        when(securedParkingLotService.selectParkingLotOptions()).thenReturn(Collections.emptyList());

        securedController.options();

        verify(securedParkingLotService).selectParkingLotOptions();
    }

    @Test
    void optionsEndpointAllowsSpaceListPermission()
    {
        setLoginUserPermissions("parking:space:list");
        when(securedParkingLotService.selectParkingLotOptions()).thenReturn(Collections.emptyList());

        securedController.options();

        verify(securedParkingLotService).selectParkingLotOptions();
    }

    @Test
    void optionsEndpointAllowsOverviewListPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedParkingLotService.selectParkingLotOptions()).thenReturn(Collections.emptyList());

        securedController.options();

        verify(securedParkingLotService).selectParkingLotOptions();
    }

    @Test
    void getInfoPermissionAllowsQueryEndpoint()
    {
        setLoginUserPermissions("parking:lot:query");
        when(securedParkingLotService.selectParkingLotById(10L)).thenReturn(new ParkingLot());

        securedController.getInfo(10L);

        verify(securedParkingLotService).selectParkingLotById(10L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:lot:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(10L));
        verify(securedParkingLotService, never()).selectParkingLotById(10L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:lot:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingLot()));
        verify(securedParkingLotService, never()).insertParkingLot(any(ParkingLot.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:lot:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingLot()));
        verify(securedParkingLotService, never()).updateParkingLot(any(ParkingLot.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:lot:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedParkingLotService, never()).deleteParkingLotByIds(any(Long[].class));
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
        IParkingLotService parkingLotService()
        {
            return Mockito.mock(IParkingLotService.class);
        }

        @Bean
        ParkingLotController parkingLotController(IParkingLotService parkingLotService)
        {
            return new ParkingLotController(parkingLotService);
        }
    }
}
