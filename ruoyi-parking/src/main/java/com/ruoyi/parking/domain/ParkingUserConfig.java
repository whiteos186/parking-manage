package com.ruoyi.parking.domain;

import java.util.Date;

/**
 * 用户个人配置
 */
public class ParkingUserConfig
{
    private static final long serialVersionUID = 1L;

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 配置键 */
    private String configKey;

    /** 配置值（JSON字符串） */
    private String configValue;

    private Date updateTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    public String getConfigValue()
    {
        return configValue;
    }

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}
