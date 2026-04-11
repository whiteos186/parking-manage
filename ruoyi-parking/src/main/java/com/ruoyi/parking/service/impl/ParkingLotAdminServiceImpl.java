package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingLotAdmin;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.service.IParkingLotAdminService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ParkingLotAdminServiceImpl implements IParkingLotAdminService
{
    private final ParkingLotAdminMapper parkingLotAdminMapper;
    private final ParkingLotMapper parkingLotMapper;

    public ParkingLotAdminServiceImpl(ParkingLotAdminMapper parkingLotAdminMapper, ParkingLotMapper parkingLotMapper)
    {
        this.parkingLotAdminMapper = parkingLotAdminMapper;
        this.parkingLotMapper = parkingLotMapper;
    }

    @Override
    public ParkingLotAdmin selectParkingLotAdminById(Long lotAdminId)
    {
        return parkingLotAdminMapper.selectParkingLotAdminById(lotAdminId);
    }

    @Override
    public List<ParkingLotAdmin> selectParkingLotAdminList(ParkingLotAdmin parkingLotAdmin)
    {
        return parkingLotAdminMapper.selectParkingLotAdminList(parkingLotAdmin);
    }

    @Override
    public int insertParkingLotAdmin(ParkingLotAdmin parkingLotAdmin)
    {
        if (parkingLotAdmin.getLotId() == null)
        {
            throw new ServiceException("停车场编号不能为空");
        }
        if (parkingLotAdmin.getUserId() == null)
        {
            throw new ServiceException("用户编号不能为空");
        }
        if (StringUtils.isEmpty(parkingLotAdmin.getStatus()))
        {
            parkingLotAdmin.setStatus("0");
        }
        validateLotExists(parkingLotAdmin.getLotId());
        ParkingLotAdmin existing = parkingLotAdminMapper.selectParkingLotAdminByLotIdAndUserId(
            parkingLotAdmin.getLotId(), parkingLotAdmin.getUserId());
        if (existing != null)
        {
            throw new ServiceException("该用户已绑定此停车场");
        }
        return parkingLotAdminMapper.insertParkingLotAdmin(parkingLotAdmin);
    }

    @Override
    public int updateParkingLotAdmin(ParkingLotAdmin parkingLotAdmin)
    {
        ParkingLotAdmin current = parkingLotAdminMapper.selectParkingLotAdminById(parkingLotAdmin.getLotAdminId());
        if (current == null)
        {
            throw new ServiceException("绑定记录不存在");
        }
        if (StringUtils.isEmpty(parkingLotAdmin.getStatus()))
        {
            parkingLotAdmin.setStatus(current.getStatus());
        }
        Long targetLotId = parkingLotAdmin.getLotId() != null ? parkingLotAdmin.getLotId() : current.getLotId();
        Long targetUserId = parkingLotAdmin.getUserId() != null ? parkingLotAdmin.getUserId() : current.getUserId();
        parkingLotAdmin.setLotId(targetLotId);
        parkingLotAdmin.setUserId(targetUserId);
        validateLotExists(targetLotId);
        boolean lotChanged = !Objects.equals(current.getLotId(), targetLotId);
        boolean userChanged = !Objects.equals(current.getUserId(), targetUserId);
        if (lotChanged || userChanged)
        {
            ParkingLotAdmin conflict = parkingLotAdminMapper.selectParkingLotAdminByLotIdAndUserId(
                targetLotId, targetUserId);
            if (conflict != null && !Objects.equals(conflict.getLotAdminId(), parkingLotAdmin.getLotAdminId()))
            {
                throw new ServiceException("该用户已绑定此停车场");
            }
        }
        return parkingLotAdminMapper.updateParkingLotAdmin(parkingLotAdmin);
    }

    @Override
    public int deleteParkingLotAdminByIds(Long[] lotAdminIds)
    {
        return parkingLotAdminMapper.deleteParkingLotAdminByIds(lotAdminIds);
    }

    @Override
    public List<Map<String, Object>> selectUserOptions()
    {
        return parkingLotAdminMapper.selectUserOptions();
    }

    private void validateLotExists(Long lotId)
    {
        ParkingLot parkingLot = parkingLotMapper.selectParkingLotById(lotId);
        if (parkingLot == null)
        {
            throw new ServiceException("停车场不存在");
        }
    }
}
