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
import com.ruoyi.parking.controller.ParkingLotAdminController;
import com.ruoyi.parking.domain.ParkingLotAdmin;
import com.ruoyi.parking.service.IParkingLotAdminService;
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
@ContextConfiguration(classes = ParkingLotAdminControllerTest.TestConfig.class)
class ParkingLotAdminControllerTest
{
    @Autowired
    private ParkingLotAdminController securedController;

    @Autowired
    private IParkingLotAdminService securedParkingLotAdminService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedParkingLotAdminService);
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
        IParkingLotAdminService parkingLotAdminService = Mockito.mock(IParkingLotAdminService.class);
        ParkingLotAdmin record = new ParkingLotAdmin();
        record.setLotAdminId(1L);
        record.setLotId(10L);
        record.setUserId(100L);
        record.setLotName("Lot-A");
        record.setNickName("Admin User");
        record.setStatus("0");
        when(parkingLotAdminService.selectParkingLotAdminList(any(ParkingLotAdmin.class))).thenReturn(List.of(record));

        ParkingLotAdminController controller = new ParkingLotAdminController(parkingLotAdminService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lotadmin/list")
                .param("lotId", "10")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].lotAdminId").value(1))
            .andExpect(jsonPath("$.rows[0].lotId").value(10))
            .andExpect(jsonPath("$.rows[0].userId").value(100))
            .andExpect(jsonPath("$.rows[0].lotName").value("Lot-A"))
            .andExpect(jsonPath("$.rows[0].status").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingLotAdmin> captor = ArgumentCaptor.forClass(ParkingLotAdmin.class);
        verify(parkingLotAdminService).selectParkingLotAdminList(captor.capture());
        assertEquals(10L, captor.getValue().getLotId());
        assertEquals("0", captor.getValue().getStatus());
    }

    @Test
    void getInfoEndpointReturnsLotAdminPayload() throws Exception
    {
        IParkingLotAdminService parkingLotAdminService = Mockito.mock(IParkingLotAdminService.class);
        ParkingLotAdmin record = new ParkingLotAdmin();
        record.setLotAdminId(6L);
        record.setLotId(20L);
        record.setUserId(200L);
        record.setLotName("Lot-B");
        when(parkingLotAdminService.selectParkingLotAdminById(6L)).thenReturn(record);

        ParkingLotAdminController controller = new ParkingLotAdminController(parkingLotAdminService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/lotadmin/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.lotAdminId").value(6))
            .andExpect(jsonPath("$.data.lotId").value(20))
            .andExpect(jsonPath("$.data.lotName").value("Lot-B"));

        verify(parkingLotAdminService).selectParkingLotAdminById(6L);
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingLotAdminService parkingLotAdminService = Mockito.mock(IParkingLotAdminService.class);
        when(parkingLotAdminService.insertParkingLotAdmin(any(ParkingLotAdmin.class))).thenReturn(1);

        setLoginUserPermissions("parking:lotadmin:add");
        ParkingLotAdminController controller = new ParkingLotAdminController(parkingLotAdminService);

        ParkingLotAdmin form = new ParkingLotAdmin();
        form.setLotId(10L);
        form.setUserId(100L);
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingLotAdmin> captor = ArgumentCaptor.forClass(ParkingLotAdmin.class);
        verify(parkingLotAdminService).insertParkingLotAdmin(captor.capture());
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingLotAdminService parkingLotAdminService = Mockito.mock(IParkingLotAdminService.class);
        when(parkingLotAdminService.updateParkingLotAdmin(any(ParkingLotAdmin.class))).thenReturn(1);

        setLoginUserPermissions("parking:lotadmin:edit");
        ParkingLotAdminController controller = new ParkingLotAdminController(parkingLotAdminService);

        ParkingLotAdmin form = new ParkingLotAdmin();
        form.setLotAdminId(5L);
        form.setLotId(10L);
        form.setUserId(100L);
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingLotAdmin> captor = ArgumentCaptor.forClass(ParkingLotAdmin.class);
        verify(parkingLotAdminService).updateParkingLotAdmin(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingLotAdminService parkingLotAdminService = Mockito.mock(IParkingLotAdminService.class);
        when(parkingLotAdminService.deleteParkingLotAdminByIds(new Long[] { 3L, 4L })).thenReturn(2);

        ParkingLotAdminController controller = new ParkingLotAdminController(parkingLotAdminService);
        AjaxResult result = controller.remove(new Long[] { 3L, 4L });

        assertEquals(200, result.get("code"));
        verify(parkingLotAdminService).deleteParkingLotAdminByIds(new Long[] { 3L, 4L });
    }

    @Test
    void listPermissionAllowsLotAdminListEndpoint()
    {
        setLoginUserPermissions("parking:lotadmin:list");
        when(securedParkingLotAdminService.selectParkingLotAdminList(any(ParkingLotAdmin.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingLotAdmin());

        verify(securedParkingLotAdminService).selectParkingLotAdminList(any(ParkingLotAdmin.class));
    }

    @Test
    void listPermissionAllowsLotAdminListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedParkingLotAdminService.selectParkingLotAdminList(any(ParkingLotAdmin.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingLotAdmin());

        verify(securedParkingLotAdminService).selectParkingLotAdminList(any(ParkingLotAdmin.class));
    }

    @Test
    void missingListPermissionDeniesLotAdminListEndpoint()
    {
        setLoginUserPermissions("parking:lotadmin:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingLotAdmin()));
        verify(securedParkingLotAdminService, never()).selectParkingLotAdminList(any(ParkingLotAdmin.class));
    }

    @Test
    void getInfoPermissionAllowsQueryEndpoint()
    {
        setLoginUserPermissions("parking:lotadmin:query");
        when(securedParkingLotAdminService.selectParkingLotAdminById(10L)).thenReturn(new ParkingLotAdmin());

        securedController.getInfo(10L);

        verify(securedParkingLotAdminService).selectParkingLotAdminById(10L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:lotadmin:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(10L));
        verify(securedParkingLotAdminService, never()).selectParkingLotAdminById(10L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:lotadmin:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingLotAdmin()));
        verify(securedParkingLotAdminService, never()).insertParkingLotAdmin(any(ParkingLotAdmin.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:lotadmin:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingLotAdmin()));
        verify(securedParkingLotAdminService, never()).updateParkingLotAdmin(any(ParkingLotAdmin.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:lotadmin:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedParkingLotAdminService, never()).deleteParkingLotAdminByIds(any(Long[].class));
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
        IParkingLotAdminService parkingLotAdminService()
        {
            return Mockito.mock(IParkingLotAdminService.class);
        }

        @Bean
        ParkingLotAdminController parkingLotAdminController(IParkingLotAdminService parkingLotAdminService)
        {
            return new ParkingLotAdminController(parkingLotAdminService);
        }
    }
}
