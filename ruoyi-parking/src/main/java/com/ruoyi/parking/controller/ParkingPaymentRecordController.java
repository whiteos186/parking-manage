package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingPaymentRecord;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
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
@RequestMapping("/parking/payment")
public class ParkingPaymentRecordController extends BaseController
{
    private final IParkingPaymentRecordService parkingPaymentRecordService;
    private final ParkingLotAdminMapper parkingLotAdminMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;

    public ParkingPaymentRecordController(IParkingPaymentRecordService parkingPaymentRecordService,
                                          ParkingLotAdminMapper parkingLotAdminMapper,
                                          ParkingCustomerMapper parkingCustomerMapper)
    {
        this.parkingPaymentRecordService = parkingPaymentRecordService;
        this.parkingLotAdminMapper = parkingLotAdminMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:payment:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingPaymentRecord record)
    {
        startPage();
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            if (ParkingAuthUtils.isLotAdmin())
            {
                Long scopedLotId = ParkingAuthUtils.resolveSingleLotId(parkingLotAdminMapper);
                if (scopedLotId != null && record.getLotId() == null)
                {
                    record.setLotId(scopedLotId);
                }
            }
            else if (ParkingAuthUtils.isCustomer())
            {
                record.setCustomerId(ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper));
            }
        }
        return getDataTable(parkingPaymentRecordService.selectParkingPaymentRecordList(record));
    }

    @PreAuthorize("@ss.hasPermi('parking:payment:query')")
    @GetMapping("/{paymentId}")
    public AjaxResult getInfo(@PathVariable Long paymentId)
    {
        ParkingPaymentRecord record = parkingPaymentRecordService.selectParkingPaymentRecordById(paymentId);
        assertCurrentCustomerOwns(record);
        return success(record);
    }

    @PreAuthorize("@ss.hasPermi('parking:payment:add')")
    @Log(title = "支付流水", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ParkingPaymentRecord record)
    {
        record.setCreateBy(getUsername());
        return toAjax(parkingPaymentRecordService.insertParkingPaymentRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('parking:payment:edit')")
    @Log(title = "支付流水", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingPaymentRecord record)
    {
        record.setUpdateBy(getUsername());
        return toAjax(parkingPaymentRecordService.updateParkingPaymentRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('parking:payment:refund')")
    @Log(title = "支付流水", businessType = BusinessType.UPDATE)
    @PutMapping("/refund")
    public AjaxResult refund(@RequestBody ParkingPaymentRecord record)
    {
        record.setUpdateBy(getUsername());
        return toAjax(parkingPaymentRecordService.refundParkingPaymentRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('parking:payment:remove')")
    @Log(title = "支付流水", businessType = BusinessType.DELETE)
    @DeleteMapping("/{paymentIds}")
    public AjaxResult remove(@PathVariable Long[] paymentIds)
    {
        return toAjax(parkingPaymentRecordService.deleteParkingPaymentRecordByIds(paymentIds));
    }

    private void assertCurrentCustomerOwns(ParkingPaymentRecord record)
    {
        if (record == null || !ParkingAuthUtils.isCustomer() || SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            return;
        }
        Long customerId = ParkingAuthUtils.resolveRequiredCustomerId(parkingCustomerMapper);
        if (!customerId.equals(record.getCustomerId()))
        {
            throw new com.ruoyi.common.exception.ServiceException("Payment record does not belong to current customer");
        }
    }
}
