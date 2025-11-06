package com.ajaxjs.sqlman.expression;

public interface Visitor<R> {
    R visitVariableExpr(Expr.Variable expr);

    R visitGetPropertyExpr(Expr.GetProperty expr);

    R visitGetIndexExpr(Expr.GetIndex expr);

    R visitLiteralExpr(Expr.Literal expr);
}
