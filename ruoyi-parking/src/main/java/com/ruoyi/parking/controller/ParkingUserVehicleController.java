package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.parking.domain.ParkingUserVehicle;
import com.ruoyi.parking.service.IParkingUserVehicleService;
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
@RequestMapping("/parking/vehicle")
public class ParkingUserVehicleController extends BaseController
{
    private final IParkingUserVehicleService parkingUserVehicleService;

    public ParkingUserVehicleController(IParkingUserVehicleService parkingUserVehicleService)
    {
        this.parkingUserVehicleService = parkingUserVehicleService;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:vehicle:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingUserVehicle parkingUserVehicle)
    {
        startPage();
        return getDataTable(parkingUserVehicleService.selectParkingUserVehicleList(parkingUserVehicle));
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:vehicle:list,parking:monthly:list,parking:membership:list')")
    @GetMapping("/options")
    public AjaxResult options(Long customerId)
    {
        return success(parkingUserVehicleService.selectParkingUserVehicleOptions(customerId));
    }

    @PreAuthorize("@ss.hasPermi('parking:vehicle:query')")
    @GetMapping("/{vehicleId}")
    public AjaxResult getInfo(@PathVariable Long vehicleId)
    {
        return success(parkingUserVehicleService.selectParkingUserVehicleById(vehicleId));
    }

    @PreAuthorize("@ss.hasPermi('parking:vehicle:add')")
    @Log(title = "用户车辆", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingUserVehicle parkingUserVehicle)
    {
        parkingUserVehicle.setCreateBy(getUsername());
        return toAjax(parkingUserVehicleService.insertParkingUserVehicle(parkingUserVehicle));
    }

    @PreAuthorize("@ss.hasPermi('parking:vehicle:edit')")
    @Log(title = "用户车辆", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingUserVehicle parkingUserVehicle)
    {
        parkingUserVehicle.setUpdateBy(getUsername());
        return toAjax(parkingUserVehicleService.updateParkingUserVehicle(parkingUserVehicle));
    }

    @PreAuthorize("@ss.hasPermi('parking:vehicle:setDefault')")
    @Log(title = "用户车辆", businessType = BusinessType.UPDATE)
    @PutMapping("/default/{vehicleId}")
    public AjaxResult setDefault(@PathVariable Long vehicleId)
    {
        parkingUserVehicleService.setDefaultVehicle(vehicleId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('parking:vehicle:remove')")
    @Log(title = "用户车辆", businessType = BusinessType.DELETE)
    @DeleteMapping("/{vehicleIds}")
    public AjaxResult remove(@PathVariable Long[] vehicleIds)
    {
        return toAjax(parkingUserVehicleService.deleteParkingUserVehicleByIds(vehicleIds));
    }
}
