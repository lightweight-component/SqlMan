package com.ajaxjs.data;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

public class TestJSQLParser {
    @Test
    public void test() {
        Select selectStatement = null;

        try {
            selectStatement = (Select) CCJSqlParserUtil.parse("SELECT * FROM widget_config WHERE 1=1 ORDER BY create_date DESC AND stat = 0 AND type = 'LIST'");
            System.out.println(selectStatement);
        } catch (JSQLParserException e) {
           e.printStackTrace();
        }
    }
}
