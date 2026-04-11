package com.ruoyi.parking.service;

public interface IParkingUserConfigService
{
    /**
     * 查询当前用户指定键的配置值
     */
    String getConfigValue(Long userId, String configKey);

    /**
     * 保存当前用户指定键的配置值
     */
    void saveConfigValue(Long userId, String configKey, String configValue);
}
