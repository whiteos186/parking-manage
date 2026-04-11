package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingMonthlyOrderMapper;
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
    private final IParkingPaymentRecordService parkingPaymentRecordService;

    public ParkingMonthlyOrderServiceImpl(ParkingMonthlyOrderMapper parkingMonthlyOrderMapper,
        ParkingLotMapper parkingLotMapper,
        ParkingCustomerMapper parkingCustomerMapper,
        IParkingPaymentRecordService parkingPaymentRecordService)
    {
        this.parkingMonthlyOrderMapper = parkingMonthlyOrderMapper;
        this.parkingLotMapper = parkingLotMapper;
        this.parkingCustomerMapper = parkingCustomerMapper;
        this.parkingPaymentRecordService = parkingPaymentRecordService;
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
        // Generate order no if absent
        if (StringUtils.isEmpty(parkingMonthlyOrder.getOrderNo()))
        {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String suffix = String.format("%04d", new Random().nextInt(10000));
            parkingMonthlyOrder.setOrderNo("MO" + timestamp + suffix);
        }

        // Default pay/biz status
        if (StringUtils.isEmpty(parkingMonthlyOrder.getPayStatus()))
        {
            parkingMonthlyOrder.setPayStatus("0");
        }
        if (StringUtils.isEmpty(parkingMonthlyOrder.getBizStatus()))
        {
            parkingMonthlyOrder.setBizStatus("0");
        }

        // monthCount must be >= 1
        if (parkingMonthlyOrder.getMonthCount() == null || parkingMonthlyOrder.getMonthCount() < 1)
        {
            parkingMonthlyOrder.setMonthCount(1);
        }

        // Validate lot exists
        if (parkingMonthlyOrder.getLotId() == null
            || parkingLotMapper.selectParkingLotById(parkingMonthlyOrder.getLotId()) == null)
        {
            throw new ServiceException("停车场不存在");
        }

        // Default startTime to now if null
        if (parkingMonthlyOrder.getStartTime() == null)
        {
            parkingMonthlyOrder.setStartTime(LocalDateTime.now());
        }

        // Compute endTime = startTime + monthCount months if null
        if (parkingMonthlyOrder.getEndTime() == null)
        {
            parkingMonthlyOrder.setEndTime(
                parkingMonthlyOrder.getStartTime().plusMonths(parkingMonthlyOrder.getMonthCount()));
        }

        // 计算价格（originalAmount / discountAmount / payAmount）
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

        // 若影响价格的关键字段发生变化，则重算折扣和应付金额；
        // 否则保留入参中的金额（尊重管理员手动改价）
        boolean priceKeysChanged = !Objects.equals(current.getCustomerId(), parkingMonthlyOrder.getCustomerId())
            || !Objects.equals(current.getLotId(), parkingMonthlyOrder.getLotId())
            || !Objects.equals(current.getMonthCount(), parkingMonthlyOrder.getMonthCount());
        if (priceKeysChanged)
        {
            // 强制清空三个金额字段，令 recalcPricing 全量重算
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
        // Cascade a successful payment record so the payment ledger stays consistent.
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
        // If already paid, mark as refunded; otherwise leave payStatus unchanged
        if ("1".equals(current.getPayStatus()))
        {
            current.setPayStatus("2");
        }
        current.setBizStatus("3");
        current.setCancelTime(LocalDateTime.now());
        return parkingMonthlyOrderMapper.updateParkingMonthlyOrder(current);
    }

    @Override
    public int deleteParkingMonthlyOrderByIds(Long[] monthlyOrderIds)
    {
        return parkingMonthlyOrderMapper.deleteParkingMonthlyOrderByIds(monthlyOrderIds);
    }

    /**
     * 根据停车场月租价 × 月数计算 originalAmount，再结合客户会员等级算 discountAmount 和 payAmount。
     * 仅在字段为 null 时才写入，不覆盖已有值（insert 路径全 null，update 路径按需清空后调用）。
     */
    private void recalcPricing(ParkingMonthlyOrder order)
    {
        // 计算原价 = 停车场月租价 × 月数
        if (order.getOriginalAmount() == null && order.getLotId() != null)
        {
            com.ruoyi.parking.domain.ParkingLot lot = parkingLotMapper.selectParkingLotById(order.getLotId());
            if (lot != null && lot.getMonthlyPrice() != null)
            {
                int months = order.getMonthCount() != null ? order.getMonthCount() : 1;
                order.setOriginalAmount(lot.getMonthlyPrice().multiply(BigDecimal.valueOf(months)));
            }
        }

        // 应用会员折扣（仅当调用方未显式设置 discountAmount 时自动计算）
        if (order.getDiscountAmount() == null && order.getOriginalAmount() != null
            && order.getCustomerId() != null)
        {
            ParkingCustomer customer = parkingCustomerMapper.selectParkingCustomerById(order.getCustomerId());
            BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(order.getOriginalAmount(), customer);
            order.setDiscountAmount(discount);
        }

        // payAmount = originalAmount - discountAmount（discount 为 null 则视为 0）
        if (order.getPayAmount() == null && order.getOriginalAmount() != null)
        {
            BigDecimal discount = order.getDiscountAmount() != null
                ? order.getDiscountAmount()
                : BigDecimal.ZERO;
            order.setPayAmount(order.getOriginalAmount().subtract(discount).max(BigDecimal.ZERO));
        }
    }
}
