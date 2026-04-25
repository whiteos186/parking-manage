package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingSpace;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingSpaceMapper
{
    ParkingSpace selectParkingSpaceById(Long spaceId);

    List<ParkingSpace> selectParkingSpaceList(ParkingSpace parkingSpace);

    ParkingSpace selectParkingSpaceByLotIdAndSpaceCode(@Param("lotId") Long lotId, @Param("spaceCode") String spaceCode);

    List<ParkingSpace> selectParkingSpaceByIds(@Param("spaceIds") Long[] spaceIds);

    int insertParkingSpace(ParkingSpace parkingSpace);

    int updateParkingSpace(ParkingSpace parkingSpace);

    int deleteParkingSpaceByIds(@Param("spaceIds") Long[] spaceIds);

    int countParkingSpaceByLotIds(@Param("lotIds") Long[] lotIds);

    int countParkingSpaceByLotId(@Param("lotId") Long lotId);

    int countAvailableParkingSpaceByLotId(@Param("lotId") Long lotId);

    int countOccupiedParkingSpaceByLotId(@Param("lotId") Long lotId);
}
