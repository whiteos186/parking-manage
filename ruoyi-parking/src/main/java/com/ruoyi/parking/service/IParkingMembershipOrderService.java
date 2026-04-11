package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingMembershipOrder;
import java.util.List;

public interface IParkingMembershipOrderService
{
    ParkingMembershipOrder selectParkingMembershipOrderById(Long membershipOrderId);

    List<ParkingMembershipOrder> selectParkingMembershipOrderList(ParkingMembershipOrder order);

    int insertParkingMembershipOrder(ParkingMembershipOrder order);

    int updateParkingMembershipOrder(ParkingMembershipOrder order);

    int payMembershipOrder(ParkingMembershipOrder form);

    int cancelMembershipOrder(ParkingMembershipOrder form);

    int deleteParkingMembershipOrderByIds(Long[] orderIds);
}
