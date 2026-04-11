# parking-status

Report the current completion status of the parking management system. Do not make any changes — this is a read-only audit.

Check each area below and produce a structured report.

---

## 1. Database schema

Read `sql/parking/parking_bootstrap.sql`. Check whether these tables exist:

- [ ] `parking_lot`
- [ ] `parking_space`
- [ ] `parking_lot_admin`
- [ ] `parking_customer`
- [ ] `parking_user_vehicle`
- [ ] `parking_monthly_order`
- [ ] `parking_membership_order`
- [ ] `parking_temp_order`
- [ ] `parking_payment_record`

For `parking_user_vehicle`, `parking_monthly_order`, `parking_membership_order`, `parking_temp_order`, `parking_payment_record`: confirm whether they use `customer_id` (correct) or still `user_id` (pending customer mainline migration).

## 2. Navigation SQL

Read `sql/parking/parking_menu.sql`. Check whether:

- [ ] A top-level `工作台` menu exists (not `停车管理`)
- [ ] `基础档案` root (path: archives) exists
- [ ] `运营中心` root (path: operations) exists
- [ ] `客户中心` root (path: customers) exists

## 3. Backend controllers

List which controllers exist under `ruoyi-parking/src/main/java/com/ruoyi/parking/controller/`. Check whether `ParkingCustomerController.java` exists.

## 4. Frontend views

Check which view directories exist under `ruoyi-ui/src/views/parking/`:

- [ ] `lot/index.vue`
- [ ] `space/index.vue`
- [ ] `lotadmin/index.vue`
- [ ] `temp/index.vue`
- [ ] `monthly/index.vue`
- [ ] `membership/index.vue`
- [ ] `payment/index.vue`
- [ ] `vehicle/index.vue`
- [ ] `customer/index.vue`

## 5. Overview quick-action routes

Read `ruoyi-ui/src/views/parking/index.vue` and find the quick-action route push targets. Check whether they use the new IA paths (`/archives/lot`, `/operations/temp`, etc.) or the old paths (`/parking/lot`, `/parking/temp`, etc.).

## 6. Pending plans

Read both plan documents in `docs/superpowers/plans/` and count how many `- [ ]` (unchecked) steps remain vs `- [x]` (done) across all tasks.

---

## Output format

Produce a concise table or checklist. For each item: ✓ done, ✗ missing/pending, or ⚠ partially done. End with a summary of which work streams are complete and which are pending.
