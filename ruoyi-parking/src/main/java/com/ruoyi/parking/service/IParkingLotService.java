package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingLot;
import java.util.List;

public interface IParkingLotService
{
    ParkingLot selectParkingLotById(Long lotId);

    List<ParkingLot> selectParkingLotList(ParkingLot parkingLot);

    List<ParkingLot> selectParkingLotOptions();

    int insertParkingLot(ParkingLot parkingLot);

    int updateParkingLot(ParkingLot parkingLot);

    int deleteParkingLotByIds(Long[] lotIds);
}
