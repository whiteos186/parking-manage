package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.parking.domain.ParkingLotAdmin;
import com.ruoyi.parking.service.IParkingLotAdminService;
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
@RequestMapping("/parking/lotadmin")
public class ParkingLotAdminController extends BaseController
{
    private final IParkingLotAdminService parkingLotAdminService;

    public ParkingLotAdminController(IParkingLotAdminService parkingLotAdminService)
    {
        this.parkingLotAdminService = parkingLotAdminService;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:lotadmin:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingLotAdmin parkingLotAdmin)
    {
        startPage();
        return getDataTable(parkingLotAdminService.selectParkingLotAdminList(parkingLotAdmin));
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:lotadmin:list,parking:lotadmin:add,parking:lotadmin:edit')")
    @GetMapping("/user-options")
    public AjaxResult userOptions()
    {
        return success(parkingLotAdminService.selectUserOptions());
    }

    @PreAuthorize("@ss.hasPermi('parking:lotadmin:query')")
    @GetMapping("/{lotAdminId}")
    public AjaxResult getInfo(@PathVariable Long lotAdminId)
    {
        return success(parkingLotAdminService.selectParkingLotAdminById(lotAdminId));
    }

    @PreAuthorize("@ss.hasPermi('parking:lotadmin:add')")
    @Log(title = "停车场管理员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingLotAdmin parkingLotAdmin)
    {
        parkingLotAdmin.setCreateBy(getUsername());
        return toAjax(parkingLotAdminService.insertParkingLotAdmin(parkingLotAdmin));
    }

    @PreAuthorize("@ss.hasPermi('parking:lotadmin:edit')")
    @Log(title = "停车场管理员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingLotAdmin parkingLotAdmin)
    {
        parkingLotAdmin.setUpdateBy(getUsername());
        return toAjax(parkingLotAdminService.updateParkingLotAdmin(parkingLotAdmin));
    }

    @PreAuthorize("@ss.hasPermi('parking:lotadmin:remove')")
    @Log(title = "停车场管理员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{lotAdminIds}")
    public AjaxResult remove(@PathVariable Long[] lotAdminIds)
    {
        return toAjax(parkingLotAdminService.deleteParkingLotAdminByIds(lotAdminIds));
    }
}
