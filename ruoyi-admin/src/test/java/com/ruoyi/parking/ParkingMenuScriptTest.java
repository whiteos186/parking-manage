package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ParkingMenuScriptTest
{
    @Test
    void parkingMenuScriptUsesBusinessKeyUpsertAndContainsPlatformNavigationGroups() throws IOException
    {
        Path scriptPath = locateParkingMenuScript();
        assertTrue(Files.exists(scriptPath), () -> "Missing parking menu script: " + scriptPath);

        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8);
        String normalizedSql = stripLineComments(sql);

        assertFalse(
            Pattern.compile("delete\\s+from\\s+sys_menu\\s+where\\s+menu_id", Pattern.CASE_INSENSITIVE)
                .matcher(normalizedSql)
                .find(),
            "Menu bootstrap must not delete sys_menu rows by fixed menu_id"
        );

        assertTrue(normalizedSql.contains("@parking_workbench_id"), "Expected SQL variable @parking_workbench_id");
        assertTrue(normalizedSql.contains("@parking_archive_root_id"), "Expected SQL variable @parking_archive_root_id");
        assertTrue(normalizedSql.contains("@parking_operations_root_id"), "Expected SQL variable @parking_operations_root_id");
        assertTrue(normalizedSql.contains("@parking_customers_root_id"), "Expected SQL variable @parking_customers_root_id");
        assertTrue(normalizedSql.contains("@parking_lot_id"), "Expected SQL variable @parking_lot_id");
        assertTrue(normalizedSql.contains("@parking_space_id"), "Expected SQL variable @parking_space_id");
        assertTrue(normalizedSql.contains("@parking_temp_id"), "Expected SQL variable @parking_temp_id");
        assertTrue(normalizedSql.contains("@parking_monthly_id"), "Expected SQL variable @parking_monthly_id");
        assertTrue(normalizedSql.contains("@parking_membership_id"), "Expected SQL variable @parking_membership_id");
        assertTrue(normalizedSql.contains("@parking_payment_id"), "Expected SQL variable @parking_payment_id");
        assertTrue(normalizedSql.contains("@parking_customer_id"), "Expected SQL variable @parking_customer_id");
        assertTrue(normalizedSql.contains("@parking_vehicle_id"), "Expected SQL variable @parking_vehicle_id");
        assertTrue(normalizedSql.contains("@parking_lotadmin_id"), "Expected SQL variable @parking_lotadmin_id");

        List<MenuInsertSpec> inserts = extractInsertIfNotExistsSpecs(normalizedSql);
        assertEquals(63, inserts.size(), "Expected 63 insert-if-not-exists blocks");

        MenuInsertSpec workbenchPage = findInsertByComponent(inserts, "'parking/index'");
        assertNotNull(workbenchPage, "Expected workbench row backed by parking overview page");
        assertEquals("'\u5de5\u4f5c\u53f0'", workbenchPage.valuesByColumn().get("menu_name"));
        assertEquals("0", workbenchPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking'", workbenchPage.valuesByColumn().get("path"));
        assertEquals("'parking:overview:list'", workbenchPage.valuesByColumn().get("perms"));
        assertTrue(
            workbenchPage.notExistsClause().contains("component = 'parking/index'")
                && workbenchPage.notExistsClause().contains("menu_type = 'C'"),
            "Expected workbench business key to use component/menu_type"
        );

        MenuInsertSpec archiveRoot = findInsertByPathAndMenuType(inserts, "'archives'", "'M'");
        assertNotNull(archiveRoot, "Expected archive root directory row");
        assertEquals("'\u57fa\u7840\u6863\u6848'", archiveRoot.valuesByColumn().get("menu_name"));
        assertEquals("0", archiveRoot.valuesByColumn().get("parent_id"));

        MenuInsertSpec operationsRoot = findInsertByPathAndMenuType(inserts, "'operations'", "'M'");
        assertNotNull(operationsRoot, "Expected operations root directory row");
        assertEquals("'\u8fd0\u8425\u4e2d\u5fc3'", operationsRoot.valuesByColumn().get("menu_name"));
        assertEquals("0", operationsRoot.valuesByColumn().get("parent_id"));

        MenuInsertSpec customersRoot = findInsertByPathAndMenuType(inserts, "'customers'", "'M'");
        assertNotNull(customersRoot, "Expected customers root directory row");
        assertEquals("'\u5ba2\u6237\u4e2d\u5fc3'", customersRoot.valuesByColumn().get("menu_name"));
        assertEquals("0", customersRoot.valuesByColumn().get("parent_id"));
        assertTrue(
            Pattern.compile(
                "update\\s+sys_menu\\s+set\\s+.*visible\\s*=\\s*'1'.*where\\s+path\\s*=\\s*'monitor'\\s+and\\s+menu_type\\s*=\\s*'M'",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            ).matcher(normalizedSql).find(),
            "Expected SQL to hide the generic 系统监控 top-level menu"
        );
        assertTrue(
            Pattern.compile(
                "update\\s+sys_menu\\s+set\\s+.*visible\\s*=\\s*'1'.*where\\s+path\\s*=\\s*'http://ruoyi\\.vip'\\s+and\\s+menu_type\\s*=\\s*'M'",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            ).matcher(normalizedSql).find(),
            "Expected SQL to hide the 若依官网 top-level menu"
        );

        MenuInsertSpec lotPage = findInsertByComponent(inserts, "'parking/lot/index'");
        assertNotNull(lotPage, "Expected parking lot page row");
        assertEquals("'\u505c\u8f66\u573a\u7ba1\u7406'", lotPage.valuesByColumn().get("menu_name"));
        assertEquals("@parking_archive_root_id", lotPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:lot:list'", lotPage.valuesByColumn().get("perms"));
        assertTrue(
            lotPage.notExistsClause().contains("component = 'parking/lot/index'")
                && lotPage.notExistsClause().contains("menu_type = 'C'"),
            "Expected lot page business key to use component/menu_type"
        );

        MenuInsertSpec spacePage = findInsertByComponent(inserts, "'parking/space/index'");
        assertNotNull(spacePage, "Expected parking space page row");
        assertEquals("'\u8f66\u4f4d\u7ba1\u7406'", spacePage.valuesByColumn().get("menu_name"));
        assertEquals("@parking_archive_root_id", spacePage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:space:list'", spacePage.valuesByColumn().get("perms"));
        assertTrue(
            spacePage.notExistsClause().contains("component = 'parking/space/index'")
                && spacePage.notExistsClause().contains("menu_type = 'C'"),
            "Expected space page business key to use component/menu_type"
        );

        assertFunctionButton(inserts, "'parking:overview:query'", "@parking_workbench_id");
        assertFunctionButton(inserts, "'parking:lot:query'", "@parking_lot_id");
        assertFunctionButton(inserts, "'parking:lot:add'", "@parking_lot_id");
        assertFunctionButton(inserts, "'parking:lot:edit'", "@parking_lot_id");
        assertFunctionButton(inserts, "'parking:lot:remove'", "@parking_lot_id");
        assertFunctionButton(inserts, "'parking:space:query'", "@parking_space_id");
        assertFunctionButton(inserts, "'parking:space:add'", "@parking_space_id");
        assertFunctionButton(inserts, "'parking:space:edit'", "@parking_space_id");
        assertFunctionButton(inserts, "'parking:space:remove'", "@parking_space_id");

        MenuInsertSpec tempPage = findInsertByComponent(inserts, "'parking/temp/index'");
        assertNotNull(tempPage, "Expected parking temp order page row");
        assertEquals("@parking_operations_root_id", tempPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:temp:list'", tempPage.valuesByColumn().get("perms"));
        assertFunctionButton(inserts, "'parking:temp:query'", "@parking_temp_id");
        assertFunctionButton(inserts, "'parking:temp:add'", "@parking_temp_id");
        assertFunctionButton(inserts, "'parking:temp:edit'", "@parking_temp_id");
        assertFunctionButton(inserts, "'parking:temp:remove'", "@parking_temp_id");
        assertFunctionButton(inserts, "'parking:temp:settle'", "@parking_temp_id");
        assertFunctionButton(inserts, "'parking:temp:entry'", "@parking_temp_id");
        assertFunctionButton(inserts, "'parking:temp:exit'", "@parking_temp_id");

        MenuInsertSpec tempFormPage = findInsertByComponent(inserts, "'parking/temp/form'");
        assertNotNull(tempFormPage, "Expected parking temp order form page row");
        assertEquals("@parking_temp_id", tempFormPage.valuesByColumn().get("parent_id"));
        assertEquals("'1'", tempFormPage.valuesByColumn().get("visible"));
        assertEquals("'C'", tempFormPage.valuesByColumn().get("menu_type"));

        MenuInsertSpec monthlyPage = findInsertByComponent(inserts, "'parking/monthly/index'");
        assertNotNull(monthlyPage, "Expected parking monthly order page row");
        assertEquals("@parking_operations_root_id", monthlyPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:monthly:list'", monthlyPage.valuesByColumn().get("perms"));
        assertFunctionButton(inserts, "'parking:monthly:query'", "@parking_monthly_id");
        assertFunctionButton(inserts, "'parking:monthly:add'", "@parking_monthly_id");
        assertFunctionButton(inserts, "'parking:monthly:edit'", "@parking_monthly_id");
        assertFunctionButton(inserts, "'parking:monthly:remove'", "@parking_monthly_id");
        assertFunctionButton(inserts, "'parking:monthly:pay'", "@parking_monthly_id");
        assertFunctionButton(inserts, "'parking:monthly:cancel'", "@parking_monthly_id");

        MenuInsertSpec monthlyFormPage = findInsertByComponent(inserts, "'parking/monthly/form'");
        assertNotNull(monthlyFormPage, "Expected parking monthly order form page row");
        assertEquals("@parking_monthly_id", monthlyFormPage.valuesByColumn().get("parent_id"));
        assertEquals("'1'", monthlyFormPage.valuesByColumn().get("visible"));
        assertEquals("'C'", monthlyFormPage.valuesByColumn().get("menu_type"));

        MenuInsertSpec membershipPage = findInsertByComponent(inserts, "'parking/membership/index'");
        assertNotNull(membershipPage, "Expected parking membership order page row");
        assertEquals("@parking_operations_root_id", membershipPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:membership:list'", membershipPage.valuesByColumn().get("perms"));
        assertFunctionButton(inserts, "'parking:membership:query'", "@parking_membership_id");
        assertFunctionButton(inserts, "'parking:membership:add'", "@parking_membership_id");
        assertFunctionButton(inserts, "'parking:membership:edit'", "@parking_membership_id");
        assertFunctionButton(inserts, "'parking:membership:remove'", "@parking_membership_id");
        assertFunctionButton(inserts, "'parking:membership:pay'", "@parking_membership_id");
        assertFunctionButton(inserts, "'parking:membership:cancel'", "@parking_membership_id");

        MenuInsertSpec membershipFormPage = findInsertByComponent(inserts, "'parking/membership/form'");
        assertNotNull(membershipFormPage, "Expected parking membership order form page row");
        assertEquals("@parking_membership_id", membershipFormPage.valuesByColumn().get("parent_id"));
        assertEquals("'1'", membershipFormPage.valuesByColumn().get("visible"));
        assertEquals("'C'", membershipFormPage.valuesByColumn().get("menu_type"));

        MenuInsertSpec paymentPage = findInsertByComponent(inserts, "'parking/payment/index'");
        assertNotNull(paymentPage, "Expected parking payment record page row");
        assertEquals("@parking_operations_root_id", paymentPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:payment:list'", paymentPage.valuesByColumn().get("perms"));
        assertFunctionButton(inserts, "'parking:payment:query'", "@parking_payment_id");
        assertFunctionButton(inserts, "'parking:payment:add'", "@parking_payment_id");
        assertFunctionButton(inserts, "'parking:payment:edit'", "@parking_payment_id");
        assertFunctionButton(inserts, "'parking:payment:remove'", "@parking_payment_id");
        assertFunctionButton(inserts, "'parking:payment:refund'", "@parking_payment_id");

        MenuInsertSpec paymentFormPage = findInsertByComponent(inserts, "'parking/payment/form'");
        assertNotNull(paymentFormPage, "Expected parking payment form page row");
        assertEquals("@parking_payment_id", paymentFormPage.valuesByColumn().get("parent_id"));
        assertEquals("'1'", paymentFormPage.valuesByColumn().get("visible"));
        assertEquals("'C'", paymentFormPage.valuesByColumn().get("menu_type"));

        MenuInsertSpec customerPage = findInsertByComponent(inserts, "'parking/customer/index'");
        assertNotNull(customerPage, "Expected parking customer archive page row");
        assertEquals("@parking_customers_root_id", customerPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:customer:list'", customerPage.valuesByColumn().get("perms"));
        assertFunctionButton(inserts, "'parking:customer:query'", "@parking_customer_id");
        assertFunctionButton(inserts, "'parking:customer:add'", "@parking_customer_id");
        assertFunctionButton(inserts, "'parking:customer:edit'", "@parking_customer_id");
        assertFunctionButton(inserts, "'parking:customer:remove'", "@parking_customer_id");

        MenuInsertSpec vehiclePage = findInsertByComponent(inserts, "'parking/vehicle/index'");
        assertNotNull(vehiclePage, "Expected parking user vehicle page row");
        assertEquals("@parking_customers_root_id", vehiclePage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:vehicle:list'", vehiclePage.valuesByColumn().get("perms"));
        assertFunctionButton(inserts, "'parking:vehicle:query'", "@parking_vehicle_id");
        assertFunctionButton(inserts, "'parking:vehicle:add'", "@parking_vehicle_id");
        assertFunctionButton(inserts, "'parking:vehicle:edit'", "@parking_vehicle_id");
        assertFunctionButton(inserts, "'parking:vehicle:remove'", "@parking_vehicle_id");
        assertFunctionButton(inserts, "'parking:vehicle:setDefault'", "@parking_vehicle_id");

        MenuInsertSpec lotAdminPage = findInsertByComponent(inserts, "'parking/lotadmin/index'");
        assertNotNull(lotAdminPage, "Expected parking lot admin binding page row");
        assertEquals("@parking_archive_root_id", lotAdminPage.valuesByColumn().get("parent_id"));
        assertEquals("'parking:lotadmin:list'", lotAdminPage.valuesByColumn().get("perms"));
        assertFunctionButton(inserts, "'parking:lotadmin:query'", "@parking_lotadmin_id");
        assertFunctionButton(inserts, "'parking:lotadmin:add'", "@parking_lotadmin_id");
        assertFunctionButton(inserts, "'parking:lotadmin:edit'", "@parking_lotadmin_id");
        assertFunctionButton(inserts, "'parking:lotadmin:remove'", "@parking_lotadmin_id");
    }

    private static void assertFunctionButton(List<MenuInsertSpec> inserts, String permsToken, String expectedParentToken)
    {
        MenuInsertSpec button = findInsertByPerms(inserts, permsToken);
        assertNotNull(button, "Expected function row for perms " + permsToken);
        assertEquals(expectedParentToken, button.valuesByColumn().get("parent_id"));
        assertEquals("'F'", button.valuesByColumn().get("menu_type"));
        assertTrue(
            button.notExistsClause().contains("perms = " + permsToken)
                && button.notExistsClause().contains("menu_type = 'F'"),
            "Expected function business key to use perms/menu_type for " + permsToken
        );
    }

    private Path locateParkingMenuScript()
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null)
        {
            Path candidate = current.resolve(Paths.get("sql", "parking", "parking_menu.sql"));
            if (Files.exists(candidate))
            {
                return candidate;
            }
            current = current.getParent();
        }
        return Paths.get("sql", "parking", "parking_menu.sql");
    }

    private static List<MenuInsertSpec> extractInsertIfNotExistsSpecs(String sql)
    {
        Pattern insertPattern = Pattern.compile(
            "insert\\s+into\\s+`?sys_menu`?\\s*\\((.*?)\\)\\s*select\\s*(.*?)\\s*from\\s+dual\\s+where\\s+not\\s+exists\\s*\\((.*?)\\);",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = insertPattern.matcher(sql);

        List<MenuInsertSpec> rows = new ArrayList<>();
        while (matcher.find())
        {
            List<String> columns = parseCsv(matcher.group(1));
            List<String> values = parseCsv(matcher.group(2));
            assertEquals(columns.size(), values.size(), "Insert columns and select values count must match");
            rows.add(new MenuInsertSpec(columns, values, matcher.group(3)));
        }
        return rows;
    }

    private static List<String> parseCsv(String csvBlock)
    {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int nestedDepth = 0;
        boolean inString = false;

        for (int i = 0; i < csvBlock.length(); i++)
        {
            char ch = csvBlock.charAt(i);
            if (ch == '\'')
            {
                current.append(ch);
                if (inString && i + 1 < csvBlock.length() && csvBlock.charAt(i + 1) == '\'')
                {
                    current.append(csvBlock.charAt(i + 1));
                    i++;
                }
                else
                {
                    inString = !inString;
                }
                continue;
            }

            if (!inString)
            {
                if (ch == '(')
                {
                    nestedDepth++;
                }
                else if (ch == ')' && nestedDepth > 0)
                {
                    nestedDepth--;
                }
                else if (ch == ',' && nestedDepth == 0)
                {
                    values.add(normalizeSqlToken(current.toString()));
                    current.setLength(0);
                    continue;
                }
            }
            current.append(ch);
        }

        values.add(normalizeSqlToken(current.toString()));
        return values;
    }

    private static String normalizeSqlToken(String rawValue)
    {
        return rawValue.trim();
    }

    private static String stripLineComments(String sql)
    {
        return sql.replaceAll("(?m)--.*$", "");
    }

    private static MenuInsertSpec findInsertByPathAndMenuType(List<MenuInsertSpec> inserts, String pathToken, String menuTypeToken)
    {
        for (MenuInsertSpec insert : inserts)
        {
            Map<String, String> values = insert.valuesByColumn();
            if (pathToken.equals(values.get("path")) && menuTypeToken.equals(values.get("menu_type")))
            {
                return insert;
            }
        }
        return null;
    }

    private static MenuInsertSpec findInsertByComponent(List<MenuInsertSpec> inserts, String componentToken)
    {
        for (MenuInsertSpec insert : inserts)
        {
            Map<String, String> values = insert.valuesByColumn();
            if (componentToken.equals(values.get("component")) && "'C'".equals(values.get("menu_type")))
            {
                return insert;
            }
        }
        return null;
    }

    private static MenuInsertSpec findInsertByPerms(List<MenuInsertSpec> inserts, String permsToken)
    {
        for (MenuInsertSpec insert : inserts)
        {
            Map<String, String> values = insert.valuesByColumn();
            if (permsToken.equals(values.get("perms")) && "'F'".equals(values.get("menu_type")))
            {
                return insert;
            }
        }
        return null;
    }

    private static final class MenuInsertSpec
    {
        private final List<String> columns;
        private final List<String> values;
        private final String notExistsClause;

        private MenuInsertSpec(List<String> columns, List<String> values, String notExistsClause)
        {
            this.columns = columns;
            this.values = values;
            this.notExistsClause = notExistsClause;
        }

        private Map<String, String> valuesByColumn()
        {
            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++)
            {
                map.put(stripOptionalIdentifierQuotes(columns.get(i)), values.get(i));
            }
            return map;
        }

        private String notExistsClause()
        {
            return notExistsClause;
        }
    }

    private static String stripOptionalIdentifierQuotes(String identifier)
    {
        String trimmed = identifier.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("`") && trimmed.endsWith("`"))
        {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
