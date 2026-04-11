package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingPaymentRecord;
import java.math.BigDecimal;
import java.util.List;

public interface IParkingPaymentRecordService
{
    ParkingPaymentRecord selectParkingPaymentRecordById(Long paymentId);

    List<ParkingPaymentRecord> selectParkingPaymentRecordList(ParkingPaymentRecord record);

    int insertParkingPaymentRecord(ParkingPaymentRecord record);

    int updateParkingPaymentRecord(ParkingPaymentRecord record);

    int refundParkingPaymentRecord(ParkingPaymentRecord form);

    int deleteParkingPaymentRecordByIds(Long[] paymentIds);

    /**
     * Create a successful payment record for a settled or paid order. Used by
     * temp/monthly/membership order services to cascade a payment row when the
     * order itself transitions to paid status.
     */
    ParkingPaymentRecord createPaymentForOrder(String bizOrderNo, String bizOrderType,
        Long customerId, Long lotId, BigDecimal amount, String payChannel, String createBy);
}
