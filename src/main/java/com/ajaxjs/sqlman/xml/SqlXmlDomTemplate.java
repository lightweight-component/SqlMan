package com.ajaxjs.sqlman.xml;

import com.ajaxjs.sqlman.xml.express_parser.JSqlParserExpressionEvaluator;
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
 * Renders one cached SQL XML DOM statement. It does not load resources, manage
 * statement ids, or bind JDBC placeholders.
 */
public class SqlXmlDomTemplate {
    private static final Pattern PLACEHOLDER = Pattern.compile("(#\\{|\\$\\{)(.*?)(})");

    private final SqlTemplateExpressionEvaluator expressionEvaluator;

    public SqlXmlDomTemplate() {
        this(new JSqlParserExpressionEvaluator());
    }

    public SqlXmlDomTemplate(SqlTemplateExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator == null)
            throw new IllegalArgumentException("SQL template expression evaluator must not be null");

        this.expressionEvaluator = expressionEvaluator;
    }

    RenderedSql render(SqlXmlStatement statement, Map<String, Object> params) {
        RenderState state = new RenderState(params);
        StringBuilder sql = new StringBuilder();

        // Standard DOM implementations do not guarantee safe concurrent
        // traversal. State remains request-local while DOM access is guarded.
        synchronized (statement.renderLock) {
            renderChildren(statement.element, state.getParams(), Collections.<String, String>emptyMap(), state, sql);
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

        if (itemSql.toString().trim().isEmpty())
            return renderedAny;

        if (!renderedAny)
            sql.append(open);
        else
            sql.append(separator);

        sql.append(itemSql);
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
