package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.service.IParkingCustomerService;
import com.ruoyi.parking.util.ParkingAuthUtils;
import java.util.Collections;
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
@RequestMapping("/parking/customer")
public class ParkingCustomerController extends BaseController
{
    private final IParkingCustomerService parkingCustomerService;
    private final ParkingCustomerMapper parkingCustomerMapper;

    public ParkingCustomerController(IParkingCustomerService parkingCustomerService,
                                     ParkingCustomerMapper parkingCustomerMapper)
    {
        this.parkingCustomerService = parkingCustomerService;
        this.parkingCustomerMapper = parkingCustomerMapper;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:customer:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingCustomer parkingCustomer)
    {
        startPage();
        if (ParkingAuthUtils.isCustomer() && !SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            parkingCustomer.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
        }
        return getDataTable(parkingCustomerService.selectParkingCustomerList(parkingCustomer));
    }

    @PreAuthorize(
        "@ss.hasAnyPermi('parking:customer:list,parking:customer:add,parking:customer:edit,"
            + "parking:vehicle:list,parking:vehicle:add,parking:vehicle:edit,"
            + "parking:monthly:list,parking:monthly:add,parking:monthly:edit,"
            + "parking:membership:list,parking:membership:add,parking:membership:edit,"
            + "parking:temp:list,parking:temp:add,parking:temp:edit,"
            + "parking:payment:list,parking:payment:add,parking:payment:edit')"
    )
    @GetMapping("/options")
    public AjaxResult options()
    {
        if (ParkingAuthUtils.isCustomer() && !SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            return success(Collections.singletonList(ParkingAuthUtils.resolveRequiredCustomer(parkingCustomerMapper)));
        }
        return success(parkingCustomerService.selectParkingCustomerOptions());
    }

    @PreAuthorize("@ss.hasPermi('parking:customer:query')")
    @GetMapping("/{customerId}")
    public AjaxResult getInfo(@PathVariable Long customerId)
    {
        return success(parkingCustomerService.selectParkingCustomerById(customerId));
    }

    @PreAuthorize("@ss.hasPermi('parking:customer:add')")
    @Log(title = "客户档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingCustomer parkingCustomer)
    {
        parkingCustomer.setCreateBy(getUsername());
        return toAjax(parkingCustomerService.insertParkingCustomer(parkingCustomer));
    }

    @PreAuthorize("@ss.hasPermi('parking:customer:edit')")
    @Log(title = "客户档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingCustomer parkingCustomer)
    {
        parkingCustomer.setUpdateBy(getUsername());
        return toAjax(parkingCustomerService.updateParkingCustomer(parkingCustomer));
    }

    @PreAuthorize("@ss.hasPermi('parking:customer:remove')")
    @Log(title = "客户档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(parkingCustomerService.deleteParkingCustomerByIds(customerIds));
    }
}
