package com.ajaxjs.util;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * 日期工具类
 * SimpleDateFormat 不是线程安全的，Java 8 之后尽量使用 java.time.DateTimeFormatter
 */
public class DateHelper {
    /**
     * 常见的日期格式
     */
    public static final String TIME = "HH:mm:ss";
    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static final String DATETIME_SHORT = "yyyy-MM-dd HH:mm";

    public static final String DATE = "yyyy-MM-dd";

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME);
    public static final DateTimeFormatter DATETIME_SHORT_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_SHORT);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE);

    /**
     * 将 LocalDate 转换为字符串。
     *
     * @param date 日期
     * @return 格式化的日期字符串
     */
    public static String formatDate(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    /**
     * 将 LocalTime 转换为字符串。
     *
     * @param time 时间
     * @return 格式化的时间字符串
     */
    public static String formatTime(LocalTime time) {
        return TIME_FORMATTER.format(time);
    }

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
     * 将字符串解析为 LocalDate。
     *
     * @param dateStr 日期字符串
     * @return 解析后的 LocalDate
     * @throws DateTimeParseException 如果解析失败
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 将字符串解析为 LocalTime。
     *
     * @param timeStr 时间字符串
     * @return 解析后的 LocalTime
     * @throws DateTimeParseException 如果解析失败
     */
    public static LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * 将字符串解析为 LocalDateTime。
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的 LocalDateTime
     * @throws DateTimeParseException 如果解析失败
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 将字符串解析为 LocalDateTime。
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的 LocalDateTime
     * @throws DateTimeParseException 如果解析失败
     */
    public static LocalDateTime parseDateTimeShort(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATETIME_SHORT_FORMATTER);
    }

    /**
     * 将 LocalDateTime 转换为 Date 对象
     * 此方法用于处理时间数据类型的转换，将 LocalDateTime 对象转换为 Date 对象
     * 主要通过获取当前时区的时区信息，将 LocalDateTime 转换为 Instant 对象，然后转换为 Date
     *
     * @param localDateTime 需要转换的 LocalDateTime 对象
     * @return 转换后的 Date 对象
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将 LocalDate类型转换为 Date 类型
     * 此方法用于处理日期转换，将 Java 8 引入的 LocalDate 对象转换为传统的 Date 对象
     * 转换基于系统默认时区，将 LocalDate 的开始时刻视为一天的起点
     *
     * @param localDate LocalDate 对象，代表需要转换的日期
     * @return Date对象，代表转换后的日期
     */
    public static Date localDate2Date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将Date对象转换为LocalDate对象
     * 此方法主要用于将Java传统日期时间API中的Date对象转换为Java 8引入的LocalDate对象
     * 这种转换通常需要考虑时区问题，因此这里使用了getZone方法（假设此方法存在且已实现）来处理时区转换
     *
     * @param date Date对象，表示要转换的日期
     * @return LocalDate对象，表示转换后的日期
     */
    public static LocalDate toLocalDate(Date date) {
        return getZone(date).toLocalDate();
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

    /**
     * 请求的时间戳，格式必须符合 RFC1123 的日期格式
     *
     * @return 当前日期
     */
    public static String getGMTDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        return format.format(new Date());
    }

    /**
     * 年月日的正则表达式，例如 2016-08-18
     */
    private final static String DATE_YEAR = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";

    /**
     * 正则实例
     */
    private final static Pattern DATE_YEAR_PATTERN = Pattern.compile(DATE_YEAR);

    /**
     * 一般日期判断的正则
     */
    private final static Pattern DATETIME_PATTERN = Pattern.compile(DATE_YEAR + " ([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");

    /**
     * 支持任意对象转换为日期类型
     *
     * @param obj 任意对象
     * @return 日期类型对象，返回 null 表示为转换失败
     */
    public static Date object2Date(Object obj) {
        if (obj == null)
            return null;
        else if (obj instanceof Date)
            return (Date) obj;
        else if (obj instanceof Long)
            return new Date((Long) obj);
        else if (obj instanceof Integer)
            return object2Date(Long.parseLong(obj + "000")); /* 10 位长 int，后面补充三个零为13位 long 时间戳 */
        else if (obj instanceof String) {
            String str = obj.toString();

            if (!StringUtils.hasText(str))
                return null;

            if (DATETIME_PATTERN.matcher(str).matches())
                return localDateTime2Date(parseDateTime(str));
            else if (DATE_YEAR_PATTERN.matcher(str).matches())
                return localDate2Date(parseDate(str));
            else
                return localDateTime2Date(parseDateTimeShort(str));
            // 输入日期不合法，不能转为日期类型。请重新输入日期字符串格式类型，或考虑其他方法。
        }

        return null;
    }

    /**
     * 获取当前日期时间。
     *
     * @return 当前日期时间
     */
    public static Date nowDateTime() {
        return localDateTime2Date(LocalDateTime.now());
    }

    /**
     * 获取当前日期时间(字符串)。
     *
     * @return 当前日期时间
     */
    public static String now() {
        return formatDateTime(LocalDateTime.now());
    }

    /**
     * 获取当前日期时间(字符串)，格式如
     *
     * @return 当前日期时间
     */
    public static String nowShort() {
        return DATETIME_SHORT_FORMATTER.format(LocalDateTime.now());
    }
}