package com.ruoyi.parking.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;

public class ParkingCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long customerId;

    @NotBlank(message = "客户编号不能为空")
    private String customerCode;

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    @NotBlank(message = "手机号不能为空")
    private String mobile;

    private String customerType;

    /** 会员标识 (0 否, 1 是) */
    private String isMember;

    /** 会员等级 (1 银, 2 金, 3 铂金) */
    private String memberType;

    /** 会员到期时间 */
    private Date memberExpireTime;

    private String status;

    private String delFlag;

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getCustomerType()
    {
        return customerType;
    }

    public void setCustomerType(String customerType)
    {
        this.customerType = customerType;
    }

    public String getIsMember()
    {
        return isMember;
    }

    public void setIsMember(String isMember)
    {
        this.isMember = isMember;
    }

    public String getMemberType()
    {
        return memberType;
    }

    public void setMemberType(String memberType)
    {
        this.memberType = memberType;
    }

    public Date getMemberExpireTime()
    {
        return memberExpireTime;
    }

    public void setMemberExpireTime(Date memberExpireTime)
    {
        this.memberExpireTime = memberExpireTime;
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

    @Override
    public String toString()
    {
        return "ParkingCustomer{customerId=" + customerId
            + ", customerCode='" + customerCode + "'"
            + ", customerName='" + customerName + "'"
            + ", mobile='" + mobile + "'"
            + ", isMember='" + isMember + "'"
            + ", memberType='" + memberType + "'"
            + ", memberExpireTime=" + memberExpireTime
            + "}";
    }
}
