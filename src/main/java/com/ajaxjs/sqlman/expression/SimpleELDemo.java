package com.ajaxjs.sqlman.expression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// --- 6. 测试类 ---
public class SimpleELDemo {
    // 示例 Bean
    public static class Person {
        private String name;
        private int age;
        private Address address;

        public Person(String name, int age, Address address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public Address getAddress() {
            return address;
        }
    }

    public static class Address {
        private String city;

        public Address(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }
    }

    public static void main(String[] args) {
        try {
            // 准备测试数据和上下文
            Address addr = new Address("New York");
            Person person = new Person("Alice", 30, addr);
            Map<String, Object> data = new HashMap<>();
            data.put("person", person);
            data.put("message", "Hello World");
            data.put("numbers", Arrays.asList(10, 20, 30));
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put("status", "active");
            infoMap.put("level", "high");
            data.put("info", infoMap);

            // 测试表达式列表
            String[] expressions = {
                    "person",
                    "person.name",
                    "person.age",
                    "person.address.city",
                    "message",
                    "message.length()", // 注意：这个例子的反射部分不支持方法调用，会失败或返回null
                    "numbers[1]",      // 访问 List 元素
                    "info['status']",  // 访问 Map 值 (使用字符串字面量)
                    "info[level]"      // 访问 Map 值 (使用变量作为键 - 需要在context中有level变量)
            };

            // 添加 level 变量用于最后一个测试
            data.put("level", "level");

            System.out.println("Context: " + data);
            System.out.println("--- Evaluating Expressions ---");

            for (String exprStr : expressions) {
                System.out.print("Expression: " + exprStr + " => ");
                try {
                    // 1. 词法分析
                    Lexer lexer = new Lexer(exprStr);
                    List<Token> tokens = lexer.scanTokens();

                    // 2. 语法分析
                    Parser parser = new Parser(tokens);
                    Expr expression = parser.parse();

                    // 3. 求值
                    Evaluator evaluator = new Evaluator(data);
                    Object result = evaluator.evaluate(expression);

                    System.out.println(result);

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}