package com.ajaxjs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpUtils {
    public boolean isMatch(Pattern pattern, String str) {
        return pattern.matcher(str).find();
    }

    /**
     * 返回 Matcher
     *
     * @param regexp 正则
     * @param str    测试的字符串
     * @return Matcher
     */
    private static Matcher getMatcher(String regexp, String str) {
        return Pattern.compile(regexp).matcher(str);
    }

    /**
     * 使用正则的快捷方式。可指定分组
     *
     * @param regexp     正则
     * @param str        测试的字符串
     * @param groupIndex 分组 id，若为 -1 则取最后一个分组
     * @return 匹配结果
     */
    public static String regMatch(String regexp, String str, int groupIndex) {
        Matcher m = getMatcher(regexp, str);

        if (groupIndex == -1)
            groupIndex = m.groupCount();

        return m.find() ? m.group(groupIndex) : null;
    }

    /**
     * 使用正则的快捷方式
     *
     * @param regexp 正则
     * @param str    测试的字符串
     * @return 匹配结果，只有匹配第一个
     */
    public static String regMatch(String regexp, String str) {
        return regMatch(regexp, str, 0);
    }

    /**
     * 返回所有匹配项
     *
     * @param regexp 正则
     * @param str    测试的字符串
     * @return 匹配结果
     */
    public static String[] regMatchAll(String regexp, String str) {
        Matcher m = getMatcher(regexp, str);
        List<String> list = new ArrayList<>();

        while (m.find())
            list.add(m.group());

        return list.toArray(new String[0]);
    }
}
