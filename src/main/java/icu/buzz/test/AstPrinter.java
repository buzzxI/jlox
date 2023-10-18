package icu.buzz.test;

import icu.buzz.lox.Expr;
import icu.buzz.lox.ExprVisitor;

/**
 * a class to test AST correctness, it acts like anti-parser
 * entry method is print, it takes an AST tree (Expr object), and print with visitor pattern
 * print behavior is defined by @function: visitExpr
 */
public class AstPrinter implements ExprVisitor<String> {

    private final Expr expr;

    public AstPrinter(Expr expr) {
        this.expr = expr;
    }

    public String print() {
        return expr.accept(this);
    }

    private String parenthesize(String name, Expr ... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ").append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitExpr(Expr.Binary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitExpr(Expr.Unary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getRight());
    }

    @Override
    public String visitExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.getExpression());
    }

    @Override
    public String visitExpr(Expr.Literal expr) {
        if (expr.getValue() == null) return "nil";
        return expr.getValue().toString();
    }
}
