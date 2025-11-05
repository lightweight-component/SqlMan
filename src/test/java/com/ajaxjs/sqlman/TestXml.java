//package com.ajaxjs.sqlman;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//
//import static com.ajaxjs.util.ObjectHelper.mapOf;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class TestXml extends BaseTest {
//    @Test
//    void test() {
//        int result;
//        result = new Sql(conn).inputXml("foo").queryOne(int.class); // fetch the first one
//        System.out.println(result);
//        assertTrue(result > 0);
//
//        result = new Sql(conn).inputXml("foo-2", 1).queryOne(int.class);
//        assertEquals(1, result);
//
//        result = new Sql(conn).inputXml("foo-3", mapOf("tableName", "shop_address", "stat", 1)).queryOne(int.class);
//        assertEquals(1, result);
//
//        result = new Sql(conn).inputXml("foo-4", mapOf("tableName", "shop_address", "abc", 2), 1).queryOne(int.class);
//        System.out.println(result); // TODO, should be return 0
//    }
//
//    @Test
//    void testTag() {
//        int result;
//        result = new Sql(conn).inputXml("foo-5", mapOf("type", "address")).queryOne(int.class); // fetch the first one
//        System.out.println(result);
//        assertTrue(result > 0);
//
//        result = new Sql(conn).inputXml("foo-5", mapOf("type", "article", "tableName", "article")).queryOne(int.class); // fetch the first one
//        System.out.println(result);
//        assertTrue(result > 0);
//    }
//
//    static String w() {
//        return " id = 1";
//    }
//
//    @Test
//    void testCallJava() {
//        int result;
//        result = new Sql(conn).inputXml("foo-6", mapOf("tableName", "shop_address")).queryOne(int.class); // fetch the first one
//        System.out.println(result);
//        assertTrue(result > 0);
//    }
//
//    @Test
//    void testIfElse() {
//        StandardEvaluationContext context = new StandardEvaluationContext();
//        context.setVariable("user", new User("John"));
//
//        String template = "Hello, <if test=\"user.name == 'John'\"><b>Welcome back John!</b><else>Welcome Guest!</else></if>";
//
//        String output = SmallMyBatis.evaluate(template, context);
//        System.out.println(output); // 输出: Hello, <b>Welcome back John!</b>
//    }
//
//    // 示例用户类
//    static class User {
//        private String name;
//
//        public User(String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//    }
//}
