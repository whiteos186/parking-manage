package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingMembershipOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingMembershipOrderMapper
{
    ParkingMembershipOrder selectParkingMembershipOrderById(Long membershipOrderId);

    List<ParkingMembershipOrder> selectParkingMembershipOrderList(ParkingMembershipOrder order);

    int insertParkingMembershipOrder(ParkingMembershipOrder order);

    int updateParkingMembershipOrder(ParkingMembershipOrder order);

    int deleteParkingMembershipOrderByIds(@Param("orderIds") Long[] orderIds);
}
