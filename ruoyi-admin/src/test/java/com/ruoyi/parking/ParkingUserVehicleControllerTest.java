package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import com.ruoyi.parking.controller.ParkingUserVehicleController;
import com.ruoyi.parking.domain.ParkingUserVehicle;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.service.IParkingUserVehicleService;
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
@ContextConfiguration(classes = ParkingUserVehicleControllerTest.TestConfig.class)
class ParkingUserVehicleControllerTest
{
    @Autowired
    private ParkingUserVehicleController securedController;

    @Autowired
    private IParkingUserVehicleService securedParkingUserVehicleService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedParkingUserVehicleService);
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
        IParkingUserVehicleService service = Mockito.mock(IParkingUserVehicleService.class);
        ParkingUserVehicle vehicle = new ParkingUserVehicle();
        vehicle.setVehicleId(1L);
        vehicle.setCustomerId(100L);
        vehicle.setPlateNo("粤A12345");
        vehicle.setVehicleType("1");
        vehicle.setStatus("0");
        when(service.selectParkingUserVehicleList(any(ParkingUserVehicle.class))).thenReturn(List.of(vehicle));

        setLoginUserPermissions("parking:vehicle:list");
        ParkingUserVehicleController controller = new ParkingUserVehicleController(service, Mockito.mock(ParkingCustomerMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/vehicle/list")
                .param("plateNo", "粤A")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].vehicleId").value(1))
            .andExpect(jsonPath("$.rows[0].plateNo").value("粤A12345"))
            .andExpect(jsonPath("$.rows[0].status").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingUserVehicle> captor = ArgumentCaptor.forClass(ParkingUserVehicle.class);
        verify(service).selectParkingUserVehicleList(captor.capture());
        assertEquals("粤A", captor.getValue().getPlateNo());
        assertEquals("0", captor.getValue().getStatus());
    }

    @Test
    void getInfoEndpointReturnsVehiclePayload() throws Exception
    {
        IParkingUserVehicleService service = Mockito.mock(IParkingUserVehicleService.class);
        ParkingUserVehicle vehicle = new ParkingUserVehicle();
        vehicle.setVehicleId(6L);
        vehicle.setPlateNo("京B99999");
        when(service.selectParkingUserVehicleById(6L)).thenReturn(vehicle);

        ParkingUserVehicleController controller = new ParkingUserVehicleController(service, Mockito.mock(ParkingCustomerMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/vehicle/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.vehicleId").value(6))
            .andExpect(jsonPath("$.data.plateNo").value("京B99999"));

        verify(service).selectParkingUserVehicleById(6L);
    }

    @Test
    void vehicleOptionsEndpointReturnsFilteredVehicleOptions() throws Exception
    {
        IParkingUserVehicleService service = Mockito.mock(IParkingUserVehicleService.class);
        ParkingUserVehicle vehicle = new ParkingUserVehicle();
        vehicle.setVehicleId(8L);
        vehicle.setCustomerId(100L);
        vehicle.setPlateNo("沪A10001");
        vehicle.setStatus("0");
        when(service.selectParkingUserVehicleOptions(100L)).thenReturn(List.of(vehicle));

        ParkingUserVehicleController controller = new ParkingUserVehicleController(service, Mockito.mock(ParkingCustomerMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/vehicle/options").param("customerId", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].vehicleId").value(8))
            .andExpect(jsonPath("$.data[0].customerId").value(100))
            .andExpect(jsonPath("$.data[0].plateNo").value("沪A10001"))
            .andExpect(jsonPath("$.data[0].status").value("0"));

        verify(service).selectParkingUserVehicleOptions(100L);
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingUserVehicleService service = Mockito.mock(IParkingUserVehicleService.class);
        when(service.insertParkingUserVehicle(any(ParkingUserVehicle.class))).thenReturn(1);

        setLoginUserPermissions("parking:vehicle:add");
        ParkingUserVehicleController controller = new ParkingUserVehicleController(service, Mockito.mock(ParkingCustomerMapper.class));

        ParkingUserVehicle form = new ParkingUserVehicle();
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        form.setPlateNo("沪C11111");
        form.setVehicleType("1");
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingUserVehicle> captor = ArgumentCaptor.forClass(ParkingUserVehicle.class);
        verify(service).insertParkingUserVehicle(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingUserVehicleService service = Mockito.mock(IParkingUserVehicleService.class);
        when(service.updateParkingUserVehicle(any(ParkingUserVehicle.class))).thenReturn(1);

        setLoginUserPermissions("parking:vehicle:edit");
        ParkingUserVehicleController controller = new ParkingUserVehicleController(service, Mockito.mock(ParkingCustomerMapper.class));

        ParkingUserVehicle form = new ParkingUserVehicle();
        form.setVehicleId(100L);
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        form.setPlateNo("粤B22222");
        form.setVehicleType("2");
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingUserVehicle> captor = ArgumentCaptor.forClass(ParkingUserVehicle.class);
        verify(service).updateParkingUserVehicle(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingUserVehicleService service = Mockito.mock(IParkingUserVehicleService.class);
        when(service.deleteParkingUserVehicleByIds(new Long[] { 3L, 4L })).thenReturn(2);

        ParkingUserVehicleController controller = new ParkingUserVehicleController(service, Mockito.mock(ParkingCustomerMapper.class));
        AjaxResult result = controller.remove(new Long[] { 3L, 4L });

        assertEquals(200, result.get("code"));
        verify(service).deleteParkingUserVehicleByIds(new Long[] { 3L, 4L });
    }

    @Test
    void setDefaultEndpointDelegatesSetDefault()
    {
        IParkingUserVehicleService service = Mockito.mock(IParkingUserVehicleService.class);
        doNothing().when(service).setDefaultVehicle(55L);

        setLoginUserPermissions("parking:vehicle:setDefault");
        ParkingUserVehicleController controller = new ParkingUserVehicleController(service, Mockito.mock(ParkingCustomerMapper.class));

        AjaxResult result = controller.setDefault(55L);

        assertEquals(200, result.get("code"));
        verify(service).setDefaultVehicle(55L);
    }

    @Test
    void listPermissionAllowsVehicleListEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:list");
        when(securedParkingUserVehicleService.selectParkingUserVehicleList(any(ParkingUserVehicle.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingUserVehicle());

        verify(securedParkingUserVehicleService).selectParkingUserVehicleList(any(ParkingUserVehicle.class));
    }

    @Test
    void listPermissionAllowsVehicleListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedParkingUserVehicleService.selectParkingUserVehicleList(any(ParkingUserVehicle.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingUserVehicle());

        verify(securedParkingUserVehicleService).selectParkingUserVehicleList(any(ParkingUserVehicle.class));
    }

    @Test
    void optionsEndpointAllowsVehicleListPermission()
    {
        setLoginUserPermissions("parking:vehicle:list");
        when(securedParkingUserVehicleService.selectParkingUserVehicleOptions(100L)).thenReturn(Collections.emptyList());

        securedController.options(100L);

        verify(securedParkingUserVehicleService).selectParkingUserVehicleOptions(100L);
    }

    @Test
    void optionsEndpointAllowsMonthlyListPermission()
    {
        setLoginUserPermissions("parking:monthly:list");
        when(securedParkingUserVehicleService.selectParkingUserVehicleOptions(100L)).thenReturn(Collections.emptyList());

        securedController.options(100L);

        verify(securedParkingUserVehicleService).selectParkingUserVehicleOptions(100L);
    }

    @Test
    void optionsEndpointAllowsMembershipListPermission()
    {
        setLoginUserPermissions("parking:membership:list");
        when(securedParkingUserVehicleService.selectParkingUserVehicleOptions(100L)).thenReturn(Collections.emptyList());

        securedController.options(100L);

        verify(securedParkingUserVehicleService).selectParkingUserVehicleOptions(100L);
    }

    @Test
    void missingListPermissionDeniesVehicleListEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingUserVehicle()));
        verify(securedParkingUserVehicleService, never()).selectParkingUserVehicleList(any(ParkingUserVehicle.class));
    }

    @Test
    void missingOptionsPermissionDeniesVehicleOptionsEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:query");

        assertThrows(AccessDeniedException.class, () -> securedController.options(100L));
        verify(securedParkingUserVehicleService, never()).selectParkingUserVehicleOptions(any(Long.class));
    }

    @Test
    void getInfoPermissionAllowsQueryEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:query");
        when(securedParkingUserVehicleService.selectParkingUserVehicleById(10L)).thenReturn(new ParkingUserVehicle());

        securedController.getInfo(10L);

        verify(securedParkingUserVehicleService).selectParkingUserVehicleById(10L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(10L));
        verify(securedParkingUserVehicleService, never()).selectParkingUserVehicleById(10L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingUserVehicle()));
        verify(securedParkingUserVehicleService, never()).insertParkingUserVehicle(any(ParkingUserVehicle.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingUserVehicle()));
        verify(securedParkingUserVehicleService, never()).updateParkingUserVehicle(any(ParkingUserVehicle.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedParkingUserVehicleService, never()).deleteParkingUserVehicleByIds(any(Long[].class));
    }

    @Test
    void missingSetDefaultPermissionDeniesSetDefaultEndpoint()
    {
        setLoginUserPermissions("parking:vehicle:edit");

        assertThrows(AccessDeniedException.class, () -> securedController.setDefault(1L));
        verify(securedParkingUserVehicleService, never()).setDefaultVehicle(any(Long.class));
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
        IParkingUserVehicleService parkingUserVehicleService()
        {
            return Mockito.mock(IParkingUserVehicleService.class);
        }

        @Bean
        ParkingCustomerMapper parkingCustomerMapper()
        {
            return Mockito.mock(ParkingCustomerMapper.class);
        }

        @Bean
        ParkingUserVehicleController parkingUserVehicleController(IParkingUserVehicleService parkingUserVehicleService,
                                                                   ParkingCustomerMapper parkingCustomerMapper)
        {
            return new ParkingUserVehicleController(parkingUserVehicleService, parkingCustomerMapper);
        }
    }
}
