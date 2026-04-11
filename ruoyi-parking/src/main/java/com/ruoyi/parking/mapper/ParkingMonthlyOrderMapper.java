package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingMonthlyOrderMapper
{
    ParkingMonthlyOrder selectParkingMonthlyOrderById(Long monthlyOrderId);

    List<ParkingMonthlyOrder> selectParkingMonthlyOrderList(ParkingMonthlyOrder parkingMonthlyOrder);

    int insertParkingMonthlyOrder(ParkingMonthlyOrder parkingMonthlyOrder);

    int updateParkingMonthlyOrder(ParkingMonthlyOrder parkingMonthlyOrder);

    int deleteParkingMonthlyOrderByIds(@Param("monthlyOrderIds") Long[] monthlyOrderIds);
}
