package com.ajaxjs.sqlman.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.ajaxjs.sqlman.util.PrettyLogger.printLog;

public class TestPrettyLogger {
    @Test
    void test() {
        String sql = "SELECT COUNT(*) FROM user WHERE 1 = 1 AND tenant_id = 3";
        Object[] params = null;
        String realSql = "SELECT COUNT(*) FROM user WHERE 1 = 1 AND tenant_id = 3";
        long durationMs = 33;
        Object result = "{COUNT(*)=1}";

        printLog("Query", null, "Biz", sql, null, realSql, null, result, true);

        // 测试超长内容
        String longSql = "SELECT * FROM a_very_long_table_name_with_many_columns" +
                " WHERE condition1 = value1 AND condition2 = value2 AND " +
                "condition3 = value3(SELECT JSON_ARRAYAGG(JSON_OBJECT(\n" +
                "                  'id', id, 'pid', pid, 'name', name, 'namespace', namespa(SELECT JSON_ARRAYAGG(JSON_OBJECT(\n" +
                "                  'id', id, 'pid', pid, 'name', name, 'namespace', namespa";
        Object[] longParams = new Object[]{"param1", "param2", "param3", "param4"};
        printLog("Query", null, "Biz", longSql, Arrays.toString(longParams), longSql, null, result, true);
    }
}
