# Parking Global Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a global parking settings feature that stores business rules in `sys_config`, moves parking enums to `sys_dict`, exposes a parking settings API/UI, and rewires core parking pricing logic to consume those settings.

**Architecture:** Add a typed parking settings layer inside `ruoyi-parking`, seed global config and dict data through SQL scripts, and migrate core business flows to use the typed settings service instead of hard-coded values or per-lot pricing fields. Keep historical order snapshots unchanged and keep `parking_user_config` scoped to personal preferences.

**Tech Stack:** Spring Boot, RuoYi system config/dict services, MyBatis, Vue 2, Element UI, JUnit 5, Mockito, SQL bootstrap scripts.

---

### Task 1: Seed Global Config And Dict Data

**Files:**
- Modify: `D:\code\parking-management-system\sql\parking\parking_upgrade_202604.sql`
- Modify: `D:\code\parking-management-system\sql\parking\parking_menu.sql`
- Test: `D:\code\parking-management-system\ruoyi-admin\src\test\java\com\ruoyi\parking\ParkingUpgradeScriptTest.java`
- Test: `D:\code\parking-management-system\ruoyi-admin\src\test\java\com\ruoyi\parking\ParkingMenuScriptTest.java`

- [ ] **Step 1: Write the failing SQL script tests**

```java
@Test
void upgradeScriptSeedsParkingGlobalConfigKeys() throws IOException
{
    Path scriptPath = locateUpgradeScript();
    String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

    assertTrue(sql.contains("parking.rule.monthlyprice"));
    assertTrue(sql.contains("parking.rule.temphourprice"));
    assertTrue(sql.contains("parking.rule.memberdiscount.gold"));
    assertTrue(sql.contains("parking_payment_channel"));
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn -pl ruoyi-admin -Dtest=ParkingUpgradeScriptTest,ParkingMenuScriptTest test`

Expected: FAIL because the upgrade script and menu script do not yet contain settings seeds or parking settings menu entries.

- [ ] **Step 3: Add idempotent config/dict seed SQL and parking settings menu SQL**

```sql
insert into sys_config (...) 
select ..., 'parking.rule.monthlyPrice', '380.00', ...
from dual
where not exists (
  select 1 from sys_config where config_key = 'parking.rule.monthlyPrice'
);

insert into sys_dict_type (...)
select ..., '停车支付渠道', 'parking_payment_channel', ...
from dual
where not exists (
  select 1 from sys_dict_type where dict_type = 'parking_payment_channel'
);
```

- [ ] **Step 4: Re-run the tests**

Run: `mvn -pl ruoyi-admin -Dtest=ParkingUpgradeScriptTest,ParkingMenuScriptTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add sql/parking/parking_upgrade_202604.sql sql/parking/parking_menu.sql ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingUpgradeScriptTest.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java
git commit -m "feat: seed parking global settings and dict data"
```

### Task 2: Add Typed Parking Settings Backend

**Files:**
- Create: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\domain\dto\ParkingSettingsDto.java`
- Create: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\domain\dto\ParkingSettingsUpdateDto.java`
- Create: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\service\IParkingGlobalRuleService.java`
- Create: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\service\impl\ParkingGlobalRuleServiceImpl.java`
- Create: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\controller\ParkingSettingsController.java`
- Test: `D:\code\parking-management-system\ruoyi-admin\src\test\java\com\ruoyi\parking\ParkingSettingsControllerTest.java`
- Test: `D:\code\parking-management-system\ruoyi-admin\src\test\java\com\ruoyi\parking\ParkingGlobalRuleServiceTest.java`

- [ ] **Step 1: Write failing service tests for typed config parsing**

```java
@Test
void loadSettingsReturnsTypedParkingRules()
{
    when(sysConfigService.selectConfigByKey("parking.rule.monthlyPrice")).thenReturn("380.00");
    when(sysConfigService.selectConfigByKey("parking.rule.tempHourPrice")).thenReturn("8.00");

    ParkingSettingsDto settings = service.getSettings();

    assertEquals(new BigDecimal("380.00"), settings.getPricing().getMonthlyPrice());
    assertEquals(new BigDecimal("8.00"), settings.getPricing().getTempHourPrice());
}
```

- [ ] **Step 2: Run the backend settings tests**

Run: `mvn -pl ruoyi-admin -Dtest=ParkingGlobalRuleServiceTest,ParkingSettingsControllerTest test`

Expected: FAIL because the settings service/controller do not exist yet.

- [ ] **Step 3: Implement the typed rule service and settings controller**

```java
public interface IParkingGlobalRuleService
{
    ParkingSettingsDto getSettings();
    void updateSettings(ParkingSettingsUpdateDto updateDto, String operator);
    BigDecimal getMonthlyPrice();
    BigDecimal getTempHourPrice();
    BigDecimal getMemberDiscountRate(String memberType);
}
```

- [ ] **Step 4: Re-run the backend settings tests**

Run: `mvn -pl ruoyi-admin -Dtest=ParkingGlobalRuleServiceTest,ParkingSettingsControllerTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add ruoyi-parking/src/main/java/com/ruoyi/parking/domain/dto/ParkingSettingsDto.java ruoyi-parking/src/main/java/com/ruoyi/parking/domain/dto/ParkingSettingsUpdateDto.java ruoyi-parking/src/main/java/com/ruoyi/parking/service/IParkingGlobalRuleService.java ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/ParkingGlobalRuleServiceImpl.java ruoyi-parking/src/main/java/com/ruoyi/parking/controller/ParkingSettingsController.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingSettingsControllerTest.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingGlobalRuleServiceTest.java
git commit -m "feat: add parking settings backend"
```

### Task 3: Rewire Pricing Logic To Global Settings

**Files:**
- Modify: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\service\impl\ParkingMonthlyOrderServiceImpl.java`
- Modify: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\service\impl\ParkingTempOrderServiceImpl.java`
- Modify: `D:\code\parking-management-system\ruoyi-parking\src\main\java\com\ruoyi\parking\util\ParkingMemberDiscountUtils.java`
- Test: `D:\code\parking-management-system\ruoyi-admin\src\test\java\com\ruoyi\parking\ParkingMonthlyOrderPricingTest.java`
- Test: `D:\code\parking-management-system\ruoyi-admin\src\test\java\com\ruoyi\parking\ParkingTempOrderPricingTest.java`
- Test: `D:\code\parking-management-system\ruoyi-admin\src\test\java\com\ruoyi\parking\ParkingMemberDiscountTest.java`

- [ ] **Step 1: Write failing tests proving pricing no longer reads per-lot fields**

```java
@Test
void tempOrderExitUsesGlobalTempHourPrice()
{
    when(globalRuleService.getTempHourPrice()).thenReturn(new BigDecimal("10.00"));
    when(globalRuleService.getTempBillingStepMinutes()).thenReturn(30);

    service.exitParkingTempOrder(1L);

    assertEquals(new BigDecimal("10.00"), updated.getFeeAmount());
}
```

- [ ] **Step 2: Run the pricing tests**

Run: `mvn -pl ruoyi-admin -Dtest=ParkingMonthlyOrderPricingTest,ParkingTempOrderPricingTest,ParkingMemberDiscountTest test`

Expected: FAIL because the services still depend on lot pricing fields and hard-coded discount rates.

- [ ] **Step 3: Implement the minimal pricing changes**

```java
BigDecimal monthlyPrice = parkingGlobalRuleService.getMonthlyPrice();
order.setOriginalAmount(monthlyPrice.multiply(BigDecimal.valueOf(months)));

BigDecimal discount = ParkingMemberDiscountUtils.calculateDiscount(
    order.getOriginalAmount(),
    customer,
    parkingGlobalRuleService
);
```

- [ ] **Step 4: Re-run the pricing tests**

Run: `mvn -pl ruoyi-admin -Dtest=ParkingMonthlyOrderPricingTest,ParkingTempOrderPricingTest,ParkingMemberDiscountTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/ParkingMonthlyOrderServiceImpl.java ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/ParkingTempOrderServiceImpl.java ruoyi-parking/src/main/java/com/ruoyi/parking/util/ParkingMemberDiscountUtils.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMonthlyOrderPricingTest.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingTempOrderPricingTest.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMemberDiscountTest.java
git commit -m "feat: apply parking global pricing rules"
```

### Task 4: Build Parking Settings UI And Dict Migration

**Files:**
- Create: `D:\code\parking-management-system\ruoyi-ui\src\api\parking\settings.js`
- Create: `D:\code\parking-management-system\ruoyi-ui\src\views\parking\settings\index.vue`
- Modify: `D:\code\parking-management-system\ruoyi-ui\src\views\parking\lot\index.vue`
- Modify: `D:\code\parking-management-system\ruoyi-ui\src\views\parking\payment\form.vue`
- Modify: `D:\code\parking-management-system\ruoyi-ui\src\views\parking\options.js`

- [ ] **Step 1: Write the failing UI/API wiring changes**

```javascript
export function getParkingSettings() {
  return request({
    url: '/parking/settings',
    method: 'get'
  })
}
```

- [ ] **Step 2: Run targeted frontend verification**

Run: `npm --prefix ruoyi-ui run lint -- src/api/parking/settings.js src/views/parking/settings/index.vue src/views/parking/lot/index.vue src/views/parking/payment/form.vue`

Expected: FAIL because the files and dict usages do not exist yet.

- [ ] **Step 3: Implement the settings page and first-pass dict migration**

```javascript
export default {
  name: 'ParkingSettings',
  dicts: ['parking_payment_channel'],
  created() {
    this.getSettings()
  }
}
```

- [ ] **Step 4: Re-run the frontend verification**

Run: `npm --prefix ruoyi-ui run lint -- src/api/parking/settings.js src/views/parking/settings/index.vue src/views/parking/lot/index.vue src/views/parking/payment/form.vue`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add ruoyi-ui/src/api/parking/settings.js ruoyi-ui/src/views/parking/settings/index.vue ruoyi-ui/src/views/parking/lot/index.vue ruoyi-ui/src/views/parking/payment/form.vue ruoyi-ui/src/views/parking/options.js
git commit -m "feat: add parking settings UI"
```

### Task 5: Full Regression Verification

**Files:**
- Verify only

- [ ] **Step 1: Run backend parking tests**

Run: `mvn -pl ruoyi-admin -Dtest=ParkingUpgradeScriptTest,ParkingMenuScriptTest,ParkingSchemaScriptTest,ParkingSettingsControllerTest,ParkingGlobalRuleServiceTest,ParkingMonthlyOrderPricingTest,ParkingTempOrderPricingTest,ParkingMemberDiscountTest test`

Expected: PASS

- [ ] **Step 2: Run targeted frontend lint**

Run: `npm --prefix ruoyi-ui run lint -- src/api/parking src/views/parking`

Expected: PASS

- [ ] **Step 3: Inspect diff for scope compliance**

Run: `git diff --stat HEAD~4..HEAD`

Expected: changes limited to parking settings, SQL seeds, pricing logic, and related tests/UI.

- [ ] **Step 4: Commit final polish if needed**

```bash
git add -A
git commit -m "feat: finish parking global settings integration"
```
