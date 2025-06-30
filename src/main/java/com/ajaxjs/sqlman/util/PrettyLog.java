package com.ajaxjs.sqlman.util;

import com.ajaxjs.sqlman.JdbcCommand;
import com.ajaxjs.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PrettyLog {
    public static final String LOG_TEXT = "\n" +
            "┌───────── Debugging {} ───────────\n" +
            "│ SQL:      {}\n" +
            "│ params:   {}\n" +
            "│ Real:     {}";

    public static void end(JdbcCommand jdbcCommand, String result) {
        System.out.println("│ Duration: " + (System.currentTimeMillis() - jdbcCommand.getStartTime()) + "ms");
        System.out.println("│ Result:   " + result);
        System.out.println("└───────── Debugging END ───────────");

    }

    public static String trimResult(Object result) {
        String s = result.toString();

        if (s.length() > 200)
            s = s.substring(0, 200);

        return s;
    }

    /**
     * 多行空行
     */
    private final static String SPACE_LINE = "(?m)^[ \t]*\r?\n";

    /**
     * ? 和参数的实际个数是否匹配
     *
     * @param sql    SQL 语句，可以带有 ? 的占位符
     * @param params 插入到 SQL 中的参数，可单个可多个可不填
     * @return true 表示为 ? 和参数的实际个数匹配
     */
    private static boolean match(String sql, Object[] params) {
        if (params == null || params.length == 0)
            return true; // 没有参数，完整输出

        Matcher m = Pattern.compile("(\\?)").matcher(sql);
        int count = 0;
        while (m.find())
            count++;

        return count == params.length;
    }

    /**
     * 打印真实 SQL 执行语句
     *
     * @param sql    SQL 语句，可以带有 ? 的占位符
     * @param params 插入到 SQL 中的参数，可单个可多个可不填
     * @return 实际 sql 语句
     */
    public static String printRealSql(String sql, Object[] params) {
        if (!StrUtil.hasText(sql))
            throw new IllegalArgumentException("SQL 语句不能为空！");

        try {

            //        if (isClosePrintRealSql)
            //            return null;
            sql = sql.replaceAll(SPACE_LINE, StrUtil.EMPTY_STRING);

            if (params == null || params.length == 0) // 完整的 SQL 无须填充
                return sql;

            if (!match(sql, params))
                log.info("SQL 语句中的占位符与值参数（个数上）不匹配。SQL：{}，\nparams:{}", sql, Arrays.toString(params));

            if (sql.endsWith("?"))
                sql += " ";

            String[] arr = sql.split("\\?");

            for (int i = 0; i < arr.length - 1; i++) {
                Object value = params[i];
                String inSql;

                if (value instanceof Date) // 只考虑了字符串、布尔、数字和日期类型的转换
                    inSql = "'" + value + "'";
                else if (value instanceof String)
                    inSql = "'" + value + "'";
                else if (value instanceof Boolean)
                    inSql = (Boolean) value ? "1" : "0";
                else if (value != null)
                    inSql = value.toString();// number
                else
                    inSql = StrUtil.EMPTY_STRING;

                arr[i] = arr[i] + inSql;
            }

            String str = String.join(" ", arr).trim();

            return insertNewline(str, 25);
        } catch (Throwable e) {
            log.warn("打印真实 SQL 执行语句异常", e);

            return "打印真实 SQL 执行语句异常";
        }
    }


    /**
     * 指定行数时插入换行符
     *
     * @param input 字符串
     * @param n     第几个单词就换行
     * @return 字符串
     */
    public static String insertNewline(String input, int n) {
        StringBuilder sb = new StringBuilder();
        String[] words = input.split(" ");
        int lineCount = 0;

        for (String word : words) {
            sb.append(word).append(" ");
            lineCount++;

            if (lineCount == n) {
                sb.append("\n");
                lineCount = 0;
            }
        }

        return sb.toString().trim();
    }
}
