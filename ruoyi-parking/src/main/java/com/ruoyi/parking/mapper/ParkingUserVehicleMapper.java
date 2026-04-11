package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingUserVehicle;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingUserVehicleMapper
{
    ParkingUserVehicle selectParkingUserVehicleById(Long vehicleId);

    List<ParkingUserVehicle> selectParkingUserVehicleList(ParkingUserVehicle parkingUserVehicle);

    List<ParkingUserVehicle> selectParkingUserVehicleOptions(@Param("customerId") Long customerId);

    ParkingUserVehicle selectParkingUserVehicleByPlateNo(String plateNo);

    int insertParkingUserVehicle(ParkingUserVehicle parkingUserVehicle);

    int updateParkingUserVehicle(ParkingUserVehicle parkingUserVehicle);

    int deleteParkingUserVehicleByIds(@Param("vehicleIds") Long[] vehicleIds);

    int clearDefaultByCustomerId(@Param("customerId") Long customerId);
}
