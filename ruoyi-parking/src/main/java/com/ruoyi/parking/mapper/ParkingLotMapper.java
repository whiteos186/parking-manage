package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingLot;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingLotMapper
{
    ParkingLot selectParkingLotById(Long lotId);

    List<ParkingLot> selectParkingLotList(ParkingLot parkingLot);

    List<ParkingLot> selectParkingLotOptions();

    int insertParkingLot(ParkingLot parkingLot);

    int updateParkingLot(ParkingLot parkingLot);

    int deleteParkingLotByIds(@Param("lotIds") Long[] lotIds);

    int updateParkingLotSpaceStats(@Param("lotId") Long lotId,
        @Param("availableSpaceCount") int availableSpaceCount, @Param("updateBy") String updateBy);
}
