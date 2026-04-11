package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.service.IParkingCustomerService;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ParkingCustomerServiceImpl implements IParkingCustomerService
{
    private final ParkingCustomerMapper parkingCustomerMapper;

    public ParkingCustomerServiceImpl(ParkingCustomerMapper parkingCustomerMapper)
    {
        this.parkingCustomerMapper = parkingCustomerMapper;
    }

    @Override
    public ParkingCustomer selectParkingCustomerById(Long customerId)
    {
        return parkingCustomerMapper.selectParkingCustomerById(customerId);
    }

    @Override
    public List<ParkingCustomer> selectParkingCustomerList(ParkingCustomer parkingCustomer)
    {
        return parkingCustomerMapper.selectParkingCustomerList(parkingCustomer);
    }

    @Override
    public List<ParkingCustomer> selectParkingCustomerOptions()
    {
        return parkingCustomerMapper.selectParkingCustomerOptions();
    }

    @Override
    public int insertParkingCustomer(ParkingCustomer parkingCustomer)
    {
        applyDefaults(parkingCustomer);
        validateUniqueCode(parkingCustomer);
        return parkingCustomerMapper.insertParkingCustomer(parkingCustomer);
    }

    @Override
    public int updateParkingCustomer(ParkingCustomer parkingCustomer)
    {
        ParkingCustomer current = parkingCustomerMapper.selectParkingCustomerById(parkingCustomer.getCustomerId());
        if (current == null)
        {
            throw new ServiceException("客户档案不存在");
        }
        if (StringUtils.isEmpty(parkingCustomer.getCustomerCode()))
        {
            parkingCustomer.setCustomerCode(current.getCustomerCode());
        }
        if (StringUtils.isEmpty(parkingCustomer.getStatus()))
        {
            parkingCustomer.setStatus(current.getStatus());
        }
        validateUniqueCode(parkingCustomer);
        return parkingCustomerMapper.updateParkingCustomer(parkingCustomer);
    }

    @Override
    public int deleteParkingCustomerByIds(Long[] customerIds)
    {
        return parkingCustomerMapper.deleteParkingCustomerByIds(customerIds);
    }

    private void applyDefaults(ParkingCustomer parkingCustomer)
    {
        if (StringUtils.isEmpty(parkingCustomer.getStatus()))
        {
            parkingCustomer.setStatus("0");
        }
        if (StringUtils.isEmpty(parkingCustomer.getCustomerType()))
        {
            parkingCustomer.setCustomerType("1");
        }
    }

    private void validateUniqueCode(ParkingCustomer parkingCustomer)
    {
        ParkingCustomer existing = parkingCustomerMapper.selectParkingCustomerByCode(parkingCustomer.getCustomerCode());
        if (existing != null && !Objects.equals(existing.getCustomerId(), parkingCustomer.getCustomerId()))
        {
            throw new ServiceException("客户编号已存在");
        }
    }
}
