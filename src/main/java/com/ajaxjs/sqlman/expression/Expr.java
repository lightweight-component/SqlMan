package com.ajaxjs.sqlman.expression;

import lombok.RequiredArgsConstructor;

/**
 * 表达式节点 (AST Node)
 */
public abstract class Expr {
    @RequiredArgsConstructor
    public static class Variable extends Expr {
        public final String name;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    @RequiredArgsConstructor
    public static class GetProperty extends Expr {
        public final Expr object;

        public final String property;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetPropertyExpr(this);
        }
    }

    @RequiredArgsConstructor
    public static class GetIndex extends Expr {
        public final Expr object;

        public final Expr index; // 可以是 Variable, StringLiteral, NumberLiteral

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetIndexExpr(this);
        }
    }

    @RequiredArgsConstructor
    public static class Literal extends Expr {
        public final Object value;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
