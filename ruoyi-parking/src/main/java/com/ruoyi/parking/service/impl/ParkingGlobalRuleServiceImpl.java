package com.ruoyi.parking.service.impl;

import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.domain.dto.ParkingSettingsUpdateDto;
import com.ruoyi.parking.service.IParkingGlobalRuleService;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDictTypeService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParkingGlobalRuleServiceImpl implements IParkingGlobalRuleService
{
    static final String MONTHLY_PRICE_KEY = "parking.rule.monthlyPrice";
    static final String TEMP_HOUR_PRICE_KEY = "parking.rule.tempHourPrice";
    static final String TEMP_FREE_MINUTES_KEY = "parking.rule.tempFreeMinutes";
    static final String TEMP_BILLING_STEP_MINUTES_KEY = "parking.rule.tempBillingStepMinutes";
    static final String TEMP_ROUND_UP_ENABLED_KEY = "parking.rule.tempRoundUpEnabled";
    static final String TEMP_DAILY_CAP_AMOUNT_KEY = "parking.rule.tempDailyCapAmount";
    static final String MEMBER_DISCOUNT_ENABLED_KEY = "parking.rule.memberDiscountEnabled";
    static final String MEMBER_DISCOUNT_SILVER_KEY = "parking.rule.memberDiscount.silver";
    static final String MEMBER_DISCOUNT_GOLD_KEY = "parking.rule.memberDiscount.gold";
    static final String MEMBER_DISCOUNT_PLATINUM_KEY = "parking.rule.memberDiscount.platinum";
    static final String PAYMENT_DEFAULT_CHANNEL_KEY = "parking.rule.payment.defaultChannel";
    static final String PAYMENT_CASH_ENABLED_KEY = "parking.rule.payment.cashEnabled";
    static final String ORDER_CANCEL_AUTO_REFUND_ENABLED_KEY = "parking.rule.order.cancelAutoRefundEnabled";
    static final String PAYMENT_CHANNEL_DICT_TYPE = "parking_payment_channel";
    private static final String CONFIG_MISSING_MESSAGE = "停车场全局配置缺失: ";
    private static final String CONFIG_INVALID_MESSAGE = "停车场全局配置不合法: ";

    private final ISysConfigService sysConfigService;

    private final ISysDictTypeService sysDictTypeService;

    public ParkingGlobalRuleServiceImpl(ISysConfigService sysConfigService, ISysDictTypeService sysDictTypeService)
    {
        this.sysConfigService = sysConfigService;
        this.sysDictTypeService = sysDictTypeService;
    }

    @Override
    public ParkingSettingsDto getSettings()
    {
        ParkingSettingsDto settings = new ParkingSettingsDto();
        settings.setPricing(loadPricing());
        settings.setMemberDiscount(loadMemberDiscount());
        settings.setPayment(loadPayment());
        settings.setSwitches(loadSwitches());
        return settings;
    }

    @Override
    public ParkingSettingsDto.Pricing getValidatedPricing()
    {
        ParkingSettingsDto.Pricing pricing = loadPricing();
        if (pricing.getMonthlyPrice() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + MONTHLY_PRICE_KEY);
        }
        if (pricing.getTempHourPrice() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + TEMP_HOUR_PRICE_KEY);
        }
        if (pricing.getTempFreeMinutes() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + TEMP_FREE_MINUTES_KEY);
        }
        if (pricing.getTempBillingStepMinutes() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + TEMP_BILLING_STEP_MINUTES_KEY);
        }
        if (pricing.getTempRoundUpEnabled() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + TEMP_ROUND_UP_ENABLED_KEY);
        }
        if (pricing.getTempDailyCapAmount() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + TEMP_DAILY_CAP_AMOUNT_KEY);
        }

        validateNonNegative(pricing.getMonthlyPrice(), MONTHLY_PRICE_KEY);
        validateNonNegative(pricing.getTempHourPrice(), TEMP_HOUR_PRICE_KEY);
        validateNonNegative(pricing.getTempDailyCapAmount(), TEMP_DAILY_CAP_AMOUNT_KEY);
        if (pricing.getTempFreeMinutes() < 0)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + TEMP_FREE_MINUTES_KEY);
        }
        if (pricing.getTempBillingStepMinutes() <= 0)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + TEMP_BILLING_STEP_MINUTES_KEY);
        }
        return pricing;
    }

    @Override
    public ParkingSettingsDto.MemberDiscount getValidatedMemberDiscount()
    {
        ParkingSettingsDto.MemberDiscount memberDiscount = loadMemberDiscount();
        if (memberDiscount.getMemberDiscountEnabled() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + MEMBER_DISCOUNT_ENABLED_KEY);
        }
        if (Boolean.TRUE.equals(memberDiscount.getMemberDiscountEnabled()))
        {
            if (memberDiscount.getSilverRate() == null)
            {
                throw new ServiceException(CONFIG_MISSING_MESSAGE + MEMBER_DISCOUNT_SILVER_KEY);
            }
            if (memberDiscount.getGoldRate() == null)
            {
                throw new ServiceException(CONFIG_MISSING_MESSAGE + MEMBER_DISCOUNT_GOLD_KEY);
            }
            if (memberDiscount.getPlatinumRate() == null)
            {
                throw new ServiceException(CONFIG_MISSING_MESSAGE + MEMBER_DISCOUNT_PLATINUM_KEY);
            }
            validateDiscountRate(memberDiscount.getSilverRate(), MEMBER_DISCOUNT_SILVER_KEY);
            validateDiscountRate(memberDiscount.getGoldRate(), MEMBER_DISCOUNT_GOLD_KEY);
            validateDiscountRate(memberDiscount.getPlatinumRate(), MEMBER_DISCOUNT_PLATINUM_KEY);
        }
        return memberDiscount;
    }

    @Override
    public ParkingSettingsDto.Payment getValidatedPayment()
    {
        ParkingSettingsDto.Payment payment = loadPayment();
        if (payment.getDefaultChannel() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + PAYMENT_DEFAULT_CHANNEL_KEY);
        }
        if (payment.getCashEnabled() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + PAYMENT_CASH_ENABLED_KEY);
        }
        validateDefaultPaymentChannel(payment.getDefaultChannel());
        return payment;
    }

    @Override
    public ParkingSettingsDto.Switches getValidatedSwitches()
    {
        ParkingSettingsDto.Switches switches = loadSwitches();
        if (switches.getCancelAutoRefundEnabled() == null)
        {
            throw new ServiceException(CONFIG_MISSING_MESSAGE + ORDER_CANCEL_AUTO_REFUND_ENABLED_KEY);
        }
        return switches;
    }

    @Override
    public void updateSettings(ParkingSettingsUpdateDto updateDto, String operator)
    {
        validateUpdate(updateDto);
        validateDefaultPaymentChannel(updateDto.getDefaultPaymentChannel());

        saveOrUpdateConfig(MONTHLY_PRICE_KEY, "停车场月租价", updateDto.getMonthlyPrice().toPlainString(), operator);
        saveOrUpdateConfig(TEMP_HOUR_PRICE_KEY, "停车场临停小时费率", updateDto.getTempHourPrice().toPlainString(), operator);
        saveOrUpdateConfig(TEMP_FREE_MINUTES_KEY, "停车场临停免费分钟数", updateDto.getTempFreeMinutes().toString(), operator);
        saveOrUpdateConfig(TEMP_BILLING_STEP_MINUTES_KEY, "停车场临停计费步长（分钟）", updateDto.getTempBillingStepMinutes().toString(), operator);
        saveOrUpdateConfig(TEMP_ROUND_UP_ENABLED_KEY, "停车场临停向上取整开关", updateDto.getTempRoundUpEnabled().toString(), operator);
        saveOrUpdateConfig(TEMP_DAILY_CAP_AMOUNT_KEY, "停车场临停每日封顶金额", updateDto.getTempDailyCapAmount().toPlainString(), operator);
        saveOrUpdateConfig(MEMBER_DISCOUNT_ENABLED_KEY, "停车场会员折扣开关", updateDto.getMemberDiscountEnabled().toString(), operator);
        saveOrUpdateConfig(MEMBER_DISCOUNT_SILVER_KEY, "停车场银卡会员折扣率", updateDto.getSilverDiscountRate().toPlainString(), operator);
        saveOrUpdateConfig(MEMBER_DISCOUNT_GOLD_KEY, "停车场金卡会员折扣率", updateDto.getGoldDiscountRate().toPlainString(), operator);
        saveOrUpdateConfig(MEMBER_DISCOUNT_PLATINUM_KEY, "停车场铂金会员折扣率", updateDto.getPlatinumDiscountRate().toPlainString(), operator);
        saveOrUpdateConfig(PAYMENT_DEFAULT_CHANNEL_KEY, "停车场默认支付渠道", updateDto.getDefaultPaymentChannel(), operator);
        saveOrUpdateConfig(PAYMENT_CASH_ENABLED_KEY, "停车场现金支付开关", updateDto.getCashEnabled().toString(), operator);
        saveOrUpdateConfig(ORDER_CANCEL_AUTO_REFUND_ENABLED_KEY, "停车场取消订单自动退款开关", updateDto.getCancelAutoRefundEnabled().toString(), operator);
    }

    private ParkingSettingsDto.Pricing loadPricing()
    {
        ParkingSettingsDto.Pricing pricing = new ParkingSettingsDto.Pricing();
        pricing.setMonthlyPrice(readBigDecimal(MONTHLY_PRICE_KEY));
        pricing.setTempHourPrice(readBigDecimal(TEMP_HOUR_PRICE_KEY));
        pricing.setTempFreeMinutes(readInteger(TEMP_FREE_MINUTES_KEY));
        pricing.setTempBillingStepMinutes(readInteger(TEMP_BILLING_STEP_MINUTES_KEY));
        pricing.setTempRoundUpEnabled(readBoolean(TEMP_ROUND_UP_ENABLED_KEY));
        pricing.setTempDailyCapAmount(readBigDecimal(TEMP_DAILY_CAP_AMOUNT_KEY));
        return pricing;
    }

    private ParkingSettingsDto.MemberDiscount loadMemberDiscount()
    {
        ParkingSettingsDto.MemberDiscount memberDiscount = new ParkingSettingsDto.MemberDiscount();
        memberDiscount.setMemberDiscountEnabled(readBoolean(MEMBER_DISCOUNT_ENABLED_KEY));
        memberDiscount.setSilverRate(readBigDecimal(MEMBER_DISCOUNT_SILVER_KEY));
        memberDiscount.setGoldRate(readBigDecimal(MEMBER_DISCOUNT_GOLD_KEY));
        memberDiscount.setPlatinumRate(readBigDecimal(MEMBER_DISCOUNT_PLATINUM_KEY));
        return memberDiscount;
    }

    private ParkingSettingsDto.Payment loadPayment()
    {
        ParkingSettingsDto.Payment payment = new ParkingSettingsDto.Payment();
        payment.setDefaultChannel(readString(PAYMENT_DEFAULT_CHANNEL_KEY));
        payment.setCashEnabled(readBoolean(PAYMENT_CASH_ENABLED_KEY));
        return payment;
    }

    private ParkingSettingsDto.Switches loadSwitches()
    {
        ParkingSettingsDto.Switches switches = new ParkingSettingsDto.Switches();
        switches.setCancelAutoRefundEnabled(readBoolean(ORDER_CANCEL_AUTO_REFUND_ENABLED_KEY));
        return switches;
    }

    private void validateUpdate(ParkingSettingsUpdateDto updateDto)
    {
        if (updateDto == null)
        {
            throw new ServiceException("停车场全局配置参数不能为空");
        }
        if (updateDto.getMonthlyPrice() == null)
        {
            throw new ServiceException("月租价不能为空");
        }
        if (updateDto.getTempHourPrice() == null)
        {
            throw new ServiceException("临停小时费率不能为空");
        }
        if (updateDto.getTempFreeMinutes() == null)
        {
            throw new ServiceException("临停免费分钟数不能为空");
        }
        if (updateDto.getTempBillingStepMinutes() == null)
        {
            throw new ServiceException("临停计费步长不能为空");
        }
        if (updateDto.getTempRoundUpEnabled() == null)
        {
            throw new ServiceException("临停向上取整开关不能为空");
        }
        if (updateDto.getTempDailyCapAmount() == null)
        {
            throw new ServiceException("临停每日封顶金额不能为空");
        }
        if (updateDto.getMemberDiscountEnabled() == null)
        {
            throw new ServiceException("会员折扣开关不能为空");
        }
        if (updateDto.getSilverDiscountRate() == null)
        {
            throw new ServiceException("银卡折扣率不能为空");
        }
        if (updateDto.getGoldDiscountRate() == null)
        {
            throw new ServiceException("金卡折扣率不能为空");
        }
        if (updateDto.getPlatinumDiscountRate() == null)
        {
            throw new ServiceException("铂金折扣率不能为空");
        }
        if (updateDto.getDefaultPaymentChannel() == null)
        {
            throw new ServiceException("默认支付渠道不能为空");
        }
        if (updateDto.getCashEnabled() == null)
        {
            throw new ServiceException("现金支付开关不能为空");
        }
        if (updateDto.getCancelAutoRefundEnabled() == null)
        {
            throw new ServiceException("取消订单自动退款开关不能为空");
        }

        validateNonNegative(updateDto.getMonthlyPrice(), MONTHLY_PRICE_KEY);
        validateNonNegative(updateDto.getTempHourPrice(), TEMP_HOUR_PRICE_KEY);
        validateNonNegative(updateDto.getTempDailyCapAmount(), TEMP_DAILY_CAP_AMOUNT_KEY);
        validateDiscountRate(updateDto.getSilverDiscountRate(), MEMBER_DISCOUNT_SILVER_KEY);
        validateDiscountRate(updateDto.getGoldDiscountRate(), MEMBER_DISCOUNT_GOLD_KEY);
        validateDiscountRate(updateDto.getPlatinumDiscountRate(), MEMBER_DISCOUNT_PLATINUM_KEY);

        if (updateDto.getTempFreeMinutes() < 0)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + TEMP_FREE_MINUTES_KEY);
        }
        if (updateDto.getTempBillingStepMinutes() <= 0)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + TEMP_BILLING_STEP_MINUTES_KEY);
        }
        if (StringUtils.isBlank(updateDto.getDefaultPaymentChannel()))
        {
            throw new ServiceException("默认支付渠道不能为空");
        }
    }

    private void validateDefaultPaymentChannel(String defaultPaymentChannel)
    {
        List<SysDictData> channels = sysDictTypeService.selectDictDataByType(PAYMENT_CHANNEL_DICT_TYPE);
        boolean matched = channels.stream()
            .anyMatch(channel -> StringUtils.equals(channel.getDictValue(), defaultPaymentChannel));
        if (!matched)
        {
            throw new ServiceException("默认支付渠道不合法");
        }
    }

    private void validateNonNegative(BigDecimal amount, String configKey)
    {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + configKey);
        }
    }

    private void validateDiscountRate(BigDecimal discountRate, String configKey)
    {
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) > 0)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + configKey);
        }
    }

    private void saveOrUpdateConfig(String key, String name, String value, String operator)
    {
        SysConfig query = new SysConfig();
        query.setConfigKey(key);
        List<SysConfig> configs = sysConfigService.selectConfigList(query);
        if (configs.isEmpty())
        {
            SysConfig config = new SysConfig();
            config.setConfigName(name);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("N");
            config.setCreateBy(operator);
            sysConfigService.insertConfig(config);
            return;
        }

        SysConfig config = configs.get(0);
        config.setConfigName(name);
        config.setConfigValue(value);
        config.setConfigType("N");
        config.setUpdateBy(operator);
        sysConfigService.updateConfig(config);
    }

    private String readString(String configKey)
    {
        String value = sysConfigService.selectConfigByKey(configKey);
        return StringUtils.isBlank(value) ? null : value;
    }

    private BigDecimal readBigDecimal(String configKey)
    {
        String value = readString(configKey);
        if (value == null)
        {
            return null;
        }
        try
        {
            return new BigDecimal(value);
        }
        catch (NumberFormatException ex)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + configKey);
        }
    }

    private Integer readInteger(String configKey)
    {
        String value = readString(configKey);
        if (value == null)
        {
            return null;
        }
        try
        {
            return Integer.valueOf(value);
        }
        catch (NumberFormatException ex)
        {
            throw new ServiceException(CONFIG_INVALID_MESSAGE + configKey);
        }
    }

    private Boolean readBoolean(String configKey)
    {
        String value = readString(configKey);
        if (value == null)
        {
            return null;
        }
        if ("true".equalsIgnoreCase(value))
        {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value))
        {
            return Boolean.FALSE;
        }
        throw new ServiceException(CONFIG_INVALID_MESSAGE + configKey);
    }
}
