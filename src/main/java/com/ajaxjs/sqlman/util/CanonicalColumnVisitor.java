package com.ajaxjs.sqlman.util;

import com.ajaxjs.util.StrUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * 规范化列访问器，继承自 TablesNamesFinder，用于为 SQL 语句中的列添加表名前缀或别名。
 * 该访问器会遍历 SQL 语句中的各个部分，包括 WHERE 子句、JOIN 条件、排序和分组等，
 * 为缺少表名前缀的列添加指定的表名或其别名，同时为表添加合适的别名。
 * 在遍历过程中，还会收集关联的表信息。
 */
public class CanonicalColumnVisitor extends TablesNamesFinder {
    private final String tableName;
    private final Map<String, FromItem> associatedTables = new HashMap<>();
    private final Function<String, String> aliaFunction = asAliasFunction(associatedTables);

    /**
     * 构造CanonicalColumnVisitor实例，关联表映射使用null值，适用于对完整SQL语句的处理
     *
     * @param tableName 表名，用于为列名添加表名前缀
     */
    public  CanonicalColumnVisitor(String tableName) {
        this(tableName, null);
    }

    /**
     * 构造CanonicalColumnVisitor实例
     *
     * @param tableName    表名，用于为列名添加表名前缀
     * @param joinedTables 已连接的表映射，键为表名，值为对应的FromItem对象
     */
    CanonicalColumnVisitor(String tableName, Map<String, FromItem> joinedTables) {
        this.tableName = tableName;

        if (null != joinedTables && !joinedTables.isEmpty())
            this.associatedTables.putAll(joinedTables);

        init(true);
    }

    @Override
    public void visit(Column column) {
        /* 为列名增加表名前缀，优先使用表的别名 */
        Table table = column.getTable();

        if (StrUtil.hasText(tableName)) {
            if (null == table) {
                String aliasName = aliaFunction.apply(tableName);
                column.setTable(new Table(null != aliasName ? aliasName : tableName));
            }
        }

        if (null != table) {
            Alias alias = table.getAlias();

            if (null == alias) {
                String aliasName = aliaFunction.apply(table.getName());

                if (null != aliasName && !aliasName.equals(table.getName())) {
                    alias = new Alias(aliasName);
                    table.setAlias(alias);
                }
            }
        }

        super.visit(column);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        doVisitForCollectAssociatedTable(associatedTables, plainSelect.getFromItem(), plainSelect.getJoins());
        super.visit(plainSelect);

        // 处理JOIN条件、WHERE子句、ORDER BY和GROUP BY等部分
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                for (Expression exp : join.getOnExpressions())
                    exp.accept(this);
            }
        }

        if (plainSelect.getWhere() != null)
            plainSelect.getWhere().accept(this);

        if (plainSelect.getOrderByElements() != null) {
            for (OrderByElement item : plainSelect.getOrderByElements())
                item.getExpression().accept(this);
        }

        if (plainSelect.getGroupBy() != null)
            plainSelect.getGroupBy().getGroupByExpressionList().accept(this);
    }

    // 其他SQL语句类型的visit方法实现...
    @Override
    public void visit(Update update) {
        doVisitForCollectAssociatedTable(associatedTables, update.getTable(), update.getJoins());
        super.visit(update);
        // 处理UPDATE语句的更新列和表达式
        update.getUpdateSets().forEach(us -> {
            us.getColumns().forEach(c -> c.accept(this));
            us.getExpressions().forEach(e -> e.accept(this));
        });
    }

    static class AliasFunction implements Function<String, String> {
        private final Map<String, FromItem> joinedTables;

        AliasFunction(Map<String, FromItem> joinedTables) {
            this.joinedTables = null == joinedTables ? Collections.emptyMap() : joinedTables;
        }

        @Override
        public String apply(String name) {
            FromItem fromItem = null == name ? null : joinedTables.get(name);
            if (null == fromItem)
                return null;

            Alias alias = fromItem.getAlias();

            return (null == alias || alias.getName() == null) ? name : alias.getName();
        }

        // hashCode、equals和toString方法实现...
    }

    /**
     * 创建一个用于获取表别名的函数
     * 1. 根据输入的表名，从已JOIN表映射中查找对应的FromItem对象
     * 2. 若找到FromItem且有别名，则返回别名
     * 3. 若找到FromItem但无别名，则返回原表名
     * 4. 若未找到FromItem，则返回null
     */
    static Function<String, String> asAliasFunction(Map<String, FromItem> joinedTables) {
        return new AliasFunction(joinedTables);
    }

    /**
     * 遍历FromItem和JOIN子句，将其中的表信息添加到已连接表映射中
     */
    private static void doVisitForCollectAssociatedTable(Map<String, FromItem> joinedTables, FromItem fromItem, List<Join> joins) {
        if (null != fromItem)
            joinedTables.put(tableNameOrAliasOf(fromItem), fromItem);

        if (null != joins)
            joins.stream().map(Join::getRightItem).forEach(i -> joinedTables.put(tableNameOrAliasOf(i), i));
    }

    /**
     * 获取 FromItem 对象对应的表名或别名
     */
    private static String tableNameOrAliasOf(FromItem fromItem) {
        if (null == fromItem)
            return null;

        if (fromItem instanceof Table)
            return ((Table) fromItem).getName();

        Alias alias = fromItem.getAlias();

        return (null == alias) ? null : alias.getName();
    }


    // Delete、Upsert等其他语句类型的visit方法实现...

    /**
     * 规范化SQL语句对应的Statement对象
     * 解析传入的SQL语句，获取对应的Statement对象，并使用CanonicalColumnVisitor对其进行访问，
     * 为没有指定表名的字段名自动加上 tableName 指定的表名前缀
     */
    private static Statement normalizeStatement(String tableName, String sql) throws JSQLParserException {
        Statement statement = parseStatement(sql);
        return normalizeStatement(tableName, statement);
    }

    /**
     * 规范化SQL语句对应的Statement对象
     * 使用CanonicalColumnVisitor对传入的Statement对象进行访问，
     * 为没有指定表名的字段名自动加上 tableName 指定的表名前缀
     */
    private static Statement normalizeStatement(String tableName, Statement statement) {
        statement.accept(new CanonicalColumnVisitor(tableName));
        return statement;
    }

    /**
     * 解析 SQL 语句字符串并返回对应的 Statement 对象
     */
    public static Statement parseStatement(String sql) throws JSQLParserException {
        // 解析SQL语句的实现
        Statement parse = CCJSqlParserUtil.parse(sql);
        return parse;
    }

    /**
     * 规范化SQL字符串
     */
    public static String normalizeSql(String tableName, String sql) {
        try {
            return normalizeStatement(tableName, sql).toString();
        } catch (JSQLParserException e) {
            // 解析SQL语句失败，不做任何处理返回原值
            return sql;
        }
    }

    public static void main(String[] args) throws JSQLParserException {
        String longSql = "  SELECT " +
                "        user_id, " +
                "        COUNT(order_id) AS total_orders, " +
                "        SUM(total_amount) AS lifetime_value,\n" +
                "        MIN(order_date) AS first_order_date, " +
                "        MAX(order_date) AS last_order_date,\n" +
                "        AVG(total_amount) AS avg_order_value\n" +
                "    FROM orders " +
                "    WHERE order_date >= '2023-01-01'\n" +
                "      AND status IN ('completed', 'shipped')\n" +
                "    GROUP BY user_id";


        normalizeSql("t_user", longSql);
        System.out.println("ok");
    }
}
