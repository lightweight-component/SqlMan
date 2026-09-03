package com.ajaxjs.sqlman.xml;

import com.ajaxjs.sqlman.SmallMyBatis;
import com.ajaxjs.sqlman.xml.express_parser.JSqlParserExpressionEvaluator;
import com.ajaxjs.util.io.Resources;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestSqlXmlDomTemplate {
    private static final SqlTemplateExpressionEvaluator EVALUATOR = new JSqlParserExpressionEvaluator();

    @Test
    void rendersNestedIfAndElseFromTheSelectedBranchOnly() {
        SqlXmlDomTemplate template = template();
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", true);
        params.put("tier", 1);

        String trueBranch = normalize(template.render("nested", params).getSql());
        assertEquals("SELECT * FROM sample WHERE enabled = #{enabled} AND tier <= 1", trueBranch);

        params.put("enabled", false);
        String falseBranch = normalize(template.render("nested", params).getSql());
        assertEquals("SELECT * FROM sample WHERE enabled = false", falseBranch);
    }

    @Test
    void expandsForEachIntoUniqueBoundParameters() {
        SqlXmlDomTemplate template = template();
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(4, 7, 9));

        RenderedSql rendered = template.render("ids", params);
        SmallMyBatis.PreparedSql prepared = SmallMyBatis.prepareSql(rendered.getSql(), rendered.getParams(), new Object[0]);

        assertEquals("SELECT * FROM sample WHERE id IN (?, ?, ?)", normalize(prepared.getSql()));
        assertArrayEquals(new Object[]{4, 7, 9}, prepared.getParams());
        assertEquals(Arrays.asList(4, 7, 9), params.get("ids"));
    }

    @Test
    void rejectsElseOutsideIf() {
        SqlXmlDomTemplate template = template();

        assertThrows(IllegalArgumentException.class, () -> template.render("bad", new HashMap<String, Object>()));
    }

    private static String normalize(String sql) {
        return sql.trim().replaceAll("\\s+", " ");
    }

    private static SqlXmlDomTemplate template() {
        String xml = Resources.getResourceText("sql/dom-template.xml");
        assertNotNull(xml, "Test XML resource must be available");

        return new SqlXmlDomTemplate(xml, EVALUATOR);
    }
}
