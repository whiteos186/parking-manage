package com.ruoyi.parking.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.parking.service.IParkingUserConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/parking/userConfig")
public class ParkingUserConfigController extends BaseController
{
    private final IParkingUserConfigService parkingUserConfigService;

    public ParkingUserConfigController(IParkingUserConfigService parkingUserConfigService)
    {
        this.parkingUserConfigService = parkingUserConfigService;
    }

    /**
     * 查询当前用户的指定配置
     */
    @GetMapping
    public AjaxResult get(@RequestParam("key") String key)
    {
        Long userId = SecurityUtils.getUserId();
        String value = parkingUserConfigService.getConfigValue(userId, key);
        return AjaxResult.success(value);
    }

    /**
     * 保存当前用户的指定配置
     * 请求体: { "key": "...", "value": "..." }
     */
    @PutMapping
    public AjaxResult save(@RequestBody Map<String, String> body)
    {
        String key = body.get("key");
        String value = body.get("value");
        if (key == null || key.isEmpty())
        {
            return AjaxResult.error("配置键不能为空");
        }
        Long userId = SecurityUtils.getUserId();
        parkingUserConfigService.saveConfigValue(userId, key, value);
        return AjaxResult.success();
    }
}
