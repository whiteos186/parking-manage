package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingMonthlyOrderService;
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
@RequestMapping("/parking/monthly")
public class ParkingMonthlyOrderController extends BaseController
{
    private final IParkingMonthlyOrderService parkingMonthlyOrderService;
    private final ParkingLotAdminMapper parkingLotAdminMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;

    public ParkingMonthlyOrderController(IParkingMonthlyOrderService parkingMonthlyOrderService,
                                         ParkingLotAdminMapper parkingLotAdminMapper,
                                         ParkingCustomerMapper parkingCustomerMapper)
    {
        this.parkingMonthlyOrderService = parkingMonthlyOrderService;
        this.parkingLotAdminMapper = parkingLotAdminMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:monthly:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingMonthlyOrder parkingMonthlyOrder)
    {
        startPage();
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            if (ParkingAuthUtils.isLotAdmin())
            {
                Long scopedLotId = ParkingAuthUtils.resolveSingleLotId(parkingLotAdminMapper);
                if (scopedLotId != null && parkingMonthlyOrder.getLotId() == null)
                {
                    parkingMonthlyOrder.setLotId(scopedLotId);
                }
            }
            else if (ParkingAuthUtils.isCustomer())
            {
                parkingMonthlyOrder.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
            }
        }
        return getDataTable(parkingMonthlyOrderService.selectParkingMonthlyOrderList(parkingMonthlyOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:query')")
    @GetMapping("/{monthlyOrderId}")
    public AjaxResult getInfo(@PathVariable Long monthlyOrderId)
    {
        ParkingMonthlyOrder order = parkingMonthlyOrderService.selectParkingMonthlyOrderById(monthlyOrderId);
        assertCurrentCustomerOwns(order);
        return success(order);
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:add')")
    @Log(title = "月卡订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingMonthlyOrder parkingMonthlyOrder)
    {
        if (ParkingAuthUtils.isCustomer() && !SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            parkingMonthlyOrder.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
        }
        parkingMonthlyOrder.setCreateBy(getUsername());
        logger.info("入参", parkingMonthlyOrder);
        return toAjax(parkingMonthlyOrderService.insertParkingMonthlyOrder(parkingMonthlyOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:edit')")
    @Log(title = "月卡订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingMonthlyOrder parkingMonthlyOrder)
    {
        parkingMonthlyOrder.setUpdateBy(getUsername());
        return toAjax(parkingMonthlyOrderService.updateParkingMonthlyOrder(parkingMonthlyOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:pay')")
    @Log(title = "月卡订单", businessType = BusinessType.UPDATE)
    @PutMapping("/pay")
    public AjaxResult pay(@RequestBody ParkingMonthlyOrder parkingMonthlyOrder)
    {
        assertCurrentCustomerOwns(parkingMonthlyOrderService.selectParkingMonthlyOrderById(
            parkingMonthlyOrder.getMonthlyOrderId()));
        parkingMonthlyOrder.setUpdateBy(getUsername());
        return toAjax(parkingMonthlyOrderService.payMonthlyOrder(parkingMonthlyOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:cancel')")
    @Log(title = "月卡订单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel")
    public AjaxResult cancel(@RequestBody ParkingMonthlyOrder parkingMonthlyOrder)
    {
        assertCurrentCustomerOwns(parkingMonthlyOrderService.selectParkingMonthlyOrderById(
            parkingMonthlyOrder.getMonthlyOrderId()));
        parkingMonthlyOrder.setUpdateBy(getUsername());
        return toAjax(parkingMonthlyOrderService.cancelMonthlyOrder(parkingMonthlyOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:remove')")
    @Log(title = "月卡订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{monthlyOrderIds}")
    public AjaxResult remove(@PathVariable Long[] monthlyOrderIds)
    {
        return toAjax(parkingMonthlyOrderService.deleteParkingMonthlyOrderByIds(monthlyOrderIds));
    }

    private void assertCurrentCustomerOwns(ParkingMonthlyOrder order)
    {
        if (order == null || !ParkingAuthUtils.isCustomer() || SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            return;
        }
        Long customerId = ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper);
        if (!customerId.equals(order.getCustomerId()))
        {
            throw new com.ruoyi.common.exception.ServiceException("月卡订单不属于当前客户");
        }
    }
}
