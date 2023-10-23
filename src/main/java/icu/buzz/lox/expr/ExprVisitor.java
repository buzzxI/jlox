package icu.buzz.lox.expr;

public interface ExprVisitor<R> {
    R visitExpr (Expr.Binary expr);
    R visitExpr (Expr.Unary expr);
    R visitExpr (Expr.Grouping expr);
    R visitExpr (Expr.Literal expr);
    R visitExpr (Expr.Variable expr);
}
