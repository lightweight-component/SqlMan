package com.ajaxjs.util;

import com.ajaxjs.util.reflect.Clazz;

import java.time.LocalDate;
import java.time.LocalTime;

public class EasyLogger {
    private enum Level {
        INFO, WARN, ERROR
    }

    private static final String INFO_TPL = "%s %s \033[32;4mINFO\033[0m \033[36;4m%s\033[0m : %s";
    private static final String WARN_TPL = "%s %s \033[33;4mWARN\033[0m \033[36;4m%s\033[0m : %s";
    private static final String ERROR_TPL = "%s %s \033[31;4mERROR\033[0m \033[36;4m%s\033[0m : %s";

    @SuppressWarnings("SpellCheckingInspection")
    private static final String DELIM_STR = "{}";

    private static final Object[] EMPTY_ARGS = new Object[0];

    private static void print(Level level, String format, Object... args) {
        String tpl;

        if (level == Level.INFO)
            tpl = INFO_TPL;
        else if (level == Level.WARN)
            tpl = WARN_TPL;
        else
            tpl = ERROR_TPL;

        if (null == args)
            args = EMPTY_ARGS;

        StringBuilder buffer = new StringBuilder(format.length() + 64);
        int beginIndex = 0, endIndex, count = 0;

        while ((endIndex = format.indexOf(DELIM_STR, beginIndex)) >= 0) {
            buffer.append(format, beginIndex, endIndex);

            try {
                buffer.append(args[count++]);
            } catch (IndexOutOfBoundsException e) {
                // 数组越界时对应占位符填null
                buffer.append("null");
            }
            beginIndex = endIndex + DELIM_STR.length();
        }

        buffer.append(format.substring(beginIndex));

        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
        String declaringClass = Clazz.getPrivateField(stackTrace, "declaringClass", String.class);
        String l = declaringClass + "#" + stackTrace.getLineNumber();
        String str = String.format(tpl, LocalDate.now(), LocalTime.now(), l, buffer);

        if (level == Level.INFO || level == Level.WARN)
            System.out.println(str);
        else
            System.err.println(str);
    }

    /**
     * 控制台打印 WARN 日志
     *
     * @param format 待打印
     */
    public static void info(String format, Object... args) {
        print(Level.INFO, format, args);
    }

    /**
     * 控制台打印 ERROR 日志
     *
     * @param format 待打印
     */
    public static void warn(String format, Object... args) {
        print(Level.WARN, format, args);
    }

    /**
     * 控制台打印 INFO 日志
     *
     * @param format 待打印
     */
    public static void error(String format, Object... args) {
        print(Level.ERROR, format, args);
    }


}
