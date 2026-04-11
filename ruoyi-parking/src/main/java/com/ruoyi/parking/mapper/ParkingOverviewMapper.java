package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingRecentOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingOverviewMapper
{
    ParkingOverviewStats selectOverviewStats(@Param("lotIds") List<Long> lotIds);

    List<ParkingRecentOrder> selectRecentOrders(@Param("limit") int limit, @Param("lotIds") List<Long> lotIds);
}
