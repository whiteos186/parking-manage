package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingLotService;
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
@RequestMapping("/parking/lot")
public class ParkingLotController extends BaseController
{
    private final IParkingLotService parkingLotService;
    private final ParkingLotAdminMapper parkingLotAdminMapper;

    public ParkingLotController(IParkingLotService parkingLotService,
                                ParkingLotAdminMapper parkingLotAdminMapper)
    {
        this.parkingLotService = parkingLotService;
        this.parkingLotAdminMapper = parkingLotAdminMapper;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:lot:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingLot parkingLot)
    {
        startPage();
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()) && ParkingAuthUtils.isLotAdmin())
        {
            Long scopedLotId = ParkingAuthUtils.resolveSingleLotId(parkingLotAdminMapper);
            if (scopedLotId != null && parkingLot.getLotId() == null)
            {
                parkingLot.setLotId(scopedLotId);
            }
        }
        return getDataTable(parkingLotService.selectParkingLotList(parkingLot));
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:lot:list,parking:space:list,parking:overview:list')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(parkingLotService.selectParkingLotOptions());
    }

    @PreAuthorize("@ss.hasPermi('parking:lot:query')")
    @GetMapping("/{lotId}")
    public AjaxResult getInfo(@PathVariable Long lotId)
    {
        return success(parkingLotService.selectParkingLotById(lotId));
    }

    @PreAuthorize("@ss.hasPermi('parking:lot:add')")
    @Log(title = "停车场管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingLot parkingLot)
    {
        parkingLot.setCreateBy(getUsername());
        return toAjax(parkingLotService.insertParkingLot(parkingLot));
    }

    @PreAuthorize("@ss.hasPermi('parking:lot:edit')")
    @Log(title = "停车场管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingLot parkingLot)
    {
        parkingLot.setUpdateBy(getUsername());
        return toAjax(parkingLotService.updateParkingLot(parkingLot));
    }

    @PreAuthorize("@ss.hasPermi('parking:lot:remove')")
    @Log(title = "停车场管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{lotIds}")
    public AjaxResult remove(@PathVariable Long[] lotIds)
    {
        return toAjax(parkingLotService.deleteParkingLotByIds(lotIds));
    }
}
