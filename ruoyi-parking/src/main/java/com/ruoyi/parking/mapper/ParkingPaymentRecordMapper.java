package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingPaymentRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingPaymentRecordMapper
{
    ParkingPaymentRecord selectParkingPaymentRecordById(Long paymentId);

    ParkingPaymentRecord selectParkingPaymentRecordByTradeNo(@Param("tradeNo") String tradeNo);

    List<ParkingPaymentRecord> selectParkingPaymentRecordList(ParkingPaymentRecord record);

    int insertParkingPaymentRecord(ParkingPaymentRecord record);

    int updateParkingPaymentRecord(ParkingPaymentRecord record);

    int deleteParkingPaymentRecordByIds(@Param("paymentIds") Long[] paymentIds);
}
