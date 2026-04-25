package com.ruoyi.parking.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.parking.domain.dto.ParkingSettingsUpdateDto;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parking/settings")
public class ParkingSettingsController extends BaseController
{
    private final IParkingGlobalRuleService parkingGlobalRuleService;

    public ParkingSettingsController(IParkingGlobalRuleService parkingGlobalRuleService)
    {
        this.parkingGlobalRuleService = parkingGlobalRuleService;
    }

    @PreAuthorize("@ss.hasPermi('parking:settings:query')")
    @GetMapping
    public AjaxResult getSettings()
    {
        return success(parkingGlobalRuleService.getSettings());
    }

    @PreAuthorize("@ss.hasPermi('parking:settings:edit')")
    @PutMapping
    public AjaxResult updateSettings(@Validated @RequestBody ParkingSettingsUpdateDto updateDto)
    {
        parkingGlobalRuleService.updateSettings(updateDto, getUsername());
        return success();
    }
}
