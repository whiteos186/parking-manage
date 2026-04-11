package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;

public class ParkingLotAdmin extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long lotAdminId;

    @NotNull(message = "停车场编号不能为空")
    private Long lotId;

    @NotNull(message = "用户编号不能为空")
    private Long userId;

    private String status;

    /** 关联查询字段：来自 parking_lot */
    private transient String lotName;

    /** 关联查询字段：来自 sys_user */
    private transient String userName;

    /** 关联查询字段：来自 sys_user */
    private transient String nickName;

    public Long getLotAdminId()
    {
        return lotAdminId;
    }

    public void setLotAdminId(Long lotAdminId)
    {
        this.lotAdminId = lotAdminId;
    }

    public Long getLotId()
    {
        return lotId;
    }

    public void setLotId(Long lotId)
    {
        this.lotId = lotId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getLotName()
    {
        return lotName;
    }

    public void setLotName(String lotName)
    {
        this.lotName = lotName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }
}
