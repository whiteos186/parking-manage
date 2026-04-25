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
import com.ruoyi.parking.controller.ParkingMonthlyOrderController;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingMonthlyOrderService;
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
@ContextConfiguration(classes = ParkingMonthlyOrderControllerTest.TestConfig.class)
class ParkingMonthlyOrderControllerTest
{
    @Autowired
    private ParkingMonthlyOrderController securedController;

    @Autowired
    private IParkingMonthlyOrderService securedMonthlyOrderService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedMonthlyOrderService);
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
        IParkingMonthlyOrderService service = Mockito.mock(IParkingMonthlyOrderService.class);
        ParkingMonthlyOrder order = new ParkingMonthlyOrder();
        order.setMonthlyOrderId(1L);
        order.setOrderNo("MO20240101000001");
        order.setPayStatus("0");
        order.setBizStatus("0");
        when(service.selectParkingMonthlyOrderList(any(ParkingMonthlyOrder.class))).thenReturn(List.of(order));

        setLoginUserPermissions("parking:monthly:list");
        ParkingMonthlyOrderController controller = new ParkingMonthlyOrderController(service, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/monthly/list")
                .param("orderNo", "MO2024")
                .param("payStatus", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].monthlyOrderId").value(1))
            .andExpect(jsonPath("$.rows[0].orderNo").value("MO20240101000001"))
            .andExpect(jsonPath("$.rows[0].payStatus").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingMonthlyOrder> captor = ArgumentCaptor.forClass(ParkingMonthlyOrder.class);
        verify(service).selectParkingMonthlyOrderList(captor.capture());
        assertEquals("MO2024", captor.getValue().getOrderNo());
        assertEquals("0", captor.getValue().getPayStatus());
    }

    @Test
    void getInfoEndpointReturnsOrderPayload() throws Exception
    {
        IParkingMonthlyOrderService service = Mockito.mock(IParkingMonthlyOrderService.class);
        ParkingMonthlyOrder order = new ParkingMonthlyOrder();
        order.setMonthlyOrderId(6L);
        order.setOrderNo("MO20240601000001");
        when(service.selectParkingMonthlyOrderById(6L)).thenReturn(order);

        ParkingMonthlyOrderController controller = new ParkingMonthlyOrderController(service, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/monthly/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.monthlyOrderId").value(6))
            .andExpect(jsonPath("$.data.orderNo").value("MO20240601000001"));

        verify(service).selectParkingMonthlyOrderById(6L);
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingMonthlyOrderService service = Mockito.mock(IParkingMonthlyOrderService.class);
        when(service.insertParkingMonthlyOrder(any(ParkingMonthlyOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:monthly:add");
        ParkingMonthlyOrderController controller = new ParkingMonthlyOrderController(service, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMonthlyOrder form = new ParkingMonthlyOrder();
        form.setLotId(1L);
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        form.setMonthCount(3);
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMonthlyOrder> captor = ArgumentCaptor.forClass(ParkingMonthlyOrder.class);
        verify(service).insertParkingMonthlyOrder(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingMonthlyOrderService service = Mockito.mock(IParkingMonthlyOrderService.class);
        when(service.updateParkingMonthlyOrder(any(ParkingMonthlyOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:monthly:edit");
        ParkingMonthlyOrderController controller = new ParkingMonthlyOrderController(service, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMonthlyOrder form = new ParkingMonthlyOrder();
        form.setMonthlyOrderId(10L);
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        form.setMonthCount(6);
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMonthlyOrder> captor = ArgumentCaptor.forClass(ParkingMonthlyOrder.class);
        verify(service).updateParkingMonthlyOrder(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingMonthlyOrderService service = Mockito.mock(IParkingMonthlyOrderService.class);
        when(service.deleteParkingMonthlyOrderByIds(new Long[] { 3L, 4L })).thenReturn(2);

        ParkingMonthlyOrderController controller = new ParkingMonthlyOrderController(service, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));
        AjaxResult result = controller.remove(new Long[] { 3L, 4L });

        assertEquals(200, result.get("code"));
        verify(service).deleteParkingMonthlyOrderByIds(new Long[] { 3L, 4L });
    }

    @Test
    void payEndpointDelegatesPayAndSetsUpdateBy()
    {
        IParkingMonthlyOrderService service = Mockito.mock(IParkingMonthlyOrderService.class);
        when(service.payMonthlyOrder(any(ParkingMonthlyOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:monthly:pay");
        ParkingMonthlyOrderController controller = new ParkingMonthlyOrderController(service, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMonthlyOrder form = new ParkingMonthlyOrder();
        form.setMonthlyOrderId(20L);
        AjaxResult result = controller.pay(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMonthlyOrder> captor = ArgumentCaptor.forClass(ParkingMonthlyOrder.class);
        verify(service).payMonthlyOrder(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
        assertEquals(20L, captor.getValue().getMonthlyOrderId());
    }

    @Test
    void cancelEndpointDelegatesCancelAndSetsUpdateBy()
    {
        IParkingMonthlyOrderService service = Mockito.mock(IParkingMonthlyOrderService.class);
        when(service.cancelMonthlyOrder(any(ParkingMonthlyOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:monthly:cancel");
        ParkingMonthlyOrderController controller = new ParkingMonthlyOrderController(service, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMonthlyOrder form = new ParkingMonthlyOrder();
        form.setMonthlyOrderId(21L);
        AjaxResult result = controller.cancel(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMonthlyOrder> captor = ArgumentCaptor.forClass(ParkingMonthlyOrder.class);
        verify(service).cancelMonthlyOrder(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
        assertEquals(21L, captor.getValue().getMonthlyOrderId());
    }

    @Test
    void listPermissionAllowsMonthlyListEndpoint()
    {
        setLoginUserPermissions("parking:monthly:list");
        when(securedMonthlyOrderService.selectParkingMonthlyOrderList(any(ParkingMonthlyOrder.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingMonthlyOrder());

        verify(securedMonthlyOrderService).selectParkingMonthlyOrderList(any(ParkingMonthlyOrder.class));
    }

    @Test
    void listPermissionAllowsMonthlyListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedMonthlyOrderService.selectParkingMonthlyOrderList(any(ParkingMonthlyOrder.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingMonthlyOrder());

        verify(securedMonthlyOrderService).selectParkingMonthlyOrderList(any(ParkingMonthlyOrder.class));
    }

    @Test
    void missingListPermissionDeniesMonthlyListEndpoint()
    {
        setLoginUserPermissions("parking:monthly:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingMonthlyOrder()));
        verify(securedMonthlyOrderService, never()).selectParkingMonthlyOrderList(any(ParkingMonthlyOrder.class));
    }

    @Test
    void getInfoPermissionAllowsQueryEndpoint()
    {
        setLoginUserPermissions("parking:monthly:query");
        when(securedMonthlyOrderService.selectParkingMonthlyOrderById(10L)).thenReturn(new ParkingMonthlyOrder());

        securedController.getInfo(10L);

        verify(securedMonthlyOrderService).selectParkingMonthlyOrderById(10L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:monthly:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(10L));
        verify(securedMonthlyOrderService, never()).selectParkingMonthlyOrderById(10L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:monthly:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingMonthlyOrder()));
        verify(securedMonthlyOrderService, never()).insertParkingMonthlyOrder(any(ParkingMonthlyOrder.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:monthly:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingMonthlyOrder()));
        verify(securedMonthlyOrderService, never()).updateParkingMonthlyOrder(any(ParkingMonthlyOrder.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:monthly:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedMonthlyOrderService, never()).deleteParkingMonthlyOrderByIds(any(Long[].class));
    }

    @Test
    void missingPayPermissionDeniesPayEndpoint()
    {
        setLoginUserPermissions("parking:monthly:list");

        assertThrows(AccessDeniedException.class, () -> securedController.pay(new ParkingMonthlyOrder()));
        verify(securedMonthlyOrderService, never()).payMonthlyOrder(any(ParkingMonthlyOrder.class));
    }

    @Test
    void missingCancelPermissionDeniesCancelEndpoint()
    {
        setLoginUserPermissions("parking:monthly:list");

        assertThrows(AccessDeniedException.class, () -> securedController.cancel(new ParkingMonthlyOrder()));
        verify(securedMonthlyOrderService, never()).cancelMonthlyOrder(any(ParkingMonthlyOrder.class));
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
        IParkingMonthlyOrderService parkingMonthlyOrderService()
        {
            return Mockito.mock(IParkingMonthlyOrderService.class);
        }

        @Bean
        ParkingLotAdminMapper parkingLotAdminMapper()
        {
            return Mockito.mock(ParkingLotAdminMapper.class);
        }

        @Bean
        ParkingCustomerMapper parkingCustomerMapper()
        {
            return Mockito.mock(ParkingCustomerMapper.class);
        }

        @Bean
        ParkingMonthlyOrderController parkingMonthlyOrderController(IParkingMonthlyOrderService parkingMonthlyOrderService,
                                                                    ParkingLotAdminMapper parkingLotAdminMapper,
                                                                    ParkingCustomerMapper parkingCustomerMapper)
        {
            return new ParkingMonthlyOrderController(parkingMonthlyOrderService, parkingLotAdminMapper, parkingCustomerMapper);
        }
    }
}
