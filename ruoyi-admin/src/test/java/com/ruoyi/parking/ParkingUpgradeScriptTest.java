package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * 验证 parking_init.sql 中历史升级字段已经进入全量初始化脚本。
 */
class ParkingUpgradeScriptTest
{
    @Test
    void initScriptContainsAllCustomerMemberColumns() throws IOException
    {
        Path scriptPath = locateInitScript();
        assertTrue(Files.exists(scriptPath), () -> "Missing parking init script: " + scriptPath);

        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("is_member"), "Expected parking_customer.is_member");
        assertTrue(sql.contains("member_type"), "Expected parking_customer.member_type");
        assertTrue(sql.contains("member_expire_time"), "Expected parking_customer.member_expire_time");
    }

    @Test
    void initScriptCreatesCustomerTableWithMemberColumns() throws IOException
    {
        Path scriptPath = locateInitScript();
        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

        int tableStart = sql.indexOf("create table parking_customer");
        assertTrue(tableStart >= 0, "Expected create table parking_customer");

        int nextTableStart = sql.indexOf("create table", tableStart + 1);
        String customerTableSql = nextTableStart > tableStart ? sql.substring(tableStart, nextTableStart) : sql.substring(tableStart);

        assertTrue(customerTableSql.contains("is_member"), "Expected member flag in parking_customer table");
        assertTrue(customerTableSql.contains("member_type"), "Expected member type in parking_customer table");
        assertTrue(customerTableSql.contains("member_expire_time"), "Expected member expire time in parking_customer table");
    }

    @Test
    void initScriptUsesDropAndCreatePattern() throws IOException
    {
        Path scriptPath = locateInitScript();
        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("drop table if exists parking_customer"),
            "Expected full init script to drop parking_customer");
        assertTrue(sql.contains("create table parking_customer"),
            "Expected full init script to create parking_customer");
    }

    @Test
    void initScriptContainsMonthlyOrderPlateSnapshot() throws IOException
    {
        Path scriptPath = locateInitScript();
        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8).toLowerCase();

        assertEquals(0L, countOccurrences(sql, "information_schema.columns"),
            "Full init script should create the current schema directly");
        assertTrue(sql.contains("create table parking_monthly_order"),
            "Expected monthly order table");
        assertTrue(sql.contains("vehicle_plate_no"),
            "Expected monthly order plate snapshot column");
    }

    @Test
    void initScriptSeedsParkingGlobalConfigAndDictData() throws IOException
    {
        Path scriptPath = locateInitScript();
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
        assertTrue(sql.contains("parking_space_status"),
            "Expected dict seed for parking_space_status");
        assertTrue(sql.contains("parking_vehicle_type"),
            "Expected dict seed for parking_vehicle_type");
        assertTrue(sql.contains("parking_customer_type"),
            "Expected dict seed for parking_customer_type");
        assertTrue(sql.contains("parking_temp_biz_status"),
            "Expected dict seed for parking_temp_biz_status");
        assertTrue(sql.contains("parking_monthly_pay_status"),
            "Expected dict seed for parking_monthly_pay_status");
        assertTrue(sql.contains("parking_membership_biz_status"),
            "Expected dict seed for parking_membership_biz_status");
        assertTrue(sql.contains("parking_lot_admin_status"),
            "Expected dict seed for parking_lot_admin_status");
    }

    // ---- helpers ----

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

    private Path locateInitScript()
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null)
        {
            Path candidate = current.resolve(Paths.get("sql", "parking", "parking_init.sql"));
            if (Files.exists(candidate))
            {
                return candidate;
            }
            current = current.getParent();
        }
        return Paths.get("sql", "parking", "parking_init.sql");
    }
}
