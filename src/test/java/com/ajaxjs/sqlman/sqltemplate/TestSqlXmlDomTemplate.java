package com.ajaxjs.sqlman.sqltemplate;

import com.ajaxjs.sqlman.model.PreparedSql;
import com.ajaxjs.sqlman.sqltemplate.model.RenderedSql;
import com.ajaxjs.sqlman.sqltemplate.xml.SqlXmlMgr;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestSqlXmlDomTemplate {
    @Test
    void compilesAndRendersInlineSqlTemplate() {
        SqlXmlDomTemplate template = SqlXmlDomTemplate.compile(
                "SELECT * FROM ${tableName}<if test=\"stat IS NOT NULL\"> WHERE stat = #{stat}</if>");
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "sample");
        params.put("stat", "ACTIVE");

        PreparedSql prepared = ParameterBinder.prepare(template.render(params));

        assertEquals("SELECT * FROM sample WHERE stat = ?", normalize(prepared.getSql()));
        assertArrayEquals(new Object[]{"ACTIVE"}, prepared.getParams());

        params.put("stat", null);
        prepared = ParameterBinder.prepare(template.render(params));
        assertEquals("SELECT * FROM sample", normalize(prepared.getSql()));
        assertArrayEquals(new Object[0], prepared.getParams());
    }

    @Test
    void rendersNestedIfAndElseFromTheSelectedBranchOnly() {
        SqlXmlMgr manager = manager();
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", true);
        params.put("tier", 1);

        String trueBranch = normalize(manager.renderSql("nested", params).getSql());
        assertEquals("SELECT * FROM sample WHERE enabled = #{enabled} AND tier <= 1", trueBranch);

        params.put("enabled", false);
        String falseBranch = normalize(manager.renderSql("nested", params).getSql());
        assertEquals("SELECT * FROM sample WHERE enabled = false", falseBranch);
    }

    @Test
    void expandsForEachIntoUniqueBoundParameters() {
        SqlXmlMgr manager = manager();
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(4, 7, 9));

        RenderedSql rendered = manager.renderSql("ids", params);
        PreparedSql prepared = ParameterBinder.prepare(rendered);

        assertEquals("SELECT * FROM sample WHERE id IN (?, ?, ?)", normalize(prepared.getSql()));
        assertArrayEquals(new Object[]{4, 7, 9}, prepared.getParams());
        assertEquals(Arrays.asList(4, 7, 9), params.get("ids"));
    }

    @Test
    void rejectsElseOutsideIf() {
        SqlXmlMgr manager = manager();

        assertThrows(IllegalArgumentException.class, () -> manager.renderSql("bad", new HashMap<String, Object>()));
    }

    private static String normalize(String sql) {
        return sql.trim().replaceAll("\\s+", " ");
    }

    private static SqlXmlMgr manager() {
        SqlXmlMgr manager = new SqlXmlMgr();
        manager.init("sql");
        return manager;
    }
}
