package com.ajaxjs.sqlman.v1;

import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class PrettyLogger extends BoxLogger {
    // 日志方框宽度
    private static final int BOX_WIDTH = 137;

    private static final String REGEXP = "[\n\r\t]";
    private static final int WORDS_SIZE_LIMIT = 800;

    /**
     * 打印数据库操作日志
     *
     * @param type          类型
     * @param traceId       链路 id
     * @param bizAction     链路业务名称
     * @param sql           SQL 语句
     * @param _params       参数（字符串，或者拼接好的参数描述）
     * @param realSql       实际执行SQL（带参数）
     * @param jdbcCommand   用于计算耗时（如 33ms）
     * @param result        执行结果（Object）
     * @param wrapLongLines 是否允许完整显示超长字符串，自动换行
     */
    public static void printLog(String type, String traceId, String bizAction, String sql, Object _params, String realSql, JdbcCommand jdbcCommand, Object result, boolean wrapLongLines) {
        String title = " Debugging " + type + " ";
        String params;

        if (_params instanceof String)
            params = (String) _params;
        else if (_params instanceof Object[]) {
            Object[] arr = (Object[]) _params;

            if (ObjectHelper.isEmpty(arr))
                params = NONE;
            else
                params = Arrays.toString(arr);
        } else if (_params == null)
            params = NONE;
        else
            params = _params.toString();

        if (params.length() > WORDS_SIZE_LIMIT) // 限制长度
            params = params.substring(0, WORDS_SIZE_LIMIT) + "...";

        String _result = result == null ? "null" : String.valueOf(result);

        if (_result.length() > WORDS_SIZE_LIMIT) // 限制长度
            _result = _result.substring(0, WORDS_SIZE_LIMIT) + "...";

        if (realSql.length() > WORDS_SIZE_LIMIT) // 限制长度
            realSql = realSql.substring(0, WORDS_SIZE_LIMIT) + "...";

        realSql = realSql.replaceAll(REGEXP, " ");

        String duration;

        if (jdbcCommand != null)
            duration = String.valueOf(System.currentTimeMillis() - jdbcCommand.getStartTime());
        else
            duration = NONE;

        StringBuilder sb = new StringBuilder();

        sb.append(ANSI_GREEN).append(boxLine('┌', '─', '┐', title)).append('\n');
        printBoxContent(sb, "TraceId:  ", traceId, wrapLongLines);
        printBoxContent(sb, "BizAction:", bizAction, wrapLongLines);
        printBoxContent(sb, "SQL:      ", sql.replaceAll(REGEXP, " "), wrapLongLines);
        printBoxContent(sb, "Params:   ", params, wrapLongLines);
        printBoxContent(sb, "Real:     ", realSql, wrapLongLines);
        printBoxContent(sb, "Duration: ", duration + "ms", wrapLongLines);
        printBoxContent(sb, "Result:   ", _result, false);
        sb.append(boxLine('└', '─', '┘', CommonConstant.EMPTY_STRING)).append(ANSI_RESET);

//        System.out.println(sb);
        log.info('\n' + sb.toString());
    }

    // 打印边框行
    public static String boxLine(char left, char fill, char right, String title) {
        int fillLen = BOX_WIDTH - 1 - title.length();
        int leftFill = fillLen / 2;
        int rightFill = fillLen - leftFill;

        return left + repeat(fill, leftFill) + title + repeat(fill, rightFill) + right;
    }

    // 根据开关打印内容行
    private static void printBoxContent(StringBuilder sb, String key, String value, boolean wrapLongLines) {
        int maxLen = BOX_WIDTH - 2 - key.length();

        if (!wrapLongLines) {
            String val = truncate(value, maxLen);
            int pad = BOX_WIDTH - 1 - key.length() - getDisplayWidth(val);
            sb.append("│ ").append(key).append(val).append(repeat(' ', pad - 1)).append("│").append('\n');
        } else {
            List<String> lines = wrap(value, maxLen);

            for (int i = 0; i < lines.size(); i++) {
                String prefix = (i == 0 ? key : repeat(' ', key.length()));
                int pad = BOX_WIDTH - 1 - prefix.length() - getDisplayWidth(lines.get(i));
                sb.append("│ ").append(prefix).append(lines.get(i)).append(repeat(' ', pad - 1)).append("│").append('\n');
            }
        }
    }

    // 按最大宽度自动换行，返回每行内容
    private static List<String> wrap(String s, int maxDisplayLen) {
        List<String> result = new ArrayList<>();

        if (s == null) {
            result.add(CommonConstant.EMPTY_STRING);
            return result;
        }

        StringBuilder line = new StringBuilder();
        int width = 0;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int w = isWideChar(ch) ? 2 : 1;

            if (width + w > maxDisplayLen) {
                result.add(line.toString());
                line.setLength(0);
                width = 0;
            }

            line.append(ch);
            width += w;
        }

        if (line.length() > 0)
            result.add(line.toString());
        if (result.isEmpty())
            result.add(CommonConstant.EMPTY_STRING);

        return result;
    }
}
