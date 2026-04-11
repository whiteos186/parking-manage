package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingUserVehicle;
import java.util.List;

public interface IParkingUserVehicleService
{
    ParkingUserVehicle selectParkingUserVehicleById(Long vehicleId);

    List<ParkingUserVehicle> selectParkingUserVehicleList(ParkingUserVehicle parkingUserVehicle);

    List<ParkingUserVehicle> selectParkingUserVehicleOptions(Long customerId);

    int insertParkingUserVehicle(ParkingUserVehicle parkingUserVehicle);

    int updateParkingUserVehicle(ParkingUserVehicle parkingUserVehicle);

    int deleteParkingUserVehicleByIds(Long[] vehicleIds);

    void setDefaultVehicle(Long vehicleId);
}
