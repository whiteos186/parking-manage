package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ParkingSchemaScriptTest
{
    private static final Map<String, List<String>> REQUIRED_TABLE_COLUMNS = Map.ofEntries(
        Map.entry("parking_lot", List.of("lot_id", "lot_name", "total_space_count", "available_space_count")),
        Map.entry("parking_space", List.of("space_id", "lot_id", "space_code", "space_type", "status")),
        Map.entry("parking_lot_admin", List.of("lot_admin_id", "lot_id", "user_id")),
        Map.entry("parking_user_vehicle", List.of("vehicle_id", "user_id", "plate_no", "vehicle_type")),
        Map.entry("parking_membership_order", List.of("membership_order_id", "order_no", "user_id", "lot_id", "pay_status")),
        Map.entry("parking_monthly_order", List.of("monthly_order_id", "order_no", "user_id", "lot_id")),
        Map.entry("parking_temp_order", List.of("temp_order_id", "order_no", "lot_id", "user_id", "vehicle_plate_no")),
        Map.entry("parking_payment_record", List.of("payment_id", "biz_order_no", "biz_order_type", "pay_amount", "pay_status"))
    );

    @Test
    void parkingBootstrapScriptContainsRequiredCreateTables() throws IOException
    {
        Path scriptPath = locateParkingBootstrapScript();
        assertTrue(Files.exists(scriptPath), () -> "Missing schema script: " + scriptPath);

        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8);
        String normalizedSql = stripLineComments(sql);

        for (Map.Entry<String, List<String>> entry : REQUIRED_TABLE_COLUMNS.entrySet())
        {
            assertTableContainsColumns(normalizedSql, entry.getKey(), entry.getValue());
        }

        Pattern lotCountPattern = Pattern.compile(
            "'No\\. 88 Innovation Road, Shenzhen'\\s*,\\s*3\\s*,\\s*2\\s*,\\s*380\\.00",
            Pattern.CASE_INSENSITIVE
        );
        assertTrue(
            lotCountPattern.matcher(normalizedSql).find(),
            () -> "Expected demo lot to report total_space_count=3 and available_space_count=2"
        );

        Pattern membershipPaymentPattern = Pattern.compile(
            "insert\\s+into\\s+parking_payment_record[\\s\\S]*?\\(\\s*\\d+\\s*,\\s*'PO202603290001'\\s*,",
            Pattern.CASE_INSENSITIVE
        );
        assertTrue(
            membershipPaymentPattern.matcher(normalizedSql).find(),
            () -> "Expected a payment record for PO202603290001 in parking_payment_record"
        );
    }

    private Path locateParkingBootstrapScript()
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null)
        {
            Path candidate = current.resolve(Paths.get("sql", "parking", "parking_bootstrap.sql"));
            if (Files.exists(candidate))
            {
                return candidate;
            }
            current = current.getParent();
        }
        return Paths.get("sql", "parking", "parking_bootstrap.sql");
    }

    private static void assertTableContainsColumns(String sql, String tableName, List<String> columns)
    {
        String columnsBlock = extractCreateTableColumnsBlock(sql, tableName);
        for (String column : columns)
        {
            assertTrue(
                hasColumnDefinition(columnsBlock, column),
                () -> "Expected column '" + column + "' in table " + tableName
            );
        }
    }

    private static String extractCreateTableColumnsBlock(String sql, String tableName)
    {
        Pattern tablePattern = Pattern.compile(
            "create\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?`?" + Pattern.quote(tableName) + "`?\\s*\\(",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = tablePattern.matcher(sql);
        assertTrue(
            matcher.find(),
            () -> "Expected CREATE TABLE block for: " + tableName
        );

        int openParenIndex = matcher.end() - 1;
        return extractParenthesizedContent(sql, openParenIndex);
    }

    private static String extractParenthesizedContent(String sql, int openParenIndex)
    {
        int depth = 0;
        for (int i = openParenIndex; i < sql.length(); i++)
        {
            char ch = sql.charAt(i);
            if (ch == '(')
            {
                depth++;
            }
            else if (ch == ')')
            {
                depth--;
                if (depth == 0)
                {
                    return sql.substring(openParenIndex + 1, i);
                }
            }
        }
        throw new IllegalStateException("Unterminated parenthesis after index " + openParenIndex);
    }

    private static String stripLineComments(String sql)
    {
        return sql.replaceAll("(?m)--.*$", "");
    }

    private static boolean hasColumnDefinition(String columnsBlock, String columnName)
    {
        Pattern columnPattern = Pattern.compile(
            "(?m)^\\s*`?" + Pattern.quote(columnName) + "`?\\s+[a-z]",
            Pattern.CASE_INSENSITIVE
        );
        return columnPattern.matcher(columnsBlock).find();
    }
}
