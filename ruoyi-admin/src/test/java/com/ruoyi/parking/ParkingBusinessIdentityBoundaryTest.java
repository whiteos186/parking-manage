package com.ruoyi.parking;

import com.ruoyi.parking.domain.ParkingLotAdmin;
import com.ruoyi.parking.domain.ParkingMembershipOrder;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.domain.ParkingPaymentRecord;
import com.ruoyi.parking.domain.ParkingTempOrder;
import com.ruoyi.parking.domain.ParkingUserVehicle;
import org.junit.jupiter.api.Test;

class ParkingBusinessIdentityBoundaryTest
{
    @Test
    void customerFacingBusinessObjectsUseParkingCustomerIdentity()
    {
        ParkingTestBeanProperties.assertHasProperty(ParkingUserVehicle.class, "customerId");
        ParkingTestBeanProperties.assertNoProperty(ParkingUserVehicle.class, "userId");

        ParkingTestBeanProperties.assertHasProperty(ParkingMonthlyOrder.class, "customerId");
        ParkingTestBeanProperties.assertNoProperty(ParkingMonthlyOrder.class, "userId");

        ParkingTestBeanProperties.assertHasProperty(ParkingMembershipOrder.class, "customerId");
        ParkingTestBeanProperties.assertNoProperty(ParkingMembershipOrder.class, "userId");

        ParkingTestBeanProperties.assertHasProperty(ParkingTempOrder.class, "customerId");
        ParkingTestBeanProperties.assertNoProperty(ParkingTempOrder.class, "userId");

        ParkingTestBeanProperties.assertHasProperty(ParkingPaymentRecord.class, "customerId");
        ParkingTestBeanProperties.assertNoProperty(ParkingPaymentRecord.class, "userId");
    }

    @Test
    void lotAdminBindingsStayOnInternalUserAccounts()
    {
        ParkingTestBeanProperties.assertHasProperty(ParkingLotAdmin.class, "userId");
        ParkingTestBeanProperties.assertNoProperty(ParkingLotAdmin.class, "customerId");
    }
}
