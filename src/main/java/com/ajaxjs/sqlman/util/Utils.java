package com.ajaxjs.sqlman.util;

import com.ajaxjs.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
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

    public static Map<String, Object> changeFieldToColumnName(Map<String, Object> map) {
        Map<String, Object> n = new HashMap<>();

        for (String key : map.keySet())
            n.put(changeFieldToColumnName(key), map.get(key));

        return n;
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
            throw new RuntimeException(e);
        }

        return false;
    }

    private static final Pattern PATTERN = Pattern.compile("(?i)select|update|delete|insert|drop|truncate|union|\\*|--|;");

    /**
     * 过滤输入字符串以避免 SQL 注入攻击。
     * 该方法通过正则表达式匹配并移除可能导致 SQL 注入的特殊字符或关键字。
     * 使用该方法对用户输入进行清理，可以增强系统的安全性。
     *
     * @param input 待过滤的字符串，通常是用户输入
     * @return 过滤后的字符串，移除了可能的 SQL 注入关键字或字符
     */
    public static String escapeSqlInjection(String input) {
        return PATTERN.matcher(input).replaceAll(StrUtil.EMPTY_STRING);
    }

    /**
     * 利用反射获取数据源连接信息
     *
     * @param dataSource 数据源
     * @return 数据源连接信息
     */
    public static String retrieveCredentials(DataSource dataSource) {
        String result = null;

        try {
            Field host = dataSource.getClass().getDeclaredField("host");
            host.setAccessible(true);
            Object hostValue = host.get(dataSource);
            Field port = dataSource.getClass().getDeclaredField("port");
            port.setAccessible(true);
            Object portValue = port.get(dataSource);
            Field database = dataSource.getClass().getDeclaredField("database");
            database.setAccessible(true);
            Object databaseValue = database.get(dataSource);
            Field user = dataSource.getClass().getDeclaredField("user");
            user.setAccessible(true);
            Object userValue = user.get(dataSource);
            Field pwd = dataSource.getClass().getDeclaredField("password");
            pwd.setAccessible(true);
            Object pwdValue = pwd.get(dataSource);

            result = String.format("{\"user\": \"%s\", \"pwd\": \"%s\", \"host\": \"%s\", \"port\": %s, \"database\": \"%s\"}",
                    userValue, pwdValue.toString(), hostValue.toString(),
                    portValue.toString(), databaseValue.toString());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }
}
