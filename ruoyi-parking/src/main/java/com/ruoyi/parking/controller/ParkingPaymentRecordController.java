package com.ruoyi.parking.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.parking.domain.ParkingPaymentRecord;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
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

    public ParkingPaymentRecordController(IParkingPaymentRecordService parkingPaymentRecordService)
    {
        this.parkingPaymentRecordService = parkingPaymentRecordService;
    }

    @PreAuthorize("@ss.hasAnyPermi('parking:payment:list,parking:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(ParkingPaymentRecord record)
    {
        startPage();
        return getDataTable(parkingPaymentRecordService.selectParkingPaymentRecordList(record));
    }

    @PreAuthorize("@ss.hasPermi('parking:payment:query')")
    @GetMapping("/{paymentId}")
    public AjaxResult getInfo(@PathVariable Long paymentId)
    {
        return success(parkingPaymentRecordService.selectParkingPaymentRecordById(paymentId));
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
}
