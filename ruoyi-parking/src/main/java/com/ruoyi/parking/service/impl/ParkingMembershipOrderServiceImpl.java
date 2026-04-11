package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingMembershipOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingMembershipOrderMapper;
import com.ruoyi.parking.service.IParkingMembershipOrderService;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingMembershipOrderServiceImpl implements IParkingMembershipOrderService
{
    private final ParkingMembershipOrderMapper membershipOrderMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;
    private final IParkingPaymentRecordService parkingPaymentRecordService;

    public ParkingMembershipOrderServiceImpl(ParkingMembershipOrderMapper membershipOrderMapper,
        ParkingLotMapper parkingLotMapper,
        ParkingCustomerMapper parkingCustomerMapper,
        IParkingPaymentRecordService parkingPaymentRecordService)
    {
        this.membershipOrderMapper = membershipOrderMapper;
        this.parkingLotMapper = parkingLotMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
        this.parkingPaymentRecordService = parkingPaymentRecordService;
    }

    @Override
    public ParkingMembershipOrder selectParkingMembershipOrderById(Long membershipOrderId)
    {
        return membershipOrderMapper.selectParkingMembershipOrderById(membershipOrderId);
    }

    @Override
    public List<ParkingMembershipOrder> selectParkingMembershipOrderList(ParkingMembershipOrder order)
    {
        return membershipOrderMapper.selectParkingMembershipOrderList(order);
    }

    @Override
    public int insertParkingMembershipOrder(ParkingMembershipOrder order)
    {
        // Generate order_no if missing
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            String datePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            int rand = new Random().nextInt(9000) + 1000;
            order.setOrderNo("PO" + datePart + rand);
        }
        // Default payStatus, bizStatus, membershipType
        if (StringUtils.isEmpty(order.getPayStatus()))
        {
            order.setPayStatus("0");
        }
        if (StringUtils.isEmpty(order.getBizStatus()))
        {
            order.setBizStatus("0");
        }
        if (StringUtils.isEmpty(order.getMembershipType()))
        {
            order.setMembershipType("1");
        }
        // Validate lotId exists
        ParkingLot lot = parkingLotMapper.selectParkingLotById(order.getLotId());
        if (lot == null)
        {
            throw new ServiceException("停车场不存在");
        }
        // Default amounts
        if (order.getOriginalAmount() == null)
        {
            order.setOriginalAmount(BigDecimal.ZERO);
        }
        if (order.getDiscountAmount() == null)
        {
            order.setDiscountAmount(BigDecimal.ZERO);
        }
        // Default validity window
        if (order.getValidStartTime() == null)
        {
            order.setValidStartTime(new Date());
        }
        if (order.getValidEndTime() == null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(order.getValidStartTime());
            cal.add(Calendar.DAY_OF_YEAR, 365);
            order.setValidEndTime(cal.getTime());
        }
        // Default payAmount = originalAmount - discountAmount
        if (order.getPayAmount() == null)
        {
            order.setPayAmount(order.getOriginalAmount().subtract(order.getDiscountAmount()));
        }
        return membershipOrderMapper.insertParkingMembershipOrder(order);
    }

    @Override
    public int updateParkingMembershipOrder(ParkingMembershipOrder order)
    {
        ParkingMembershipOrder current = membershipOrderMapper.selectParkingMembershipOrderById(order.getMembershipOrderId());
        if (current == null)
        {
            throw new ServiceException("会员订单不存在");
        }
        // Validate lotId if changed
        if (order.getLotId() != null && !order.getLotId().equals(current.getLotId()))
        {
            ParkingLot lot = parkingLotMapper.selectParkingLotById(order.getLotId());
            if (lot == null)
            {
                throw new ServiceException("停车场不存在");
            }
        }
        // Do not touch payStatus / bizStatus from this method — keep stored values
        order.setPayStatus(null);
        order.setBizStatus(null);
        return membershipOrderMapper.updateParkingMembershipOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payMembershipOrder(ParkingMembershipOrder form)
    {
        ParkingMembershipOrder order = membershipOrderMapper.selectParkingMembershipOrderById(form.getMembershipOrderId());
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if ("1".equals(order.getPayStatus()))
        {
            throw new ServiceException("订单已支付");
        }
        order.setPayStatus("1");
        order.setBizStatus("1");
        order.setPayTime(new Date());
        if (form.getPayAmount() != null)
        {
            order.setPayAmount(form.getPayAmount());
        }
        order.setUpdateBy(form.getUpdateBy());
        int rows = membershipOrderMapper.updateParkingMembershipOrder(order);
        // 支付成功后同步客户会员状态
        syncCustomerMembership(order);
        // Cascade a successful payment record so the payment ledger stays consistent.
        BigDecimal cascadeAmount = order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO;
        if (cascadeAmount.compareTo(BigDecimal.ZERO) > 0)
        {
            parkingPaymentRecordService.createPaymentForOrder(
                order.getOrderNo(),
                "1",
                order.getCustomerId(),
                order.getLotId(),
                cascadeAmount,
                null,
                form.getUpdateBy()
            );
        }
        return rows;
    }

    /** 会员订单支付后更新客户 is_member/member_type/member_expire_time */
    private void syncCustomerMembership(ParkingMembershipOrder order)
    {
        if (order.getCustomerId() == null || order.getValidEndTime() == null)
        {
            return;
        }
        ParkingCustomer customer = parkingCustomerMapper.selectParkingCustomerById(order.getCustomerId());
        if (customer == null)
        {
            return;
        }
        // 取当前到期时间与订单到期时间的最大值
        Date newExpire = order.getValidEndTime();
        if (customer.getMemberExpireTime() != null && customer.getMemberExpireTime().after(newExpire))
        {
            newExpire = customer.getMemberExpireTime();
        }
        ParkingCustomer update = new ParkingCustomer();
        update.setCustomerId(customer.getCustomerId());
        update.setIsMember("1");
        update.setMemberType(order.getMembershipType());
        update.setMemberExpireTime(newExpire);
        parkingCustomerMapper.updateParkingCustomer(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelMembershipOrder(ParkingMembershipOrder form)
    {
        ParkingMembershipOrder order = membershipOrderMapper.selectParkingMembershipOrderById(form.getMembershipOrderId());
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if ("3".equals(order.getBizStatus()))
        {
            throw new ServiceException("订单已取消");
        }
        // If already paid, mark as refunded
        if ("1".equals(order.getPayStatus()))
        {
            order.setPayStatus("2");
        }
        order.setBizStatus("3");
        order.setCancelTime(new Date());
        order.setUpdateBy(form.getUpdateBy());
        return membershipOrderMapper.updateParkingMembershipOrder(order);
    }

    @Override
    public int deleteParkingMembershipOrderByIds(Long[] orderIds)
    {
        return membershipOrderMapper.deleteParkingMembershipOrderByIds(orderIds);
    }
}
