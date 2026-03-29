package com.ruoyi.parking.domain;

/**
 * Minimal parking module health domain object.
 */
public class ParkingHealth
{
    private String module;

    private String status;

    public String getModule()
    {
        return module;
    }

    public void setModule(String module)
    {
        this.module = module;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
