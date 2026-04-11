package com.ruoyi.parking.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.ParkingPaymentRecord;
import com.ruoyi.parking.mapper.ParkingPaymentRecordMapper;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class ParkingPaymentRecordServiceImpl implements IParkingPaymentRecordService
{
    private final ParkingPaymentRecordMapper parkingPaymentRecordMapper;

    public ParkingPaymentRecordServiceImpl(ParkingPaymentRecordMapper parkingPaymentRecordMapper)
    {
        this.parkingPaymentRecordMapper = parkingPaymentRecordMapper;
    }

    @Override
    public ParkingPaymentRecord selectParkingPaymentRecordById(Long paymentId)
    {
        return parkingPaymentRecordMapper.selectParkingPaymentRecordById(paymentId);
    }

    @Override
    public List<ParkingPaymentRecord> selectParkingPaymentRecordList(ParkingPaymentRecord record)
    {
        return parkingPaymentRecordMapper.selectParkingPaymentRecordList(record);
    }

    @Override
    public int insertParkingPaymentRecord(ParkingPaymentRecord record)
    {
        if (StringUtils.isEmpty(record.getBizOrderNo()))
        {
            throw new ServiceException("业务订单号不能为空");
        }
        if (StringUtils.isEmpty(record.getBizOrderType()))
        {
            throw new ServiceException("业务订单类型不能为空");
        }
        if (StringUtils.isEmpty(record.getPayStatus()))
        {
            record.setPayStatus("0");
        }
        if (StringUtils.isEmpty(record.getPayChannel()))
        {
            record.setPayChannel("1");
        }
        if (record.getPayTime() == null && "1".equals(record.getPayStatus()))
        {
            record.setPayTime(new Date());
        }
        if (StringUtils.isNotEmpty(record.getTradeNo()))
        {
            ParkingPaymentRecord existing = parkingPaymentRecordMapper.selectParkingPaymentRecordByTradeNo(record.getTradeNo());
            if (existing != null)
            {
                throw new ServiceException("交易号已存在");
            }
        }
        else
        {
            record.setTradeNo(generateTradeNo());
        }
        return parkingPaymentRecordMapper.insertParkingPaymentRecord(record);
    }

    @Override
    public int updateParkingPaymentRecord(ParkingPaymentRecord record)
    {
        ParkingPaymentRecord current = parkingPaymentRecordMapper.selectParkingPaymentRecordById(record.getPaymentId());
        if (current == null)
        {
            throw new ServiceException("支付记录不存在");
        }
        if (StringUtils.isEmpty(record.getPayStatus()))
        {
            record.setPayStatus(current.getPayStatus());
        }
        if (StringUtils.isEmpty(record.getPayChannel()))
        {
            record.setPayChannel(current.getPayChannel());
        }
        if (record.getPayAmount() == null)
        {
            record.setPayAmount(current.getPayAmount());
        }
        return parkingPaymentRecordMapper.updateParkingPaymentRecord(record);
    }

    @Override
    public int refundParkingPaymentRecord(ParkingPaymentRecord form)
    {
        ParkingPaymentRecord current = parkingPaymentRecordMapper.selectParkingPaymentRecordById(form.getPaymentId());
        if (current == null)
        {
            throw new ServiceException("支付记录不存在");
        }
        if (!"1".equals(current.getPayStatus()))
        {
            throw new ServiceException("只有成功的支付才能退款");
        }
        current.setPayStatus("3");
        current.setRefundTime(new Date());
        if (StringUtils.isNotEmpty(form.getUpdateBy()))
        {
            current.setUpdateBy(form.getUpdateBy());
        }
        return parkingPaymentRecordMapper.updateParkingPaymentRecord(current);
    }

    @Override
    public int deleteParkingPaymentRecordByIds(Long[] paymentIds)
    {
        return parkingPaymentRecordMapper.deleteParkingPaymentRecordByIds(paymentIds);
    }

    @Override
    public ParkingPaymentRecord createPaymentForOrder(String bizOrderNo, String bizOrderType,
        Long customerId, Long lotId, BigDecimal amount, String payChannel, String createBy)
    {
        ParkingPaymentRecord record = new ParkingPaymentRecord();
        record.setBizOrderNo(bizOrderNo);
        record.setBizOrderType(bizOrderType);
        record.setCustomerId(customerId);
        record.setLotId(lotId);
        record.setPayAmount(amount != null ? amount : BigDecimal.ZERO);
        record.setPayChannel(StringUtils.isNotEmpty(payChannel) ? payChannel : "3");
        // Cascaded payments are created at the moment of successful settlement,
        // so they are always recorded as successful with payTime = now.
        record.setPayStatus("1");
        record.setPayTime(new Date());
        record.setCreateBy(createBy);
        insertParkingPaymentRecord(record);
        return record;
    }

    private String generateTradeNo()
    {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int suffix = new Random().nextInt(9000) + 1000;
        return "MAN" + timestamp + suffix;
    }
}
