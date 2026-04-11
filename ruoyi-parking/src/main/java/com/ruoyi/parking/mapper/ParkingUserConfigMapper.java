package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingUserConfig;
import org.apache.ibatis.annotations.Param;

public interface ParkingUserConfigMapper
{
    /**
     * 查询用户配置
     */
    ParkingUserConfig selectByUserIdAndKey(@Param("userId") Long userId, @Param("configKey") String configKey);

    /**
     * 新增或更新用户配置（ON DUPLICATE KEY UPDATE）
     */
    int upsert(ParkingUserConfig config);
}
