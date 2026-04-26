package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingLotOverview;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingRecentOrder;
import java.util.List;

public interface IParkingOverviewService
{
    ParkingOverviewStats selectOverviewStats();

    List<ParkingLotOverview> selectTopLots(int limit);

    List<ParkingRecentOrder> selectRecentOrders(int limit);
}
