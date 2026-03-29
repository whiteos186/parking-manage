package com.ruoyi.parking.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.parking.service.IParkingHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Parking module health controller.
 */
@RestController
@RequestMapping("/parking")
public class ParkingHealthController
{
    private final IParkingHealthService parkingHealthService;

    public ParkingHealthController(IParkingHealthService parkingHealthService)
    {
        this.parkingHealthService = parkingHealthService;
    }

    @GetMapping("/health")
    public AjaxResult health()
    {
        return AjaxResult.success(parkingHealthService.getHealth());
    }
}
