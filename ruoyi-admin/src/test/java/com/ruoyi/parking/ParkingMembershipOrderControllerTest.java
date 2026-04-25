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
import com.ruoyi.parking.controller.ParkingMembershipOrderController;
import com.ruoyi.parking.domain.ParkingMembershipOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingMembershipOrderService;
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
@ContextConfiguration(classes = ParkingMembershipOrderControllerTest.TestConfig.class)
class ParkingMembershipOrderControllerTest
{
    @Autowired
    private ParkingMembershipOrderController securedController;

    @Autowired
    private IParkingMembershipOrderService securedMembershipOrderService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedMembershipOrderService);
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
        IParkingMembershipOrderService membershipOrderService = Mockito.mock(IParkingMembershipOrderService.class);
        ParkingMembershipOrder order = new ParkingMembershipOrder();
        order.setMembershipOrderId(1L);
        order.setOrderNo("PO20240101120000001");
        order.setPayStatus("0");
        order.setBizStatus("0");
        when(membershipOrderService.selectParkingMembershipOrderList(any(ParkingMembershipOrder.class)))
            .thenReturn(List.of(order));

        setLoginUserPermissions("parking:membership:list");
        ParkingMembershipOrderController controller = new ParkingMembershipOrderController(membershipOrderService, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/membership/list")
                .param("orderNo", "PO2024")
                .param("payStatus", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].membershipOrderId").value(1))
            .andExpect(jsonPath("$.rows[0].orderNo").value("PO20240101120000001"))
            .andExpect(jsonPath("$.rows[0].payStatus").value("0"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingMembershipOrder> captor = ArgumentCaptor.forClass(ParkingMembershipOrder.class);
        verify(membershipOrderService).selectParkingMembershipOrderList(captor.capture());
        assertEquals("PO2024", captor.getValue().getOrderNo());
        assertEquals("0", captor.getValue().getPayStatus());
    }

    @Test
    void getInfoEndpointReturnsOrderPayload() throws Exception
    {
        IParkingMembershipOrderService membershipOrderService = Mockito.mock(IParkingMembershipOrderService.class);
        ParkingMembershipOrder order = new ParkingMembershipOrder();
        order.setMembershipOrderId(6L);
        order.setOrderNo("PO20240601000001");
        when(membershipOrderService.selectParkingMembershipOrderById(6L)).thenReturn(order);

        ParkingMembershipOrderController controller = new ParkingMembershipOrderController(membershipOrderService, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/membership/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.membershipOrderId").value(6))
            .andExpect(jsonPath("$.data.orderNo").value("PO20240601000001"));

        verify(membershipOrderService).selectParkingMembershipOrderById(6L);
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingMembershipOrderService membershipOrderService = Mockito.mock(IParkingMembershipOrderService.class);
        when(membershipOrderService.insertParkingMembershipOrder(any(ParkingMembershipOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:membership:add");
        ParkingMembershipOrderController controller = new ParkingMembershipOrderController(membershipOrderService, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMembershipOrder form = new ParkingMembershipOrder();
        form.setLotId(1L);
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMembershipOrder> captor = ArgumentCaptor.forClass(ParkingMembershipOrder.class);
        verify(membershipOrderService).insertParkingMembershipOrder(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingMembershipOrderService membershipOrderService = Mockito.mock(IParkingMembershipOrderService.class);
        when(membershipOrderService.updateParkingMembershipOrder(any(ParkingMembershipOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:membership:edit");
        ParkingMembershipOrderController controller = new ParkingMembershipOrderController(membershipOrderService, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMembershipOrder form = new ParkingMembershipOrder();
        form.setMembershipOrderId(100L);
        form.setLotId(1L);
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMembershipOrder> captor = ArgumentCaptor.forClass(ParkingMembershipOrder.class);
        verify(membershipOrderService).updateParkingMembershipOrder(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingMembershipOrderService membershipOrderService = Mockito.mock(IParkingMembershipOrderService.class);
        when(membershipOrderService.deleteParkingMembershipOrderByIds(new Long[] { 3L, 4L })).thenReturn(2);

        ParkingMembershipOrderController controller = new ParkingMembershipOrderController(membershipOrderService, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));
        AjaxResult result = controller.remove(new Long[] { 3L, 4L });

        assertEquals(200, result.get("code"));
        verify(membershipOrderService).deleteParkingMembershipOrderByIds(new Long[] { 3L, 4L });
    }

    @Test
    void payEndpointSetsUpdateByAndDelegatesPay()
    {
        IParkingMembershipOrderService membershipOrderService = Mockito.mock(IParkingMembershipOrderService.class);
        when(membershipOrderService.payMembershipOrder(any(ParkingMembershipOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:membership:pay");
        ParkingMembershipOrderController controller = new ParkingMembershipOrderController(membershipOrderService, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMembershipOrder form = new ParkingMembershipOrder();
        form.setMembershipOrderId(50L);
        AjaxResult result = controller.pay(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMembershipOrder> captor = ArgumentCaptor.forClass(ParkingMembershipOrder.class);
        verify(membershipOrderService).payMembershipOrder(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void cancelEndpointSetsUpdateByAndDelegatesCancel()
    {
        IParkingMembershipOrderService membershipOrderService = Mockito.mock(IParkingMembershipOrderService.class);
        when(membershipOrderService.cancelMembershipOrder(any(ParkingMembershipOrder.class))).thenReturn(1);

        setLoginUserPermissions("parking:membership:cancel");
        ParkingMembershipOrderController controller = new ParkingMembershipOrderController(membershipOrderService, Mockito.mock(ParkingLotAdminMapper.class), Mockito.mock(ParkingCustomerMapper.class));

        ParkingMembershipOrder form = new ParkingMembershipOrder();
        form.setMembershipOrderId(51L);
        AjaxResult result = controller.cancel(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingMembershipOrder> captor = ArgumentCaptor.forClass(ParkingMembershipOrder.class);
        verify(membershipOrderService).cancelMembershipOrder(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void listPermissionAllowsMembershipListEndpoint()
    {
        setLoginUserPermissions("parking:membership:list");
        when(securedMembershipOrderService.selectParkingMembershipOrderList(any(ParkingMembershipOrder.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingMembershipOrder());

        verify(securedMembershipOrderService).selectParkingMembershipOrderList(any(ParkingMembershipOrder.class));
    }

    @Test
    void listPermissionAllowsMembershipListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedMembershipOrderService.selectParkingMembershipOrderList(any(ParkingMembershipOrder.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingMembershipOrder());

        verify(securedMembershipOrderService).selectParkingMembershipOrderList(any(ParkingMembershipOrder.class));
    }

    @Test
    void missingListPermissionDeniesMembershipListEndpoint()
    {
        setLoginUserPermissions("parking:membership:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingMembershipOrder()));
        verify(securedMembershipOrderService, never()).selectParkingMembershipOrderList(any(ParkingMembershipOrder.class));
    }

    @Test
    void getInfoPermissionAllowsQueryEndpoint()
    {
        setLoginUserPermissions("parking:membership:query");
        when(securedMembershipOrderService.selectParkingMembershipOrderById(10L))
            .thenReturn(new ParkingMembershipOrder());

        securedController.getInfo(10L);

        verify(securedMembershipOrderService).selectParkingMembershipOrderById(10L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:membership:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(10L));
        verify(securedMembershipOrderService, never()).selectParkingMembershipOrderById(10L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:membership:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingMembershipOrder()));
        verify(securedMembershipOrderService, never()).insertParkingMembershipOrder(any(ParkingMembershipOrder.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:membership:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingMembershipOrder()));
        verify(securedMembershipOrderService, never()).updateParkingMembershipOrder(any(ParkingMembershipOrder.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:membership:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedMembershipOrderService, never()).deleteParkingMembershipOrderByIds(any(Long[].class));
    }

    @Test
    void missingPayPermissionDeniesPayEndpoint()
    {
        setLoginUserPermissions("parking:membership:list");

        assertThrows(AccessDeniedException.class, () -> securedController.pay(new ParkingMembershipOrder()));
        verify(securedMembershipOrderService, never()).payMembershipOrder(any(ParkingMembershipOrder.class));
    }

    @Test
    void missingCancelPermissionDeniesCancelEndpoint()
    {
        setLoginUserPermissions("parking:membership:list");

        assertThrows(AccessDeniedException.class, () -> securedController.cancel(new ParkingMembershipOrder()));
        verify(securedMembershipOrderService, never()).cancelMembershipOrder(any(ParkingMembershipOrder.class));
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
        IParkingMembershipOrderService membershipOrderService()
        {
            return Mockito.mock(IParkingMembershipOrderService.class);
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
        ParkingMembershipOrderController parkingMembershipOrderController(IParkingMembershipOrderService membershipOrderService,
                                                                          ParkingLotAdminMapper parkingLotAdminMapper,
                                                                          ParkingCustomerMapper parkingCustomerMapper)
        {
            return new ParkingMembershipOrderController(membershipOrderService, parkingLotAdminMapper, parkingCustomerMapper);
        }
    }
}
