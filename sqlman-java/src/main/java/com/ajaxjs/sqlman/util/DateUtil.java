package com.ajaxjs.sqlman.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 将 LocalDateTime 转换为字符串。
     *
     * @param dateTime 日期时间
     * @return 格式化的日期时间字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return DATETIME_FORMATTER.format(dateTime);
    }

    /**
     * 将 Date 转换为字符串。
     *
     * @param dateTime 日期时间
     * @return 格式化的日期时间字符串
     */
    public static String formatDateTime(Date dateTime) {
        return formatDateTime(toLocalDateTime(dateTime));
    }

    /**
     * 将Date对象转换为LocalDateTime对象
     * 此方法用于处理时间转换，将一个Date对象（表示特定的瞬间，精确到毫秒）
     * 转换为LocalDateTime对象（表示日期和时间，没有时区信息）
     * 主要用于需要进行日期和时间操作，但不涉及时区的情景
     *
     * @param date Date对象，表示需要转换的时间点
     * @return LocalDateTime对象，表示转换后的日期和时间
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return getZone(date).toLocalDateTime();
    }

    /**
     * 将Date对象转换为ZonedDateTime对象
     * 此方法处理两种类型的Date对象：java.util.Date和java.sql.Date
     * 由于java.sql.Date不支持时间组件，因此需要特殊处理以避免UnsupportedOperationException异常
     *
     * @param date 一个Date对象，可以是java.util.Date或java.sql.Date的实例
     * @return 对应的ZonedDateTime对象，使用系统默认时区
     */
    private static ZonedDateTime getZone(Date date) {
        Instant instant;
        /*
            java.sql.Date仅支持日期组件（日期、月份、年份）。它不支持时间组件（小时、分钟、秒、毫秒）。
            toInstant需要 Date 和 Time 组件，
            因此 java.sql.Date 实例上的 toInstant 会引发 UnsupportedOperationException 异常
        */
        if (date instanceof java.sql.Date)
            instant = Instant.ofEpochMilli(date.getTime());
        else
            instant = date.toInstant();

        return instant.atZone(ZoneId.systemDefault());
    }
}
