package com.ruoyi.parking.service;

import com.ruoyi.parking.domain.dto.ParkingSettingsDto;
import com.ruoyi.parking.domain.dto.ParkingSettingsUpdateDto;

public interface IParkingGlobalRuleService
{
    ParkingSettingsDto getSettings();

    ParkingSettingsDto.Pricing getValidatedPricing();

    ParkingSettingsDto.MemberDiscount getValidatedMemberDiscount();

    ParkingSettingsDto.Payment getValidatedPayment();

    ParkingSettingsDto.Switches getValidatedSwitches();

    void updateSettings(ParkingSettingsUpdateDto updateDto, String operator);
}
