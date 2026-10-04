package com.ajaxjs.sqlman.sqltemplate;

import com.ajaxjs.sqlman.sqltemplate.express_parser.JSqlParserExpressionEvaluator;
import com.ajaxjs.sqlman.sqltemplate.model.RenderState;
import com.ajaxjs.sqlman.sqltemplate.model.RenderedSql;
import com.ajaxjs.sqlman.sqltemplate.model.SqlXmlStatement;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiled SQL template backed by one immutable XML DOM statement.
 * <p>
 * Templates can originate from a {@code <sql>} node in a resource file or be
 * compiled from an inline SQL fragment through {@link #compile(String)}. This
 * class does not load resources, manage statement ids, or bind JDBC placeholders.
 * </p>
 */
public class SqlXmlDomTemplate {
    private static final Pattern PLACEHOLDER = Pattern.compile("(#\\{|\\$\\{)(.*?)(})");

    private static final Pattern DYNAMIC_ELEMENT = Pattern.compile("<\\s*/?\\s*(if|else|forEach)\\b");

    private static final String INLINE_SOURCE = "<inline SQL template>";

    private final SqlXmlStatement statement;

    private final ExpressionEvaluator expressionEvaluator;

    private SqlXmlDomTemplate(SqlXmlStatement statement, ExpressionEvaluator expressionEvaluator) {
        if (statement == null)
            throw new IllegalArgumentException("SQL XML statement must not be null");

        if (expressionEvaluator == null)
            throw new IllegalArgumentException("SQL template expression evaluator must not be null");

        this.statement = statement;
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Compiles an inline SQL fragment containing optional {@code <if>},
     * {@code <else>}, and {@code <forEach>} nodes.
     *
     * @param sqlTemplate an XML-compatible SQL fragment without a root element
     * @return the compiled reusable template
     * @throws IllegalArgumentException if the template is {@code null} or not a valid XML fragment
     */
    public static SqlXmlDomTemplate compile(String sqlTemplate) {
        return compile(sqlTemplate, new JSqlParserExpressionEvaluator());
    }

    /**
     * Compiles an inline SQL fragment using the supplied condition evaluator.
     *
     * @param sqlTemplate         an XML-compatible SQL fragment without a root element
     * @param expressionEvaluator evaluator used by {@code <if test="...">}
     * @return the compiled reusable template
     * @throws IllegalArgumentException if an argument is {@code null} or the template is not a valid XML fragment
     */
    public static SqlXmlDomTemplate compile(String sqlTemplate, ExpressionEvaluator expressionEvaluator) {
        if (sqlTemplate == null)
            throw new IllegalArgumentException("Inline SQL template must not be null");

        Element element = XmlHelper.getRoot("<sql>" + sqlTemplate + "</sql>");

        return new SqlXmlDomTemplate(new SqlXmlStatement(element, INLINE_SOURCE), expressionEvaluator);
    }

    /**
     * Determines whether SQL contains a supported XML dynamic element and must
     * be compiled before it can be rendered.
     *
     * @param sql the SQL text to inspect
     * @return {@code true} if a supported dynamic element is present
     */
    public static boolean hasDynamicElement(String sql) {
        return sql != null && DYNAMIC_ELEMENT.matcher(sql).find();
    }

    /**
     * Creates a reusable template for a statement already parsed from an XML resource.
     *
     * @param statement           parsed SQL statement descriptor
     * @param expressionEvaluator evaluator used by {@code <if test="...">}
     * @return a DOM-backed SQL template
     */
    public static SqlXmlDomTemplate fromStatement(SqlXmlStatement statement, ExpressionEvaluator expressionEvaluator) {
        return new SqlXmlDomTemplate(statement, expressionEvaluator);
    }

    /**
     * Returns the inline marker or XML resource location from which this template originated.
     *
     * @return template source location
     */
    public String getSource() {
        return statement.source;
    }

    /**
     * Renders this template using the supplied dynamic condition and placeholder values.
     *
     * @param params caller values, or {@code null} when no values are required
     * @return rendered SQL and its immutable effective parameter map
     */
    public RenderedSql render(Map<String, Object> params) {
        RenderState state = new RenderState(params);
        StringBuilder sql = new StringBuilder();

        // Standard DOM implementations do not guarantee safe concurrent
        // traversal. State remains request-local while DOM access is guarded.
        synchronized (statement.renderLock) {
            renderChildren(statement.element, state.getParams(), Collections.emptyMap(), state, sql);
        }

        return new RenderedSql(sql.toString(), state.getParams());
    }

    private void renderChildren(Node parent, Map<String, Object> context, Map<String, String> aliases, RenderState state, StringBuilder sql) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++)
            renderNode(children.item(i), context, aliases, state, sql);
    }

    private void renderNode(Node node, Map<String, Object> context, Map<String, String> aliases, RenderState state, StringBuilder sql) {
        short type = node.getNodeType();

        if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
            sql.append(rewritePlaceholders(node.getNodeValue(), aliases));

            return;
        }

        if (type != Node.ELEMENT_NODE)
            return;

        Element element = (Element) node;

        switch (element.getNodeName()) {
            case "if":
                renderIf(element, context, aliases, state, sql);
                break;
            case "forEach":
                renderForEach(element, context, aliases, state, sql);
                break;
            case "else":
                throw new IllegalArgumentException("<else> must be a direct child of <if>");
            default:
                throw new IllegalArgumentException("Unsupported dynamic SQL element: " + element.getNodeName());
        }
    }

    private void renderIf(Element ifElement, Map<String, Object> context, Map<String, String> aliases, RenderState state, StringBuilder sql) {
        String test = XmlHelper.getNodeAttribute(ifElement, "test");

        if (ObjectHelper.isEmptyText(test))
            throw new IllegalArgumentException("<if> is missing test");

        boolean useThen = expressionEvaluator.evaluate(test, context);
        NodeList children = ifElement.getChildNodes();
        int elseIndex = -1;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE && "else".equals(child.getNodeName())) {
                if (elseIndex != -1)
                    throw new IllegalArgumentException("<if> may contain at most one direct <else>");

                elseIndex = i;
            }
        }

        if (useThen) {
            int end = elseIndex == -1 ? children.getLength() : elseIndex;

            for (int i = 0; i < end; i++)
                renderNode(children.item(i), context, aliases, state, sql);
        } else if (elseIndex != -1)
            renderChildren(children.item(elseIndex), context, aliases, state, sql);
    }

    private void renderForEach(Element forEach, Map<String, Object> context, Map<String, String> aliases, RenderState state, StringBuilder sql) {
        String collectionName = requiredAttribute(forEach, "collection");
        Object collection = context.get(collectionName);

        if (collection == null)
            return;

        String itemName = optionalAttribute(forEach, "item", "item");
        String indexName = optionalAttribute(forEach, "index", "index");
        String open = optionalAttribute(forEach, "open", "");
        String separator = optionalAttribute(forEach, "separator", "");
        String close = optionalAttribute(forEach, "close", "");
        boolean renderedAny = false;

        if (collection instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) collection).entrySet())
                renderedAny = renderForEachItem(forEach, itemName, entry.getValue(), indexName, entry.getKey(), open, separator,
                        aliases, context, state, sql, renderedAny);
        } else {
            int index = 0;

            for (Object item : asIterable(collection, collectionName)) {
                renderedAny = renderForEachItem(forEach, itemName, item, indexName, index, open, separator,
                        aliases, context, state, sql, renderedAny);
                index++;
            }
        }

        if (renderedAny)
            sql.append(close);
    }

    private boolean renderForEachItem(Element forEach, String itemName, Object item, String indexName, Object index,
                                      String open, String separator, Map<String, String> aliases, Map<String, Object> context,
                                      RenderState state, StringBuilder sql, boolean renderedAny) {
        Map<String, Object> itemContext = new HashMap<>(context);
        Map<String, String> itemAliases = new HashMap<>(aliases);
        addForEachBinding(itemName, item, itemContext, itemAliases, state);
        addForEachBinding(indexName, index, itemContext, itemAliases, state);

        StringBuilder itemSql = new StringBuilder();
        renderChildren(forEach, itemContext, itemAliases, state, itemSql);
        String renderedItem = itemSql.toString().trim();

        if (renderedItem.isEmpty())
            return renderedAny;

        if (!renderedAny)
            sql.append(open);
        else
            sql.append(separator);

        sql.append(renderedItem);

        return true;
    }

    private static void addForEachBinding(String name, Object value, Map<String, Object> context, Map<String, String> aliases, RenderState state) {
        String generatedName = "__sqlman_foreach_" + state.getNextBinding();
        state.setNextBinding(state.getNextBinding() + 1);
        context.put(name, value);
        aliases.put(name, generatedName);
        state.getParams().put(generatedName, value);
    }

    private static Iterable<?> asIterable(Object value, String collectionName) {
        if (value instanceof Iterable<?>)
            return (Iterable<?>) value;

        if (value.getClass().isArray()) {
            List<Object> values = new ArrayList<>();
            int length = Array.getLength(value);

            for (int i = 0; i < length; i++)
                values.add(Array.get(value, i));

            return values;
        }

        throw new IllegalArgumentException("<forEach> collection is not iterable: " + collectionName);
    }

    private static String rewritePlaceholders(String text, Map<String, String> aliases) {
        if (aliases.isEmpty())
            return text;

        Matcher matcher = PLACEHOLDER.matcher(text);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            String alias = aliases.get(matcher.group(2));

            if (alias == null)
                matcher.appendReplacement(output, Matcher.quoteReplacement(matcher.group()));
            else
                matcher.appendReplacement(output, Matcher.quoteReplacement(matcher.group(1) + alias + matcher.group(3)));
        }

        matcher.appendTail(output);

        return output.toString();
    }

    private static String requiredAttribute(Element element, String name) {
        String value = XmlHelper.getNodeAttribute(element, name);

        if (ObjectHelper.isEmptyText(value))
            throw new IllegalArgumentException("<" + element.getNodeName() + "> is missing " + name);

        return value;
    }

    private static String optionalAttribute(Element element, String name, String defaultValue) {
        String value = XmlHelper.getNodeAttribute(element, name);

        return value == null ? defaultValue : value;
    }
}
