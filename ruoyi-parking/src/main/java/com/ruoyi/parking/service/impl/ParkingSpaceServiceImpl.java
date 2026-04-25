package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingSpaceMapper;
import com.ruoyi.parking.service.IParkingSpaceService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingSpaceServiceImpl implements IParkingSpaceService
{
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final ParkingLotMapper parkingLotMapper;

    public ParkingSpaceServiceImpl(ParkingSpaceMapper parkingSpaceMapper, ParkingLotMapper parkingLotMapper)
    {
        this.parkingSpaceMapper = parkingSpaceMapper;
        this.parkingLotMapper = parkingLotMapper;
    }

    @Override
    public ParkingSpace selectParkingSpaceById(Long spaceId)
    {
        return parkingSpaceMapper.selectParkingSpaceById(spaceId);
    }

    @Override
    public List<ParkingSpace> selectParkingSpaceList(ParkingSpace parkingSpace)
    {
        return parkingSpaceMapper.selectParkingSpaceList(parkingSpace);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertParkingSpace(ParkingSpace parkingSpace)
    {
        applySpaceInsertDefaults(parkingSpace);
        ParkingLot parkingLot = validateLotExists(parkingSpace.getLotId());
        validateCapacity(parkingLot);
        validateUniqueSpaceCode(parkingSpace);
        int rows = parkingSpaceMapper.insertParkingSpace(parkingSpace);
        syncLotSpaceStats(parkingSpace.getLotId(), parkingSpace.getCreateBy());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateParkingSpace(ParkingSpace parkingSpace)
    {
        ParkingSpace current = parkingSpaceMapper.selectParkingSpaceById(parkingSpace.getSpaceId());
        if (current == null)
        {
            throw new ServiceException("车位不存在");
        }
        mergeSpaceUpdateDefaults(parkingSpace, current);
        validateLotExists(parkingSpace.getLotId());
        validateUniqueSpaceCode(parkingSpace);
        int rows = parkingSpaceMapper.updateParkingSpace(parkingSpace);
        syncLotSpaceStats(current.getLotId(), parkingSpace.getUpdateBy());
        if (!Objects.equals(current.getLotId(), parkingSpace.getLotId()))
        {
            syncLotSpaceStats(parkingSpace.getLotId(), parkingSpace.getUpdateBy());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteParkingSpaceByIds(Long[] spaceIds)
    {
        List<ParkingSpace> spaces = parkingSpaceMapper.selectParkingSpaceByIds(spaceIds);
        int rows = parkingSpaceMapper.deleteParkingSpaceByIds(spaceIds);
        Set<Long> lotIds = new LinkedHashSet<>();
        for (ParkingSpace parkingSpace : spaces)
        {
            lotIds.add(parkingSpace.getLotId());
        }
        for (Long lotId : lotIds)
        {
            syncLotSpaceStats(lotId, null);
        }
        return rows;
    }

    private ParkingLot validateLotExists(Long lotId)
    {
        ParkingLot parkingLot = parkingLotMapper.selectParkingLotById(lotId);
        if (parkingLot == null)
        {
            throw new ServiceException("停车场不存在");
        }
        return parkingLot;
    }

    private void validateCapacity(ParkingLot parkingLot)
    {
        Integer total = parkingLot.getTotalSpaceCount();
        if (total == null || total < 1)
        {
            return;
        }
        int existing = parkingSpaceMapper.countParkingSpaceByLotId(parkingLot.getLotId());
        if (existing >= total)
        {
            throw new ServiceException("已达到该停车场的总车位数（" + total + " 个），无法再新增");
        }
    }

    private void validateUniqueSpaceCode(ParkingSpace parkingSpace)
    {
        if (parkingSpace.getLotId() == null || StringUtils.isEmpty(parkingSpace.getSpaceCode()))
        {
            throw new ServiceException("停车场和车位编码不能为空");
        }
        ParkingSpace existing = parkingSpaceMapper.selectParkingSpaceByLotIdAndSpaceCode(
            parkingSpace.getLotId(),
            parkingSpace.getSpaceCode()
        );
        if (existing != null && !Objects.equals(existing.getSpaceId(), parkingSpace.getSpaceId()))
        {
            throw new ServiceException("同一停车场下车位编码已存在");
        }
    }

    private void syncLotSpaceStats(Long lotId, String updateBy)
    {
        if (lotId == null)
        {
            return;
        }
        ParkingLot parkingLot = parkingLotMapper.selectParkingLotById(lotId);
        if (parkingLot == null || parkingLot.getTotalSpaceCount() == null)
        {
            return;
        }
        int occupied = parkingSpaceMapper.countOccupiedParkingSpaceByLotId(lotId);
        int availableSpaceCount = Math.max(parkingLot.getTotalSpaceCount() - occupied, 0);
        parkingLotMapper.updateParkingLotSpaceStats(lotId, availableSpaceCount, updateBy);
    }

    private void applySpaceInsertDefaults(ParkingSpace parkingSpace)
    {
        if (StringUtils.isEmpty(parkingSpace.getSpaceType()))
        {
            parkingSpace.setSpaceType("1");
        }
        if (StringUtils.isEmpty(parkingSpace.getStatus()))
        {
            parkingSpace.setStatus("0");
        }
    }

    private void mergeSpaceUpdateDefaults(ParkingSpace parkingSpace, ParkingSpace current)
    {
        if (parkingSpace.getLotId() == null)
        {
            parkingSpace.setLotId(current.getLotId());
        }
        if (StringUtils.isEmpty(parkingSpace.getSpaceCode()))
        {
            parkingSpace.setSpaceCode(current.getSpaceCode());
        }
        if (StringUtils.isEmpty(parkingSpace.getSpaceType()))
        {
            parkingSpace.setSpaceType(current.getSpaceType());
        }
        if (StringUtils.isEmpty(parkingSpace.getStatus()))
        {
            parkingSpace.setStatus(current.getStatus());
        }
    }
}
