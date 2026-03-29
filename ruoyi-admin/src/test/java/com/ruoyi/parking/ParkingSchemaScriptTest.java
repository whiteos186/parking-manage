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
import java.util.Arrays;
import java.util.LinkedHashMap;
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

        assertParkingSpaceAndLotSeedInvariants(normalizedSql);
        assertMembershipPaymentSeedConsistency(normalizedSql);
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

    private static void assertParkingSpaceAndLotSeedInvariants(String sql)
    {
        List<Map<String, String>> seededSpaces = extractInsertedRows(sql, "parking_space");
        assertEquals(3, seededSpaces.size(), "Expected exactly 3 seeded parking_space rows");

        long freeSpaceCount = seededSpaces.stream().filter(row -> "0".equals(row.get("status"))).count();
        long occupiedSpaceCount = seededSpaces.stream().filter(row -> "1".equals(row.get("status"))).count();
        assertEquals(2L, freeSpaceCount, "Expected 2 seeded free spaces (status='0')");
        assertEquals(1L, occupiedSpaceCount, "Expected 1 seeded occupied space (status='1')");

        List<Map<String, String>> seededLots = extractInsertedRows(sql, "parking_lot");
        assertTrue(!seededLots.isEmpty(), "Expected seeded parking_lot data");
        Map<String, String> demoLot = seededLots.get(0);
        assertEquals("3", demoLot.get("total_space_count"), "Expected parking_lot.total_space_count=3");
        assertEquals("2", demoLot.get("available_space_count"), "Expected parking_lot.available_space_count=2");
    }

    private static void assertMembershipPaymentSeedConsistency(String sql)
    {
        List<Map<String, String>> paymentRows = extractInsertedRows(sql, "parking_payment_record");
        Map<String, String> membershipPayment = paymentRows.stream()
            .filter(row -> "PO202603290001".equals(row.get("biz_order_no")))
            .findFirst()
            .orElse(null);

        assertNotNull(
            membershipPayment,
            "Expected payment record for membership order PO202603290001 in parking_payment_record"
        );
        assertEquals("1", membershipPayment.get("biz_order_type"), "Expected membership payment biz_order_type='1'");
        assertEquals("1000.00", membershipPayment.get("pay_amount"), "Expected membership payment pay_amount=1000.00");
        assertEquals("1", membershipPayment.get("pay_status"), "Expected membership payment pay_status='1'");
    }

    private static List<Map<String, String>> extractInsertedRows(String sql, String tableName)
    {
        Pattern insertPattern = Pattern.compile(
            "insert\\s+into\\s+`?" + Pattern.quote(tableName) + "`?\\s*\\((.*?)\\)\\s*values\\s*(.*?);",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = insertPattern.matcher(sql);

        List<Map<String, String>> rows = new ArrayList<>();
        while (matcher.find())
        {
            List<String> columns = parseInsertColumns(matcher.group(1));
            List<String> tuples = parseSqlTuples(matcher.group(2));
            for (String tuple : tuples)
            {
                List<String> values = splitSqlValues(tuple);
                assertEquals(
                    columns.size(),
                    values.size(),
                    () -> "Column/value count mismatch in insert for table " + tableName
                );
                rows.add(mapRow(columns, values));
            }
        }
        return rows;
    }

    private static List<String> parseInsertColumns(String columnsBlock)
    {
        return Arrays.stream(columnsBlock.split(","))
            .map(String::trim)
            .map(ParkingSchemaScriptTest::stripOptionalIdentifierQuotes)
            .toList();
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

    private static Map<String, String> mapRow(List<String> columns, List<String> values)
    {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++)
        {
            row.put(columns.get(i), values.get(i));
        }
        return row;
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
