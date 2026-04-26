package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingSpaceService;
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
@RequestMapping("/parking/space")
public class ParkingSpaceController extends BaseController
{
    private final IParkingSpaceService parkingSpaceService;
    private final ParkingLotAdminMapper parkingLotAdminMapper;

    public ParkingSpaceController(IParkingSpaceService parkingSpaceService,
                                  ParkingLotAdminMapper parkingLotAdminMapper)
    {
        this.parkingSpaceService = parkingSpaceService;
        this.parkingLotAdminMapper = parkingLotAdminMapper;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:space:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingSpace parkingSpace)
    {
        startPage();
        parkingSpace.setLotId(ParkingAuthUtils.enforceLotIdForLotAdmin(parkingLotAdminMapper, parkingSpace.getLotId()));
        return getDataTable(parkingSpaceService.selectParkingSpaceList(parkingSpace));
    }

    @PreAuthorize("@ss.hasPermi('parking:space:query')")
    @GetMapping("/{spaceId}")
    public AjaxResult getInfo(@PathVariable Long spaceId)
    {
        return success(parkingSpaceService.selectParkingSpaceById(spaceId));
    }

    @PreAuthorize("@ss.hasPermi('parking:space:add')")
    @Log(title = "车位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingSpace parkingSpace)
    {
        parkingSpace.setCreateBy(getUsername());
        return toAjax(parkingSpaceService.insertParkingSpace(parkingSpace));
    }

    @PreAuthorize("@ss.hasPermi('parking:space:edit')")
    @Log(title = "车位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingSpace parkingSpace)
    {
        parkingSpace.setUpdateBy(getUsername());
        return toAjax(parkingSpaceService.updateParkingSpace(parkingSpace));
    }

    @PreAuthorize("@ss.hasPermi('parking:space:remove')")
    @Log(title = "车位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{spaceIds}")
    public AjaxResult remove(@PathVariable Long[] spaceIds)
    {
        return toAjax(parkingSpaceService.deleteParkingSpaceByIds(spaceIds));
    }
}
