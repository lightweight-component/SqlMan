package com.ajaxjs.sqlman.sql;


import com.ajaxjs.util.CollUtils;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.XmlHelper;
import com.ajaxjs.util.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类似 MyBatis 把 SQL 存储在 XML 中，这个是低配版
 */
@Slf4j
public class SmallMyBatis {
    /**
     * key=id/value=sql
     */
    private final Map<String, String> ALL_SQL = new HashMap<>();

    /**
     * 加载 XML SQL
     *
     * @param xmlFiles XML Files
     */
    public void loadXML(String... xmlFiles) {
        Pattern pattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL);  // 使用 DOT ALL 匹配多行注释

        for (String xmlFile : xmlFiles) {
            String xmlBody = Resources.getResourceText(xmlFile);

            if (xmlBody != null) {
                // 删除注释
                xmlBody = pattern.matcher(xmlBody).replaceAll(StrUtil.EMPTY_STRING);
                XmlHelper.parseXML(xmlBody, (node, nodeList) -> {
                    if ("sql".equals(node.getNodeName())) {
                        String id = XmlHelper.getNodeAttribute(node, "id");

                        if (ALL_SQL.containsKey(id))
                            log.warn("已有相同 sqlId [{}]", id);

                        String sql = XmlHelper.getNodeText(node);
                        ALL_SQL.put(id, sql);
                    }
                });
            } else
                log.warn("找不到 {}-XML 资源", xmlFile);
        }
    }

    /**
     * 根据目录加载多个 XML
     *
     * @param sqlLocations 目录
     */
    public void loadBySqlLocations(String sqlLocations) { // 如 classpath*:sql/**/*.xml
        Resource[] resources = null;

        try {
            resources = new PathMatchingResourcePatternResolver().getResources(sqlLocations);// 扫描目录生成文件
        } catch (IOException e) {
            log.warn("文件路径没有 xml", e);
        }

        if (CollUtils.isEmpty(resources))
            throw new RuntimeException("文件路径[" + sqlLocations + "]没有 xml");

        // 写死 sql 目录下。Resource 不能返回相对目录
        String[] xmlArray = Arrays.stream(resources).map(n -> "sql/" + n.getFilename()).toArray(String[]::new);
        loadXML(xmlArray);
    }

    /**
     * 根据 id 获取 SQL
     *
     * @param id xml 里面的 id
     * @return SQL
     */
    public String getSqlById(String id) {
        String sql = ALL_SQL.get(id);

        if (StrUtil.isEmptyText(sql))
            throw new IllegalArgumentException("查询 id 为 " + id + "　的 SQL 为空");

        return sql;
    }

    /**
     * 类似 Mybatis 替换动态 sql 的方法，要求支持 if 标签
     *
     * @param sqlTemplate SQL 语句
     * @param params      SQL 插值参数集合
     * @return 实际要执行的 SQL
     */
    public static String generateIfBlock(String sqlTemplate, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(sqlTemplate);
        int startIdx = sb.indexOf("<if");

        while (startIdx != -1) {
            int endIdx = sb.indexOf("</if>", startIdx);
            String ifBlock = sb.substring(startIdx, endIdx + 5);

            if (evalIfBlock(ifBlock, params)) {
                sb.delete(startIdx, endIdx + 5);
                String content = ifBlock.substring(ifBlock.indexOf(">") + 1, ifBlock.lastIndexOf("<"));
                sb.insert(startIdx, content);
            } else
                sb.delete(startIdx, endIdx + 5);

            startIdx = sb.indexOf("<if", startIdx);
        }

        return sb.toString();
    }

    static class BooleanExpressionParser extends SpelExpressionParser {
        private final StandardEvaluationContext context = new StandardEvaluationContext();

        public BooleanExpressionParser() {
            super();
            context.setPropertyAccessors(Collections.singletonList(new MapAccessor()));
            //            context.setVariables(paramMap);
        }

        public boolean get(String expression, Map<String, Object> paramMap) {
            SpelExpression expr = (SpelExpression) parseExpression(expression);
            expr.setEvaluationContext(context);

            try {
                return Boolean.TRUE.equals(expr.getValue(paramMap, boolean.class));
            } catch (SpelEvaluationException e) {// 为防止 null 值
                return false;
            }
        }
    }

    private static final BooleanExpressionParser BOL_EXP_PARSER = new BooleanExpressionParser();

    private static final ExpressionParser EXP_PARSER = new SpelExpressionParser();

    /**
     * 判断 ifBlock 是否满足条件，满足则返回 true，否则返回 false
     */
    private static boolean evalIfBlock(String ifBlock, Map<String, Object> params) {
        String test = ifBlock.substring(ifBlock.indexOf("test=") + 6, ifBlock.lastIndexOf("\""));// 从 ifBlock 中截取出 test= 后面的字符串，并去掉双引号

        return BOL_EXP_PARSER.get(test, params); // 调用 BOL_EXP_PARSER 解析 test 字符串，传入 params 参数，返回解析结果的布尔值
    }

    /**
     * 解析 SQL 中的 forEach 语句
     *
     * @param sql    原始 SQL 语句
     * @param params 参数集合
     * @return 解析后的 SQL 语句
     */
    private static String parseForEach(String sql, Map<String, Object> params) {
        StringBuilder result = new StringBuilder(sql.length());
        int start = 0, end = sql.indexOf("<forEach>", start);

        while (end != -1) {
            result.append(sql, start, end);
            int subStart = end + "<forEach>".length(), subEnd = sql.indexOf("</forEach>", subStart);
            String subSql = sql.substring(subStart, subEnd), varName = sql.substring(end + "<forEach>".length(), subStart - "</forEach>".length() - 1).trim();
            Object list = params.get(varName);

            if (list instanceof List<?>) {
                List<?> itemList = (List<?>) list;

                for (int i = 0; i < itemList.size(); i++) {
                    Map<String, Object> subParams = new HashMap<>(params);
                    subParams.put("item", itemList.get(i));
                    subParams.put("index", i);
                    String subResult = parseForEach(subSql, subParams);
                    result.append(subResult);
                }
            }

            start = subEnd + "</forEach>".length();
            end = sql.indexOf("<forEach>", start);
        }

        result.append(sql.substring(start));

        return result.toString();
    }

    /**
     * 匹配占位符的正则表达式
     */
    private static final Pattern PATTERN = Pattern.compile("(#\\{|\\$\\{)(.*?)(})");

    /**
     * 根据传入的模板和参数映射，生成带值的 SQL 语句
     *
     * @param template SQL 语句模板
     * @param paramMap 参数映射，键为占位符，值为对应的参数值
     * @return 生成的带值的 SQL 语句
     */
    public static String getValuedSQL(String template, Map<String, Object> paramMap) {
        Matcher matcher = PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(2); // 获取占位符中的键名
            String strValue;

            if (placeholder.startsWith("T(")) { // 调用 Java 类的方法
                Object value = EXP_PARSER.parseExpression(placeholder).getValue();
                strValue = value == null ? StrUtil.EMPTY_STRING : value.toString();
            } else {
                Object value = paramMap.get(placeholder); // 获取键名对应的值

                if (value == null) {
                    strValue = StrUtil.EMPTY_STRING; // 如果值为空，替换为空字符串
                } else {
                    strValue = value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$"); // 处理转义字符和 $ 符号

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
            }

            matcher.appendReplacement(sb, strValue); // 替换占位符
        }

        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 处理 SQL 语句
     *
     * @param paramsMap 参数映射关系
     * @param sqlId     SQL ID
     * @return 处理后的 SQL 语句
     */
    public String handleSql(Map<String, Object> paramsMap, String sqlId) {
        return handleSql(getSqlById(sqlId), paramsMap);// 处理 SQL 语句
    }

    /**
     * 处理 SQL 语句
     *
     * @param sql       SQL 语句
     * @param paramsMap 参数映射关系
     * @return 处理后的 SQL 语句
     */
    public static String handleSql(String sql, Map<String, Object> paramsMap) {
        if (paramsMap == null)
            paramsMap = ObjectHelper.EMPTY_PARAMS_MAP;

        sql = generateIfBlock(sql, paramsMap);
        //        sql = parseForEach(sql, paramsMap);
        sql = getValuedSQL(sql, paramsMap);
        sql = sql.replaceAll("&lt;", "<").replaceAll("&gt;", ">");

        return sql;
    }

    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 解析并评估模板中的条件标签。
     *
     * @param template 包含 <if> 和 <else> 标签的模板字符串。
     * @param context  用于评估表达式的上下文。
     * @return 解析后的最终字符串。
     */
    public static String evaluate(String template, StandardEvaluationContext context) {
        StringBuilder result = new StringBuilder();
        int pos = 0;

        while (pos < template.length()) {
            int ifStart = template.indexOf("<if test=", pos);
            if (ifStart == -1) break;

            // 将 <if> 标签之前的内容直接添加到结果中
            result.append(template, pos, ifStart);

            // 查找 </if> 或 <else> 结束标签的位置
            int elsePos = template.indexOf("<else>", ifStart);
            int endIfPos = template.indexOf("</if>", ifStart);
            if (endIfPos == -1) throw new IllegalArgumentException("Missing closing </if>");

            // 提取并评估条件表达式
            int exprEnd = template.indexOf('>', ifStart);
            if (exprEnd == -1 || exprEnd > endIfPos) throw new IllegalArgumentException("Invalid <if> tag");

            String exprText = template.substring(ifStart + "<if test=\"".length(), exprEnd).trim();
            Expression expr = parser.parseExpression(exprText);
            boolean condition = expr.getValue(context, Boolean.class);

            // 根据条件选择内容片段
            String content;
            if (condition) {
                int bodyStart = exprEnd + 1;
                content = template.substring(bodyStart, elsePos != -1 ? elsePos : endIfPos);
            } else {
                if (elsePos == -1) {
                    content = "";
                } else {
                    content = template.substring(elsePos + "<else>".length(), endIfPos);
                }
            }

            // 添加选定的内容到结果中
            result.append(content.trim());

            // 更新位置指针
            pos = endIfPos + "</if>".length();
        }

        // 添加剩余部分（如果有的话）
        if (pos < template.length()) {
            result.append(template.substring(pos));
        }

        return result.toString();
    }
}