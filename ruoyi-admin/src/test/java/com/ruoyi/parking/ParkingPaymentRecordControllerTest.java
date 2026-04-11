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
import com.ruoyi.parking.controller.ParkingPaymentRecordController;
import com.ruoyi.parking.domain.ParkingPaymentRecord;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import java.math.BigDecimal;
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
@ContextConfiguration(classes = ParkingPaymentRecordControllerTest.TestConfig.class)
class ParkingPaymentRecordControllerTest
{
    @Autowired
    private ParkingPaymentRecordController securedController;

    @Autowired
    private IParkingPaymentRecordService securedPaymentService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedPaymentService);
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
        IParkingPaymentRecordService paymentService = Mockito.mock(IParkingPaymentRecordService.class);
        ParkingPaymentRecord record = new ParkingPaymentRecord();
        record.setPaymentId(1L);
        record.setBizOrderNo("ORD-001");
        record.setPayStatus("1");
        record.setPayChannel("1");
        when(paymentService.selectParkingPaymentRecordList(any(ParkingPaymentRecord.class))).thenReturn(List.of(record));

        ParkingPaymentRecordController controller = new ParkingPaymentRecordController(paymentService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/payment/list")
                .param("bizOrderNo", "ORD")
                .param("payStatus", "1")
                .param("payChannel", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.rows[0].paymentId").value(1))
            .andExpect(jsonPath("$.rows[0].bizOrderNo").value("ORD-001"))
            .andExpect(jsonPath("$.rows[0].payStatus").value("1"))
            .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<ParkingPaymentRecord> captor = ArgumentCaptor.forClass(ParkingPaymentRecord.class);
        verify(paymentService).selectParkingPaymentRecordList(captor.capture());
        assertEquals("ORD", captor.getValue().getBizOrderNo());
        assertEquals("1", captor.getValue().getPayStatus());
        assertEquals("1", captor.getValue().getPayChannel());
    }

    @Test
    void getInfoEndpointReturnsPaymentPayload() throws Exception
    {
        IParkingPaymentRecordService paymentService = Mockito.mock(IParkingPaymentRecordService.class);
        ParkingPaymentRecord record = new ParkingPaymentRecord();
        record.setPaymentId(6L);
        record.setBizOrderNo("ORD-006");
        record.setPayAmount(new BigDecimal("99.00"));
        when(paymentService.selectParkingPaymentRecordById(6L)).thenReturn(record);

        ParkingPaymentRecordController controller = new ParkingPaymentRecordController(paymentService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/payment/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.paymentId").value(6))
            .andExpect(jsonPath("$.data.bizOrderNo").value("ORD-006"));

        verify(paymentService).selectParkingPaymentRecordById(6L);
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingPaymentRecordService paymentService = Mockito.mock(IParkingPaymentRecordService.class);
        when(paymentService.insertParkingPaymentRecord(any(ParkingPaymentRecord.class))).thenReturn(1);

        setLoginUserPermissions("parking:payment:add");
        ParkingPaymentRecordController controller = new ParkingPaymentRecordController(paymentService);

        ParkingPaymentRecord form = new ParkingPaymentRecord();
        form.setBizOrderNo("ORD-NEW");
        form.setBizOrderType("3");
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingPaymentRecord> captor = ArgumentCaptor.forClass(ParkingPaymentRecord.class);
        verify(paymentService).insertParkingPaymentRecord(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingPaymentRecordService paymentService = Mockito.mock(IParkingPaymentRecordService.class);
        when(paymentService.updateParkingPaymentRecord(any(ParkingPaymentRecord.class))).thenReturn(1);

        setLoginUserPermissions("parking:payment:edit");
        ParkingPaymentRecordController controller = new ParkingPaymentRecordController(paymentService);

        ParkingPaymentRecord form = new ParkingPaymentRecord();
        form.setPaymentId(100L);
        form.setBizOrderNo("ORD-EDIT");
        form.setBizOrderType("1");
        ParkingTestBeanProperties.setLongProperty(form, "customerId", 100L);
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingPaymentRecord> captor = ArgumentCaptor.forClass(ParkingPaymentRecord.class);
        verify(paymentService).updateParkingPaymentRecord(captor.capture());
        assertEquals(100L, ParkingTestBeanProperties.getLongProperty(captor.getValue(), "customerId"));
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingPaymentRecordService paymentService = Mockito.mock(IParkingPaymentRecordService.class);
        when(paymentService.deleteParkingPaymentRecordByIds(new Long[] { 3L, 4L })).thenReturn(2);

        ParkingPaymentRecordController controller = new ParkingPaymentRecordController(paymentService);
        AjaxResult result = controller.remove(new Long[] { 3L, 4L });

        assertEquals(200, result.get("code"));
        verify(paymentService).deleteParkingPaymentRecordByIds(new Long[] { 3L, 4L });
    }

    @Test
    void refundEndpointSetsUpdateByAndDelegatesRefund()
    {
        IParkingPaymentRecordService paymentService = Mockito.mock(IParkingPaymentRecordService.class);
        when(paymentService.refundParkingPaymentRecord(any(ParkingPaymentRecord.class))).thenReturn(1);

        setLoginUserPermissions("parking:payment:refund");
        ParkingPaymentRecordController controller = new ParkingPaymentRecordController(paymentService);

        ParkingPaymentRecord form = new ParkingPaymentRecord();
        form.setPaymentId(55L);
        AjaxResult result = controller.refund(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingPaymentRecord> captor = ArgumentCaptor.forClass(ParkingPaymentRecord.class);
        verify(paymentService).refundParkingPaymentRecord(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void listPermissionAllowsPaymentListEndpoint()
    {
        setLoginUserPermissions("parking:payment:list");
        when(securedPaymentService.selectParkingPaymentRecordList(any(ParkingPaymentRecord.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingPaymentRecord());

        verify(securedPaymentService).selectParkingPaymentRecordList(any(ParkingPaymentRecord.class));
    }

    @Test
    void listPermissionAllowsPaymentListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedPaymentService.selectParkingPaymentRecordList(any(ParkingPaymentRecord.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingPaymentRecord());

        verify(securedPaymentService).selectParkingPaymentRecordList(any(ParkingPaymentRecord.class));
    }

    @Test
    void missingListPermissionDeniesPaymentListEndpoint()
    {
        setLoginUserPermissions("parking:payment:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingPaymentRecord()));
        verify(securedPaymentService, never()).selectParkingPaymentRecordList(any(ParkingPaymentRecord.class));
    }

    @Test
    void getInfoPermissionAllowsQueryEndpoint()
    {
        setLoginUserPermissions("parking:payment:query");
        when(securedPaymentService.selectParkingPaymentRecordById(10L)).thenReturn(new ParkingPaymentRecord());

        securedController.getInfo(10L);

        verify(securedPaymentService).selectParkingPaymentRecordById(10L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:payment:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(10L));
        verify(securedPaymentService, never()).selectParkingPaymentRecordById(10L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:payment:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingPaymentRecord()));
        verify(securedPaymentService, never()).insertParkingPaymentRecord(any(ParkingPaymentRecord.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:payment:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingPaymentRecord()));
        verify(securedPaymentService, never()).updateParkingPaymentRecord(any(ParkingPaymentRecord.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:payment:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedPaymentService, never()).deleteParkingPaymentRecordByIds(any(Long[].class));
    }

    @Test
    void missingRefundPermissionDeniesRefundEndpoint()
    {
        setLoginUserPermissions("parking:payment:edit");

        assertThrows(AccessDeniedException.class, () -> securedController.refund(new ParkingPaymentRecord()));
        verify(securedPaymentService, never()).refundParkingPaymentRecord(any(ParkingPaymentRecord.class));
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
        IParkingPaymentRecordService parkingPaymentRecordService()
        {
            return Mockito.mock(IParkingPaymentRecordService.class);
        }

        @Bean
        ParkingPaymentRecordController parkingPaymentRecordController(IParkingPaymentRecordService parkingPaymentRecordService)
        {
            return new ParkingPaymentRecordController(parkingPaymentRecordService);
        }
    }
}
