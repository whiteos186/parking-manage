package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingMonthlyOrderMapper;
import com.ruoyi.parking.mapper.ParkingUserVehicleMapper;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import com.ruoyi.parking.service.IParkingMonthlyOrderService;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import com.ruoyi.parking.util.ParkingMemberDiscountUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingMonthlyOrderServiceImpl implements IParkingMonthlyOrderService
{
    private final ParkingMonthlyOrderMapper parkingMonthlyOrderMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;
    private final ParkingUserVehicleMapper parkingUserVehicleMapper;
    private final IParkingPaymentRecordService parkingPaymentRecordService;
    private final IParkingGlobalRuleService parkingGlobalRuleService;

    public ParkingMonthlyOrderServiceImpl(
        ParkingMonthlyOrderMapper parkingMonthlyOrderMapper,
        ParkingLotMapper parkingLotMapper,
        ParkingCustomerMapper parkingCustomerMapper,
        ParkingUserVehicleMapper parkingUserVehicleMapper,
        IParkingPaymentRecordService parkingPaymentRecordService,
        IParkingGlobalRuleService parkingGlobalRuleService)
    {
        this.parkingMonthlyOrderMapper = parkingMonthlyOrderMapper;
        this.parkingLotMapper = parkingLotMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
        this.parkingUserVehicleMapper = parkingUserVehicleMapper;
        this.parkingPaymentRecordService = parkingPaymentRecordService;
        this.parkingGlobalRuleService = parkingGlobalRuleService;
    }

    @Override
    public ParkingMonthlyOrder selectParkingMonthlyOrderById(Long monthlyOrderId)
    {
        return parkingMonthlyOrderMapper.selectParkingMonthlyOrderById(monthlyOrderId);
    }

    @Override
    public List<ParkingMonthlyOrder> selectParkingMonthlyOrderList(ParkingMonthlyOrder parkingMonthlyOrder)
    {
        return parkingMonthlyOrderMapper.selectParkingMonthlyOrderList(parkingMonthlyOrder);
    }

    @Override
    public int insertParkingMonthlyOrder(ParkingMonthlyOrder parkingMonthlyOrder)
    {
        if (StringUtils.isEmpty(parkingMonthlyOrder.getOrderNo()))
        {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String suffix = String.format("%04d", new Random().nextInt(10000));
            parkingMonthlyOrder.setOrderNo("MO" + timestamp + suffix);
        }
        if (StringUtils.isEmpty(parkingMonthlyOrder.getPayStatus()))
        {
            parkingMonthlyOrder.setPayStatus("0");
        }
        if (StringUtils.isEmpty(parkingMonthlyOrder.getBizStatus()))
        {
            parkingMonthlyOrder.setBizStatus("0");
        }
        if (parkingMonthlyOrder.getMonthCount() == null || parkingMonthlyOrder.getMonthCount() < 1)
        {
            parkingMonthlyOrder.setMonthCount(1);
        }
        if (parkingMonthlyOrder.getLotId() == null
            || parkingLotMapper.selectParkingLotById(parkingMonthlyOrder.getLotId()) == null)
        {
            throw new ServiceException("停车场不存在");
        }
        if (parkingMonthlyOrder.getCustomerId() == null
            || parkingCustomerMapper.selectParkingCustomerById(parkingMonthlyOrder.getCustomerId()) == null)
        {
            throw new ServiceException("Customer is required");
        }
        if (parkingMonthlyOrder.getStartTime() == null)
        {
            parkingMonthlyOrder.setStartTime(LocalDateTime.now());
        }
        if (parkingMonthlyOrder.getEndTime() == null)
        {
            parkingMonthlyOrder.setEndTime(
                parkingMonthlyOrder.getStartTime().plusMonths(parkingMonthlyOrder.getMonthCount()));
        }

        if (parkingMonthlyOrder.getVehicleId() != null)
        {
            com.ruoyi.parking.domain.ParkingUserVehicle vehicle =
                parkingUserVehicleMapper.selectParkingUserVehicleById(parkingMonthlyOrder.getVehicleId());
            if (vehicle == null || !Objects.equals(vehicle.getCustomerId(), parkingMonthlyOrder.getCustomerId()))
            {
                throw new ServiceException("Vehicle does not belong to order customer");
            }
            if (StringUtils.isEmpty(parkingMonthlyOrder.getVehiclePlateNo()) && !StringUtils.isEmpty(vehicle.getPlateNo()))
            {
                parkingMonthlyOrder.setVehiclePlateNo(vehicle.getPlateNo());
            }
        }
        recalcPricing(parkingMonthlyOrder);
        return parkingMonthlyOrderMapper.insertParkingMonthlyOrder(parkingMonthlyOrder);
    }

    @Override
    public int updateParkingMonthlyOrder(ParkingMonthlyOrder parkingMonthlyOrder)
    {
        ParkingMonthlyOrder current = parkingMonthlyOrderMapper.selectParkingMonthlyOrderById(
            parkingMonthlyOrder.getMonthlyOrderId());
        if (current == null)
        {
            throw new ServiceException("月卡订单不存在");
        }
        if (StringUtils.isEmpty(parkingMonthlyOrder.getPayStatus()))
        {
            parkingMonthlyOrder.setPayStatus(current.getPayStatus());
        }
        if (StringUtils.isEmpty(parkingMonthlyOrder.getBizStatus()))
        {
            parkingMonthlyOrder.setBizStatus(current.getBizStatus());
        }
        if (parkingMonthlyOrder.getMonthCount() == null)
        {
            parkingMonthlyOrder.setMonthCount(current.getMonthCount());
        }
        if (!Objects.equals(current.getLotId(), parkingMonthlyOrder.getLotId())
            && parkingLotMapper.selectParkingLotById(parkingMonthlyOrder.getLotId()) == null)
        {
            throw new ServiceException("停车场不存在");
        }

        boolean priceKeysChanged = !Objects.equals(current.getCustomerId(), parkingMonthlyOrder.getCustomerId())
            || !Objects.equals(current.getMonthCount(), parkingMonthlyOrder.getMonthCount());
        if (priceKeysChanged)
        {
            parkingMonthlyOrder.setOriginalAmount(null);
            parkingMonthlyOrder.setDiscountAmount(null);
            parkingMonthlyOrder.setPayAmount(null);
            recalcPricing(parkingMonthlyOrder);
        }

        return parkingMonthlyOrderMapper.updateParkingMonthlyOrder(parkingMonthlyOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payMonthlyOrder(ParkingMonthlyOrder form)
    {
        ParkingMonthlyOrder current = parkingMonthlyOrderMapper.selectParkingMonthlyOrderById(
            form.getMonthlyOrderId());
        if (current == null)
        {
            throw new ServiceException("订单不存在");
        }
        if ("1".equals(current.getPayStatus()))
        {
            throw new ServiceException("订单已支付");
        }
        current.setPayStatus("1");
        current.setBizStatus("1");
        current.setPayTime(LocalDateTime.now());
        if (form.getPayAmount() != null)
        {
            current.setPayAmount(form.getPayAmount());
        }

        int rows = parkingMonthlyOrderMapper.updateParkingMonthlyOrder(current);
        BigDecimal cascadeAmount = current.getPayAmount() != null ? current.getPayAmount() : BigDecimal.ZERO;
        if (cascadeAmount.compareTo(BigDecimal.ZERO) > 0)
        {
            parkingPaymentRecordService.createPaymentForOrder(
                current.getOrderNo(),
                "2",
                current.getCustomerId(),
                current.getLotId(),
                cascadeAmount,
                null,
                form.getUpdateBy()
            );
        }
        if (current.getLotId() != null)
        {
            com.ruoyi.parking.domain.ParkingLot lot = parkingLotMapper.selectParkingLotById(current.getLotId());
            if (lot != null)
            {
                int available = lot.getAvailableSpaceCount() != null ? lot.getAvailableSpaceCount() : 0;
                lot.setAvailableSpaceCount(Math.max(available - 1, 0));
                parkingLotMapper.updateParkingLot(lot);
            }
        }
        return rows;
    }

    @Override
    public int cancelMonthlyOrder(ParkingMonthlyOrder form)
    {
        ParkingMonthlyOrder current = parkingMonthlyOrderMapper.selectParkingMonthlyOrderById(
            form.getMonthlyOrderId());
        if (current == null)
        {
            throw new ServiceException("订单不存在");
        }
        if ("3".equals(current.getBizStatus()))
        {
            throw new ServiceException("订单已取消");
        }
        boolean wasPaid = "1".equals(current.getPayStatus());
        if (wasPaid)
        {
            current.setPayStatus("2");
        }
        current.setBizStatus("3");
        current.setCancelTime(LocalDateTime.now());
        int rows = parkingMonthlyOrderMapper.updateParkingMonthlyOrder(current);
        if (wasPaid && current.getLotId() != null)
        {
            com.ruoyi.parking.domain.ParkingLot lot = parkingLotMapper.selectParkingLotById(current.getLotId());
            if (lot != null)
            {
                int available = lot.getAvailableSpaceCount() != null ? lot.getAvailableSpaceCount() : 0;
                lot.setAvailableSpaceCount(available + 1);
                parkingLotMapper.updateParkingLot(lot);
            }
        }
        return rows;
    }

    @Override
    public int deleteParkingMonthlyOrderByIds(Long[] monthlyOrderIds)
    {
        return parkingMonthlyOrderMapper.deleteParkingMonthlyOrderByIds(monthlyOrderIds);
    }

    private void recalcPricing(ParkingMonthlyOrder order)
    {
        ParkingSettingsDto.Pricing pricing = parkingGlobalRuleService.getValidatedPricing();
        ParkingSettingsDto.MemberDiscount memberDiscount = parkingGlobalRuleService.getValidatedMemberDiscount();

        if (order.getOriginalAmount() == null)
        {
            int months = order.getMonthCount() != null ? order.getMonthCount() : 1;
            order.setOriginalAmount(pricing.getMonthlyPrice().multiply(BigDecimal.valueOf(months)));
        }

        if (order.getDiscountAmount() == null && order.getOriginalAmount() != null
            && order.getCustomerId() != null)
        {
            ParkingCustomer customer = parkingCustomerMapper.selectParkingCustomerById(order.getCustomerId());
            BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(
                order.getOriginalAmount(),
                customer,
                memberDiscount
            );
            order.setDiscountAmount(discount);
        }

        if (order.getPayAmount() == null && order.getOriginalAmount() != null)
        {
            BigDecimal discount = order.getDiscountAmount() != null
                ? order.getDiscountAmount()
                : BigDecimal.ZERO;
            order.setPayAmount(order.getOriginalAmount().subtract(discount).max(BigDecimal.ZERO));
        }
    }
}
