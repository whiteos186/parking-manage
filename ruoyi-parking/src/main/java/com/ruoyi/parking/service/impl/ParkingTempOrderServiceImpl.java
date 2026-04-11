package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingSpace;
import com.ruoyi.parking.domain.ParkingTempOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingSpaceMapper;
import com.ruoyi.parking.mapper.ParkingTempOrderMapper;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import com.ruoyi.parking.service.IParkingTempOrderService;
import com.ruoyi.parking.util.ParkingMemberDiscountUtils;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingTempOrderServiceImpl implements IParkingTempOrderService
{
    private final ParkingTempOrderMapper parkingTempOrderMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;
    private final IParkingPaymentRecordService parkingPaymentRecordService;

    public ParkingTempOrderServiceImpl(ParkingTempOrderMapper parkingTempOrderMapper,
        ParkingLotMapper parkingLotMapper,
        ParkingSpaceMapper parkingSpaceMapper,
        ParkingCustomerMapper parkingCustomerMapper,
        IParkingPaymentRecordService parkingPaymentRecordService)
    {
        this.parkingTempOrderMapper = parkingTempOrderMapper;
        this.parkingLotMapper = parkingLotMapper;
        this.parkingSpaceMapper = parkingSpaceMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
        this.parkingPaymentRecordService = parkingPaymentRecordService;
    }

    @Override
    public ParkingTempOrder selectParkingTempOrderById(Long tempOrderId)
    {
        return parkingTempOrderMapper.selectParkingTempOrderById(tempOrderId);
    }

    @Override
    public List<ParkingTempOrder> selectParkingTempOrderList(ParkingTempOrder parkingTempOrder)
    {
        return parkingTempOrderMapper.selectParkingTempOrderList(parkingTempOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertParkingTempOrder(ParkingTempOrder parkingTempOrder)
    {
        // Generate order number if not provided
        if (StringUtils.isEmpty(parkingTempOrder.getOrderNo()))
        {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String suffix = String.format("%04d", new Random().nextInt(10000));
            parkingTempOrder.setOrderNo("TO" + timestamp + suffix);
        }
        // Default inTime to now
        if (parkingTempOrder.getInTime() == null)
        {
            parkingTempOrder.setInTime(new Date());
        }
        // Apply status/amount defaults
        parkingTempOrder.setBizStatus("0");
        parkingTempOrder.setPayStatus("0");
        parkingTempOrder.setFeeAmount(BigDecimal.ZERO);
        parkingTempOrder.setDiscountAmount(BigDecimal.ZERO);
        parkingTempOrder.setPayAmount(BigDecimal.ZERO);
        if (parkingTempOrder.getParkingDurationMin() == null)
        {
            parkingTempOrder.setParkingDurationMin(0);
        }
        // Validate lot exists
        ParkingLot lot = parkingLotMapper.selectParkingLotById(parkingTempOrder.getLotId());
        if (lot == null)
        {
            throw new ServiceException("停车场不存在");
        }
        // Validate space and mark occupied
        if (parkingTempOrder.getSpaceId() != null)
        {
            ParkingSpace space = parkingSpaceMapper.selectParkingSpaceById(parkingTempOrder.getSpaceId());
            if (space == null || !parkingTempOrder.getLotId().equals(space.getLotId()))
            {
                throw new ServiceException("车位不存在或不属于所选停车场");
            }
            ParkingSpace spaceUpdate = new ParkingSpace();
            spaceUpdate.setSpaceId(space.getSpaceId());
            spaceUpdate.setLotId(space.getLotId());
            spaceUpdate.setSpaceCode(space.getSpaceCode());
            spaceUpdate.setSpaceType(space.getSpaceType());
            spaceUpdate.setStatus("1");
            spaceUpdate.setUpdateBy(parkingTempOrder.getCreateBy());
            parkingSpaceMapper.updateParkingSpace(spaceUpdate);
        }
        return parkingTempOrderMapper.insertParkingTempOrder(parkingTempOrder);
    }

    @Override
    public int updateParkingTempOrder(ParkingTempOrder parkingTempOrder)
    {
        ParkingTempOrder current = parkingTempOrderMapper.selectParkingTempOrderById(
            parkingTempOrder.getTempOrderId());
        if (current == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (StringUtils.isEmpty(parkingTempOrder.getVehiclePlateNo()))
        {
            parkingTempOrder.setVehiclePlateNo(current.getVehiclePlateNo());
        }
        if (parkingTempOrder.getLotId() == null)
        {
            parkingTempOrder.setLotId(current.getLotId());
        }
        if (StringUtils.isEmpty(parkingTempOrder.getPayStatus()))
        {
            parkingTempOrder.setPayStatus(current.getPayStatus());
        }
        if (StringUtils.isEmpty(parkingTempOrder.getBizStatus()))
        {
            parkingTempOrder.setBizStatus(current.getBizStatus());
        }
        return parkingTempOrderMapper.updateParkingTempOrder(parkingTempOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int entryParkingTempOrder(Long tempOrderId)
    {
        ParkingTempOrder current = parkingTempOrderMapper.selectParkingTempOrderById(tempOrderId);
        if (current == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"0".equals(current.getBizStatus()))
        {
            throw new ServiceException("订单状态不允许入场，当前状态：" + current.getBizStatus());
        }
        Date now = new Date();
        ParkingTempOrder update = new ParkingTempOrder();
        update.setTempOrderId(current.getTempOrderId());
        update.setInTime(now);
        update.setBizStatus("1");
        parkingTempOrderMapper.updateParkingTempOrder(update);
        // 标记车位为占用
        if (current.getSpaceId() != null)
        {
            ParkingSpace space = parkingSpaceMapper.selectParkingSpaceById(current.getSpaceId());
            if (space != null)
            {
                ParkingSpace spaceUpdate = new ParkingSpace();
                spaceUpdate.setSpaceId(space.getSpaceId());
                spaceUpdate.setLotId(space.getLotId());
                spaceUpdate.setSpaceCode(space.getSpaceCode());
                spaceUpdate.setSpaceType(space.getSpaceType());
                spaceUpdate.setStatus("1");
                parkingSpaceMapper.updateParkingSpace(spaceUpdate);
            }
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int exitParkingTempOrder(Long tempOrderId)
    {
        ParkingTempOrder current = parkingTempOrderMapper.selectParkingTempOrderById(tempOrderId);
        if (current == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"1".equals(current.getBizStatus()))
        {
            throw new ServiceException("订单状态不允许出场，当前状态：" + current.getBizStatus());
        }
        Date now = new Date();
        Date inTime = current.getInTime() != null ? current.getInTime() : now;
        long durationMs = now.getTime() - inTime.getTime();
        int durationMin = (int) Math.ceil(durationMs / 60000.0);

        // 按半小时向上取整计费
        ParkingLot lot = parkingLotMapper.selectParkingLotById(current.getLotId());
        BigDecimal hourPrice = (lot != null && lot.getTempHourPrice() != null)
            ? lot.getTempHourPrice()
            : BigDecimal.ZERO;
        long halfHours = (long) Math.ceil(durationMin / 30.0);
        if (halfHours < 1)
        {
            halfHours = 1;
        }
        BigDecimal feeAmount = hourPrice.multiply(BigDecimal.valueOf(halfHours)).divide(BigDecimal.valueOf(2));

        // 应用会员折扣
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (current.getCustomerId() != null)
        {
            ParkingCustomer customer = parkingCustomerMapper.selectParkingCustomerById(current.getCustomerId());
            discountAmount = ParkingMemberDiscountUtils.calculateDiscount(feeAmount, customer);
        }
        BigDecimal payAmount = feeAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        ParkingTempOrder update = new ParkingTempOrder();
        update.setTempOrderId(current.getTempOrderId());
        update.setOutTime(now);
        update.setParkingDurationMin(durationMin);
        update.setFeeAmount(feeAmount);
        update.setDiscountAmount(discountAmount);
        update.setPayAmount(payAmount);
        update.setBizStatus("2");
        parkingTempOrderMapper.updateParkingTempOrder(update);

        // 释放车位
        if (current.getSpaceId() != null)
        {
            ParkingSpace space = parkingSpaceMapper.selectParkingSpaceById(current.getSpaceId());
            if (space != null)
            {
                ParkingSpace spaceUpdate = new ParkingSpace();
                spaceUpdate.setSpaceId(space.getSpaceId());
                spaceUpdate.setLotId(space.getLotId());
                spaceUpdate.setSpaceCode(space.getSpaceCode());
                spaceUpdate.setSpaceType(space.getSpaceType());
                spaceUpdate.setStatus("0");
                parkingSpaceMapper.updateParkingSpace(spaceUpdate);
            }
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingTempOrder settleParkingTempOrder(ParkingTempOrder form)
    {
        ParkingTempOrder current = parkingTempOrderMapper.selectParkingTempOrderById(form.getTempOrderId());
        if (current == null)
        {
            throw new ServiceException("订单不存在");
        }
        if ("3".equals(current.getBizStatus()))
        {
            throw new ServiceException("订单已结算");
        }
        if (!"2".equals(current.getBizStatus()))
        {
            throw new ServiceException("订单尚未出场，无法结算，当前状态：" + current.getBizStatus());
        }
        // 支付确认：biz_status=2 → 3，pay_status='1'
        Date now = new Date();
        ParkingTempOrder update = new ParkingTempOrder();
        update.setTempOrderId(current.getTempOrderId());
        update.setPayStatus("1");
        update.setBizStatus("3");
        update.setPayTime(now);
        update.setCloseTime(now);
        update.setUpdateBy(form.getUpdateBy());
        parkingTempOrderMapper.updateParkingTempOrder(update);

        BigDecimal payAmount = current.getPayAmount() != null ? current.getPayAmount() : BigDecimal.ZERO;
        if (payAmount.compareTo(BigDecimal.ZERO) > 0)
        {
            parkingPaymentRecordService.createPaymentForOrder(
                current.getOrderNo(),
                "3",
                current.getCustomerId(),
                current.getLotId(),
                payAmount,
                null,
                form.getUpdateBy()
            );
        }

        return parkingTempOrderMapper.selectParkingTempOrderById(current.getTempOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteParkingTempOrderByIds(Long[] tempOrderIds)
    {
        List<ParkingTempOrder> orders = parkingTempOrderMapper.selectParkingTempOrderByIds(tempOrderIds);
        int rows = parkingTempOrderMapper.deleteParkingTempOrderByIds(tempOrderIds);
        // 释放停放中（1）或待入场（0）订单占用的车位
        for (ParkingTempOrder order : orders)
        {
            if (("0".equals(order.getBizStatus()) || "1".equals(order.getBizStatus())) && order.getSpaceId() != null)
            {
                ParkingSpace space = parkingSpaceMapper.selectParkingSpaceById(order.getSpaceId());
                if (space != null)
                {
                    ParkingSpace spaceUpdate = new ParkingSpace();
                    spaceUpdate.setSpaceId(space.getSpaceId());
                    spaceUpdate.setLotId(space.getLotId());
                    spaceUpdate.setSpaceCode(space.getSpaceCode());
                    spaceUpdate.setSpaceType(space.getSpaceType());
                    spaceUpdate.setStatus("0");
                    parkingSpaceMapper.updateParkingSpace(spaceUpdate);
                }
            }
        }
        return rows;
    }
}
