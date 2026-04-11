package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingSpace;
import java.util.List;

public interface IParkingSpaceService
{
    ParkingSpace selectParkingSpaceById(Long spaceId);

    List<ParkingSpace> selectParkingSpaceList(ParkingSpace parkingSpace);

    int insertParkingSpace(ParkingSpace parkingSpace);

    int updateParkingSpace(ParkingSpace parkingSpace);

    int deleteParkingSpaceByIds(Long[] spaceIds);
}
