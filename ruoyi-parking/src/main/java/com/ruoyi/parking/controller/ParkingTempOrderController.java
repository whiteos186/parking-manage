package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingTempOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingTempOrderService;
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
@RequestMapping("/parking/temp")
public class ParkingTempOrderController extends BaseController
{
    private final IParkingTempOrderService parkingTempOrderService;
    private final ParkingLotAdminMapper parkingLotAdminMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;

    public ParkingTempOrderController(IParkingTempOrderService parkingTempOrderService,
                                      ParkingLotAdminMapper parkingLotAdminMapper,
                                      ParkingCustomerMapper parkingCustomerMapper)
    {
        this.parkingTempOrderService = parkingTempOrderService;
        this.parkingLotAdminMapper = parkingLotAdminMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:temp:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingTempOrder parkingTempOrder)
    {
        startPage();
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            if (ParkingAuthUtils.isLotAdmin())
            {
                Long scopedLotId = ParkingAuthUtils.resolveSingleLotId(parkingLotAdminMapper);
                if (scopedLotId != null && parkingTempOrder.getLotId() == null)
                {
                    parkingTempOrder.setLotId(scopedLotId);
                }
            }
            else if (ParkingAuthUtils.isCustomer())
            {
                parkingTempOrder.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
            }
        }
        return getDataTable(parkingTempOrderService.selectParkingTempOrderList(parkingTempOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:temp:query')")
    @GetMapping("/{tempOrderId}")
    public AjaxResult getInfo(@PathVariable Long tempOrderId)
    {
        ParkingTempOrder order = parkingTempOrderService.selectParkingTempOrderById(tempOrderId);
        assertCurrentCustomerOwns(order);
        return success(order);
    }

    @PreAuthorize("@ss.hasPermi('parking:temp:add')")
    @Log(title = "临停订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingTempOrder parkingTempOrder)
    {
        if (ParkingAuthUtils.isCustomer() && !SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            parkingTempOrder.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
        }
        parkingTempOrder.setCreateBy(getUsername());
        return toAjax(parkingTempOrderService.insertParkingTempOrder(parkingTempOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:temp:edit')")
    @Log(title = "临停订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingTempOrder parkingTempOrder)
    {
        parkingTempOrder.setUpdateBy(getUsername());
        return toAjax(parkingTempOrderService.updateParkingTempOrder(parkingTempOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:temp:entry')")
    @Log(title = "临停订单", businessType = BusinessType.UPDATE)
    @PutMapping("/entry/{tempOrderId}")
    public AjaxResult entry(@PathVariable Long tempOrderId)
    {
        return toAjax(parkingTempOrderService.entryParkingTempOrder(tempOrderId));
    }

    @PreAuthorize("@ss.hasPermi('parking:temp:exit')")
    @Log(title = "临停订单", businessType = BusinessType.UPDATE)
    @PutMapping("/exit/{tempOrderId}")
    public AjaxResult exit(@PathVariable Long tempOrderId)
    {
        return toAjax(parkingTempOrderService.exitParkingTempOrder(tempOrderId));
    }

    @PreAuthorize("@ss.hasPermi('parking:temp:settle')")
    @Log(title = "临停订单", businessType = BusinessType.UPDATE)
    @PutMapping("/settle")
    public AjaxResult settle(@RequestBody ParkingTempOrder parkingTempOrder)
    {
        parkingTempOrder.setUpdateBy(getUsername());
        return success(parkingTempOrderService.settleParkingTempOrder(parkingTempOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:temp:remove')")
    @Log(title = "临停订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tempOrderIds}")
    public AjaxResult remove(@PathVariable Long[] tempOrderIds)
    {
        return toAjax(parkingTempOrderService.deleteParkingTempOrderByIds(tempOrderIds));
    }

    private void assertCurrentCustomerOwns(ParkingTempOrder order)
    {
        if (order == null || !ParkingAuthUtils.isCustomer() || SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            return;
        }
        Long customerId = ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper);
        if (!customerId.equals(order.getCustomerId()))
        {
            throw new com.ruoyi.common.exception.ServiceException("临停订单不属于当前客户");
        }
    }
}
