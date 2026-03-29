package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingSpace;
import java.util.List;

public interface ParkingOverviewMapper
{
    ParkingOverviewStats selectOverviewStats();

    List<ParkingLot> selectParkingLotOptions();

    List<ParkingLot> selectParkingLotList(ParkingLot parkingLot);

    List<ParkingSpace> selectParkingSpaceList(ParkingSpace parkingSpace);
}
