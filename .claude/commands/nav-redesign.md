# nav-redesign

Execute the parking navigation IA restructuring plan end-to-end.

Read `docs/superpowers/plans/2026-04-08-parking-navigation.md` in full first, then work through every task and step in order.

## What this plan delivers

The menu migrates from a single `停车管理` bucket to a four-root platform IA:

```
工作台          ← top-level, component parking/index  (概览页直达)
基础档案        ← path: archives
  停车场管理 / 车位管理 / 管理员绑定
运营中心        ← path: operations
  临停订单 / 月卡订单 / 会员订单 / 支付流水
客户中心        ← path: customers
  用户车辆
```

The overview page's quick-action route pushes are also updated to match the new paths (`/archives/lot`, `/operations/temp`, etc.).

## Execution rules

1. Use the `parking-developer` agent for all code tasks
2. Mark each `- [ ]` step done as you complete it
3. After Task 2 run `mvn -pl ruoyi-admin -am "-Dtest=ParkingMenuScriptTest,ParkingModuleSmokeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` and confirm green
4. Do not commit unless explicitly asked
5. Do not change permission codes, component paths, or unrelated files

Start now by reading the plan document and then executing Task 1.
