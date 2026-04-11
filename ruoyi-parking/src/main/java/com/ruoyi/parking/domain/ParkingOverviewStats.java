package com.ruoyi.parking.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class ParkingOverviewStats implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long lotCount;

    private Long totalSpaceCount;

    private Long availableSpaceCount;

    private Long occupiedSpaceCount;

    private Long disabledSpaceCount;

    private Long lockedSpaceCount;

    private Long todayEntryCount;

    private Long todayExitCount;

    private BigDecimal todayRevenue;

    private Long activeTempOrderCount;

    public Long getLotCount()
    {
        return lotCount;
    }

    public void setLotCount(Long lotCount)
    {
        this.lotCount = lotCount;
    }

    public Long getTotalSpaceCount()
    {
        return totalSpaceCount;
    }

    public void setTotalSpaceCount(Long totalSpaceCount)
    {
        this.totalSpaceCount = totalSpaceCount;
    }

    public Long getAvailableSpaceCount()
    {
        return availableSpaceCount;
    }

    public void setAvailableSpaceCount(Long availableSpaceCount)
    {
        this.availableSpaceCount = availableSpaceCount;
    }

    public Long getOccupiedSpaceCount()
    {
        return occupiedSpaceCount;
    }

    public void setOccupiedSpaceCount(Long occupiedSpaceCount)
    {
        this.occupiedSpaceCount = occupiedSpaceCount;
    }

    public Long getDisabledSpaceCount()
    {
        return disabledSpaceCount;
    }

    public void setDisabledSpaceCount(Long disabledSpaceCount)
    {
        this.disabledSpaceCount = disabledSpaceCount;
    }

    public Long getLockedSpaceCount()
    {
        return lockedSpaceCount;
    }

    public void setLockedSpaceCount(Long lockedSpaceCount)
    {
        this.lockedSpaceCount = lockedSpaceCount;
    }

    public Long getTodayEntryCount()
    {
        return todayEntryCount;
    }

    public void setTodayEntryCount(Long todayEntryCount)
    {
        this.todayEntryCount = todayEntryCount;
    }

    public Long getTodayExitCount()
    {
        return todayExitCount;
    }

    public void setTodayExitCount(Long todayExitCount)
    {
        this.todayExitCount = todayExitCount;
    }

    public BigDecimal getTodayRevenue()
    {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue)
    {
        this.todayRevenue = todayRevenue;
    }

    public Long getActiveTempOrderCount()
    {
        return activeTempOrderCount;
    }

    public void setActiveTempOrderCount(Long activeTempOrderCount)
    {
        this.activeTempOrderCount = activeTempOrderCount;
    }
}
