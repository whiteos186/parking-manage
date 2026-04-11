package com.ruoyi.parking.mapper;

import com.ruoyi.parking.domain.ParkingCustomer;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ParkingCustomerMapper
{
    ParkingCustomer selectParkingCustomerById(Long customerId);

    ParkingCustomer selectParkingCustomerByCode(@Param("customerCode") String customerCode);

    List<ParkingCustomer> selectParkingCustomerList(ParkingCustomer parkingCustomer);

    List<ParkingCustomer> selectParkingCustomerOptions();

    int insertParkingCustomer(ParkingCustomer parkingCustomer);

    int updateParkingCustomer(ParkingCustomer parkingCustomer);

    int deleteParkingCustomerByIds(@Param("customerIds") Long[] customerIds);
}
