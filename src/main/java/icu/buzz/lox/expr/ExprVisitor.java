package icu.buzz.lox.expr;

public interface ExprVisitor<R> {
    R visitExpr (Expr.Assign expr);
    R visitExpr (Expr.Logical expr);
    R visitExpr (Expr.Binary expr);
    R visitExpr (Expr.Unary expr);
    R visitExpr (Expr.Call expr);
    R visitExpr (Expr.Grouping expr);
    R visitExpr (Expr.Literal expr);
    R visitExpr (Expr.Variable expr);
}