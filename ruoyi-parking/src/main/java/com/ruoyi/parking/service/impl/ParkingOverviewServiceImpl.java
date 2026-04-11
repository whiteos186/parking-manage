package com.ruoyi.parking.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingRecentOrder;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.mapper.ParkingOverviewMapper;
import com.ruoyi.parking.service.IParkingOverviewService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParkingOverviewServiceImpl implements IParkingOverviewService
{
    private final ParkingOverviewMapper parkingOverviewMapper;
    private final ParkingLotAdminMapper parkingLotAdminMapper;

    public ParkingOverviewServiceImpl(ParkingOverviewMapper parkingOverviewMapper,
                                      ParkingLotAdminMapper parkingLotAdminMapper)
    {
        this.parkingOverviewMapper = parkingOverviewMapper;
        this.parkingLotAdminMapper = parkingLotAdminMapper;
    }

    @Override
    public ParkingOverviewStats selectOverviewStats()
    {
        List<Long> lotIds = resolveLotIds();
        ParkingOverviewStats stats = parkingOverviewMapper.selectOverviewStats(lotIds);
        if (stats == null)
        {
            stats = new ParkingOverviewStats();
            stats.setLotCount(0L);
            stats.setTotalSpaceCount(0L);
            stats.setAvailableSpaceCount(0L);
            stats.setOccupiedSpaceCount(0L);
            stats.setDisabledSpaceCount(0L);
            stats.setLockedSpaceCount(0L);
            stats.setTodayEntryCount(0L);
            stats.setTodayExitCount(0L);
            stats.setTodayRevenue(BigDecimal.ZERO);
            stats.setActiveTempOrderCount(0L);
        }
        return stats;
    }

    @Override
    public List<ParkingRecentOrder> selectRecentOrders(int limit)
    {
        if (limit <= 0)
        {
            return Collections.emptyList();
        }
        List<Long> lotIds = resolveLotIds();
        List<ParkingRecentOrder> rows = parkingOverviewMapper.selectRecentOrders(limit, lotIds);
        return rows != null ? rows : Collections.emptyList();
    }

    private List<Long> resolveLotIds()
    {
        if (SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            return null;
        }
        List<Long> ids = parkingLotAdminMapper.selectLotIdsByUserId(SecurityUtils.getUserId());
        return (ids != null && !ids.isEmpty()) ? ids : null;
    }
}
