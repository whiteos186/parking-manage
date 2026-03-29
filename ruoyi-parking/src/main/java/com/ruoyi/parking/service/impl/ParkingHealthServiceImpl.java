package com.ruoyi.parking.service.impl;

import com.ruoyi.parking.domain.ParkingHealth;
import com.ruoyi.parking.service.IParkingHealthService;
import org.springframework.stereotype.Service;

@Service
public class ParkingHealthServiceImpl implements IParkingHealthService
{
    @Override
    public ParkingHealth getHealth()
    {
        ParkingHealth health = new ParkingHealth();
        health.setModule("parking");
        health.setStatus("UP");
        return health;
    }
}
