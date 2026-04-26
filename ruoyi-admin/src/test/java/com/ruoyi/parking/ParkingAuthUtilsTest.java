package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.util.ParkingAuthUtils;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class ParkingAuthUtilsTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void enforceLotIdReturnsRequestedForAdminUnchanged()
    {
        ParkingLotAdminMapper mapper = Mockito.mock(ParkingLotAdminMapper.class);
        loginAs(1L, "admin");

        assertEquals(99L, ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, 99L));
        assertNull(ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, null));
    }

    @Test
    void enforceLotIdReturnsRequestedForNonLotAdminUnchanged()
    {
        ParkingLotAdminMapper mapper = Mockito.mock(ParkingLotAdminMapper.class);
        loginAs(2L, "customer");

        assertEquals(99L, ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, 99L));
        assertNull(ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, null));
    }

    @Test
    void enforceLotIdOverridesNullForLotAdmin()
    {
        ParkingLotAdminMapper mapper = Mockito.mock(ParkingLotAdminMapper.class);
        when(mapper.selectLotIdsByUserId(2L)).thenReturn(List.of(7L));
        loginAs(2L, "lot_admin");

        assertEquals(7L, ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, null));
    }

    @Test
    void enforceLotIdAllowsMatchingRequestedForLotAdmin()
    {
        ParkingLotAdminMapper mapper = Mockito.mock(ParkingLotAdminMapper.class);
        when(mapper.selectLotIdsByUserId(2L)).thenReturn(List.of(7L));
        loginAs(2L, "lot_admin");

        assertEquals(7L, ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, 7L));
    }

    @Test
    void enforceLotIdRejectsNonMatchingRequestedForLotAdmin()
    {
        ParkingLotAdminMapper mapper = Mockito.mock(ParkingLotAdminMapper.class);
        when(mapper.selectLotIdsByUserId(2L)).thenReturn(List.of(7L));
        loginAs(2L, "lot_admin");

        ServiceException ex = assertThrows(ServiceException.class,
            () -> ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, 99L));
        assertEquals("无权访问其他停车场数据", ex.getMessage());
    }

    @Test
    void enforceLotIdThrowsWhenLotAdminHasNoBinding()
    {
        ParkingLotAdminMapper mapper = Mockito.mock(ParkingLotAdminMapper.class);
        when(mapper.selectLotIdsByUserId(2L)).thenReturn(Collections.emptyList());
        loginAs(2L, "lot_admin");

        ServiceException ex = assertThrows(ServiceException.class,
            () -> ParkingAuthUtils.enforceLotIdForLotAdmin(mapper, null));
        assertEquals("当前管理员未绑定停车场", ex.getMessage());
    }

    private void loginAs(Long userId, String roleKey)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName("u" + userId);
        user.setPassword("N/A");
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        user.setRoles(List.of(role));

        LoginUser loginUser = new LoginUser(userId, userId, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }
}
