package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingMembershipOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingMembershipOrderService;
import com.ruoyi.parking.util.ParkingAuthUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parking/membership")
public class ParkingMembershipOrderController extends BaseController
{
    private final IParkingMembershipOrderService membershipOrderService;
    private final ParkingLotAdminMapper parkingLotAdminMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;

    public ParkingMembershipOrderController(IParkingMembershipOrderService membershipOrderService,
                                            ParkingLotAdminMapper parkingLotAdminMapper,
                                            ParkingCustomerMapper parkingCustomerMapper)
    {
        this.membershipOrderService = membershipOrderService;
        this.parkingLotAdminMapper = parkingLotAdminMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:membership:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingMembershipOrder order)
    {
        startPage();
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            if (ParkingAuthUtils.isLotAdmin())
            {
                Long scopedLotId = ParkingAuthUtils.resolveSingleLotId(parkingLotAdminMapper);
                if (scopedLotId != null && order.getLotId() == null)
                {
                    order.setLotId(scopedLotId);
                }
            }
            else if (ParkingAuthUtils.isCustomer())
            {
                order.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
            }
        }
        return getDataTable(membershipOrderService.selectParkingMembershipOrderList(order));
    }

    @PreAuthorize("@ss.hasPermi('parking:membership:query')")
    @GetMapping("/{membershipOrderId}")
    public AjaxResult getInfo(@PathVariable Long membershipOrderId)
    {
        ParkingMembershipOrder order = membershipOrderService.selectParkingMembershipOrderById(membershipOrderId);
        assertCurrentCustomerOwns(order);
        return success(order);
    }

    @PreAuthorize("@ss.hasPermi('parking:membership:add')")
    @Log(title = "会员订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingMembershipOrder order)
    {
        if (ParkingAuthUtils.isCustomer() && !SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            order.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
        }
        order.setCreateBy(getUsername());
        return toAjax(membershipOrderService.insertParkingMembershipOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('parking:membership:edit')")
    @Log(title = "会员订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingMembershipOrder order)
    {
        order.setUpdateBy(getUsername());
        return toAjax(membershipOrderService.updateParkingMembershipOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('parking:membership:pay')")
    @Log(title = "会员订单", businessType = BusinessType.UPDATE)
    @PutMapping("/pay")
    public AjaxResult pay(@RequestBody ParkingMembershipOrder form)
    {
        assertCurrentCustomerOwns(membershipOrderService.selectParkingMembershipOrderById(
            form.getMembershipOrderId()));
        form.setUpdateBy(getUsername());
        return toAjax(membershipOrderService.payMembershipOrder(form));
    }

    @PreAuthorize("@ss.hasPermi('parking:membership:cancel')")
    @Log(title = "会员订单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel")
    public AjaxResult cancel(@RequestBody ParkingMembershipOrder form)
    {
        assertCurrentCustomerOwns(membershipOrderService.selectParkingMembershipOrderById(
            form.getMembershipOrderId()));
        form.setUpdateBy(getUsername());
        return toAjax(membershipOrderService.cancelMembershipOrder(form));
    }

    @PreAuthorize("@ss.hasPermi('parking:membership:remove')")
    @Log(title = "会员订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(membershipOrderService.deleteParkingMembershipOrderByIds(orderIds));
    }

    private void assertCurrentCustomerOwns(ParkingMembershipOrder order)
    {
        if (order == null || !ParkingAuthUtils.isCustomer() || SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            return;
        }
        Long customerId = ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper);
        if (!customerId.equals(order.getCustomerId()))
        {
            throw new com.ruoyi.common.exception.ServiceException("Membership order does not belong to current customer");
        }
    }
}
