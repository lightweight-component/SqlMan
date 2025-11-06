package com.ajaxjs.sqlman.expression;

import java.util.List;

/**
 * 递归下降解析器 (Parser)
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() throws Exception {
        return expression();
    }

    private Expr expression() throws Exception {
        return memberAccess(); // 开始解析成员访问链
    }

    private Expr memberAccess() throws Exception {
        Expr expr = primary(); // 先解析基础元素 (变量或字面量)

        while (match(TokenType.DOT, TokenType.LEFT_BRACKET)) {
            Token operator = previous(); // 获取匹配到的操作符 '.' 或 '['
            if (operator.type == TokenType.DOT) {
                // 处理 .property
                Token propertyToken = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.GetProperty(expr, propertyToken.lexeme);
            } else if (operator.type == TokenType.LEFT_BRACKET) {
                // 处理 [index_or_key]
                Expr indexOrKey = expression(); // 递归解析中括号内的表达式
                consume(TokenType.RIGHT_BRACKET, "Expect ']' after index or key.");
                expr = new Expr.GetIndex(expr, indexOrKey);
            }
        }

        return expr;
    }

    private Expr primary() throws Exception {
        if (match(TokenType.IDENTIFIER))
            return new Expr.Variable(previous().lexeme);

        if (match(TokenType.STRING_LITERAL, TokenType.NUMBER_LITERAL))
            return new Expr.Literal(previous().literalValue);

        if (match(TokenType.LEFT_BRACKET)) {
            // 支持 (...) 分组, 但这里为了简化，直接处理列表字面量不太现实，
            // 所以我们假设 [] 内部的表达式已经在 memberAccess 中处理了。
            // 如果需要支持数组/列表字面量 [], 这里需要更复杂的逻辑。
            // 当前逻辑下，这里的 '[' 应该在 memberAccess 中被消耗掉。
            // 如果执行到这里，说明是开头的 '[', 这不符合我们的简单 EL 规则。
            // 我们可以将其视为错误或者特殊处理，这里抛出异常。
            throw new Exception("Unexpected '[' at the beginning of expression.");
        }

        throw new Exception("Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) throws Exception {
        if (check(type))
            return advance();

        throw new Exception(message + " Found: " + peek().lexeme + " at position " + current);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;

        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
