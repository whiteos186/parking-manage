# Parking Customer Mainline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the parking module operate as one unified parking management platform by separating internal `sys_user` accounts from parking customers and using customer archives as the business mainline.

**Architecture:** Keep `sys_user/sys_role` for platform employees and lot administrators, and introduce a parking customer archive for vehicle, monthly, membership, temporary, and payment records. Update schema, Java domain/mapper/service/controller contracts, and Vue pages together so the product no longer exposes raw backend account IDs in parking business flows.

**Tech Stack:** RuoYi Vue, Spring Boot, MyBatis XML, Vue 2 + Element UI, JUnit 5, Mockito

---

### Task 1: Lock the dual-account boundary with failing tests

**Files:**
- Modify: `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingSchemaScriptTest.java`
- Modify: `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingUserVehicleControllerTest.java`
- Modify: `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMonthlyOrderControllerTest.java`

- [x] **Step 1: Write the failing schema test**

```java
Map.entry("parking_customer", List.of("customer_id", "customer_code", "customer_name", "mobile", "status")),
Map.entry("parking_user_vehicle", List.of("vehicle_id", "customer_id", "plate_no", "vehicle_type")),
Map.entry("parking_membership_order", List.of("membership_order_id", "order_no", "customer_id", "lot_id", "pay_status")),
Map.entry("parking_monthly_order", List.of("monthly_order_id", "order_no", "customer_id", "lot_id")),
Map.entry("parking_temp_order", List.of("temp_order_id", "order_no", "lot_id", "customer_id", "vehicle_plate_no")),
Map.entry("parking_payment_record", List.of("payment_id", "biz_order_no", "biz_order_type", "customer_id", "pay_amount", "pay_status"))
```

- [x] **Step 2: Run the schema test and verify it fails**

Run: `mvn -pl ruoyi-admin -am "-Dtest=ParkingSchemaScriptTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
Expected: FAIL because `parking_customer` and `customer_id` do not exist in `sql/parking/parking_bootstrap.sql`

- [x] **Step 3: Write controller tests against `customerId`**

```java
form.setCustomerId(100L);
assertEquals(100L, captor.getValue().getCustomerId());
```

- [x] **Step 4: Run the targeted controller tests and verify they fail**

Run: `mvn -pl ruoyi-admin -am "-Dtest=ParkingUserVehicleControllerTest,ParkingMonthlyOrderControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
Expected: FAIL because parking domain objects and controllers still expose `userId`

- [x] **Step 5: Commit**

```bash
git add ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingSchemaScriptTest.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingUserVehicleControllerTest.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMonthlyOrderControllerTest.java
git commit -m "test: lock parking customer boundary"
```

### Task 2: Add customer archive schema and demo data

**Files:**
- Modify: `sql/parking/parking_bootstrap.sql`

- [x] **Step 1: Add the failing schema expectations from Task 1**

```sql
drop table if exists parking_customer;

create table parking_customer (
  customer_id           bigint(20)      not null auto_increment comment 'Customer ID',
  customer_code         varchar(32)     not null                comment 'Customer code',
  customer_name         varchar(50)     not null default ''     comment 'Customer name',
  mobile                varchar(20)     not null default ''     comment 'Mobile number',
  customer_type         char(1)         default '1'             comment 'Customer type (1 owner, 2 member, 3 enterprise, 4 visitor)',
  status                char(1)         default '0'             comment 'Status (0 active, 1 disabled)',
  del_flag              char(1)         default '0'             comment 'Delete flag (0 exists, 2 deleted)',
  create_by             varchar(64)     default ''              comment 'Created by',
  create_time           datetime                                   comment 'Created time',
  update_by             varchar(64)     default ''              comment 'Updated by',
  update_time           datetime                                   comment 'Updated time',
  remark                varchar(500)    default null            comment 'Remark',
  primary key (customer_id),
  unique key uk_parking_customer_code (customer_code),
  key idx_parking_customer_mobile (mobile)
) engine=innodb auto_increment=100 comment='Parking customer archive';
```

- [x] **Step 2: Replace business-table `user_id` columns with `customer_id`**

```sql
customer_id            bigint(20)      not null                comment 'parking_customer.customer_id'
```

- [x] **Step 3: Seed one customer and repoint demo business records**

```sql
insert into parking_customer (
  customer_id, customer_code, customer_name, mobile, customer_type, status, del_flag,
  create_by, create_time, update_by, update_time, remark
) values (
  1, 'CUST-0001', '陈晓', '13800138000', '1', '0', '0',
  'admin', sysdate(), 'admin', sysdate(), 'Demo parking customer'
);
```

- [x] **Step 4: Run the schema test and verify it passes**

Run: `mvn -pl ruoyi-admin -am "-Dtest=ParkingSchemaScriptTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add sql/parking/parking_bootstrap.sql
git commit -m "feat: add parking customer archive schema"
```

### Task 3: Switch backend parking business objects from `userId` to `customerId`

**Files:**
- Create: `ruoyi-parking/src/main/java/com/ruoyi/parking/domain/ParkingCustomer.java`
- Create: `ruoyi-parking/src/main/java/com/ruoyi/parking/mapper/ParkingCustomerMapper.java`
- Create: `ruoyi-parking/src/main/java/com/ruoyi/parking/service/IParkingCustomerService.java`
- Create: `ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/ParkingCustomerServiceImpl.java`
- Create: `ruoyi-parking/src/main/java/com/ruoyi/parking/controller/ParkingCustomerController.java`
- Modify: `ruoyi-parking/src/main/java/com/ruoyi/parking/domain/ParkingUserVehicle.java`
- Modify: `ruoyi-parking/src/main/java/com/ruoyi/parking/domain/ParkingMonthlyOrder.java`
- Modify: `ruoyi-parking/src/main/java/com/ruoyi/parking/domain/ParkingMembershipOrder.java`
- Modify: `ruoyi-parking/src/main/java/com/ruoyi/parking/domain/ParkingTempOrder.java`
- Modify: `ruoyi-parking/src/main/java/com/ruoyi/parking/domain/ParkingPaymentRecord.java`
- Modify: `ruoyi-parking/src/main/java/com/ruoyi/parking/service/IParkingPaymentRecordService.java`
- Modify: `ruoyi-parking/src/main/java/com/ruoyi/parking/service/impl/ParkingPaymentRecordServiceImpl.java`

- [x] **Step 1: Implement the minimal `ParkingCustomer` domain**

```java
public class ParkingCustomer extends BaseEntity
{
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String mobile;
    private String customerType;
    private String status;
    private String delFlag;
}
```

- [x] **Step 2: Rename business properties to `customerId`**

```java
@NotNull(message = "客户不能为空")
private Long customerId;
```

- [x] **Step 3: Update payment creation API to receive customer identity**

```java
int createPaymentRecord(String bizOrderNo, String bizOrderType,
    Long customerId, Long lotId, BigDecimal amount, String payChannel, String createBy);
```

- [x] **Step 4: Run the targeted controller tests and verify they pass**

Run: `mvn -pl ruoyi-admin -am "-Dtest=ParkingUserVehicleControllerTest,ParkingMonthlyOrderControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add ruoyi-parking/src/main/java/com/ruoyi/parking
git commit -m "refactor: switch parking business objects to customer ids"
```

### Task 4: Update MyBatis mappings and customer CRUD integration

**Files:**
- Create: `ruoyi-parking/src/main/resources/mapper/parking/ParkingCustomerMapper.xml`
- Modify: `ruoyi-parking/src/main/resources/mapper/parking/ParkingUserVehicleMapper.xml`
- Modify: `ruoyi-parking/src/main/resources/mapper/parking/ParkingMonthlyOrderMapper.xml`
- Modify: `ruoyi-parking/src/main/resources/mapper/parking/ParkingMembershipOrderMapper.xml`
- Modify: `ruoyi-parking/src/main/resources/mapper/parking/ParkingTempOrderMapper.xml`
- Modify: `ruoyi-parking/src/main/resources/mapper/parking/ParkingPaymentRecordMapper.xml`

- [x] **Step 1: Add customer CRUD SQL**

```xml
<select id="selectParkingCustomerOptions" resultType="ParkingCustomer">
    select customer_id, customer_code, customer_name, mobile, customer_type, status
    from parking_customer
    where del_flag = '0'
      and status = '0'
    order by customer_id desc
</select>
```

- [x] **Step 2: Replace business table column mappings**

```xml
<result property="customerId" column="customer_id"/>
<if test="customerId != null">
    and customer_id = #{customerId}
</if>
```

- [x] **Step 3: Keep lot-admin mapper bound to `sys_user`**

```xml
left join sys_user u on u.user_id = a.user_id and u.del_flag = '0'
```

- [x] **Step 4: Run mapper-driven controller tests**

Run: `mvn -pl ruoyi-admin -am "-Dtest=ParkingUserVehicleControllerTest,ParkingMonthlyOrderControllerTest,ParkingMembershipOrderControllerTest,ParkingTempOrderControllerTest,ParkingPaymentRecordControllerTest,ParkingLotAdminControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add ruoyi-parking/src/main/resources/mapper/parking
git commit -m "feat: wire customer archive through parking mappers"
```

### Task 5: Replace raw user inputs in the parking UI with customer archive flows

**Files:**
- Create: `ruoyi-ui/src/api/parking/customer.js`
- Create: `ruoyi-ui/src/views/parking/customer/index.vue`
- Modify: `ruoyi-ui/src/views/parking/vehicle/index.vue`
- Modify: `ruoyi-ui/src/views/parking/monthly/index.vue`
- Modify: `ruoyi-ui/src/views/parking/membership/index.vue`
- Modify: `ruoyi-ui/src/views/parking/temp/index.vue`
- Modify: `ruoyi-ui/src/views/parking/payment/index.vue`
- Modify: `ruoyi-ui/src/router/index.js`
- Modify: `sql/parking/parking_menu.sql`

- [x] **Step 1: Add customer archive API**

```javascript
export function listCustomerOptions() {
  return request({
    url: '/parking/customer/options',
    method: 'get'
  })
}
```

- [x] **Step 2: Replace `userId` filters and fields with customer selectors**

```javascript
queryParams: {
  customerId: undefined
}
```

```html
<el-form-item label="客户" prop="customerId">
  <el-select v-model="form.customerId" filterable placeholder="请选择客户">
    <el-option
      v-for="item in customerOptions"
      :key="item.customerId"
      :label="customerOptionLabel(item)"
      :value="item.customerId"
    />
  </el-select>
</el-form-item>
```

- [x] **Step 3: Add one parking-platform menu entry for customer archive**

```sql
('客户档案', 'parking/customer', 'parking/customer/index', ...)
```

- [x] **Step 4: Run the production UI build**

Run: `npm run build:prod`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add ruoyi-ui/src/api/parking/customer.js ruoyi-ui/src/views/parking/customer/index.vue ruoyi-ui/src/views/parking/vehicle/index.vue ruoyi-ui/src/views/parking/monthly/index.vue ruoyi-ui/src/views/parking/membership/index.vue ruoyi-ui/src/views/parking/temp/index.vue ruoyi-ui/src/views/parking/payment/index.vue ruoyi-ui/src/router/index.js sql/parking/parking_menu.sql
git commit -m "feat: use parking customer archive across parking ui"
```

### Task 6: Verify application behavior and local SQL import

**Files:**
- Modify: `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingModuleSmokeTest.java`
- Modify: `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java`

- [x] **Step 1: Extend smoke coverage for customer archive**

```java
assertTrue(sql.contains("客户档案"));
assertTrue(sql.contains("parking_customer"));
```

- [x] **Step 2: Run the focused backend suite**

Run: `mvn -pl ruoyi-admin -am "-Dtest=ParkingSchemaScriptTest,ParkingMenuScriptTest,ParkingModuleSmokeTest,ParkingUserVehicleControllerTest,ParkingMonthlyOrderControllerTest,ParkingMembershipOrderControllerTest,ParkingTempOrderControllerTest,ParkingPaymentRecordControllerTest,ParkingLotAdminControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
Expected: PASS

- [x] **Step 3: Re-import the local menu SQL with a UTF-8-safe path**

Run: `docker cp .\\sql\\parking\\parking_menu.sql parking-mvp-mysql:/tmp/parking_menu.sql`
Expected: file copied into the MySQL container

- [x] **Step 4: Apply the SQL in-container**

Run: `docker exec parking-mvp-mysql sh -lc "mysql --default-character-set=utf8mb4 -uroot -p123456 -D ry_vue < /tmp/parking_menu.sql"`
Expected: no output and updated menu data

- [x] **Step 5: Commit**

```bash
git add ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingModuleSmokeTest.java ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java
git commit -m "test: verify unified parking platform shell"
```
