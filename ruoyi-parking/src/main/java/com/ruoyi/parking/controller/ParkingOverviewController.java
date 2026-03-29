package com.ruoyi.parking.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.service.IParkingOverviewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parking")
public class ParkingOverviewController extends BaseController
{
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
    @GetMapping("/lot/options")
    public AjaxResult lotOptions()
    {
        return AjaxResult.success(parkingOverviewService.selectParkingLotOptions());
    }

    @PreAuthorize("@ss.hasPermi('parking:overview:list')")
    @GetMapping("/lot/list")
    public TableDataInfo lotList(ParkingLot parkingLot)
    {
        startPage();
        return getDataTable(parkingOverviewService.selectParkingLotList(parkingLot));
    }

    @PreAuthorize("@ss.hasPermi('parking:overview:list')")
    @GetMapping("/space/list")
    public TableDataInfo spaceList(ParkingSpace parkingSpace)
    {
        startPage();
        return getDataTable(parkingOverviewService.selectParkingSpaceList(parkingSpace));
    }
}
