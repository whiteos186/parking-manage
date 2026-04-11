package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingLotAdmin;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface ParkingLotAdminMapper
{
    ParkingLotAdmin selectParkingLotAdminById(Long lotAdminId);

    List<ParkingLotAdmin> selectParkingLotAdminList(ParkingLotAdmin parkingLotAdmin);

    ParkingLotAdmin selectParkingLotAdminByLotIdAndUserId(@Param("lotId") Long lotId, @Param("userId") Long userId);

    int insertParkingLotAdmin(ParkingLotAdmin parkingLotAdmin);

    int updateParkingLotAdmin(ParkingLotAdmin parkingLotAdmin);

    int deleteParkingLotAdminByIds(@Param("lotAdminIds") Long[] lotAdminIds);

    /**
     * Lightweight projection of active sys_user rows for the lot-admin form's
     * user picker. Returns userId / userName / nickName.
     */
    List<Map<String, Object>> selectUserOptions();

    List<Long> selectLotIdsByUserId(@Param("userId") Long userId);
}
