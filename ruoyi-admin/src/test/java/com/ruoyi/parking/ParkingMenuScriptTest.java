package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ParkingMenuScriptTest
{
    private static final int SYS_MENU_COLUMN_COUNT = 20;

    @Test
    void parkingMenuScriptContainsMinimalParkingMenuTree() throws IOException
    {
        Path scriptPath = locateParkingMenuScript();
        assertTrue(Files.exists(scriptPath), () -> "Missing parking menu script: " + scriptPath);

        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8);
        String normalizedSql = stripLineComments(sql);

        List<List<String>> sysMenuRows = extractSysMenuInsertRows(normalizedSql);
        assertTrue(!sysMenuRows.isEmpty(), "Expected at least one sys_menu insert row in parking_menu.sql");

        MenuRow rootDirectory = findByMenuTypeAndParent(sysMenuRows, "M", "0");
        assertNotNull(rootDirectory, "Expected a top-level parking directory menu (menu_type='M', parent_id='0')");
        assertEquals("parking", rootDirectory.path, "Expected parking top-level menu path to be 'parking'");

        MenuRow pageMenu = findByMenuTypeAndParent(sysMenuRows, "C", rootDirectory.menuId);
        assertNotNull(pageMenu, "Expected a child page menu under parking directory");
        assertEquals("parking/index", pageMenu.component, "Expected parking child menu component to be 'parking/index'");

        MenuRow queryButton = findByMenuTypeAndParent(sysMenuRows, "F", pageMenu.menuId);
        assertNotNull(queryButton, "Expected at least one function/button permission under parking page");
        assertEquals("parking:overview:query", queryButton.perms, "Expected function permission parking:overview:query");
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

    private static List<List<String>> extractSysMenuInsertRows(String sql)
    {
        Pattern insertPattern = Pattern.compile(
            "insert\\s+into\\s+`?sys_menu`?(?:\\s*\\([^)]*\\))?\\s*values\\s*(.*?);",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = insertPattern.matcher(sql);

        List<List<String>> rows = new ArrayList<>();
        while (matcher.find())
        {
            List<String> tuples = parseSqlTuples(matcher.group(1));
            for (String tuple : tuples)
            {
                List<String> values = splitSqlValues(tuple);
                assertEquals(
                    SYS_MENU_COLUMN_COUNT,
                    values.size(),
                    "Expected sys_menu inserts to use full-value style with 20 columns"
                );
                rows.add(values);
            }
        }
        return rows;
    }

    private static List<String> parseSqlTuples(String valuesBlock)
    {
        List<String> tuples = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        int tupleStart = -1;

        for (int i = 0; i < valuesBlock.length(); i++)
        {
            char ch = valuesBlock.charAt(i);
            if (ch == '\'')
            {
                if (inString && i + 1 < valuesBlock.length() && valuesBlock.charAt(i + 1) == '\'')
                {
                    i++;
                }
                else
                {
                    inString = !inString;
                }
            }
            if (inString)
            {
                continue;
            }

            if (ch == '(')
            {
                if (depth == 0)
                {
                    tupleStart = i;
                }
                depth++;
            }
            else if (ch == ')')
            {
                depth--;
                if (depth == 0 && tupleStart >= 0)
                {
                    tuples.add(valuesBlock.substring(tupleStart + 1, i));
                    tupleStart = -1;
                }
            }
        }
        return tuples;
    }

    private static List<String> splitSqlValues(String tupleContent)
    {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int nestedDepth = 0;
        boolean inString = false;

        for (int i = 0; i < tupleContent.length(); i++)
        {
            char ch = tupleContent.charAt(i);
            if (ch == '\'')
            {
                current.append(ch);
                if (inString && i + 1 < tupleContent.length() && tupleContent.charAt(i + 1) == '\'')
                {
                    current.append(tupleContent.charAt(i + 1));
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
                    values.add(normalizeSqlLiteral(current.toString()));
                    current.setLength(0);
                    continue;
                }
            }
            current.append(ch);
        }

        values.add(normalizeSqlLiteral(current.toString()));
        return values;
    }

    private static String normalizeSqlLiteral(String rawValue)
    {
        String trimmed = rawValue.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("'") && trimmed.endsWith("'"))
        {
            return trimmed.substring(1, trimmed.length() - 1).replace("''", "'");
        }
        return trimmed;
    }

    private static String stripLineComments(String sql)
    {
        return sql.replaceAll("(?m)--.*$", "");
    }

    private static MenuRow findByMenuTypeAndParent(List<List<String>> rows, String menuType, String parentId)
    {
        for (List<String> values : rows)
        {
            MenuRow row = MenuRow.from(values);
            if (menuType.equals(row.menuType) && parentId.equals(row.parentId))
            {
                return row;
            }
        }
        return null;
    }

    private static final class MenuRow
    {
        private final String menuId;
        private final String parentId;
        private final String path;
        private final String component;
        private final String menuType;
        private final String perms;

        private MenuRow(String menuId, String parentId, String path, String component, String menuType, String perms)
        {
            this.menuId = menuId;
            this.parentId = parentId;
            this.path = path;
            this.component = component;
            this.menuType = menuType;
            this.perms = perms;
        }

        private static MenuRow from(List<String> values)
        {
            return new MenuRow(
                values.get(0),
                values.get(2),
                values.get(4),
                values.get(5),
                values.get(10),
                values.get(13)
            );
        }
    }
}
