package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingUserVehicle;
import com.ruoyi.parking.mapper.ParkingUserVehicleMapper;
import com.ruoyi.parking.service.IParkingUserVehicleService;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingUserVehicleServiceImpl implements IParkingUserVehicleService
{
    private final ParkingUserVehicleMapper parkingUserVehicleMapper;

    public ParkingUserVehicleServiceImpl(ParkingUserVehicleMapper parkingUserVehicleMapper)
    {
        this.parkingUserVehicleMapper = parkingUserVehicleMapper;
    }

    @Override
    public ParkingUserVehicle selectParkingUserVehicleById(Long vehicleId)
    {
        return parkingUserVehicleMapper.selectParkingUserVehicleById(vehicleId);
    }

    @Override
    public List<ParkingUserVehicle> selectParkingUserVehicleList(ParkingUserVehicle parkingUserVehicle)
    {
        return parkingUserVehicleMapper.selectParkingUserVehicleList(parkingUserVehicle);
    }

    @Override
    public List<ParkingUserVehicle> selectParkingUserVehicleOptions(Long customerId)
    {
        return parkingUserVehicleMapper.selectParkingUserVehicleOptions(customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertParkingUserVehicle(ParkingUserVehicle parkingUserVehicle)
    {
        applyInsertDefaults(parkingUserVehicle);
        validateUniquePlateNo(parkingUserVehicle);
        if ("1".equals(parkingUserVehicle.getIsDefault()))
        {
            parkingUserVehicleMapper.clearDefaultByCustomerId(parkingUserVehicle.getCustomerId());
        }
        return parkingUserVehicleMapper.insertParkingUserVehicle(parkingUserVehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateParkingUserVehicle(ParkingUserVehicle parkingUserVehicle)
    {
        ParkingUserVehicle current = parkingUserVehicleMapper.selectParkingUserVehicleById(parkingUserVehicle.getVehicleId());
        if (current == null)
        {
            throw new ServiceException("车辆不存在");
        }
        mergeUpdateDefaults(parkingUserVehicle, current);
        if (!Objects.equals(parkingUserVehicle.getPlateNo(), current.getPlateNo()))
        {
            validateUniquePlateNo(parkingUserVehicle);
        }
        boolean becomingDefault = "1".equals(parkingUserVehicle.getIsDefault())
            && (!"1".equals(current.getIsDefault())
                || !Objects.equals(parkingUserVehicle.getCustomerId(), current.getCustomerId()));
        if (becomingDefault)
        {
            parkingUserVehicleMapper.clearDefaultByCustomerId(parkingUserVehicle.getCustomerId());
        }
        return parkingUserVehicleMapper.updateParkingUserVehicle(parkingUserVehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteParkingUserVehicleByIds(Long[] vehicleIds)
    {
        return parkingUserVehicleMapper.deleteParkingUserVehicleByIds(vehicleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultVehicle(Long vehicleId)
    {
        ParkingUserVehicle vehicle = parkingUserVehicleMapper.selectParkingUserVehicleById(vehicleId);
        if (vehicle == null)
        {
            throw new ServiceException("车辆不存在");
        }
        parkingUserVehicleMapper.clearDefaultByCustomerId(vehicle.getCustomerId());
        vehicle.setIsDefault("1");
        parkingUserVehicleMapper.updateParkingUserVehicle(vehicle);
    }

    private void applyInsertDefaults(ParkingUserVehicle parkingUserVehicle)
    {
        if (StringUtils.isEmpty(parkingUserVehicle.getVehicleType()))
        {
            parkingUserVehicle.setVehicleType("1");
        }
        if (StringUtils.isEmpty(parkingUserVehicle.getStatus()))
        {
            parkingUserVehicle.setStatus("0");
        }
        if (StringUtils.isEmpty(parkingUserVehicle.getIsDefault()))
        {
            parkingUserVehicle.setIsDefault("0");
        }
        if (parkingUserVehicle.getBindTime() == null)
        {
            parkingUserVehicle.setBindTime(new Date());
        }
    }

    private void mergeUpdateDefaults(ParkingUserVehicle parkingUserVehicle, ParkingUserVehicle current)
    {
        if (StringUtils.isEmpty(parkingUserVehicle.getPlateNo()))
        {
            parkingUserVehicle.setPlateNo(current.getPlateNo());
        }
        if (StringUtils.isEmpty(parkingUserVehicle.getVehicleType()))
        {
            parkingUserVehicle.setVehicleType(current.getVehicleType());
        }
        if (StringUtils.isEmpty(parkingUserVehicle.getStatus()))
        {
            parkingUserVehicle.setStatus(current.getStatus());
        }
        if (StringUtils.isEmpty(parkingUserVehicle.getIsDefault()))
        {
            parkingUserVehicle.setIsDefault(current.getIsDefault());
        }
        if (parkingUserVehicle.getCustomerId() == null)
        {
            parkingUserVehicle.setCustomerId(current.getCustomerId());
        }
    }

    private void validateUniquePlateNo(ParkingUserVehicle parkingUserVehicle)
    {
        ParkingUserVehicle existing = parkingUserVehicleMapper.selectParkingUserVehicleByPlateNo(
            parkingUserVehicle.getPlateNo()
        );
        if (existing != null && !Objects.equals(existing.getVehicleId(), parkingUserVehicle.getVehicleId()))
        {
            throw new ServiceException("车牌号已存在");
        }
    }
}
