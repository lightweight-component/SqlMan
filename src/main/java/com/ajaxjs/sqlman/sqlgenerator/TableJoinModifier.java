package com.ajaxjs.sqlman.sqlgenerator;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class TableJoinModifier {
    public static String addLeftJoinWithAutoAlias(String originalSql, TableJoin tableJoin) {
        try {
            return addLeftJoinWithAutoAlias(originalSql, tableJoin.getJoinTableName(), tableJoin.getMainTableJoinColumn(),
                    tableJoin.getJoinedTableJoinColumn(), tableJoin.getFieldsToSelectFromJoinedTable());
        } catch (JSQLParserException e) {
            log.warn("TableJoinModifier", e);
            return originalSql;
        }
    }

    /**
     * 向 SELECT 语句添加 LEFT JOIN，并自动处理别名。
     *
     * @param originalSql                   原始 SELECT 语句
     * @param joinTableName                 要 JOIN 的表名
     * @param mainTableJoinColumn           主表用于连接的列名 (不含别名)
     * @param joinedTableJoinColumn         被连接表用于连接的列名 (不含别名)
     * @param fieldsToSelectFromJoinedTable 要从被连接表选取的字段列表 (不含别名)
     * @return 修改后的 SQL 字符串
     * @throws JSQLParserException 如果解析失败
     */
    public static String addLeftJoinWithAutoAlias(String originalSql,
                                                  String joinTableName,
                                                  String mainTableJoinColumn,
                                                  String joinedTableJoinColumn,
                                                  List<String> fieldsToSelectFromJoinedTable) throws JSQLParserException {
        // 1. 解析原始 SQL
        Statement statement = CCJSqlParserUtil.parse(originalSql);

        // 2. 确保它是一个 SELECT 语句
        if (!(statement instanceof Select)) {
            throw new IllegalArgumentException("Provided SQL is not a SELECT statement.");
        }

        Select selectStatement = (Select) statement;
        SelectBody selectBody = selectStatement.getSelectBody();

        // 3. 确保它是 PlainSelect
        if (!(selectBody instanceof PlainSelect)) {
            throw new IllegalArgumentException("This example only supports modifying PlainSelect statements.");
        }

        PlainSelect plainSelect = (PlainSelect) selectBody;

        // 4. --- 自动推断别名 ---
        // 4.1 获取主表及其别名
        FromItem fromItem = plainSelect.getFromItem();
        String mainTableAlias = null;
        if (fromItem instanceof Table) {
            Alias alias = ((Table) fromItem).getAlias();
            if (alias != null) {
                mainTableAlias = alias.getName();
            } else {
                // 如果 FROM 的主表没有别名，我们尝试一个简单的策略：
                // 使用表名小写作为别名（注意：这可能与后续JOIN的别名冲突！）
                // 更健壮的方法是分析 SELECT 和 WHERE 子句，但这比较复杂。
                // 这里警告一下，并继续。
                mainTableAlias = ((Table) fromItem).getName().toLowerCase();
                System.out.println("Warning: Main table '" + ((Table) fromItem).getName() + "' has no alias. Using '" + mainTableAlias + "'. Risk of conflict.");
                // 或者抛出异常: throw new IllegalStateException("Main table must have an alias for reliable operation.");
            }
        } else {
            // FROM 子句是子查询或其他复杂结构，别名处理会更复杂。
            // 这里简化处理：假设它有别名。
            Alias alias = fromItem.getAlias();
            if (alias != null) {
                mainTableAlias = alias.getName();
            } else {
                throw new IllegalStateException("FROM item is complex and lacks an alias. Cannot determine main table alias reliably.");
            }
        }


        // 4.2 收集所有现有别名，用于为新 JOIN 表生成不冲突的别名
        Set<String> existingAliases = collectAllAliases(plainSelect);

        // 4.3 为主表连接列构造带别名的列名
        String fullMainTableJoinColumn = mainTableAlias + "." + mainTableJoinColumn;


        // 4.4 为被连接表生成别名 (例如 t1, t2, ...)
        String joinedTableAlias = generateUniqueAlias("t", existingAliases);

        // 4.5 为被连接表连接列构造带别名的列名
        String fullJoinedTableJoinColumn = joinedTableAlias + "." + joinedTableJoinColumn;

        // --- 构造新的 LEFT JOIN ---
        Join join = new Join();
        join.setLeft(true);
        join.setOuter(true);
        join.setRight(false);
//        join.setLeftOuter(true); // 设置为 LEFT JOIN

        Table joinTable = new Table(joinTableName);
        joinTable.setAlias(new Alias(joinedTableAlias, false));
        join.setRightItem(joinTable);

        EqualsTo equalsTo = new EqualsTo();
        Column leftJoinColumn = new Column(fullMainTableJoinColumn);
        Column rightJoinColumn = new Column(fullJoinedTableJoinColumn);
        equalsTo.setLeftExpression(leftJoinColumn);
        equalsTo.setRightExpression(rightJoinColumn);
        join.setOnExpression(equalsTo);

        // --- 将新的 JOIN 添加到 SELECT 语句中 ---
        List<Join> currentJoins = plainSelect.getJoins();
        if (currentJoins == null)
            currentJoins = new ArrayList<>();

        currentJoins.add(join);
        plainSelect.setJoins(currentJoins);

        // --- 添加 SELECT 字段 ---
        if (fieldsToSelectFromJoinedTable != null && !fieldsToSelectFromJoinedTable.isEmpty()) {
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            // 确保 SELECT 列表不是 SELECT * (如果是，需要展开，这里简化处理)
            // 假设已经是具体列名列表

            for (String fieldName : fieldsToSelectFromJoinedTable) {
                // 为每个字段创建 SelectExpressionItem
                Column column = new Column(joinedTableAlias + "." + fieldName);
                SelectExpressionItem item = new SelectExpressionItem(column);
                // 可以为字段设置别名，例如 t1.field_name -> field_name 或保持原样
                // item.setAlias(new Alias(fieldName, false)); // 可选：设置别名
                selectItems.add(item);
            }
            plainSelect.setSelectItems(selectItems);
        }

        // 11. 返回修改后的 SQL 字符串
        return selectStatement.toString();
    }

    /**
     * 收集 PlainSelect 中所有已存在的别名。
     *
     * @param plainSelect PlainSelect 对象
     * @return 包含所有别名的 Set
     */
    private static Set<String> collectAllAliases(PlainSelect plainSelect) {
        Set<String> aliases = new HashSet<>();

        // 收集 FROM 子句的别名
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem != null && fromItem.getAlias() != null) {
            aliases.add(fromItem.getAlias().getName().toLowerCase()); // 统一转小写比较
        }

        // 收集 JOIN 子句的别名
        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            for (Join j : joins) {
                if (j.getRightItem() != null && j.getRightItem().getAlias() != null) {
                    aliases.add(j.getRightItem().getAlias().getName().toLowerCase());
                }
            }
        }

        // (可选) 收集 SELECT 列的别名 - 如果担心列别名冲突
        // List<SelectItem> selectItems = plainSelect.getSelectItems();
        // if (selectItems != null) {
        //     for (SelectItem item : selectItems) {
        //         if (item instanceof SelectExpressionItem) {
        //             Alias colAlias = ((SelectExpressionItem) item).getAlias();
        //             if (colAlias != null) {
        //                 aliases.add(colAlias.getName().toLowerCase());
        //             }
        //         }
        //     }
        // }

        return aliases;
    }

    /**
     * 生成一个唯一的别名。
     *
     * @param prefix          别名前缀 (e.g., "t")
     * @param existingAliases 已存在的别名集合
     * @return 唯一的别名
     */
    private static String generateUniqueAlias(String prefix, Set<String> existingAliases) {
        int counter = 1;
        String candidateAlias;

        do {
            candidateAlias = prefix + counter;
            counter++;
        } while (existingAliases.contains(candidateAlias.toLowerCase())); // 比较时转小写

        return candidateAlias;
    }
}
