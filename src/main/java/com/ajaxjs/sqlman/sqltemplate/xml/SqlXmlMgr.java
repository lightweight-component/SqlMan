package com.ajaxjs.sqlman.sqltemplate.xml;

import com.ajaxjs.sqlman.model.PreparedSql;
import com.ajaxjs.sqlman.sqltemplate.ExpressionEvaluator;
import com.ajaxjs.sqlman.sqltemplate.ParameterBinder;
import com.ajaxjs.sqlman.sqltemplate.SqlXmlDomTemplate;
import com.ajaxjs.sqlman.sqltemplate.express_parser.JSqlParserExpressionEvaluator;
import com.ajaxjs.sqlman.sqltemplate.model.RenderedSql;
import com.ajaxjs.sqlman.sqltemplate.model.SqlXmlResource;
import com.ajaxjs.sqlman.sqltemplate.model.SqlXmlStatement;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.XmlHelper;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads SQL XML resources and manages their cached {@code sqlId -> DOM}
 * statements. Call {@link #init()} or {@link #init(String)} before rendering.
 */
@Slf4j
public class SqlXmlMgr {
    private final ExpressionEvaluator expressionEvaluator;

    private volatile Map<String, SqlXmlDomTemplate> statements = Collections.emptyMap();

    /**
     * Creates a manager using the JSqlParser condition evaluator.
     */
    public SqlXmlMgr() {
        this(new JSqlParserExpressionEvaluator());
    }

    /**
     * @param expressionEvaluator evaluator used by {@code <if test="...">}.
     */
    public SqlXmlMgr(ExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator == null)
            throw new IllegalArgumentException("SQL template expression evaluator must not be null");

        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Loads XML files recursively below the default classpath {@code sql}
     * directory.
     */
    public void init() {
        init("sql");
    }

    /**
     * Loads XML files recursively below one logical classpath directory.
     * Ordinary directories and ordinary JAR files are supported.
     *
     * @param dirName e.g. {@code sql} or {@code myapp/sql}.
     */
    public synchronized void init(String dirName) {
        List<SqlXmlResource> resources = ClasspathSqlXmlScanner.scan(dirName);

        if (resources.isEmpty())
            throw new IllegalArgumentException("No SQL XML resources found below classpath directory: " + dirName);

        Map<String, SqlXmlDomTemplate> loaded = new LinkedHashMap<>();

        for (SqlXmlResource resource : resources)
            loadResource(resource, loaded);

        statements = Collections.unmodifiableMap(loaded);
    }

    /**
     * Renders one SQL statement and returns the SQL template plus its immutable
     * effective parameter map. {@link ParameterBinder} converts the result
     * into JDBC SQL and ordered bindings.
     *
     * @param sqlId  statement id from {@code <sql id="...">}.
     * @param params caller values for dynamic conditions and placeholders.
     */
    public RenderedSql renderSql(String sqlId, Map<String, Object> params) {
        SqlXmlDomTemplate statement = statements.get(sqlId);

        if (statement == null)
            throw new IllegalArgumentException("SQL template not found: " + sqlId);

        return statement.render(params);
    }

    /**
     * Renders one XML statement and compiles it into JDBC SQL and ordered
     * parameter values.
     *
     * @param sqlId statement id from {@code <sql id="...">}
     * @param params caller values for dynamic conditions and placeholders
     * @return prepared JDBC SQL ready to be supplied to an {@code Action}
     */
    public PreparedSql prepareSql(String sqlId, Map<String, Object> params) {
        return ParameterBinder.prepare(renderSql(sqlId, params));
    }

    private void loadResource(SqlXmlResource resource, Map<String, SqlXmlDomTemplate> loaded) {
        if (resource.xml == null)
            throw new IllegalArgumentException("SQL XML content must not be null: " + resource.source);

        Element root = XmlHelper.getRoot(resource.xml);

        if (!"mapper".equals(root.getNodeName()))
            throw new IllegalArgumentException("SQL XML root element must be <mapper>: " + resource.source);

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) child;

            if (!"sql".equals(element.getNodeName()))
                throw new IllegalArgumentException("Unsupported root element <" + element.getNodeName() + "> in " + resource.source);

            String id = XmlHelper.getNodeAttribute(element, "id");

            if (ObjectHelper.isEmptyText(id))
                throw new IllegalArgumentException("SQL template is missing id: " + resource.source);

            SqlXmlDomTemplate previous = loaded.put(id, SqlXmlDomTemplate.fromStatement(new SqlXmlStatement(element, resource.source), expressionEvaluator));

            if (previous != null)
                log.warn("Duplicate SQL template id [{}]; [{}] is overridden by [{}]", id,
                        previous.getSource(), resource.source);
        }
    }
}
