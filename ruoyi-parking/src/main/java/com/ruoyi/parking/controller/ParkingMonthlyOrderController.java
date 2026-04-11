package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.service.IParkingMonthlyOrderService;
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

    public ParkingMonthlyOrderController(IParkingMonthlyOrderService parkingMonthlyOrderService)
    {
        this.parkingMonthlyOrderService = parkingMonthlyOrderService;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:monthly:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingMonthlyOrder parkingMonthlyOrder)
    {
        startPage();
        return getDataTable(parkingMonthlyOrderService.selectParkingMonthlyOrderList(parkingMonthlyOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:query')")
    @GetMapping("/{monthlyOrderId}")
    public AjaxResult getInfo(@PathVariable Long monthlyOrderId)
    {
        return success(parkingMonthlyOrderService.selectParkingMonthlyOrderById(monthlyOrderId));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:add')")
    @Log(title = "月卡订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingMonthlyOrder parkingMonthlyOrder)
    {
        parkingMonthlyOrder.setCreateBy(getUsername());
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
        parkingMonthlyOrder.setUpdateBy(getUsername());
        return toAjax(parkingMonthlyOrderService.payMonthlyOrder(parkingMonthlyOrder));
    }

    @PreAuthorize("@ss.hasPermi('parking:monthly:cancel')")
    @Log(title = "月卡订单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel")
    public AjaxResult cancel(@RequestBody ParkingMonthlyOrder parkingMonthlyOrder)
    {
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
}
