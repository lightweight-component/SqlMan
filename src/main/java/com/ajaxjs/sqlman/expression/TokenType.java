package com.ajaxjs.sqlman.expression;

/**
 * 定义基本的 Token 类型
 */
enum TokenType {

    IDENTIFIER, // 标识符 (变量名, 属性名)

    STRING_LITERAL, // 字符串字面量 'value'

    NUMBER_LITERAL, // 数字字面量 (简化处理)

    DOT,        // .

    LEFT_BRACKET,  // [

    RIGHT_BRACKET, // ]

    EOF         // 输入结束

}