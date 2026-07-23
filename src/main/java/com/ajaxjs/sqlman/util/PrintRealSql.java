package com.ajaxjs.sqlman.util;

import com.ajaxjs.sqlman.crud.BaseAction;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.date.Formatter;
import com.ajaxjs.util.log.TextBox;
import com.ajaxjs.util.log.Trace;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        return countPlaceholders(sql) == (params == null ? 0 : params.length);
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
            return "'" + new Formatter(date).format() + "'";
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

            List<Integer> placeholders = findPlaceholders(sql);
            StringBuilder sb = new StringBuilder(sql.length() + safeParams.length * 8);
            int previous = 0;

            for (int i = 0; i < placeholders.size(); i++) {
                int position = placeholders.get(i);
                sb.append(sql, previous, position);
                sb.append(i < safeParams.length ? formatValue(safeParams[i]) : "?");
                previous = position + 1;
            }

            return sb.append(sql, previous, sql.length()).toString();
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
        return findPlaceholders(sql).size();
    }

    /**
     * 查找真正的 JDBC 参数占位符，忽略字符串、标识符以及 SQL 注释中的问号。
     */
    private static List<Integer> findPlaceholders(String sql) {
        List<Integer> positions = new ArrayList<>();
        boolean singleQuote = false, doubleQuote = false, backtick = false;
        boolean lineComment = false, blockComment = false;

        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';

            if (lineComment) {
                if (current == '\n' || current == '\r')
                    lineComment = false;
                continue;
            }

            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }

            if (singleQuote) {
                if (current == '\'' && next == '\'')
                    i++; // SQL 标准的单引号转义：''
                else if (current == '\\' && next != '\0')
                    i++; // 兼容 MySQL 的反斜杠转义
                else if (current == '\'')
                    singleQuote = false;
                continue;
            }

            if (doubleQuote) {
                if (current == '"' && next == '"')
                    i++;
                else if (current == '"')
                    doubleQuote = false;
                continue;
            }

            if (backtick) {
                if (current == '`' && next == '`')
                    i++;
                else if (current == '`')
                    backtick = false;
                continue;
            }

            if ((current == '-' && next == '-') || current == '#') {
                lineComment = true;
                if (current == '-')
                    i++;
            } else if (current == '/' && next == '*') {
                blockComment = true;
                i++;
            } else if (current == '\'')
                singleQuote = true;
            else if (current == '"')
                doubleQuote = true;
            else if (current == '`')
                backtick = true;
            else if (current == '?')
                positions.add(i);
        }

        return positions;
    }

    private static final int MAX_REPEAT = 3;

    private static String lastBizAction;

    private static int repeatCount;

    /**
     * 日志限流（Log Throttling）
     * <p>
     * 对于同一个 bizAction，如果连续出现超过 N 次（例如 3 次），则后续相同 bizAction 的日志不再打印；直到出现其他 bizAction，计数重新开始。
     *
     * @param bizAction
     * @return
     */
    public static synchronized boolean shouldPrint(String bizAction) {
        if (Objects.equals(lastBizAction, bizAction)) {
            repeatCount++;

            return repeatCount <= MAX_REPEAT;
        }

        lastBizAction = bizAction;
        repeatCount = 1;

        return true;
    }

    /**
     * 打印数据库操作日志
     *
     * @param type          类型
     * @param traceId       链路 id
     * @param bizAction     链路业务名称
     * @param sql           SQL 语句
     * @param params        参数（字符串，或者拼接好的参数描述）
     * @param realSql       实际执行SQL（带参数）
     * @param action        用于计算耗时（如 33ms）
     * @param result        执行结果（Object）
     * @param wrapLongLines 是否允许完整显示超长字符串，自动换行
     */
    public static void printLog(String type, String traceId, String bizAction, String sql, Object params, String realSql, BaseAction action, Object result, boolean wrapLongLines) {
        if (MDC.get(Trace.ENABLE_LOG_THROTTLING) != null && ObjectHelper.hasText(bizAction) && !shouldPrint(bizAction))
            return;

        String title = " Debugging " + type + " ";
        realSql = realSql.replaceAll(REGEXP, " ");

        String duration;

        if (action != null)
            duration = String.valueOf(System.currentTimeMillis() - action.startTime);
        else
            duration = TextBox.NONE;

        TextBox textBox = new TextBox();
        textBox.boxStart(title)
                .line("Time:     ", DateTools.now())
                .line("TraceId:  ", traceId)
                .line("BizAction:", bizAction)
                .line("SQL:      ", sql.replaceAll(REGEXP, " "))
                .line("Params:   ", params)
                .line("Real:     ", realSql)
                .line("Duration: ", duration + "ms")
                .line("Result:   ", result);

        String _log = textBox.boxEnd();
        Trace.saveLogToMDC(_log);
        log.info(_log);
    }

    private static final String REGEXP = "[\n\r\t]";
}
