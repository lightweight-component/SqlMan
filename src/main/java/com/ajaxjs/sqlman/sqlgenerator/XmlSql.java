package com.ajaxjs.sqlman.sqlgenerator;

import com.ajaxjs.util.CommonConstant;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves named placeholders in XML-style SQL templates.
 */
public class XmlSql {
    private static final Pattern PATTERN = Pattern.compile("(#\\{|\\$\\{)(.*?)(})");

    /**
     * Applies parameter values to an XML-style SQL template.
     *
     * @param template the SQL template.
     * @param paramMap the parameter values by name.
     * @return the resolved SQL.
     */
    public String getValuedSQL(String template, Map<String, Object> paramMap) {
        Matcher matcher = PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(2); // 获取占位符中的键名
            String strValue;

//            if (placeholder.startsWith("T(")) { // 调用 Java 类的方法
//                Object value = EXP_PARSER.parseExpression(placeholder).getValue();
//                strValue = value == null ? CommonConstant.EMPTY_STRING : value.toString();
//            } else {
            Object value = paramMap.get(placeholder);

            if (value == null) {
                strValue = CommonConstant.EMPTY_STRING; // 如果值为空，替换为空字符串
            } else {
                // 处理转义字符和 $ 符号
                strValue = value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");

                if (matcher.group(1).equals("#{")) { // 使用 PreparedStatement 设置参数，自动转换类型
                    if (value instanceof Number)
                        strValue = String.valueOf(value);
                    else if (value.equals(true))
                        strValue = "1";
                    else if (value.equals(false))
                        strValue = "0";
                    else
                        strValue = "'" + value + "'"; // 如果是非数字类型，加上单引号
                }
            }
//            }

            matcher.appendReplacement(sb, strValue); // 替换占位符
        }

        matcher.appendTail(sb);

        return sb.toString();
    }
}
