package com.ruoyi.parking.service.impl;

import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.mapper.ParkingOverviewMapper;
import com.ruoyi.parking.service.IParkingOverviewService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParkingOverviewServiceImpl implements IParkingOverviewService
{
    private final ParkingOverviewMapper parkingOverviewMapper;

    public ParkingOverviewServiceImpl(ParkingOverviewMapper parkingOverviewMapper)
    {
        this.parkingOverviewMapper = parkingOverviewMapper;
    }

    @Override
    public ParkingOverviewStats selectOverviewStats()
    {
        ParkingOverviewStats stats = parkingOverviewMapper.selectOverviewStats();
        if (stats == null)
        {
            stats = new ParkingOverviewStats();
            stats.setLotCount(0L);
            stats.setTotalSpaceCount(0L);
            stats.setAvailableSpaceCount(0L);
            stats.setOccupiedSpaceCount(0L);
            stats.setDisabledSpaceCount(0L);
            stats.setLockedSpaceCount(0L);
        }
        return stats;
    }

    @Override
    public List<ParkingLot> selectParkingLotList(ParkingLot parkingLot)
    {
        return parkingOverviewMapper.selectParkingLotList(parkingLot);
    }

    @Override
    public List<ParkingSpace> selectParkingSpaceList(ParkingSpace parkingSpace)
    {
        return parkingOverviewMapper.selectParkingSpaceList(parkingSpace);
    }
}
