package com.ajaxjs.sqlman.crud.page;

import com.ajaxjs.sqlman.model.DatabaseVendor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

/**
 * Generate the paging SQL
 */
@Data
@Slf4j
@RequiredArgsConstructor
public class PageControl {
    final DatabaseVendor databaseVendor;

    /**
     * The SQL of list data, before paging
     */
    final String sql;

    final int start;

    final int limit;

    String pagedSql;

    String countSql;

    /**
     * Generate the count SQL
     */
    public void getCount() {
        Select selectStatement = getSelectStatement();
        PlainSelect plainSelect = selectStatement instanceof PlainSelect ? (PlainSelect) selectStatement : null;

        if (plainSelect != null)
            getCount(plainSelect);

        countSql = selectStatement.toString();
    }

    /**
     * Generate the count SQL
     *
     * @param plainSelect Object in JSQLParser
     */
    private void getCount(PlainSelect plainSelect) {

        // 设置分页语句
//            Limit limitObj = new Limit();
//            limitObj.setRowCount(new LongValue(limit));
//            limitObj.setOffset(new LongValue(start));
//            plainSelect.setLimit(limitObj);

//            pageSql = selectStatement.toString();
        if (databaseVendor == DatabaseVendor.MYSQL || databaseVendor == DatabaseVendor.MARIADB || databaseVendor == DatabaseVendor.H2)
            pagedSql = sql + " LIMIT " + start + ", " + limit;
        else if (databaseVendor == DatabaseVendor.POSTGRESQL || databaseVendor == DatabaseVendor.SQL_LITE || databaseVendor == DatabaseVendor.HSQLDB)
            pagedSql = sql + " LIMIT " + limit + " OFFSET " + start;
        else if (databaseVendor == DatabaseVendor.SQL_SERVER || databaseVendor == DatabaseVendor.ORACLE || databaseVendor == DatabaseVendor.DB2 || databaseVendor == DatabaseVendor.DERBY)
            pagedSql = sql + " OFFSET " + start + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
        else
            throw new UnsupportedOperationException("TODO: add db vendor");

        // 移除 排序 语句
        if (sql.toUpperCase().contains("ORDER BY")) {
            List<OrderByElement> orderBy = plainSelect.getOrderByElements();

            if (orderBy != null)
                plainSelect.setOrderByElements(null);
        }

        Function countFunc = new Function();// 创建一个 count 函数的表达式
        countFunc.setName("COUNT");
        countFunc.setParameters(new ExpressionList<>(new AllColumns()));

        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();// 替换所有的 Select Item
        selectItems.clear();
        selectItems.add(new SelectItem<>(countFunc));
    }

    private Select getSelectStatement() {
        Select selectStatement;

        try {
            selectStatement = (Select) CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.warn("Parsed Paging SQL error. {}", sql);
            throw new RuntimeException("Parsed Paging SQL error.", e);
        }

        return selectStatement;
    }
}
