package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ruoyi.parking.domain.ParkingCustomer;
import com.ruoyi.parking.domain.ParkingLot;
import com.ruoyi.parking.domain.ParkingMonthlyOrder;
import com.ruoyi.parking.mapper.ParkingCustomerMapper;
import com.ruoyi.parking.mapper.ParkingLotMapper;
import com.ruoyi.parking.mapper.ParkingMonthlyOrderMapper;
import com.ruoyi.parking.service.IParkingPaymentRecordService;
import com.ruoyi.parking.service.impl.ParkingMonthlyOrderServiceImpl;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 验证 updateParkingMonthlyOrder 中的条件重算逻辑：
 * - 价格关键字段（customerId / lotId / monthCount）变化时重算折扣和应付金额
 * - 非价格字段（remark）变化时保留原金额（尊重管理员手动改价）
 */
class ParkingMonthlyOrderPricingTest
{
    private ParkingMonthlyOrderMapper monthlyOrderMapper;
    private ParkingLotMapper lotMapper;
    private ParkingCustomerMapper customerMapper;
    private ParkingMonthlyOrderServiceImpl service;

    @BeforeEach
    void setUp()
    {
        monthlyOrderMapper = mock(ParkingMonthlyOrderMapper.class);
        lotMapper = mock(ParkingLotMapper.class);
        customerMapper = mock(ParkingCustomerMapper.class);
        IParkingPaymentRecordService paymentRecordService = mock(IParkingPaymentRecordService.class);

        service = new ParkingMonthlyOrderServiceImpl(
            monthlyOrderMapper, lotMapper, customerMapper, paymentRecordService);

        // 默认 update 返回 1 行受影响
        when(monthlyOrderMapper.updateParkingMonthlyOrder(any())).thenReturn(1);
    }

    /** 改 monthCount 时，payAmount 应按新月数重算 */
    @Test
    void updateWithChangedMonthCountRecalculatesPayAmount()
    {
        // 旧订单：客户非会员，1 个月，月租 380，原价 380，已结 380
        ParkingMonthlyOrder existing = buildExistingOrder(1L, 1L, 1, null, null, "380.00", "0.00", "380.00");
        when(monthlyOrderMapper.selectParkingMonthlyOrderById(1L)).thenReturn(existing);

        // 停车场月租 380
        ParkingLot lot = new ParkingLot();
        lot.setMonthlyPrice(new BigDecimal("380.00"));
        when(lotMapper.selectParkingLotById(1L)).thenReturn(lot);

        // 非会员客户
        ParkingCustomer customer = buildCustomer(1L, "0", null, null);
        when(customerMapper.selectParkingCustomerById(1L)).thenReturn(customer);

        // 入参：月数改为 3
        ParkingMonthlyOrder update = new ParkingMonthlyOrder();
        update.setMonthlyOrderId(1L);
        update.setCustomerId(1L);
        update.setLotId(1L);
        update.setMonthCount(3);

        service.updateParkingMonthlyOrder(update);

        // originalAmount = 380 × 3 = 1140
        assertNotNull(update.getOriginalAmount());
        assertEquals(0, new BigDecimal("1140.00").compareTo(update.getOriginalAmount()),
            "Expected originalAmount recalculated as 380 * 3");
        // 非会员，discountAmount = 0（忽略 BigDecimal scale 差异）
        assertNotNull(update.getDiscountAmount());
        assertEquals(0, BigDecimal.ZERO.compareTo(update.getDiscountAmount()),
            "Expected zero discount for non-member");
        // payAmount = 1140 - 0 = 1140
        assertNotNull(update.getPayAmount());
        assertEquals(0, new BigDecimal("1140.00").compareTo(update.getPayAmount()),
            "Expected payAmount recalculated to 1140");
    }

    /** 改 customerId 为金卡会员时，应扣除 10% 折扣 */
    @Test
    void updateWithMemberCustomerAppliesDiscount()
    {
        // 旧订单：原客户 ID=1，1 个月，月租 380
        ParkingMonthlyOrder existing = buildExistingOrder(2L, 1L, 1, 1L, null, "380.00", "0.00", "380.00");
        when(monthlyOrderMapper.selectParkingMonthlyOrderById(2L)).thenReturn(existing);

        ParkingLot lot = new ParkingLot();
        lot.setMonthlyPrice(new BigDecimal("380.00"));
        when(lotMapper.selectParkingLotById(1L)).thenReturn(lot);

        // 新客户 ID=2，金卡会员（member_type='2'，优惠 10%）
        Date futureExpire = buildFutureDate(365);
        ParkingCustomer memberCustomer = buildCustomer(2L, "1", "2", futureExpire);
        when(customerMapper.selectParkingCustomerById(2L)).thenReturn(memberCustomer);

        // 入参：换新客户 ID=2
        ParkingMonthlyOrder update = new ParkingMonthlyOrder();
        update.setMonthlyOrderId(2L);
        update.setCustomerId(2L); // 变更客户
        update.setLotId(1L);
        update.setMonthCount(1);

        service.updateParkingMonthlyOrder(update);

        // originalAmount = 380
        assertNotNull(update.getOriginalAmount());
        assertEquals(0, new BigDecimal("380.00").compareTo(update.getOriginalAmount()));
        // discount = 380 × 10% = 38.00
        assertNotNull(update.getDiscountAmount());
        assertEquals(0, new BigDecimal("38.00").compareTo(update.getDiscountAmount()),
            "Expected 10% member discount applied");
        // payAmount = 380 - 38 = 342
        assertNotNull(update.getPayAmount());
        assertEquals(0, new BigDecimal("342.00").compareTo(update.getPayAmount()),
            "Expected payAmount after gold member discount");
    }

    /** 只改 remark（非价格字段）时，金额不变（保留管理员手改值） */
    @Test
    void updateWithOnlyRemarkChangedPreservesOriginalAmounts()
    {
        // 旧订单：管理员手改了 payAmount 为 300（低于原价 380）
        ParkingMonthlyOrder existing = buildExistingOrder(3L, 1L, 1, 1L, 1L, "380.00", "0.00", "300.00");
        when(monthlyOrderMapper.selectParkingMonthlyOrderById(3L)).thenReturn(existing);

        // 入参：只改 remark，价格关键字段与旧订单相同
        ParkingMonthlyOrder update = new ParkingMonthlyOrder();
        update.setMonthlyOrderId(3L);
        update.setCustomerId(1L); // 与旧相同
        update.setLotId(1L);      // 与旧相同
        update.setMonthCount(1);  // 与旧相同
        update.setRemark("更新备注");
        // 故意不传金额字段，模拟前端只改备注后提交

        service.updateParkingMonthlyOrder(update);

        // 价格字段应保持 null（未被重算，由 mapper update 时沿用 DB 值），
        // 关键是不应被 recalcPricing 强制覆盖
        assertEquals(null, update.getOriginalAmount(),
            "Expected originalAmount not recalculated when price keys unchanged");
        assertEquals(null, update.getDiscountAmount(),
            "Expected discountAmount not recalculated when price keys unchanged");
        assertEquals(null, update.getPayAmount(),
            "Expected payAmount not recalculated when price keys unchanged");
    }

    // ---- 辅助方法 ----

    private ParkingMonthlyOrder buildExistingOrder(
        Long orderId, Long lotId, Integer monthCount,
        Long customerId, Long vehicleId,
        String original, String discount, String pay)
    {
        ParkingMonthlyOrder o = new ParkingMonthlyOrder();
        o.setMonthlyOrderId(orderId);
        o.setLotId(lotId);
        o.setMonthCount(monthCount);
        o.setCustomerId(customerId);
        o.setVehicleId(vehicleId);
        o.setOriginalAmount(new BigDecimal(original));
        o.setDiscountAmount(new BigDecimal(discount));
        o.setPayAmount(new BigDecimal(pay));
        o.setPayStatus("0");
        o.setBizStatus("0");
        return o;
    }

    private ParkingCustomer buildCustomer(Long customerId, String isMember, String memberType, Date expireTime)
    {
        ParkingCustomer c = new ParkingCustomer();
        c.setCustomerId(customerId);
        c.setIsMember(isMember);
        c.setMemberType(memberType);
        c.setMemberExpireTime(expireTime);
        return c;
    }

    private Date buildFutureDate(int daysFromNow)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysFromNow);
        return cal.getTime();
    }
}
