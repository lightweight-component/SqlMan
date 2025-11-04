package com.ajaxjs.sqlman.v1;

import com.ajaxjs.sqlman.model.DatabaseVendor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A bean object for paging.
 */
@Data
@Slf4j
public class Pager {
    private int start = 0;

    private int limit = 12;

    /**
     * 当前第几页
     */
    private int currentPage;

    /**
     * 统计总数的 SQL
     */
    private String countTotal;

    /**
     * 分页 SQL
     */
    private String pageSql;

    private DatabaseVendor databaseVendor = DatabaseVendor.MYSQL;

    /**
     * 分页
     *
     * @param sql 普通 SELECT 语句
     */
    public void parse(String sql) {
        Select selectStatement;

        try {
            selectStatement = (Select) CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.warn("Parsed Paging SQL error. {}", sql);
            throw new RuntimeException("Parsed Paging SQL error.", e);
        }

        SelectBody selectBody = selectStatement.getSelectBody();

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;

            // 设置分页语句
//            Limit limitObj = new Limit();
//            limitObj.setRowCount(new LongValue(limit));
//            limitObj.setOffset(new LongValue(start));
//            plainSelect.setLimit(limitObj);

//            pageSql = selectStatement.toString();
            if (databaseVendor == DatabaseVendor.MYSQL)
                pageSql = sql + " LIMIT " + start + ", " + limit;
            else if (databaseVendor == DatabaseVendor.DERBY)
                pageSql = sql + " OFFSET " + start + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
            else
                throw new DataAccessException("TODO: add db vendor");

            // 移除 排序 语句
            if (sql.toUpperCase().contains("ORDER BY")) {
                List<OrderByElement> orderBy = plainSelect.getOrderByElements();

                if (orderBy != null)
                    plainSelect.setOrderByElements(null);
            }

            Function countFunc = new Function();// 创建一个 count 函数的表达式
            countFunc.setName("COUNT");
            countFunc.setParameters(new ExpressionList(new AllColumns()));

            List<SelectItem> selectItems = plainSelect.getSelectItems();// 替换所有的 Select Item
            selectItems.clear();
            selectItems.add(new SelectExpressionItem(countFunc));

            countTotal = selectStatement.toString();
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            List<SelectBody> selectBodies = setOperationList.getSelects();

            /*
             * 我们还考虑了 SQL 查询语句中使用了 SetOperationList 的情况，这时需要对每个 SELECT 子查询都进行分页，同时修改 FROM
             * 部分的表名，以避免语法错误。
             */
            selectBodies.forEach(selectItem -> {
                if (selectItem instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectItem;
                    Limit limitObj = new Limit();
                    limitObj.setRowCount(new LongValue(limit));
                    limitObj.setOffset(new LongValue(start));
                    plainSelect.setLimit(limitObj);

//                    if (plainSelect.getFromItem() != null) {
                    // modify the original table by adding an alias
//						plainSelect.getFromItem().setAlias(new Table("original_table_alias"));
//                    }
                }
            });

            countTotal = selectStatement.toString();
        }
    }

    /* -------------------------- INPUT ------------------*/

    private static final String[] PAGE_SIZE = new String[]{"pageSize", "rows", "limit"};

    private static final String[] PAGE_NO = new String[]{"pageNo", "page"};

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 12;

    /**
     * 获取分页参数
     *
     * @param req 判断分页参数，兼容 MySQL or 页面两者。最后统一使用 start/limit
     */
    public void getParams(HttpServletRequest req) {
        int start, limit;
        Integer pageNo;

        if (req == null) {// 可能在测试
            start = 0;
            limit = DEFAULT_PAGE_SIZE;

            pageNo = 1;
        } else {
            Integer pageSize = get(req, PAGE_SIZE);
            limit = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
            pageNo = get(req, PAGE_NO);

            if (pageNo != null)
                start = pageNo2start(pageNo, limit);
            else if (req.getParameter("start") != null)
                start = Integer.parseInt(req.getParameter("start"));
            else
                start = 0;
        }

        this.start = start;
        this.limit = limit;
        this.currentPage = pageNo == null ? 0 : pageNo;
    }

    /**
     * 根据 HttpServletRequest 和字符串数组返回一个整数。
     *
     * @param req   请求对象
     * @param maybe 字符串数组，包含可能的参数名
     * @return 返回一个整数，如果参数存在且为整数，则返回对应的整数值；否则返回 null
     */
    private static Integer get(HttpServletRequest req, String[] maybe) {
        for (String m : maybe) {
            if (req.getParameter(m) != null)
                return Integer.parseInt(req.getParameter(m));
        }

        return null;
    }

    /**
     * 将页码和每页数量转换为起始位置
     * pageSize 转换为 MySQL 的 start 分页
     *
     * @param pageNo 页码
     * @param limit  每页数量
     * @return 起始位置
     */
    public static int pageNo2start(int pageNo, int limit) {
        int start = (pageNo - 1) * limit;

        return (start < 0) ? 0 : start;
    }
}
