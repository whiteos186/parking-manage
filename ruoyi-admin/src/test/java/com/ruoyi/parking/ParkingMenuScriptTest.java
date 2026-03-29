package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    void parkingMenuScriptUsesBusinessKeyUpsertStrategyWithExactRows() throws IOException
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

        assertTrue(normalizedSql.contains("@parking_root_id"), "Expected SQL variable @parking_root_id");
        assertTrue(normalizedSql.contains("@parking_overview_id"), "Expected SQL variable @parking_overview_id");

        List<MenuInsertSpec> inserts = extractInsertIfNotExistsSpecs(normalizedSql);
        assertEquals(3, inserts.size(), "Expected exactly 3 parking menu bootstrap insert-if-not-exists blocks");

        MenuInsertSpec root = findInsertByMenuType(inserts, "'M'");
        assertNotNull(root, "Expected directory insert block (menu_type='M')");
        assertEquals("'停车管理'", root.valuesByColumn().get("menu_name"));
        assertEquals("0", root.valuesByColumn().get("parent_id"));
        assertEquals("'parking'", root.valuesByColumn().get("path"));
        assertEquals("null", root.valuesByColumn().get("component"));
        assertEquals("'guide'", root.valuesByColumn().get("icon"));
        assertTrue(
            root.notExistsClause().contains("path = 'parking'") && root.notExistsClause().contains("menu_type = 'M'"),
            "Expected root insert to use path/menu_type business key"
        );

        MenuInsertSpec page = findInsertByMenuType(inserts, "'C'");
        assertNotNull(page, "Expected page insert block (menu_type='C')");
        assertEquals("'停车概览'", page.valuesByColumn().get("menu_name"));
        assertEquals("@parking_root_id", page.valuesByColumn().get("parent_id"));
        assertEquals("'index'", page.valuesByColumn().get("path"));
        assertEquals("'parking/index'", page.valuesByColumn().get("component"));
        assertEquals("'parking:overview:list'", page.valuesByColumn().get("perms"));
        assertEquals("'build'", page.valuesByColumn().get("icon"));
        assertTrue(
            page.notExistsClause().contains("component = 'parking/index'") && page.notExistsClause().contains("menu_type = 'C'"),
            "Expected page insert to use component/menu_type business key"
        );

        MenuInsertSpec button = findInsertByMenuType(inserts, "'F'");
        assertNotNull(button, "Expected function insert block (menu_type='F')");
        assertEquals("'停车概览查询'", button.valuesByColumn().get("menu_name"));
        assertEquals("@parking_overview_id", button.valuesByColumn().get("parent_id"));
        assertEquals("'parking:overview:query'", button.valuesByColumn().get("perms"));
        assertEquals("'#'", button.valuesByColumn().get("icon"));
        assertTrue(
            button.notExistsClause().contains("perms = 'parking:overview:query'") && button.notExistsClause().contains("menu_type = 'F'"),
            "Expected function insert to use perms/menu_type business key"
        );
    }

    @Test
    void parkingOverviewPermissionIsActuallyWiredInFrontAndBack() throws IOException
    {
        String vue = readProjectFile("ruoyi-ui/src/views/parking/index.vue");
        assertTrue(
            vue.contains("v-hasPermi=\"['parking:overview:query']\""),
            "Expected parking page query button to be guarded by v-hasPermi parking:overview:query"
        );

        String controller = readProjectFile("ruoyi-parking/src/main/java/com/ruoyi/parking/controller/ParkingHealthController.java");
        assertTrue(
            controller.contains("@PreAuthorize(\"@ss.hasPermi('parking:overview:query')\")"),
            "Expected backend endpoint to enforce parking:overview:query via @PreAuthorize"
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

    private static String readProjectFile(String relativePath) throws IOException
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null)
        {
            Path candidate = current.resolve(relativePath);
            if (Files.exists(candidate))
            {
                return Files.readString(candidate, StandardCharsets.UTF_8);
            }
            current = current.getParent();
        }
        throw new IOException("Missing file: " + relativePath);
    }

    private static MenuInsertSpec findInsertByMenuType(List<MenuInsertSpec> inserts, String menuTypeToken)
    {
        for (MenuInsertSpec insert : inserts)
        {
            String menuType = insert.valuesByColumn().get("menu_type");
            if (menuTypeToken.equals(menuType))
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
