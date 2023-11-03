package icu.buzz.lox;

import icu.buzz.lox.callable.LoxCallable;
import icu.buzz.lox.callable.LoxFunction;
import icu.buzz.lox.exceptions.ExecuteError;
import icu.buzz.lox.exceptions.Return;
import icu.buzz.lox.expr.Expr;
import icu.buzz.lox.expr.ExprVisitor;
import icu.buzz.lox.callable.foreign.Clock;
import icu.buzz.lox.oop.LoxClass;
import icu.buzz.lox.oop.LoxInstance;
import icu.buzz.lox.stmt.Stmt;
import icu.buzz.lox.stmt.StmtVisitor;
import icu.buzz.lox.token.Token;
import icu.buzz.lox.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
    private final Map<Expr, Integer> depthMap;

    private final Environment global;
    private Environment environment;

    private final List<Stmt> statements;

    public Interpreter(List<Stmt> statements) {
        this.depthMap = new HashMap<>();
        this.global = new Environment();
        global.define("clock", new Clock());
        this.environment = global;
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
    public Object visitExpr(Expr.Assign expr) {
        Object value = expr.getValue().accept(this);
        Integer distance = depthMap.get(expr);
        if (distance == null) global.assign(expr.getName(), value);
        else environment.assign(expr.getName(), value, distance);
        return value;
    }

    @Override
    public Object visitExpr(Expr.Set expr) {
        Object instance = expr.getObject().accept(this);
        if (!(instance instanceof LoxInstance loxInstance)) throw new ExecuteError(expr.getName(), "field only allowed for instance");
        Object value = expr.getValue().accept(this);
        loxInstance.set(expr.getName(), value);
        return value;
    }

    @Override
    public Object visitExpr(Expr.Logical expr) {
        Token operator = expr.getOperator();
        Object left = expr.getLeft().accept(this);
        if (operator.getType() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return expr.getRight().accept(this);
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
                if (isString(left, right)) yield left + (String)right;
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
    public Object visitExpr(Expr.Call expr) {
        Object callee = expr.getCallee().accept(this);
        List<Expr> argLists = expr.getArguments();
        int size = Math.min(argLists.size(), Lox.MAX_ARGS);
        List<Object> arguments = new ArrayList<>(size);
        for (int i = 0; i < size; i++) arguments.add(argLists.get(i).accept(this));

        if (!(callee instanceof LoxCallable function)) throw new ExecuteError(expr.getParen(), "callee is not callable");
        if (function.arity() != arguments.size()) throw new ExecuteError(expr.getParen(), "function except:" + function.arity()+ " but got:" + arguments.size());

        return function.call(this, arguments);
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
    public Object visitExpr(Expr.Get expr) {
        Object instance = expr.getObject().accept(this);
        if (!(instance instanceof LoxInstance loxInstance)) throw new ExecuteError(expr.getName(), "property only allowed for an instance");
        return loxInstance.get(expr.getName());
    }

    @Override
    public Object visitExpr(Expr.Variable expr) {
        return loopUp(expr, expr.getName());
    }

    @Override
    public Object visitExpr(Expr.This expr) {
        return loopUp(expr, expr.getKeyword());
    }

    private Object loopUp(Expr expr, Token name) {
        Integer distance = depthMap.get(expr);
        if (distance == null) return global.get(name);
        return environment.get(name, distance);
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
    public Void visitStmt(Stmt.Block stmt) {
        executeBlock(stmt.getStatements(), new Environment(this.environment));
        return null;
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        this.environment = environment;
        try {
            statements.forEach(s -> s.accept(this));
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitStmt(Stmt.If stmt) {
        if (isTruthy(stmt.getCondition().accept(this))) {
            stmt.getThenBranch().accept(this);
        } else if (stmt.getElseBranch() != null) {
            stmt.getElseBranch().accept(this);
        }
        return null;
    }

    @Override
    public Void visitStmt(Stmt.While stmt) {
        Expr condition = stmt.getCondition();
        Stmt body = stmt.getBody();
        while (isTruthy(condition.accept(this))) body.accept(this);
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.getValue() != null) value = stmt.getValue().accept(this);
        throw new Return(value);
    }

    @Override
    public Void visitStmt(Stmt.Var stmt) {
        Object rst = null;

        Expr initializer = stmt.getInitializer();
        if (initializer != null) rst = initializer.accept(this);

        environment.define(stmt.getName().getLexeme(), rst);
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Class stmt) {
        String className = stmt.getName().getLexeme();
        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Fun method : stmt.getMethods()) methods.put(method.getName().getLexeme(), new LoxFunction(method, environment, method.getName().getLexeme().equals("init")));
        LoxClass loxClass = new LoxClass(className, methods);
        environment.define(className, loxClass);
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Fun stmt) {
        this.environment.define(stmt.getName().getLexeme(), new LoxFunction(stmt, this.environment, false));
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

    /**
     * mark expression with depth
     * @param expr variable expression
     * @param depth resolution depth
     */
    public void resolve(Expr expr, int depth) {
        depthMap.put(expr, depth);
    }
}
