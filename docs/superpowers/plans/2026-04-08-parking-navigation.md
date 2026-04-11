# Parking Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure the parking module navigation so parking business becomes the primary system menu hierarchy instead of a single `停车管理` bucket.

**Architecture:** Keep dynamic menus driven by `sql/parking/parking_menu.sql`, migrate the old parking root into the new top-level information architecture, and update the overview page's internal route jumps to match the new dynamic paths. Verification stays centered on backend SQL parsing tests plus a UI smoke test for route strings.

**Tech Stack:** MySQL bootstrap SQL, Spring Boot/JUnit 5 tests in `ruoyi-admin`, Vue 2 + RuoYi dynamic router in `ruoyi-ui`

---

### Task 1: Lock the new navigation contract with failing tests

**Files:**
- Modify: `../ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java`
- Modify: `src/views/parking/index.vue`
- Modify: `../ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingModuleSmokeTest.java`

- [x] **Step 1: Write the failing test expectations for the new root menus**

Add assertions for:

```java
assertTrue(normalizedSql.contains("@parking_workbench_id"));
assertTrue(normalizedSql.contains("@parking_archive_root_id"));
assertTrue(normalizedSql.contains("@parking_operations_root_id"));
assertTrue(normalizedSql.contains("@parking_customers_root_id"));

MenuInsertSpec workbench = findInsertByComponent(inserts, "'parking/index'");
assertEquals("'工作台'", workbench.valuesByColumn().get("menu_name"));
assertEquals("0", workbench.valuesByColumn().get("parent_id"));

MenuInsertSpec archives = findInsertByPathAndMenuType(inserts, "'archives'", "'M'");
MenuInsertSpec operations = findInsertByPathAndMenuType(inserts, "'operations'", "'M'");
MenuInsertSpec customers = findInsertByPathAndMenuType(inserts, "'customers'", "'M'");
```

- [x] **Step 2: Verify the new test fails against the current SQL**

Run:

```bash
mvn -pl ruoyi-admin -Dtest=ParkingMenuScriptTest,ParkingModuleSmokeTest test
```

Expected: `ParkingMenuScriptTest` fails because the SQL still inserts `停车管理` as the root and the overview page still routes child links to `/parking/...`.

- [x] **Step 3: Extend the smoke test for the overview quick links**

Add assertions for:

```java
assertTrue(overviewVue.contains("/archives/lot"));
assertTrue(overviewVue.contains("/archives/space"));
assertTrue(overviewVue.contains("/operations/temp"));
assertTrue(overviewVue.contains("/operations/monthly"));
assertTrue(overviewVue.contains("/operations/membership"));
assertTrue(overviewVue.contains("/operations/payment"));
assertTrue(overviewVue.contains("/customers/vehicle"));
assertTrue(overviewVue.contains("/archives/lotadmin"));
```

- [x] **Step 4: Re-run the focused test command and confirm it is still red for the intended reasons**

Run:

```bash
mvn -pl ruoyi-admin -Dtest=ParkingMenuScriptTest,ParkingModuleSmokeTest test
```

Expected: failures reference missing `@parking_workbench_id`, missing root directories, and stale `/parking/...` quick-link paths.

### Task 2: Implement the new parking menu hierarchy

**Files:**
- Modify: `../sql/parking/parking_menu.sql`
- Modify: `src/views/parking/index.vue`

- [x] **Step 1: Rework the menu SQL roots**

Update the SQL so it produces these root items:

```sql
'工作台'      -> top-level C menu using component 'parking/index'
'基础档案'    -> top-level M menu using path 'archives'
'运营中心'    -> top-level M menu using path 'operations'
'客户中心'    -> top-level M menu using path 'customers'
```

Keep all button permissions unchanged.

- [x] **Step 2: Re-parent business pages under the new directories**

Apply this mapping:

```text
基础档案: parking/lot/index, parking/space/index, parking/lotadmin/index
运营中心: parking/temp/index, parking/monthly/index, parking/membership/index, parking/payment/index
客户中心: parking/vehicle/index
工作台: parking/index
```

- [x] **Step 3: Update overview quick-action route pushes**

Set the route targets to:

```js
'/archives/lot'
'/archives/space'
'/operations/temp'
'/operations/monthly'
'/operations/membership'
'/operations/payment'
'/customers/vehicle'
'/archives/lotadmin'
```

- [x] **Step 4: Keep the script idempotent and migration-safe**

Preserve these properties:

```sql
where not exists (...)
select menu_id ... order by menu_id limit 1
update sys_menu ... where menu_id = @variable
```

Do not introduce deletes keyed by fixed `menu_id`.

### Task 3: Verify the restructured navigation

**Files:**
- Verify: `../ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java`
- Verify: `../ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingModuleSmokeTest.java`
- Verify: `../sql/parking/parking_menu.sql`
- Verify: `src/views/parking/index.vue`

- [x] **Step 1: Run the focused regression tests**

Run:

```bash
mvn -pl ruoyi-admin -Dtest=ParkingMenuScriptTest,ParkingModuleSmokeTest test
```

Expected: `BUILD SUCCESS` and both tests green.

- [x] **Step 2: Inspect the diff for only the intended navigation changes**

Run:

```bash
git diff -- ../sql/parking/parking_menu.sql ../ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java ../ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingModuleSmokeTest.java src/views/parking/index.vue ../docs/superpowers/specs/2026-04-08-parking-navigation-design.md ../docs/superpowers/plans/2026-04-08-parking-navigation.md
```

Expected: the diff only shows the new parking navigation structure, the updated overview route pushes, and the two planning documents.

- [x] **Step 3: Leave the worktree uncommitted unless the user asks for a commit**

Run:

```bash
git status --short
```

Expected: the new navigation files are modified, and unrelated existing user changes remain untouched.
