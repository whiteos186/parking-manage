package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import java.util.List;

public interface IParkingMonthlyOrderService
{
    ParkingMonthlyOrder selectParkingMonthlyOrderById(Long monthlyOrderId);

    List<ParkingMonthlyOrder> selectParkingMonthlyOrderList(ParkingMonthlyOrder parkingMonthlyOrder);

    int insertParkingMonthlyOrder(ParkingMonthlyOrder parkingMonthlyOrder);

    int updateParkingMonthlyOrder(ParkingMonthlyOrder parkingMonthlyOrder);

    int deleteParkingMonthlyOrderByIds(Long[] monthlyOrderIds);

    int payMonthlyOrder(ParkingMonthlyOrder form);

    int cancelMonthlyOrder(ParkingMonthlyOrder form);
}
