package com.ajaxjs.sqlman.xml;

import com.ajaxjs.sqlman.SqlTemplateParameterBinder;
import com.ajaxjs.sqlman.SmallMyBatis;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TestSqlXmlDomTemplateLoading {
    @Test
    void loadsCustomDirectoryInStableOrderAndPreparesBindings() {
        SqlXmlMgr template = manager("template-scan");

        assertEquals("SELECT 'second'", normalize(template.renderSql("duplicate", null).getSql()));

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("tableName", "sample_table");
        params.put("name", "Ada");
        SmallMyBatis.PreparedSql prepared = SqlTemplateParameterBinder.prepare(template.renderSql("bound", params), "ACTIVE");

        assertEquals("SELECT * FROM sample_table WHERE name = ? AND status = ?", normalize(prepared.getSql()));
        assertArrayEquals(new Object[]{"Ada", "ACTIVE"}, prepared.getParams());
        assertEquals("Ada", params.get("name"));

        params.put("tableName", "sample_table; DROP TABLE sample_table");
        assertThrows(IllegalArgumentException.class, () -> SqlTemplateParameterBinder.prepare(template.renderSql("bound", params), "ACTIVE"));
    }

    @Test
    void rendersMapForEachWithKeyAsIndexAndValueAsItem() {
        SqlXmlMgr template = manager("template-scan");
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("A", 3);
        values.put("B", 7);
        params.put("values", values);

        SmallMyBatis.PreparedSql prepared = SqlTemplateParameterBinder.prepare(template.renderSql("map-values", params));

        assertEquals("VALUES (?, ?), (?, ?)", normalize(prepared.getSql()));
        assertArrayEquals(new Object[]{"A", 3, "B", 7}, prepared.getParams());
        assertEquals(values, params.get("values"));
    }

    @Test
    void loadsDefaultSqlDirectoryAndRejectsMissingDirectoryOrInvalidXml() {
        SqlXmlMgr template = manager(null);

        assertEquals("SELECT * FROM sample WHERE enabled = false", normalize(template.renderSql("nested", mapOf(false, 1)).getSql()));
        SqlXmlMgr missing = new SqlXmlMgr();
        assertThrows(IllegalArgumentException.class, () -> missing.init("does-not-exist"));
    }

    @Test
    void scansAnOrdinaryJarFromTheContextClassLoader() throws Exception {
        Path jar = Files.createTempFile("sql-template", ".jar");
        ClassLoader previous = Thread.currentThread().getContextClassLoader();

        try {
            writeJar(jar);

            try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, previous)) {
                Thread.currentThread().setContextClassLoader(loader);
                SqlXmlMgr template = manager("jar-scan");

                assertEquals("SELECT #{id}", normalize(template.renderSql("jar-statement", null).getSql()));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
            Files.deleteIfExists(jar);
        }
    }

    @Test
    void rendersCachedDomConcurrentlyWithoutSharedState() throws Exception {
        SqlXmlMgr template = manager("template-scan");
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            Callable<String> enabled = () -> normalize(template.renderSql("conditional", mapOf(true, 5)).getSql());
            Callable<String> disabled = () -> normalize(template.renderSql("conditional", mapOf(false, 5)).getSql());
            Future<String> first = executor.submit(enabled);
            Future<String> second = executor.submit(disabled);

            assertEquals("SELECT 1 WHERE id = #{id}", first.get());
            assertEquals("SELECT 1 WHERE id IS NULL", second.get());
        } finally {
            executor.shutdownNow();
        }
    }

    private static Map<String, Object> mapOf(boolean enabled, int id) {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", enabled);
        params.put("id", id);
        return params;
    }

    private static SqlXmlMgr manager(String directory) {
        SqlXmlMgr manager = new SqlXmlMgr();

        if (directory == null)
            manager.init();
        else
            manager.init(directory);

        return manager;
    }

    private static void writeJar(Path jar) throws Exception {
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("jar-scan/"));
            output.closeEntry();
            output.putNextEntry(new JarEntry("jar-scan/statement.xml"));
            output.write(("<?xml version=\"1.0\"?><mapper><sql id=\"jar-statement\">SELECT #{id}</sql></mapper>")
                    .getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
    }

    private static String normalize(String sql) {
        return sql.trim().replaceAll("\\s+", " ");
    }
}
