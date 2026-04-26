package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;

public class ParkingUserVehicle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long vehicleId;

    private Long customerId;

    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    @NotBlank(message = "车辆类型不能为空")
    private String vehicleType;

    private String brandName;

    private String vehicleColor;

    private String isDefault;

    private String status;

    private Date bindTime;

    private String delFlag;

    /** Transient — joined from parking_customer for list view */
    private String customerName;

    /** Transient — joined from parking_customer for list view */
    private String customerCode;

    /** Transient — joined from parking_customer for list view */
    private String mobile;

    public Long getVehicleId()
    {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId)
    {
        this.vehicleId = vehicleId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getPlateNo()
    {
        return plateNo;
    }

    public void setPlateNo(String plateNo)
    {
        this.plateNo = plateNo;
    }

    public String getVehicleType()
    {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType)
    {
        this.vehicleType = vehicleType;
    }

    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    public String getVehicleColor()
    {
        return vehicleColor;
    }

    public void setVehicleColor(String vehicleColor)
    {
        this.vehicleColor = vehicleColor;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getBindTime()
    {
        return bindTime;
    }

    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
}
