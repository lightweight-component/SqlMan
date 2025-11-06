package com.ajaxjs.sqlman.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * 词法分析器
 */
public class Lexer {
    private final String source;

    private int start = 0;

    private int current = 0;

    private final List<Token> tokens = new ArrayList<>();

    public Lexer(String source) {
        this.source = source;
    }


    List<Token> scanTokens() throws Exception {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null));

        return tokens;

    }

    private void scanToken() throws Exception {
        char c = advance();

        switch (c) {
            case '.':
                addToken(TokenType.DOT, ".");
                break;
            case '[':
                addToken(TokenType.LEFT_BRACKET, "[");
                break;
            case ']':
                addToken(TokenType.RIGHT_BRACKET, "]");
                break;
            case '\'':
            case '\"': // 简化处理，都当作字符串
                string(c);
                break;
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                // 忽略空白字符
                break;
            default:
                if (isAlphaNumeric(c))
                    identifierOrNumber();
                else
                    throw new Exception("Unexpected character: " + c + " at position " + current);

                break;
        }
    }

    private void string(char quoteChar) throws Exception {
        StringBuilder value = new StringBuilder();

        while (peek() != quoteChar && !isAtEnd())
            value.append(advance());

        if (isAtEnd())
            throw new Exception("Unterminated string.");

        advance(); // 跳过结束引号
        String lexeme = source.substring(start, current); // 包含引号
        addToken(TokenType.STRING_LITERAL, lexeme, value.toString()); // literalValue 是去掉引号的内容
    }

    private void identifierOrNumber() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);

        try { // 尝试解析为数字
            // 简化：只尝试整数
            Integer intValue = Integer.valueOf(text);
            addToken(TokenType.NUMBER_LITERAL, text, intValue);

            return;
        } catch (NumberFormatException e) {
            // 不是数字，当作标识符
        }

        addToken(TokenType.IDENTIFIER, text, null);
    }

    private boolean isAlphaNumeric(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') ||
                c == '_';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd())
            return '\0';

        return source.charAt(current);
    }

    private void addToken(TokenType type, String lexeme) {
        addToken(type, lexeme, null);
    }

    private void addToken(TokenType type, String lexeme, Object literalValue) {
        tokens.add(new Token(type, lexeme, literalValue));
    }
}
