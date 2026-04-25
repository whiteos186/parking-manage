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
import com.ruoyi.parking.controller.ParkingSpaceController;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingSpaceService;
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
@ContextConfiguration(classes = ParkingSpaceControllerTest.TestConfig.class)
class ParkingSpaceControllerTest
{
    @Autowired
    private ParkingSpaceController securedController;

    @Autowired
    private IParkingSpaceService securedParkingSpaceService;

    @BeforeEach
    void resetSecurityMock()
    {
        reset(securedParkingSpaceService);
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
        IParkingSpaceService parkingSpaceService = Mockito.mock(IParkingSpaceService.class);
        ParkingSpace space = new ParkingSpace();
        space.setSpaceId(11L);
        space.setLotId(1L);
        space.setSpaceCode("A1-001");
        space.setStatus("0");
        when(parkingSpaceService.selectParkingSpaceList(any(ParkingSpace.class))).thenReturn(List.of(space));

        setLoginUserPermissions("parking:space:list");
        ParkingSpaceController controller = new ParkingSpaceController(parkingSpaceService, Mockito.mock(ParkingLotAdminMapper.class));
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
        verify(parkingSpaceService).selectParkingSpaceList(captor.capture());
        assertEquals(1L, captor.getValue().getLotId());
        assertEquals("A1", captor.getValue().getSpaceCode());
        assertEquals("0", captor.getValue().getStatus());
    }

    @Test
    void getInfoEndpointReturnsSpacePayload() throws Exception
    {
        IParkingSpaceService parkingSpaceService = Mockito.mock(IParkingSpaceService.class);
        ParkingSpace space = new ParkingSpace();
        space.setSpaceId(6L);
        space.setSpaceCode("B2-010");
        when(parkingSpaceService.selectParkingSpaceById(6L)).thenReturn(space);

        ParkingSpaceController controller = new ParkingSpaceController(parkingSpaceService, Mockito.mock(ParkingLotAdminMapper.class));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/parking/space/6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.spaceId").value(6))
            .andExpect(jsonPath("$.data.spaceCode").value("B2-010"));

        verify(parkingSpaceService).selectParkingSpaceById(6L);
    }

    @Test
    void addEndpointSetsCreateByAndDelegatesInsert()
    {
        IParkingSpaceService parkingSpaceService = Mockito.mock(IParkingSpaceService.class);
        when(parkingSpaceService.insertParkingSpace(any(ParkingSpace.class))).thenReturn(1);

        setLoginUserPermissions("parking:space:add");
        ParkingSpaceController controller = new ParkingSpaceController(parkingSpaceService, Mockito.mock(ParkingLotAdminMapper.class));

        ParkingSpace form = new ParkingSpace();
        form.setSpaceCode("C3-001");
        AjaxResult result = controller.add(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingSpace> captor = ArgumentCaptor.forClass(ParkingSpace.class);
        verify(parkingSpaceService).insertParkingSpace(captor.capture());
        assertEquals("tester", captor.getValue().getCreateBy());
    }

    @Test
    void editEndpointSetsUpdateByAndDelegatesUpdate()
    {
        IParkingSpaceService parkingSpaceService = Mockito.mock(IParkingSpaceService.class);
        when(parkingSpaceService.updateParkingSpace(any(ParkingSpace.class))).thenReturn(1);

        setLoginUserPermissions("parking:space:edit");
        ParkingSpaceController controller = new ParkingSpaceController(parkingSpaceService, Mockito.mock(ParkingLotAdminMapper.class));

        ParkingSpace form = new ParkingSpace();
        form.setSpaceId(100L);
        form.setSpaceCode("C3-002");
        AjaxResult result = controller.edit(form);

        assertEquals(200, result.get("code"));
        ArgumentCaptor<ParkingSpace> captor = ArgumentCaptor.forClass(ParkingSpace.class);
        verify(parkingSpaceService).updateParkingSpace(captor.capture());
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void removeEndpointDelegatesDelete()
    {
        IParkingSpaceService parkingSpaceService = Mockito.mock(IParkingSpaceService.class);
        when(parkingSpaceService.deleteParkingSpaceByIds(new Long[] { 3L, 4L })).thenReturn(2);

        ParkingSpaceController controller = new ParkingSpaceController(parkingSpaceService, Mockito.mock(ParkingLotAdminMapper.class));
        AjaxResult result = controller.remove(new Long[] { 3L, 4L });

        assertEquals(200, result.get("code"));
        verify(parkingSpaceService).deleteParkingSpaceByIds(new Long[] { 3L, 4L });
    }

    @Test
    void listPermissionAllowsSpaceListEndpoint()
    {
        setLoginUserPermissions("parking:space:list");
        when(securedParkingSpaceService.selectParkingSpaceList(any(ParkingSpace.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingSpace());

        verify(securedParkingSpaceService).selectParkingSpaceList(any(ParkingSpace.class));
    }

    @Test
    void listPermissionAllowsSpaceListEndpointByOverviewPermission()
    {
        setLoginUserPermissions("parking:overview:list");
        when(securedParkingSpaceService.selectParkingSpaceList(any(ParkingSpace.class)))
            .thenReturn(Collections.emptyList());

        securedController.list(new ParkingSpace());

        verify(securedParkingSpaceService).selectParkingSpaceList(any(ParkingSpace.class));
    }

    @Test
    void missingListPermissionDeniesSpaceListEndpoint()
    {
        setLoginUserPermissions("parking:space:query");

        assertThrows(AccessDeniedException.class, () -> securedController.list(new ParkingSpace()));
        verify(securedParkingSpaceService, never()).selectParkingSpaceList(any(ParkingSpace.class));
    }

    @Test
    void getInfoPermissionAllowsQueryEndpoint()
    {
        setLoginUserPermissions("parking:space:query");
        when(securedParkingSpaceService.selectParkingSpaceById(10L)).thenReturn(new ParkingSpace());

        securedController.getInfo(10L);

        verify(securedParkingSpaceService).selectParkingSpaceById(10L);
    }

    @Test
    void missingQueryPermissionDeniesGetInfoEndpoint()
    {
        setLoginUserPermissions("parking:space:list");

        assertThrows(AccessDeniedException.class, () -> securedController.getInfo(10L));
        verify(securedParkingSpaceService, never()).selectParkingSpaceById(10L);
    }

    @Test
    void missingAddPermissionDeniesAddEndpoint()
    {
        setLoginUserPermissions("parking:space:list");

        assertThrows(AccessDeniedException.class, () -> securedController.add(new ParkingSpace()));
        verify(securedParkingSpaceService, never()).insertParkingSpace(any(ParkingSpace.class));
    }

    @Test
    void missingEditPermissionDeniesEditEndpoint()
    {
        setLoginUserPermissions("parking:space:query");

        assertThrows(AccessDeniedException.class, () -> securedController.edit(new ParkingSpace()));
        verify(securedParkingSpaceService, never()).updateParkingSpace(any(ParkingSpace.class));
    }

    @Test
    void missingRemovePermissionDeniesRemoveEndpoint()
    {
        setLoginUserPermissions("parking:space:query");

        assertThrows(AccessDeniedException.class, () -> securedController.remove(new Long[] { 1L }));
        verify(securedParkingSpaceService, never()).deleteParkingSpaceByIds(any(Long[].class));
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
        IParkingSpaceService parkingSpaceService()
        {
            return Mockito.mock(IParkingSpaceService.class);
        }

        @Bean
        ParkingLotAdminMapper parkingLotAdminMapper()
        {
            return Mockito.mock(ParkingLotAdminMapper.class);
        }

        @Bean
        ParkingSpaceController parkingSpaceController(IParkingSpaceService parkingSpaceService,
                                                      ParkingLotAdminMapper parkingLotAdminMapper)
        {
            return new ParkingSpaceController(parkingSpaceService, parkingLotAdminMapper);
        }
    }
}
