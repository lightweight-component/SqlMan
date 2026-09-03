package com.ajaxjs.sqlman.xml;

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
 * Renders SQL templates stored as XML DOM nodes.
 *
 * <p>Supported dynamic elements are {@code if}, {@code else}, and
 * {@code forEach}. The renderer never mutates its cached DOM, so one template
 * instance can safely render separate requests concurrently.</p>
 */
public class SqlXmlDomTemplate {
    private static final Pattern PLACEHOLDER = Pattern.compile("(#\\{|\\$\\{)(.*?)(})");

    private final Map<String, Element> statements;
    private final SqlTemplateExpressionEvaluator expressionEvaluator;

    /**
     * Loads all direct {@code <sql id="...">} elements from an XML document.
     *
     * @param xml XML template text.
     */
    public SqlXmlDomTemplate(String xml, SqlTemplateExpressionEvaluator expressionEvaluator) {
        if (xml == null)
            throw new IllegalArgumentException("XML template must not be null");

        if (expressionEvaluator == null)
            throw new IllegalArgumentException("SQL template expression evaluator must not be null");

        Element root = XmlHelper.getRoot(xml);
        Map<String, Element> loaded = new LinkedHashMap<>();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) child;

            if (!"sql".equals(element.getNodeName()))
                throw new IllegalArgumentException("Unsupported root element: " + element.getNodeName());

            String id = XmlHelper.getNodeAttribute(element, "id");

            if (ObjectHelper.isEmptyText(id))
                throw new IllegalArgumentException("SQL template is missing id");

            if (loaded.put(id, element) != null)
                throw new IllegalArgumentException("Duplicate SQL template id: " + id);
        }

        statements = Collections.unmodifiableMap(loaded);
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Renders a statement by id. The returned parameter map contains both the
     * caller values and generated bindings for {@code forEach} items.
     *
     * @param id     statement id.
     * @param params values used by dynamic conditions and placeholders.
     * @return the rendered SQL template and effective parameters.
     */
    public RenderedSql render(String id, Map<String, Object> params) {
        Element statement = statements.get(id);

        if (statement == null)
            throw new IllegalArgumentException("SQL template not found: " + id);

        RenderState state = new RenderState(params);
        StringBuilder sql = new StringBuilder();
        renderChildren(statement, state.getParams(), Collections.emptyMap(), state, sql);

        return new RenderedSql(sql.toString(), state.getParams());
    }

    /**
     * Returns the cached statement ids.
     */
    public Set<String> getStatementIds() {
        return statements.keySet();
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
        if (test == null || test.trim().isEmpty())
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
        } else if (elseIndex != -1) {
            renderChildren(children.item(elseIndex), context, aliases, state, sql);
        }
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
        Iterable<?> items = asIterable(collection, collectionName);
        boolean renderedAny = false;
        int index = 0;

        for (Object item : items) {
            Map<String, Object> itemContext = new HashMap<>(context);
            Map<String, String> itemAliases = new HashMap<>(aliases);
            addForEachBinding(itemName, item, itemContext, itemAliases, state);
            addForEachBinding(indexName, index, itemContext, itemAliases, state);

            StringBuilder itemSql = new StringBuilder();
            renderChildren(forEach, itemContext, itemAliases, state, itemSql);

            if (!itemSql.toString().trim().isEmpty()) {
                if (!renderedAny) {
                    sql.append(open);
                    renderedAny = true;
                } else
                    sql.append(separator);

                sql.append(itemSql);
            }

            index++;
        }

        if (renderedAny)
            sql.append(close);
    }

    private static void addForEachBinding(String name, Object value, Map<String, Object> context, Map<String, String> aliases, RenderState state) {
        int nextBinding = state.getNextBinding();
        String generatedName = "__sqlman_foreach_" + nextBinding++;
        state.setNextBinding(nextBinding);

        context.put(name, value);
        aliases.put(name, generatedName);
        state.getParams().put(generatedName, value);
    }

    private static Iterable<?> asIterable(Object value, String collectionName) {
        if (value instanceof Iterable<?>)
            return (Iterable<?>) value;

        if (value instanceof Map<?, ?>)
            return ((Map<?, ?>) value).entrySet();

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
