package com.ruoyi.parking.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.domain.ParkingLotOverview;
import com.ruoyi.parking.domain.ParkingOverviewStats;
import com.ruoyi.parking.domain.ParkingRecentOrder;
import com.ruoyi.parking.mapper.ParkingLotAdminMapper;
import com.ruoyi.parking.mapper.ParkingOverviewMapper;
import com.ruoyi.parking.service.IParkingOverviewService;
import com.ruoyi.parking.util.ParkingAuthUtils;
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
        if (isEmptyScope(lotIds))
        {
            return emptyStats();
        }
        ParkingOverviewStats stats = parkingOverviewMapper.selectOverviewStats(lotIds);
        if (stats == null)
        {
            stats = emptyStats();
        }
        return stats;
    }

    @Override
    public List<ParkingLotOverview> selectTopLots(int limit)
    {
        if (limit <= 0)
        {
            return Collections.emptyList();
        }
        List<Long> lotIds = resolveLotIds();
        if (isEmptyScope(lotIds))
        {
            return Collections.emptyList();
        }
        List<ParkingLotOverview> rows = parkingOverviewMapper.selectTopLots(limit, lotIds);
        return rows != null ? rows : Collections.emptyList();
    }

    @Override
    public List<ParkingRecentOrder> selectRecentOrders(int limit)
    {
        if (limit <= 0)
        {
            return Collections.emptyList();
        }
        List<Long> lotIds = resolveLotIds();
        if (isEmptyScope(lotIds))
        {
            return Collections.emptyList();
        }
        List<ParkingRecentOrder> rows = parkingOverviewMapper.selectRecentOrders(limit, lotIds);
        return rows != null ? rows : Collections.emptyList();
    }

    private List<Long> resolveLotIds()
    {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId))
        {
            return null;
        }
        if (!ParkingAuthUtils.isLotAdmin())
        {
            return Collections.emptyList();
        }
        List<Long> ids = parkingLotAdminMapper.selectLotIdsByUserId(userId);
        return ids != null ? ids : Collections.emptyList();
    }

    private boolean isEmptyScope(List<Long> lotIds)
    {
        return lotIds != null && lotIds.isEmpty();
    }

    private ParkingOverviewStats emptyStats()
    {
        ParkingOverviewStats stats = new ParkingOverviewStats();
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
        return stats;
    }
}
