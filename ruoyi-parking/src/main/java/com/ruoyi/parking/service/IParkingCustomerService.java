package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.ParkingCustomer;
import java.util.List;

public interface IParkingCustomerService
{
    ParkingCustomer selectParkingCustomerById(Long customerId);

    List<ParkingCustomer> selectParkingCustomerList(ParkingCustomer parkingCustomer);

    List<ParkingCustomer> selectParkingCustomerOptions();

    int insertParkingCustomer(ParkingCustomer parkingCustomer);

    int updateParkingCustomer(ParkingCustomer parkingCustomer);

    int deleteParkingCustomerByIds(Long[] customerIds);
}
