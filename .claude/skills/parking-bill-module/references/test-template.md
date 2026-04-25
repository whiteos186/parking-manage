# Test Template

本文件提供 Controller 单元测试和回归测试更新的填空式模板。
占位符参考 `backend-template.md` 的命名速查表。

---

## Controller 单元测试模板

文件路径: `ruoyi-admin/src/test/java/com/ruoyi/parking/Parking{{Entity}}ControllerTest.java`

```java
package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ruoyi.parking.controller.Parking{{Entity}}Controller;
import com.ruoyi.parking.domain.Parking{{Entity}};
import com.ruoyi.parking.service.IParking{{Entity}}Service;

class Parking{{Entity}}ControllerTest
{
    private IParking{{Entity}}Service buildService()
    {
        return Mockito.mock(IParking{{Entity}}Service.class);
    }

    private MockMvc mvc(IParking{{Entity}}Service service)
    {
        Parking{{Entity}}Controller controller = new Parking{{Entity}}Controller(service);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Parking{{Entity}} demoBill()
    {
        Parking{{Entity}} bill = new Parking{{Entity}}();
        bill.set{{Entity}}Id(1L);
        bill.setBillNo("{{BillPrefix}}20260101120000001");
        bill.setLotId(1L);
        bill.setBillStatus("00");
        return bill;
    }

    // ========== 列表 ==========
    @Test
    void listEndpointReturnsTableDataAndBindsFilters() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.selectParking{{Entity}}List(any(Parking{{Entity}}.class)))
                .thenReturn(List.of(demoBill()));

        mvc(service).perform(get("/parking/{{resource}}/list")
                .param("billNo", "{{BillPrefix}}2026")
                .param("billStatus", "00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows[0].{{pk}}").value(1))
                .andExpect(jsonPath("$.rows[0].billNo").value("{{BillPrefix}}20260101120000001"))
                .andExpect(jsonPath("$.rows[0].billStatus").value("00"))
                .andExpect(jsonPath("$.total").value(1));

        ArgumentCaptor<Parking{{Entity}}> captor = ArgumentCaptor.forClass(Parking{{Entity}}.class);
        verify(service).selectParking{{Entity}}List(captor.capture());
        assertEquals("{{BillPrefix}}2026", captor.getValue().getBillNo());
        assertEquals("00", captor.getValue().getBillStatus());
    }

    // ========== 详情 ==========
    @Test
    void getInfoEndpointReturnsSingleBill() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.selectParking{{Entity}}ById(1L)).thenReturn(demoBill());

        mvc(service).perform(get("/parking/{{resource}}/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.{{pk}}").value(1))
                .andExpect(jsonPath("$.data.billNo").value("{{BillPrefix}}20260101120000001"));
    }

    // ========== 新增 ==========
    @Test
    void addEndpointPassesPayloadToService() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.insertParking{{Entity}}(any(Parking{{Entity}}.class))).thenReturn(1);

        mvc(service).perform(post("/parking/{{resource}}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"billNo\":\"{{BillPrefix}}20260101120000001\",\"lotId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<Parking{{Entity}}> captor = ArgumentCaptor.forClass(Parking{{Entity}}.class);
        verify(service).insertParking{{Entity}}(captor.capture());
        assertEquals("{{BillPrefix}}20260101120000001", captor.getValue().getBillNo());
        assertEquals(1L, captor.getValue().getLotId());
    }

    // ========== 修改 ==========
    @Test
    void editEndpointPassesPayloadToService() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.updateParking{{Entity}}(any(Parking{{Entity}}.class))).thenReturn(1);

        mvc(service).perform(put("/parking/{{resource}}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"{{pk}}\":1,\"billNo\":\"{{BillPrefix}}20260101120000001\",\"lotId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<Parking{{Entity}}> captor = ArgumentCaptor.forClass(Parking{{Entity}}.class);
        verify(service).updateParking{{Entity}}(captor.capture());
        assertEquals(Long.valueOf(1L), captor.getValue().get{{Entity}}Id());
        assertEquals(Long.valueOf(2L), captor.getValue().getLotId());
    }

    // ========== 提交审核 ==========
    @Test
    void submitEndpointDelegatesToService() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.submitParking{{Entity}}(any(Parking{{Entity}}.class))).thenReturn(1);

        mvc(service).perform(put("/parking/{{resource}}/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"{{pk}}\":1}"))
                .andExpect(status().isOk());

        verify(service).submitParking{{Entity}}(any(Parking{{Entity}}.class));
    }

    // ========== 审核通过 ==========
    @Test
    void approveEndpointDelegatesToService() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.approveParking{{Entity}}(any(Parking{{Entity}}.class))).thenReturn(1);

        mvc(service).perform(put("/parking/{{resource}}/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"{{pk}}\":1}"))
                .andExpect(status().isOk());

        verify(service).approveParking{{Entity}}(any(Parking{{Entity}}.class));
    }

    // ========== 审核拒绝 ==========
    @Test
    void rejectEndpointPassesAuditRemark() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.rejectParking{{Entity}}(any(Parking{{Entity}}.class))).thenReturn(1);

        mvc(service).perform(put("/parking/{{resource}}/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"{{pk}}\":1,\"auditRemark\":\"资料不全\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Parking{{Entity}}> captor = ArgumentCaptor.forClass(Parking{{Entity}}.class);
        verify(service).rejectParking{{Entity}}(captor.capture());
        assertEquals("资料不全", captor.getValue().getAuditRemark());
    }

    // ========== 作废 ==========
    @Test
    void voidEndpointDelegatesToService() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.voidParking{{Entity}}(any(Parking{{Entity}}.class))).thenReturn(1);

        mvc(service).perform(put("/parking/{{resource}}/void")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"{{pk}}\":1}"))
                .andExpect(status().isOk());

        verify(service).voidParking{{Entity}}(any(Parking{{Entity}}.class));
    }

    // ========== 删除 ==========
    @Test
    void removeEndpointPassesIdsToService() throws Exception
    {
        IParking{{Entity}}Service service = buildService();
        when(service.deleteParking{{Entity}}ByIds(any(Long[].class))).thenReturn(2);

        mvc(service).perform(delete("/parking/{{resource}}/1,2"))
                .andExpect(status().isOk());

        ArgumentCaptor<Long[]> captor = ArgumentCaptor.forClass(Long[].class);
        verify(service).deleteParking{{Entity}}ByIds(captor.capture());
        assertEquals(2, captor.getValue().length);
    }
}
```

**要点**:
- 用 `MockMvcBuilders.standaloneSetup(controller).build()` 构造,**不起** Spring 上下文(快 + 不依赖数据库)
- 所有依赖(Service)都 mock 掉
- 每个 endpoint 至少一个 happy-path 测试,业务流转端点额外验证关键字段(如 `auditRemark`)传对了
- 用 `ArgumentCaptor` 捕获 Service 调用参数,断言传入值正确
- 测试方法命名: `<endpoint><Verb><What>` (`listEndpointReturnsTableDataAndBindsFilters`)

---

## 回归测试更新

### ParkingSchemaScriptTest

文件: `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingSchemaScriptTest.java`

这个测试**只有一个常量**:`REQUIRED_TABLE_COLUMNS`,类型 `Map<String, List<String>>`,用 `Map.ofEntries(Map.entry(table, List.of(col1, col2, ...)))` 构造。每个 entry 表达"这张表必须有这几列"。它配合 `parkingBootstrapScriptContainsRequiredCreateTables` 这个 @Test 方法跑断言,**不需要新增 @Test 方法**,只要在 Map 末尾追加你模块的 entry。

改动只有一行:在 Map 的最后一个 entry 后面追加。

```java
private static final Map<String, List<String>> REQUIRED_TABLE_COLUMNS = Map.ofEntries(
    Map.entry("parking_lot", List.of("lot_id", "lot_name", "total_space_count", "available_space_count")),
    // ... 现有 entries ...
    Map.entry("parking_payment_record", List.of("payment_id", "biz_order_no", "biz_order_type", "customer_id", "pay_amount", "pay_status")),
    Map.entry("{{table}}", List.of("{{pk_col}}", "bill_no", "lot_id", "bill_status", "del_flag"))   // ← 新增,注意末尾不加逗号
);
```

**先 Read 当前文件**确认行末逗号格式 — 如果你新增的是最后一行,它末尾不能有逗号;但你要先把原本最后一行加上逗号。

### ParkingMenuScriptTest

文件: `ruoyi-admin/src/test/java/com/ruoyi/parking/ParkingMenuScriptTest.java`

这个测试**没有用任何常量**,所有断言都是 inline 写死的 — 没有 `EXPECTED_*` 常量集合。所有改动都是直接编辑 `parkingMenuScriptUsesBusinessKeyUpsertAndContainsPlatformNavigationGroups` 这个 @Test 方法体。

**改动 1 — block count(必改)**:

```java
// 找到这一行(N 是当前实际值,可能不是 66 — 别的模块可能刚加过)
List<MenuInsertSpec> inserts = extractInsertIfNotExistsSpecs(normalizedSql);
assertEquals(N, inserts.size(), "Expected N insert-if-not-exists blocks");
//           ↑                              ↑
//   把 N 改成 N + 你新增的块数(标准单据 = 10)
```

**改动 2 — 变量名断言(必改)**: 在那一片 `assertTrue(normalizedSql.contains("@parking_xxx_id"), ...)` 的末尾追加一行:

```java
assertTrue(normalizedSql.contains("@parking_{{resource}}_id"), "Expected SQL variable @parking_{{resource}}_id");
```

**改动 3 — 页面 + 按钮断言(推荐,与现有 membership/monthly 模块对齐)**: 在测试方法末尾追加一段(参考现有 membership 段的写法):

```java
MenuInsertSpec {{resource}}Page = findInsertByComponent(inserts, "'parking/{{resource}}/index'");
assertNotNull({{resource}}Page, "Expected parking {{resource}} page row");
assertEquals("'{{模块中文unicode}}'", {{resource}}Page.valuesByColumn().get("menu_name"));
assertEquals("@parking_operations_root_id", {{resource}}Page.valuesByColumn().get("parent_id"));
assertEquals("'parking:{{resource}}:list'", {{resource}}Page.valuesByColumn().get("perms"));

assertFunctionButton(inserts, "'parking:{{resource}}:query'",   "@parking_{{resource}}_id");
assertFunctionButton(inserts, "'parking:{{resource}}:add'",     "@parking_{{resource}}_id");
assertFunctionButton(inserts, "'parking:{{resource}}:edit'",    "@parking_{{resource}}_id");
assertFunctionButton(inserts, "'parking:{{resource}}:remove'",  "@parking_{{resource}}_id");
assertFunctionButton(inserts, "'parking:{{resource}}:submit'",  "@parking_{{resource}}_id");
assertFunctionButton(inserts, "'parking:{{resource}}:approve'", "@parking_{{resource}}_id");
assertFunctionButton(inserts, "'parking:{{resource}}:reject'",  "@parking_{{resource}}_id");
assertFunctionButton(inserts, "'parking:{{resource}}:void'",    "@parking_{{resource}}_id");

MenuInsertSpec {{resource}}FormPage = findInsertByComponent(inserts, "'parking/{{resource}}/form'");
assertNotNull({{resource}}FormPage, "Expected parking {{resource}} form page row");
assertEquals("@parking_{{resource}}_id", {{resource}}FormPage.valuesByColumn().get("parent_id"));
assertEquals("'1'", {{resource}}FormPage.valuesByColumn().get("visible"));
assertEquals("'C'", {{resource}}FormPage.valuesByColumn().get("menu_type"));
```

`{{模块中文unicode}}` 替换成中文模块名的 Unicode 转义形式(因为 Java 源文件是 UTF-8 但项目里其他模块的字符串字面量已经写成 unicode 了)。例如 "维修工单" → `'\u7ef4\u4fee\u5de5\u5355'`。也可以直接写中文字符,只要文件保存为 UTF-8 即可。

**先 Read 当前 `ParkingMenuScriptTest.java`**,确认 N 的当前值和现有断言段的写法,再改。

### ParkingModuleSmokeTest

这个测试主要扫前端:
- 每个 Vue 页面必须用中文 empty-text
- 按钮用 `v-hasPermi`
- 路由不能重定向到老的 `/index`

新增模块后通常不需要改它的断言,除非你加了新的"必需前端断言"。Read 一遍确认即可。

---

## 本地验证命令

```bash
cd D:/code/parking-management-system

# 只跑新测试(快速迭代)
mvn -pl ruoyi-admin -am "-Dtest=Parking{{Entity}}ControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test

# 跑完整回归套件(提交前)
mvn -pl ruoyi-admin -am "-Dtest=ParkingSchemaScriptTest,ParkingMenuScriptTest,ParkingModuleSmokeTest,Parking{{Entity}}ControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test

# 前端构建
cd ruoyi-ui && npm run build:prod
```

所有测试必须绿,前端构建必须零 error(warning 可接受)才算交付完成。
