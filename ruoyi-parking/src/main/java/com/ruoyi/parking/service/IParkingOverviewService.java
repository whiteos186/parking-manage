package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingSpace;
import java.util.List;

public interface IParkingOverviewService
{
    ParkingOverviewStats selectOverviewStats();

    List<ParkingLot> selectParkingLotList(ParkingLot parkingLot);

    List<ParkingSpace> selectParkingSpaceList(ParkingSpace parkingSpace);
}
