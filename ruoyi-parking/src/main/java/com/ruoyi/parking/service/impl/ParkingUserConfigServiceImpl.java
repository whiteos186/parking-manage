package com.ruoyi.parking.service.impl;

import com.ruoyi.parking.domain.ParkingUserConfig;
import com.ruoyi.parking.mapper.ParkingUserConfigMapper;
import com.ruoyi.parking.service.IParkingUserConfigService;
import org.springframework.stereotype.Service;

@Service
public class ParkingUserConfigServiceImpl implements IParkingUserConfigService
{
    private final ParkingUserConfigMapper parkingUserConfigMapper;

    public ParkingUserConfigServiceImpl(ParkingUserConfigMapper parkingUserConfigMapper)
    {
        this.parkingUserConfigMapper = parkingUserConfigMapper;
    }

    @Override
    public String getConfigValue(Long userId, String configKey)
    {
        ParkingUserConfig config = parkingUserConfigMapper.selectByUserIdAndKey(userId, configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public void saveConfigValue(Long userId, String configKey, String configValue)
    {
        ParkingUserConfig config = new ParkingUserConfig();
        config.setUserId(userId);
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        parkingUserConfigMapper.upsert(config);
    }
}
