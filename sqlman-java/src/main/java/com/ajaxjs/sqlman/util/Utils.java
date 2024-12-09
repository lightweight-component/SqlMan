package com.ajaxjs.sqlman.util;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
    /**
     * Check whether the given {@code String} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.
     *
     * @param str the {@code String} to check (maybe {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     * length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i)))
                return true;

        }

        return false;
    }

    /**
     * 从指定的数据源获取数据库连接
     *
     * @param dataSource 数据源对象，用于提供数据库连接
     * @return Connection 数据库连接对象
     * @throws RuntimeException 如果无法从数据源获取连接，则抛出运行时异常
     */
    public static Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Can't get a connection from a DataSource: " + dataSource, e);
        }
    }

    /**
     * 将以下划线分隔的数据库字段转换为驼峰风格的字符串
     *
     * @param columnName 下划线分隔的字符串
     * @return 驼峰风格的字符串
     */
    public static String changeColumnToFieldName(String columnName) {
        StringBuilder result = new StringBuilder();
        String[] words = columnName.split("_");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i != 0) {
                // 将每个单词的首字母大写
                char firstChar = word.charAt(0);
                word = Character.toUpperCase(firstChar) + word.substring(1);
            }

            result.append(word);
        }

        return result.toString();
    }

    /**
     * 将驼峰风格的字符串转换为以下划线分隔的数据库字段
     *
     * @param fieldName 驼峰风格的字符串
     * @return 下划线分隔的数据库字段
     */
    public static String changeFieldToColumnName(String fieldName) {
        if (fieldName == null)
            return null;

        StringBuilder columnName = new StringBuilder();
        int length = fieldName.length();

        for (int i = 0; i < length; i++) {
            char c = fieldName.charAt(i);

            if ('A' <= c && 'Z' >= c)
                columnName.append("_").append((char) (c + 32));
            else
                columnName.append(fieldName.charAt(i));
        }

        String str = columnName.toString();

        if (str.startsWith("_"))  // 单字母 如 SXxx 会出现 _s_xxx
            str = str.substring(1);

        return str;
    }

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
     * 多行空行
     */
    private final static String SPACE_LINE = "(?m)^[ \t]*\r?\n";

    /**
     * 打印真实 SQL 执行语句
     *
     * @param sql    SQL 语句，可以带有 ? 的占位符
     * @param params 插入到 SQL 中的参数，可单个可多个可不填
     * @return 实际 sql 语句
     */
    public static String printRealSql(String sql, Object[] params) {
        if (!hasText(sql))
            throw new IllegalArgumentException("SQL 语句不能为空！");

        //        if (isClosePrintRealSql)
        //            return null;

        sql = sql.replaceAll(SPACE_LINE, "");

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
                inSql = "";

            arr[i] = arr[i] + inSql;
        }

        String str = String.join(" ", arr).trim();

        return insertNewline(str, 20);
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

    /**
     * 检查数据库中是否存在指定的表
     * 检测表是否存在
     *
     * @param conn      数据库连接对象
     * @param tableName 表名
     * @return 如果表存在则返回true，否则返回false
     */
    public static boolean checkTableExists(Connection conn, String tableName) {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            if (rs.next())
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
