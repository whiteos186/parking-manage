package com.ruoyi.parking.util;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import java.util.List;
import org.springframework.security.core.Authentication;

public class ParkingAuthUtils
{
    private ParkingAuthUtils() {}

    /** Check if current user has the lot_admin role */
    public static boolean isLotAdmin()
    {
        LoginUser loginUser = getCurrentLoginUser();
        if (loginUser == null || loginUser.getUser() == null) return false;
        List<SysRole> roles = loginUser.getUser().getRoles();
        if (roles == null) return false;
        return roles.stream().anyMatch(r -> "lot_admin".equals(r.getRoleKey()));
    }

    /** Check if current user has the customer role */
    public static boolean isCustomer()
    {
        LoginUser loginUser = getCurrentLoginUser();
        if (loginUser == null || loginUser.getUser() == null) return false;
        List<SysRole> roles = loginUser.getUser().getRoles();
        if (roles == null) return false;
        return roles.stream().anyMatch(r -> "customer".equals(r.getRoleKey()));
    }

    /**
     * For lot_admin users, return their primary (first) bound lot ID.
     * For admin, return null (no filter needed).
     */
    public static Long resolveSingleLotId(ParkingLotAdminMapper mapper)
    {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId)) return null;
        List<Long> ids = mapper.selectLotIdsByUserId(userId);
        return (ids != null && !ids.isEmpty()) ? ids.get(0) : null;
    }

    /**
     * Resolve the effective lotId for the current request.
     * - admin / non-lot_admin: returns requestedLotId unchanged
     * - lot_admin without binding: throws
     * - lot_admin with binding and matching/empty requestedLotId: returns bound lotId (overrides)
     * - lot_admin with binding and non-matching requestedLotId: throws
     */
    public static Long enforceLotIdForLotAdmin(ParkingLotAdminMapper mapper, Long requestedLotId)
    {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId)) return requestedLotId;
        if (!isLotAdmin()) return requestedLotId;
        Long scopedLotId = resolveSingleLotId(mapper);
        if (scopedLotId == null)
        {
            throw new ServiceException("当前管理员未绑定停车场");
        }
        if (requestedLotId != null && !requestedLotId.equals(scopedLotId))
        {
            throw new ServiceException("无权访问其他停车场数据");
        }
        return scopedLotId;
    }

    /**
     * For customer users, return the ParkingCustomer linked to the current sys_user.
     * Returns null if the user is not a customer or no linked record exists.
     */
    public static ParkingCustomer resolveCustomer(ParkingCustomerMapper mapper)
    {
        if (!isCustomer()) return null;
        return mapper.selectParkingCustomerByUserId(SecurityUtils.getUserId());
    }

    public static ParkingCustomer resolveRequiredCustomer(ParkingCustomerMapper mapper)
    {
        ParkingCustomer customer = resolveCustomer(mapper);
        if (customer == null)
        {
            throw new ServiceException("当前用户未绑定停车客户");
        }
        return customer;
    }

    public static Long resolveRequiredCustomerId(ParkingCustomerMapper mapper)
    {
        return resolveRequiredCustomer(mapper).getCustomerId();
    }

    private static LoginUser getCurrentLoginUser()
    {
        Authentication authentication = SecurityUtils.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser))
        {
            return null;
        }
        return (LoginUser) authentication.getPrincipal();
    }
}
