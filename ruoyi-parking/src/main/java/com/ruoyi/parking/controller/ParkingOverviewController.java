package com.ruoyi.parking.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.parking.service.IParkingOverviewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parking")
public class ParkingOverviewController extends BaseController
{
    private static final int RECENT_ORDERS_MAX_LIMIT = 50;

    private final IParkingOverviewService parkingOverviewService;

    public ParkingOverviewController(IParkingOverviewService parkingOverviewService)
    {
        this.parkingOverviewService = parkingOverviewService;
    }

    @PreAuthorize("@ss.hasPermi('parking:overview:list')")
    @GetMapping("/overview/stats")
    public AjaxResult stats()
    {
        return AjaxResult.success(parkingOverviewService.selectOverviewStats());
    }

    @PreAuthorize("@ss.hasPermi('parking:overview:list')")
    @GetMapping("/overview/recent-orders")
    public AjaxResult recentOrders(@RequestParam(name = "limit", defaultValue = "5") int limit)
    {
        int sanitized = Math.max(1, Math.min(limit, RECENT_ORDERS_MAX_LIMIT));
        return AjaxResult.success(parkingOverviewService.selectRecentOrders(sanitized));
    }
}
