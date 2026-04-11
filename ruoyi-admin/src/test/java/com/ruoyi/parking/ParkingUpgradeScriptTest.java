package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * 验证 parking_upgrade_202604.sql 幂等 ALTER TABLE 脚本的结构正确性。
 * 该脚本用于为已有库补充 parking_customer 的三个会员字段。
 */
class ParkingUpgradeScriptTest
{
    @Test
    void upgradeScriptContainsIdempotentAlterForAllThreeMemberColumns() throws IOException
    {
        Path scriptPath = locateUpgradeScript();
        assertTrue(Files.exists(scriptPath), () -> "Missing upgrade script: " + scriptPath);

        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8);

        // 每个会员列都应有幂等 ALTER TABLE 语句
        assertIdempotentAlterPresent(sql, "is_member");
        assertIdempotentAlterPresent(sql, "member_type");
        assertIdempotentAlterPresent(sql, "member_expire_time");
    }

    @Test
    void upgradeScriptAltersCorrectTable() throws IOException
    {
        Path scriptPath = locateUpgradeScript();
        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

        // 必须操作 parking_customer 表
        assertTrue(
            sql.contains("alter table parking_customer"),
            "Expected upgrade script to alter parking_customer table"
        );
        // 不能误操作其他业务表
        assertTrue(
            !sql.contains("alter table parking_lot"),
            "Upgrade script must not alter parking_lot"
        );
    }

    @Test
    void upgradeScriptIsRepeatableViaDropAndCallPattern() throws IOException
    {
        Path scriptPath = locateUpgradeScript();
        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8);

        // 幂等模式：用 stored procedure 包装，执行完即删除
        assertTrue(
            sql.contains("drop procedure if exists"),
            "Expected idempotent pattern: drop procedure if exists"
        );
        assertTrue(
            sql.contains("create procedure"),
            "Expected idempotent pattern: create procedure"
        );
        assertTrue(
            sql.contains("call "),
            "Expected idempotent pattern: call <procedure>"
        );

        // 执行完后清理 procedure
        long dropCount = countOccurrences(sql, "drop procedure if exists");
        assertTrue(dropCount >= 2, "Expected drop before create and drop after call (idempotent cleanup)");
    }

    @Test
    void upgradeScriptChecksInformationSchemaBeforeAlter() throws IOException
    {
        Path scriptPath = locateUpgradeScript();
        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

        // 每个列的幂等检查必须查询 information_schema.columns
        long infoSchemaCheckCount = countOccurrences(sql, "information_schema.columns");
        assertEquals(3L, infoSchemaCheckCount,
            "Expected 3 information_schema.columns checks — one per member column");
    }

    @Test
    void upgradeScriptSeedsParkingGlobalConfigAndDictData() throws IOException
    {
        Path scriptPath = locateUpgradeScript();
        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("parking.rule.monthlyprice"),
            "Expected global config seed for parking.rule.monthlyPrice");
        assertTrue(sql.contains("parking.rule.temphourprice"),
            "Expected global config seed for parking.rule.tempHourPrice");
        assertTrue(sql.contains("parking.rule.memberdiscount.gold"),
            "Expected global config seed for parking.rule.memberDiscount.gold");
        assertTrue(sql.contains("parking_payment_channel"),
            "Expected dict seed for parking_payment_channel");
        assertTrue(sql.contains("parking_lot_status"),
            "Expected dict seed for parking_lot_status");
    }

    // ---- helpers ----

    private void assertIdempotentAlterPresent(String sql, String columnName)
    {
        String lowerSql = sql.toLowerCase();

        // information_schema 检查中包含列名
        assertTrue(
            lowerSql.contains("column_name  = '" + columnName + "'")
                || lowerSql.contains("column_name = '" + columnName + "'"),
            () -> "Expected information_schema guard for column: " + columnName
        );

        // alter table ... add column 中包含列名
        Pattern alterPattern = Pattern.compile(
            "add\\s+column\\s+" + Pattern.quote(columnName) + "\\s",
            Pattern.CASE_INSENSITIVE
        );
        assertTrue(
            alterPattern.matcher(sql).find(),
            () -> "Expected ALTER TABLE ... ADD COLUMN " + columnName + " in upgrade script"
        );
    }

    private static long countOccurrences(String text, String target)
    {
        String lowerText = text.toLowerCase();
        String lowerTarget = target.toLowerCase();
        long count = 0;
        int idx = 0;
        while ((idx = lowerText.indexOf(lowerTarget, idx)) >= 0)
        {
            count++;
            idx += lowerTarget.length();
        }
        return count;
    }

    private Path locateUpgradeScript()
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null)
        {
            Path candidate = current.resolve(Paths.get("sql", "parking", "parking_upgrade_202604.sql"));
            if (Files.exists(candidate))
            {
                return candidate;
            }
            current = current.getParent();
        }
        return Paths.get("sql", "parking", "parking_upgrade_202604.sql");
    }
}
