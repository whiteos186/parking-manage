package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingSpaceMapper;
import com.ruoyi.parking.service.IParkingLotService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingLotServiceImpl implements IParkingLotService
{
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;

    public ParkingLotServiceImpl(ParkingLotMapper parkingLotMapper, ParkingSpaceMapper parkingSpaceMapper)
    {
        this.parkingLotMapper = parkingLotMapper;
        this.parkingSpaceMapper = parkingSpaceMapper;
    }

    @Override
    public ParkingLot selectParkingLotById(Long lotId)
    {
        return parkingLotMapper.selectParkingLotById(lotId);
    }

    @Override
    public List<ParkingLot> selectParkingLotList(ParkingLot parkingLot)
    {
        return parkingLotMapper.selectParkingLotList(parkingLot);
    }

    @Override
    public List<ParkingLot> selectParkingLotOptions()
    {
        return parkingLotMapper.selectParkingLotOptions();
    }

    @Override
    public int insertParkingLot(ParkingLot parkingLot)
    {
        if (StringUtils.isEmpty(parkingLot.getStatus()))
        {
            parkingLot.setStatus("0");
        }
        if (parkingLot.getMonthlyPrice() == null)
        {
            parkingLot.setMonthlyPrice(BigDecimal.ZERO);
        }
        if (parkingLot.getTempHourPrice() == null)
        {
            parkingLot.setTempHourPrice(BigDecimal.ZERO);
        }
        if (parkingLot.getTotalSpaceCount() == null || parkingLot.getTotalSpaceCount() < 1)
        {
            throw new ServiceException("总车位数必须大于 0");
        }
        parkingLot.setAvailableSpaceCount(parkingLot.getTotalSpaceCount());
        return parkingLotMapper.insertParkingLot(parkingLot);
    }

    @Override
    public int updateParkingLot(ParkingLot parkingLot)
    {
        ParkingLot current = parkingLotMapper.selectParkingLotById(parkingLot.getLotId());
        if (current == null)
        {
            throw new ServiceException("停车场不存在");
        }
        if (StringUtils.isEmpty(parkingLot.getStatus()))
        {
            parkingLot.setStatus(current.getStatus());
        }
        if (parkingLot.getMonthlyPrice() == null)
        {
            parkingLot.setMonthlyPrice(current.getMonthlyPrice());
        }
        if (parkingLot.getTempHourPrice() == null)
        {
            parkingLot.setTempHourPrice(current.getTempHourPrice());
        }
        if (parkingLot.getTotalSpaceCount() == null || parkingLot.getTotalSpaceCount() < 1)
        {
            throw new ServiceException("总车位数必须大于 0");
        }
        int existingSpaces = parkingSpaceMapper.countParkingSpaceByLotId(parkingLot.getLotId());
        if (parkingLot.getTotalSpaceCount() < existingSpaces)
        {
            throw new ServiceException("总车位数不能小于已录入的车位数（当前 " + existingSpaces + " 个）");
        }
        int occupied = parkingSpaceMapper.countOccupiedParkingSpaceByLotId(parkingLot.getLotId());
        parkingLot.setAvailableSpaceCount(parkingLot.getTotalSpaceCount() - occupied);
        return parkingLotMapper.updateParkingLot(parkingLot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteParkingLotByIds(Long[] lotIds)
    {
        if (parkingSpaceMapper.countParkingSpaceByLotIds(lotIds) > 0)
        {
            throw new ServiceException("所选停车场下仍存在车位，不能删除");
        }
        return parkingLotMapper.deleteParkingLotByIds(lotIds);
    }
}
