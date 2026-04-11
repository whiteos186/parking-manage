package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.ruoyi.parking.controller.ParkingTempOrderController;
import com.ruoyi.parking.domain.ParkingTempOrder;
import com.ruoyi.parking.service.IParkingTempOrderService;
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
@ContextConfiguration(classes = ParkingTempOrderControllerTest.TestConfig.class)
class ParkingTempOrderControllerTest
{
    @Autowired
    private ParkingTempOrderController securedController;

    @Autowired
    private IParkingTempOrderService securedParkingTempOrderService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedParkingTempOrderService);
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
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        ParkingTempOrder order = new ParkingTempOrder();
        order.setTempOrderId(1L);
        order.setVehiclePlateNo("粤A12345");
        order.setPayStatus("0");
        order.setBizStatus("0");
        when(service.selectParkingTempOrderList(any(ParkingTempOrder.class))).thenReturn(List.of(order));

        ParkingTempOrderController controller = new ParkingTempOrderController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/temp/list")
                .param("vehiclePlateNo", "粤A")
                .param("payStatus", "0")
                .param("bizStatus", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].tempOrderId").value(1))
            .andExpect(jsonPath("$.rows[0].vehiclePlateNo").value("粤A12345"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingTempOrder> captor = ArgumentCaptor.forClass(ParkingTempOrder.class);
        verify(service).selectParkingTempOrderList(captor.capture());
        assertEquals("粤A", captor.getValue().getVehiclePlateNo());
        assertEquals("0", captor.getValue().getPayStatus());
        assertEquals("0", captor.getValue().getBizStatus());
    }

    @Test
    void getInfoEndpointReturnsOrderPayload() throws Exception
    {
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        ParkingTempOrder order = new ParkingTempOrder();
        order.setTempOrderId(5L);
        order.setVehiclePlateNo("京B99999");
        when(service.selectParkingTempOrderById(5L)).thenReturn(order);

        ParkingTempOrderController controller = new ParkingTempOrderController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/temp/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.tempOrderId").value(5))
            .andExpect(jsonPath("$.data.vehiclePlateNo").value("京B99999"));

        verify(service).selectParkingTempOrderById(5L);
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        when(service.insertParkingTempOrder(any(ParkingTempOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:temp:add");
        ParkingTempOrderController controller = new ParkingTempOrderController(service);

        ParkingTempOrder form = new ParkingTempOrder();
        form.setLotId(10L);
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        form.setVehiclePlateNo("沪C11111");
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingTempOrder> captor = ArgumentCaptor.forClass(ParkingTempOrder.class);
        verify(service).insertParkingTempOrder(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        when(service.updateParkingTempOrder(any(ParkingTempOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:temp:edit");
        ParkingTempOrderController controller = new ParkingTempOrderController(service);

        ParkingTempOrder form = new ParkingTempOrder();
        form.setTempOrderId(20L);
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        form.setVehiclePlateNo("津D22222");
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingTempOrder> captor = ArgumentCaptor.forClass(ParkingTempOrder.class);
        verify(service).updateParkingTempOrder(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        when(service.deleteParkingTempOrderByIds(new Long[] { 7L, 8L })).thenReturn(2);

        ParkingTempOrderController controller = new ParkingTempOrderController(service);
        AjaxResult result = controller.remove(new Long[] { 7L, 8L });

        assertEquals(200, result.get("code"));
        verify(service).deleteParkingTempOrderByIds(new Long[] { 7L, 8L });
    }

    @Test
    void settleEndpointSetsUpdateByAndReturnsUpdatedOrder()
    {
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        ParkingTempOrder settled = new ParkingTempOrder();
        settled.setTempOrderId(30L);
        settled.setBizStatus("3");
        settled.setPayStatus("1");
        when(service.settleParkingTempOrder(any(ParkingTempOrder.class))).thenReturn(settled);

        setLoginUserPermissions("parking:temp:settle");
        ParkingTempOrderController controller = new ParkingTempOrderController(service);

        ParkingTempOrder form = new ParkingTempOrder();
        form.setTempOrderId(30L);
        AjaxResult result = controller.settle(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingTempOrder> captor = ArgumentCaptor.forClass(ParkingTempOrder.class);
        verify(service).settleParkingTempOrder(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
        ParkingTempOrder data = (ParkingTempOrder) result.get("data");
        assertNotNull(data);
        assertEquals("3", data.getBizStatus());
        assertEquals("1", data.getPayStatus());
    }

    @Test
    void entryEndpointDelegatesEntryAndReturnsSuccess()
    {
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        when(service.entryParkingTempOrder(40L)).thenReturn(1);

        setLoginUserPermissions("parking:temp:entry");
        ParkingTempOrderController controller = new ParkingTempOrderController(service);

        AjaxResult result = controller.entry(40L);

        assertEquals(200, result.get("code"));
        verify(service).entryParkingTempOrder(40L);
    }

    @Test
    void exitEndpointDelegatesExitAndReturnsSuccess()
    {
        IParkingTempOrderService service = Mockito.mock(IParkingTempOrderService.class);
        when(service.exitParkingTempOrder(50L)).thenReturn(1);

        setLoginUserPermissions("parking:temp:exit");
        ParkingTempOrderController controller = new ParkingTempOrderController(service);

        AjaxResult result = controller.exit(50L);

        assertEquals(200, result.get("code"));
        verify(service).exitParkingTempOrder(50L);
    }

    @Test
    void entryPermissionAllowsEntryEndpoint()
    {
        setLoginUserPermissions("parking:temp:entry");
        when(securedParkingTempOrderService.entryParkingTempOrder(1L)).thenReturn(1);

        securedController.entry(1L);

        verify(securedParkingTempOrderService).entryParkingTempOrder(1L);
    }

    @Test
    void missingEntryPermissionDeniesEntryEndpoint()
    {
        setLoginUserPermissions("parking:temp:edit");

        assertThrows(AccessDeniedException.class, () -> securedController.entry(1L));
        verify(securedParkingTempOrderService, never()).entryParkingTempOrder(1L);
    }

    @Test
    void exitPermissionAllowsExitEndpoint()
    {
        setLoginUserPermissions("parking:temp:exit");
        when(securedParkingTempOrderService.exitParkingTempOrder(1L)).thenReturn(1);

        securedController.exit(1L);

        verify(securedParkingTempOrderService).exitParkingTempOrder(1L);
    }

    @Test
    void missingExitPermissionDeniesExitEndpoint()
    {
        setLoginUserPermissions("parking:temp:settle");

        assertThrows(AccessDeniedException.class, () -> securedController.exit(1L));
        verify(securedParkingTempOrderService, never()).exitParkingTempOrder(1L);
    }

    // -------- permission allow/deny tests --------

    @Test
    void listPermissionAllowsTempListEndpoint()
    {
        setLoginUserPermissions("parking:temp:list");
        when(securedParkingTempOrderService.selectParkingTempOrderList(any(ParkingTempOrder.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingTempOrder());

        verify(securedParkingTempOrderService).selectParkingTempOrderList(any(ParkingTempOrder.class));
    }

    @Test
    void listPermissionAllowsTempListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedParkingTempOrderService.selectParkingTempOrderList(any(ParkingTempOrder.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingTempOrder());

        verify(securedParkingTempOrderService).selectParkingTempOrderList(any(ParkingTempOrder.class));
    }

    @Test
    void missingListPermissionDeniesTempListEndpoint()
    {
        setLoginUserPermissions("parking:temp:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingTempOrder()));
        verify(securedParkingTempOrderService, never()).selectParkingTempOrderList(any(ParkingTempOrder.class));
    }

    @Test
    void queryPermissionAllowsGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:temp:query");
        when(securedParkingTempOrderService.selectParkingTempOrderById(1L)).thenReturn(new ParkingTempOrder());

        securedController.getInfo(1L);

        verify(securedParkingTempOrderService).selectParkingTempOrderById(1L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:temp:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(1L));
        verify(securedParkingTempOrderService, never()).selectParkingTempOrderById(1L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:temp:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingTempOrder()));
        verify(securedParkingTempOrderService, never()).insertParkingTempOrder(any(ParkingTempOrder.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:temp:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingTempOrder()));
        verify(securedParkingTempOrderService, never()).updateParkingTempOrder(any(ParkingTempOrder.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:temp:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedParkingTempOrderService, never()).deleteParkingTempOrderByIds(any(Long[].class));
    }

    @Test
    void settlePermissionAllowsSettleEndpoint()
    {
        setLoginUserPermissions("parking:temp:settle");
        when(securedParkingTempOrderService.settleParkingTempOrder(any(ParkingTempOrder.class)))
            .thenReturn(new ParkingTempOrder());

        securedController.settle(new ParkingTempOrder());

        verify(securedParkingTempOrderService).settleParkingTempOrder(any(ParkingTempOrder.class));
    }

    @Test
    void missingSettlePermissionDeniesSettleEndpoint()
    {
        setLoginUserPermissions("parking:temp:edit");

        assertThrows(AccessDeniedException.class, () -> securedController.settle(new ParkingTempOrder()));
        verify(securedParkingTempOrderService, never()).settleParkingTempOrder(any(ParkingTempOrder.class));
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
        IParkingTempOrderService parkingTempOrderService()
        {
            return Mockito.mock(IParkingTempOrderService.class);
        }

        @Bean
        ParkingTempOrderController parkingTempOrderController(IParkingTempOrderService parkingTempOrderService)
        {
            return new ParkingTempOrderController(parkingTempOrderService);
        }
    }
}
