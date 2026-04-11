package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingTempOrder;
import java.util.List;

public interface IParkingTempOrderService
{
    ParkingTempOrder selectParkingTempOrderById(Long tempOrderId);

    List<ParkingTempOrder> selectParkingTempOrderList(ParkingTempOrder parkingTempOrder);

    int insertParkingTempOrder(ParkingTempOrder parkingTempOrder);

    int updateParkingTempOrder(ParkingTempOrder parkingTempOrder);

    /** 车辆入场：状态 0 → 1，记录 in_time，标记车位占用 */
    int entryParkingTempOrder(Long tempOrderId);

    /** 车辆出场：状态 1 → 2，计算费用，应用会员折扣，释放车位 */
    int exitParkingTempOrder(Long tempOrderId);

    /** 支付确认：状态 2 → 3，pay_status='1'，写支付流水 */
    ParkingTempOrder settleParkingTempOrder(ParkingTempOrder parkingTempOrder);

    int deleteParkingTempOrderByIds(Long[] tempOrderIds);
}
