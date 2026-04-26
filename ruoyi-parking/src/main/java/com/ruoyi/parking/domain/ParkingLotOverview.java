package com.ruoyi.parking.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Parking lot occupancy projection for the workbench.
 */
public class ParkingLotOverview implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long lotId;

    private String lotName;

    private Long totalSpaceCount;

    private Long availableSpaceCount;

    private Long occupiedSpaceCount;

    private Long disabledSpaceCount;

    private Long lockedSpaceCount;

    private BigDecimal occupancyRate;

    public Long getLotId()
    {
        return lotId;
    }

    public void setLotId(Long lotId)
    {
        this.lotId = lotId;
    }

    public String getLotName()
    {
        return lotName;
    }

    public void setLotName(String lotName)
    {
        this.lotName = lotName;
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

    public BigDecimal getOccupancyRate()
    {
        return occupancyRate;
    }

    public void setOccupancyRate(BigDecimal occupancyRate)
    {
        this.occupancyRate = occupancyRate;
    }
}
