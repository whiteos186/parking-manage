package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class ParkingSpace extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long spaceId;

    private Long lotId;

    private String lotName;

    private String spaceCode;

    private String areaName;

    private String floorNo;

    private String spaceType;

    private String status;

    private String delFlag;

    public Long getSpaceId()
    {
        return spaceId;
    }

    public void setSpaceId(Long spaceId)
    {
        this.spaceId = spaceId;
    }

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

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    public String getAreaName()
    {
        return areaName;
    }

    public void setAreaName(String areaName)
    {
        this.areaName = areaName;
    }

    public String getFloorNo()
    {
        return floorNo;
    }

    public void setFloorNo(String floorNo)
    {
        this.floorNo = floorNo;
    }

    public String getSpaceType()
    {
        return spaceType;
    }

    public void setSpaceType(String spaceType)
    {
        this.spaceType = spaceType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
