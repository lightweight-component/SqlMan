package com.ajaxjs.sqlman.expression;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Token {
    final TokenType type;

    final String lexeme; // 原始文本

    final Object literalValue; // 如果是字面量，则存储其值

    @Override
    public String toString() {
        return "Token{type=" + type + ", lexeme='" + lexeme + '\'' + ", literalValue=" + literalValue + '}';
    }
}
