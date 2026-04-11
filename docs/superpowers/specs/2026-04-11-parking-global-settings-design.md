# 停车业务全局设置打通设计

## 目标

把当前分散在停车业务表、业务代码和前端常量中的规则，统一收敛到若依现有的系统设置体系中：

- `sys_config` 承载停车业务的全局规则
- `sys_dict_type / sys_dict_data` 承载停车业务的枚举与标签
- `parking_user_config` 继续只承载个人偏好

目标结果是：系统设置数据可以直接驱动停车场管理系统的业务计算、业务开关和展示枚举，同时不破坏现有订单快照和历史数据。

## 一期范围

一期纳入统一管理的内容如下。

### 全局业务规则

- 月卡全局价格
- 临停全局小时单价
- 临停免费分钟数
- 临停计费步长分钟数
- 临停计费向上取整开关
- 临停封顶金额
- 会员折扣启用开关
- 银卡折扣率
- 金卡折扣率
- 铂金折扣率
- 支付渠道默认值
- 线下现金支付启用开关
- 订单取消后是否自动退款开关

### 系统字典

- 停车场状态
- 车位状态
- 车位类型
- 车辆类型
- 车辆状态
- 默认车辆标识
- 客户类型
- 客户状态
- 会员类型
- 临停订单支付状态
- 临停订单业务状态
- 月卡订单支付状态
- 月卡订单业务状态
- 会员订单支付状态
- 会员订单业务状态
- 支付流水业务类型
- 支付渠道
- 支付状态
- 停车场管理员绑定状态

### 继续保留原职责的数据

- `parking_user_config` 只存用户个人偏好，例如工作台快捷入口
- 订单表中的金额、折扣、支付结果继续作为订单快照保存
- `parking_lot`、`parking_space`、`parking_customer` 等业务主数据继续保留

## 不做的事情

一期不做以下内容。

- 不新建独立的 `parking_global_config` 表
- 不支持按停车场覆盖全局规则
- 不做多租户配置隔离
- 不删除 `parking_lot.monthly_price` 和 `parking_lot.temp_hour_price` 字段
- 不在一期把所有停车页面的所有展示选项都改造成动态字典，只覆盖实际业务使用的枚举

## 总体方案

采用“`sys_config + sys_dict` 分层”的统一设置方案。

### 配置归属

- `sys_config` 存“会影响业务执行和金额计算的全局规则”
- `sys_dict` 存“会影响展示、筛选、状态流转标识的枚举”
- `parking_user_config` 存“用户个人偏好”

### 统一访问入口

停车模块新增一层配置读取服务，业务代码不再直接散落地读取系统配置或硬编码常量。

建议新增：

- `ParkingGlobalRuleService`
- `ParkingSettingsController`
- `ParkingSettingsDto`
- `ParkingSettingsUpdateDto`

这层服务负责：

- 读取 `sys_config`
- 类型转换
- 对非关键展示项提供有限默认值
- 配置合法性校验
- 向业务服务暴露强类型规则对象

## 配置键设计

一期使用如下 `sys_config.config_key`。

### 定价与计费

- `parking.rule.monthlyPrice`
- `parking.rule.tempHourPrice`
- `parking.rule.tempFreeMinutes`
- `parking.rule.tempBillingStepMinutes`
- `parking.rule.tempRoundUpEnabled`
- `parking.rule.tempDailyCapAmount`

### 会员折扣

- `parking.rule.memberDiscountEnabled`
- `parking.rule.memberDiscount.silver`
- `parking.rule.memberDiscount.gold`
- `parking.rule.memberDiscount.platinum`

### 支付与业务开关

- `parking.rule.payment.defaultChannel`
- `parking.rule.payment.cashEnabled`
- `parking.rule.order.cancelAutoRefundEnabled`

### 配置值约束

- 价格、封顶金额、折扣率使用十进制字符串保存
- 分钟数、步长使用整数保存
- 开关使用 `true/false`
- 默认支付渠道的值必须来自支付渠道字典

## 字典类型设计

一期新增如下字典类型。

- `parking_lot_status`
- `parking_space_status`
- `parking_space_type`
- `parking_vehicle_type`
- `parking_vehicle_status`
- `parking_vehicle_default`
- `parking_customer_type`
- `parking_customer_status`
- `parking_membership_type`
- `parking_temp_pay_status`
- `parking_temp_biz_status`
- `parking_monthly_pay_status`
- `parking_monthly_biz_status`
- `parking_membership_pay_status`
- `parking_membership_biz_status`
- `parking_payment_biz_type`
- `parking_payment_channel`
- `parking_payment_status`
- `parking_lot_admin_status`

这些字典将替代 [options.js](D:/code/parking-management-system/ruoyi-ui/src/views/parking/options.js) 中对应的硬编码枚举。

## 后端设计

### 新增设置聚合接口

停车模块新增设置接口，而不是让前端直接逐个操作 `system/config`。

建议接口：

- `GET /parking/settings`
- `PUT /parking/settings`

返回值为结构化 DTO，包含：

- pricing
- memberDiscount
- payment
- switches

这样前端编辑设置时不需要理解底层 `sys_config` 的平铺 key。

### 业务接入点

以下业务逻辑改为统一从 `ParkingGlobalRuleService` 取规则。

#### 临停订单

[ParkingTempOrderServiceImpl.java](D:/code/parking-management-system/ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/ParkingTempOrderServiceImpl.java)

改造点：

- 出场结算不再读取 `parking_lot.tempHourPrice`
- 改为读取全局临停价格、免费分钟、计费步长、封顶金额、向上取整规则
- 支付渠道默认值来自全局配置

#### 月卡订单

[ParkingMonthlyOrderServiceImpl.java](D:/code/parking-management-system/ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/ParkingMonthlyOrderServiceImpl.java)

改造点：

- 原价计算不再读取 `parking_lot.monthlyPrice`
- 改为读取全局月卡价格
- 折扣逻辑改为读取会员折扣配置

#### 会员折扣工具

[ParkingMemberDiscountUtils.java](D:/code/parking-management-system/ruoyi-parking/src/main/java/com/ruoyi/parking/util/ParkingMemberDiscountUtils.java)

改造点：

- 去掉银卡 `0.05`、金卡 `0.10`、铂金 `0.15` 的硬编码
- 改为由配置服务提供折扣率
- 保留“客户必须是有效会员且未过期”这一业务前置条件

#### 会员订单与支付流水

- 会员订单计算和支付记录创建统一读取默认支付渠道和业务开关
- 取消订单后的退款策略由全局业务开关控制

### 配置校验策略

系统启动时不强制校验停车配置是否齐全，但在运行时执行业务前必须校验。

规则：

- 读取到空值、非法格式、越界值时，抛出明确的业务异常
- 核心计费与折扣规则不允许静默兜底
- 不允许静默回退到 `0` 或随机默认值
- 对关键金额字段的异常提示要明确指出缺失的配置键

示例：

- `停车全局配置缺失：parking.rule.tempHourPrice`
- `停车全局配置非法：parking.rule.memberDiscount.gold`

## 前端设计

### 新增停车设置页面

新增页面建议挂在“基础档案”下，命名为“停车设置”。

职责：

- 编辑停车全局规则
- 展示当前关键字典类型的入口说明
- 不重复造系统字典管理页面

页面范围只管理 `sys_config` 类型的全局规则，不直接编辑字典项本身。字典维护继续走现有系统管理能力。

### 字典接入

停车页面逐步从硬编码常量切换到系统字典。

改造范围包括：

- 列表状态展示
- 表单下拉选项
- 详情页标签渲染
- 工作台图表和概览页中的状态文案

前端改造原则：

- 优先复用若依现有字典加载机制
- 停车模块内不再新增新的本地常量文件来维护同类枚举
- 当页面尚未接入字典前，旧常量不得与新字典并行长期共存

## 数据迁移策略

### 系统参数初始化

新增停车全局设置初始化 SQL，把一期 `sys_config` 规则写入数据库。

要求：

- 使用幂等写法
- 不依赖固定主键
- remark 中明确标记“parking global rule”

### 系统字典初始化

新增停车字典初始化 SQL，把一期字典类型和字典数据写入数据库。

要求：

- 类型和数据分开幂等初始化
- 字典值与当前业务代码中的值保持一致，避免改动业务状态语义

### 旧字段兼容

`parking_lot.monthly_price` 与 `parking_lot.temp_hour_price` 一期保留，但降级为兼容字段。

规则：

- 新业务计算不再依赖这两个字段
- 停车场表单暂时保留字段展示时，需要标注为“兼容字段”或在一期中改为只读
- 二期再决定是否删除字段或迁移为展示用途

## 权限与菜单

新增停车设置页时，为其增加独立菜单与权限标识，例如：

- `parking:settings:list`
- `parking:settings:edit`

页面建议挂载到当前停车业务主菜单体系中的“基础档案”下，与现有导航设计一致。

## 错误处理

需要覆盖以下错误场景。

- 停车设置缺失
- 配置值格式不合法
- 折扣率超出 `0` 到 `1`
- 计费步长小于等于 `0`
- 默认支付渠道不在字典范围内
- 前端提交的配置项缺少必填字段

后端统一返回明确错误，不允许把错误配置写入后继续执行业务。

## 测试策略

### 后端测试

- `ParkingSettingsController` 接口测试
- `ParkingGlobalRuleService` 读取、解析、校验测试
- 月卡订单价格计算测试
- 临停订单出场计费测试
- 会员折扣配置驱动测试
- SQL 初始化脚本测试

### 前端测试

- 停车设置页加载与保存
- 停车业务页面的字典展示
- 概览页和表单页切换到系统字典后的展示一致性

### 回归重点

- 历史订单金额不漂移
- 订单支付和取消流程不回退
- 工作台快捷入口配置不受影响
- 停车菜单与权限结构不被破坏

## 实施顺序

建议按以下顺序实施。

1. 初始化 `sys_config` 与 `sys_dict` 的停车数据
2. 增加 `ParkingGlobalRuleService` 与设置聚合接口
3. 新增停车设置页面
4. 接入月卡、临停、会员折扣等核心业务计算
5. 替换停车页面中的硬编码枚举
6. 做完整回归测试

## 预期结果

完成后，停车业务的系统设置与业务打通方式如下：

- 全局规则由 `sys_config` 统一控制
- 枚举字典由 `sys_dict` 统一控制
- 停车业务通过一层强类型配置服务消费系统设置
- 订单保留业务执行时的金额与状态快照
- 个人偏好仍由 `parking_user_config` 独立承载

这使停车场管理系统成为整体项目中的一个统一业务域，而不是一套与系统设置割裂的独立实现。
