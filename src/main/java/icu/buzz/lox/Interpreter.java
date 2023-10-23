package icu.buzz.lox;

import icu.buzz.exceptions.ExecuteError;
import icu.buzz.lox.expr.Expr;
import icu.buzz.lox.expr.ExprVisitor;
import icu.buzz.lox.stmt.Stmt;
import icu.buzz.lox.stmt.StmtVisitor;
import icu.buzz.lox.token.Token;

import java.util.List;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
    private final Environment environment;

    private final List<Stmt> statements;

    public Interpreter(List<Stmt> statements) {
        this.environment = new Environment();
        this.statements = statements;
    }

    public void interpret() {
        try {
            statements.forEach(stmt -> stmt.accept(this));
        } catch (ExecuteError error) {
            Lox.errorReport(error.getToken(), error.getMessage());
        }
    }

    @Override
    public Object visitExpr(Expr.Binary expr) {
        Object left = expr.getLeft().accept(this);
        Object right = expr.getRight().accept(this);

        Token token = expr.getOperator();
        return switch (token.getType()) {
            case MINUS -> {
                checkNumber(token, "Operands for '-' should be number", left, right);
                yield (double)left - (double)right;
            }
            case STAR -> {
                checkNumber(token, "Operands for '*' should be number", left, right);
                yield (double)left * (double)right;
            }
            case SLASH -> {
                checkNumber(token, "Operands for '/' should be number", left, right);
                if ((double)right != 0) yield (double)left / (double)right;
                throw new ExecuteError(token, "Divisor should not be zero");
            }
            case PLUS -> {
                if (isNumber(left, right)) yield (double)left + (double)right;
                if (isString(left, right)) yield (String)left + (String)right;
                throw new ExecuteError(token, "Operands for '+' should be number or string");
            }
            case GREATER -> {
                checkNumber(token, "Operands for '>' should be number", left, right);
                yield (double)left > (double)right;
            }
            case GREATER_EQUAL -> {
                checkNumber(token, "Operands for \">=\" should be number", left, right);
                yield (double)left >= (double)right;
            }
            case LESS -> {
                checkNumber(token, "Operands for '<' should be number", left, right);
                yield (double)left < (double)right;
            }
            case LESS_EQUAL -> {
                checkNumber(token, "Operands for \"<=\" should be number", left, right);
                yield (double)left <= (double)right;
            }
            case EQUAL -> isEqual(left, right);
            case BANG_EQUAL -> !isEqual(left, right);
            // never reach
            default -> throw new ExecuteError(token, "Unexpected binary operator");
        };
    }

    @Override
    public Object visitExpr(Expr.Unary expr) {
        Object right = expr.getRight().accept(this);
        Token token = expr.getOperator();
        return switch (token.getType()) {
            case MINUS -> {
                checkNumber(token, "Operand for '-' should be number", right);
                yield -(double)right;
            }
            case BANG -> !isTruthy(right);
            // never reach
            default -> throw new ExecuteError(token, "Unexpected unary operator");
        };
    }

    @Override
    public Object visitExpr(Expr.Grouping expr) {
        return expr.getExpression().accept(this);
    }

    @Override
    public Object visitExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitExpr(Expr.Variable expr) {
        return environment.get(expr.getName());
    }

    /**
     * all kinds of object can be separated into true or false
     * @param obj any kinds of object
     * @return true or false based on Ruby lexical rules
     */
    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (boolean)obj;
        return true;
    }

    /**
     * @param obj all kinds of objects
     * @return true on all objs are number
     */
    private boolean isNumber(Object ... obj) {
        for (Object o : obj) {
            if (!(o instanceof Double)) return false;
        }
        return true;
    }

    /**
     * @param obj all kinds of objects
     * @return true on all objs are number
     */
    private boolean isString(Object ... obj) {
        for (Object o : obj) {
            if (!(o instanceof String)) return false;
        }
        return true;
    }

    /**
     * @param arg1 one of the object
     * @param arg2 the other object
     * @return if two objects are equal
     */
    private boolean isEqual(Object arg1, Object arg2) {
        if (arg1 == null && arg2 == null) return true;
        if (arg1 == null) return false;
        return arg1.equals(arg2);
    }

    /**
     * wrapper function for @method: isNumber
     * @param token raise an exception with token on NAN
     * @param message raise an exception with message on NAN
     * @param obj args for @method: isNumber
     */
    private void checkNumber(Token token, String message, Object ... obj) {
        if (!isNumber(obj)) throw new ExecuteError(token, message);
    }

    @Override
    public Void visitStmt(Stmt.Expression stmt) {
        stmt.getExpr().accept(this);
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Print stmt) {
        System.out.println(stringify(stmt.getExpr().accept(this)));
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Var stmt) {
        Object rst = null;

        Expr initializer = stmt.getInitializer();
        if (initializer != null) rst = initializer.accept(this);

        environment.define(stmt.getName().getLexeme(), rst);
        return null;
    }

    /**
     * stringify all kinds of lox object
     * @param loxObj lox object
     * @return string
     */
    private String stringify(Object loxObj) {
        if (loxObj == null) return "nil";

        // when lox object is a number, lox should know if this is integer or double
        if (loxObj instanceof Double) {
            String rst = loxObj.toString();
            if (rst.endsWith(".0")) return rst.substring(0, rst.length() - 2);
            return rst;
        }

        return loxObj.toString();
    }
}
