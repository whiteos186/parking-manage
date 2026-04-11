package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingTempOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingTempOrderMapper
{
    ParkingTempOrder selectParkingTempOrderById(Long tempOrderId);

    List<ParkingTempOrder> selectParkingTempOrderList(ParkingTempOrder parkingTempOrder);

    int insertParkingTempOrder(ParkingTempOrder parkingTempOrder);

    int updateParkingTempOrder(ParkingTempOrder parkingTempOrder);

    int deleteParkingTempOrderByIds(@Param("tempOrderIds") Long[] tempOrderIds);

    List<ParkingTempOrder> selectParkingTempOrderByIds(@Param("tempOrderIds") Long[] tempOrderIds);
}
