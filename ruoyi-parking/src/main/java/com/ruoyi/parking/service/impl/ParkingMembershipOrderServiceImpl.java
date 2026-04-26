package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingMembershipOrder;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingMembershipOrderMapper;
import com.ruoyi.parking.mapper.ParkingUserVehicleMapper;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import com.ruoyi.parking.service.IParkingMembershipOrderService;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final BigDecimal SILVER_MEMBERSHIP_PRICE = new BigDecimal("100.00");
    private static final BigDecimal GOLD_MEMBERSHIP_PRICE = new BigDecimal("300.00");
    private static final BigDecimal PLATINUM_MEMBERSHIP_PRICE = new BigDecimal("600.00");

    private final ParkingMembershipOrderMapper membershipOrderMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingCustomerMapper parkingCustomerMapper;
    private final ParkingUserVehicleMapper parkingUserVehicleMapper;
    private final IParkingPaymentRecordService parkingPaymentRecordService;
    private final IParkingGlobalRuleService parkingGlobalRuleService;

    public ParkingMembershipOrderServiceImpl(ParkingMembershipOrderMapper membershipOrderMapper,
        ParkingLotMapper parkingLotMapper,
        ParkingCustomerMapper parkingCustomerMapper,
        ParkingUserVehicleMapper parkingUserVehicleMapper,
        IParkingPaymentRecordService parkingPaymentRecordService,
        IParkingGlobalRuleService parkingGlobalRuleService)
    {
        this.membershipOrderMapper = membershipOrderMapper;
        this.parkingLotMapper = parkingLotMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
        this.parkingUserVehicleMapper = parkingUserVehicleMapper;
        this.parkingPaymentRecordService = parkingPaymentRecordService;
        this.parkingGlobalRuleService = parkingGlobalRuleService;
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
        if (order.getCustomerId() == null
            || parkingCustomerMapper.selectParkingCustomerById(order.getCustomerId()) == null)
        {
            throw new ServiceException("客户不能为空");
        }
        if (order.getVehicleId() != null)
        {
            com.ruoyi.parking.domain.ParkingUserVehicle vehicle =
                parkingUserVehicleMapper.selectParkingUserVehicleById(order.getVehicleId());
            if (vehicle == null || !order.getCustomerId().equals(vehicle.getCustomerId()))
            {
                throw new ServiceException("车辆不属于订单客户");
            }
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
        recalcPricing(order);
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
        if (form.getPayAmount() != null)
        {
            order.setPayAmount(form.getPayAmount());
        }
        if (isMissingPositiveAmount(order.getPayAmount()))
        {
            order.setOriginalAmount(null);
            order.setDiscountAmount(null);
            order.setPayAmount(null);
            recalcPricing(order);
        }
        BigDecimal cascadeAmount = order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO;
        if (cascadeAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("会员订单支付金额必须大于0");
        }
        order.setPayStatus("1");
        order.setBizStatus("1");
        order.setPayTime(new Date());
        order.setUpdateBy(form.getUpdateBy());
        int rows = membershipOrderMapper.updateParkingMembershipOrder(order);
        // 支付成功后同步客户会员状态
        syncCustomerMembership(order);
        // Cascade a successful payment record so the payment ledger stays consistent.
        parkingPaymentRecordService.createPaymentForOrder(
            order.getOrderNo(),
            "1",
            order.getCustomerId(),
            order.getLotId(),
            cascadeAmount,
            null,
            form.getUpdateBy()
        );
        return rows;
    }

    private void recalcPricing(ParkingMembershipOrder order)
    {
        boolean originalMissing = isMissingPositiveAmount(order.getOriginalAmount());
        if (originalMissing)
        {
            order.setOriginalAmount(resolveMembershipPrice(order.getMembershipType()));
        }

        if (order.getDiscountAmount() == null || (originalMissing && BigDecimal.ZERO.compareTo(order.getDiscountAmount()) == 0))
        {
            order.setDiscountAmount(calculateMembershipDiscount(order.getOriginalAmount(), order.getMembershipType()));
        }

        if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
            order.setPayAmount(order.getOriginalAmount().subtract(discount).max(BigDecimal.ZERO));
        }
    }

    private BigDecimal resolveMembershipPrice(String membershipType)
    {
        if ("2".equals(membershipType))
        {
            return GOLD_MEMBERSHIP_PRICE;
        }
        if ("3".equals(membershipType))
        {
            return PLATINUM_MEMBERSHIP_PRICE;
        }
        return SILVER_MEMBERSHIP_PRICE;
    }

    private BigDecimal calculateMembershipDiscount(BigDecimal originalAmount, String membershipType)
    {
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        ParkingSettingsDto.MemberDiscount memberDiscount = parkingGlobalRuleService.getValidatedMemberDiscount();
        if (memberDiscount == null || !Boolean.TRUE.equals(memberDiscount.getMemberDiscountEnabled()))
        {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = resolveMembershipDiscountRate(memberDiscount, membershipType);
        if (rate == null)
        {
            return BigDecimal.ZERO;
        }
        return originalAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveMembershipDiscountRate(ParkingSettingsDto.MemberDiscount memberDiscount, String membershipType)
    {
        if ("2".equals(membershipType))
        {
            return memberDiscount.getGoldRate();
        }
        if ("3".equals(membershipType))
        {
            return memberDiscount.getPlatinumRate();
        }
        return memberDiscount.getSilverRate();
    }

    private boolean isMissingPositiveAmount(BigDecimal amount)
    {
        return amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
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
