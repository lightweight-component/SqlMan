package com.ajaxjs.sqlman.util;

import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Printing the final SQL with real values
 */
@Slf4j
public class PrintRealSql {
    /**
     * 编译正则表达式模式，匹配 SQL 中的 '?' 占位符
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\?");

    /**
     * 检查 SQL 中的 '?' 占位符个数与参数个数是否匹配
     *
     * @param sql    SQL 语句，可含 '?' 占位符
     * @param params 参数数组，可为 null 或空
     * @return true 表示个数匹配（或无参数）
     */
    private static boolean match(String sql, Object[] params) {
        if (params == null || params.length == 0)
            return true; // 无参数，视为匹配

        Matcher matcher = PARAM_PATTERN.matcher(sql);
        int placeholderCount = 0;

        while (matcher.find())
            placeholderCount++;

        return placeholderCount == params.length;
    }

    /**
     * 将对象格式化为 SQL 可读的字符串表示
     *
     * @param value 待格式化的值
     * @return 格式化后的字符串
     */
    private static String formatValue(Object value) {
        if (value == null)
            return "NULL";

        if (value instanceof String) {
            String str = (String) value;
            String escaped = str.replace("'", "''");// 转义单引号：O'Reilly → 'O''Reilly'

            return "'" + escaped + "'";
        }

        if (value instanceof Date) {
            Date date = (Date) value;
            return "''";
        }

        if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            return bool ? "TRUE" : "FALSE"; // 更通用，兼容多数数据库
        }

        return value.toString();   // 其他类型（Number 等）直接 toString
    }

    /**
     * 打印真实 SQL 执行语句（仅用于日志调试）
     * <p>
     * 将 SQL 中的 '?' 占位符替换为实际参数值的字符串表示
     * </p>
     *
     * @param sql    SQL 语句，可含 '?' 占位符
     * @param params 插入到 SQL 中的参数，可为 null 或多个
     * @return 包含实际值的 SQL 字符串，若出错则返回带错误信息的描述
     */
    public static String printRealSql(String sql, Object[] params) {
        if (!ObjectHelper.hasText(sql))
            throw new IllegalArgumentException("SQL 语句不能为空！");

        // 处理 null 参数
        Object[] safeParams = params == null ? new Object[0] : params;

        try {
            // 检查占位符与参数个数是否匹配
            if (!match(sql, safeParams))
                log.warn("SQL 占位符 '?' 个数与参数个数不匹配。SQL: [{}], 参数个数: {}, 占位符个数: {}", sql, safeParams.length, countPlaceholders(sql));

            // 使用 Matcher 进行安全替换
            Matcher matcher = PARAM_PATTERN.matcher(sql);
            StringBuffer sb = new StringBuffer();

            int paramIndex = 0;
            while (matcher.find()) {
                String replacement = (paramIndex < safeParams.length)
                        ? formatValue(safeParams[paramIndex])
                        : "?"; // 参数不足，保留 ?

                // 使用 Matcher.quoteReplacement 防止 $ 和 \ 引发问题
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                paramIndex++;
            }

            matcher.appendTail(sb);

            return (sb.toString());
//            return format(sb.toString());
        } catch (Exception e) {
            log.warn("生成 SQL 预览字符串时发生异常。SQL: [{}], 参数: {}", sql, java.util.Arrays.toString(params), e);
            // 返回原始 SQL + 参数信息，便于排查
            return "生成SQL失败: " + sql + " [参数: " + java.util.Arrays.toString(params) + "]";
        }
    }

    /**
     * 辅助方法：计算 SQL 中 '?' 占位符的个数
     */
    private static int countPlaceholders(String sql) {
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        int count = 0;

        while (matcher.find())
            count++;

        return count;
    }
}
