package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingLotAdmin;
import java.util.List;
import java.util.Map;

public interface IParkingLotAdminService
{
    ParkingLotAdmin selectParkingLotAdminById(Long lotAdminId);

    List<ParkingLotAdmin> selectParkingLotAdminList(ParkingLotAdmin parkingLotAdmin);

    int insertParkingLotAdmin(ParkingLotAdmin parkingLotAdmin);

    int updateParkingLotAdmin(ParkingLotAdmin parkingLotAdmin);

    int deleteParkingLotAdminByIds(Long[] lotAdminIds);

    List<Map<String, Object>> selectUserOptions();
}
