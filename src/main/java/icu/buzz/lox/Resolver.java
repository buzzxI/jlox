package icu.buzz.lox;

import icu.buzz.lox.exceptions.ResolverError;
import icu.buzz.lox.expr.Expr;
import icu.buzz.lox.expr.ExprVisitor;
import icu.buzz.lox.stmt.Stmt;
import icu.buzz.lox.stmt.StmtVisitor;
import icu.buzz.lox.token.Token;

import java.util.*;

public class Resolver implements ExprVisitor<Void>, StmtVisitor<Void> {
    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER,
    }

    private enum ClassType {
        NONE,
        CLASS,
    }

    private FunctionType currentFunc;
    private ClassType currentClass;
    private final List<Map<String, Boolean>> scopes;

    private final Interpreter interpreter;

    public Resolver(Interpreter interpreter) {
        this.currentFunc = FunctionType.NONE;
        this.currentClass = ClassType.NONE;
        this.scopes = new ArrayList<>();
        this.interpreter = interpreter;
    }

    public void resolveSource(List<Stmt> stmts) {
        try {
            resolve(stmts);
        } catch (ResolverError error) {
            Lox.errorReport(error.getToken(), error.getMessage());
        }
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(List<Stmt> stmts) {
        stmts.forEach(stmt -> stmt.accept(this));
    }

    @Override
    public Void visitExpr(Expr.Assign expr) {
        resolve(expr.getValue());
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Void visitExpr(Expr.Set expr) {
        resolve(expr.getObject());
        resolve(expr.getValue());
        return null;
    }

    @Override
    public Void visitExpr(Expr.Logical expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitExpr(Expr.Binary expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitExpr(Expr.Unary expr) {
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitExpr(Expr.Call expr) {
        resolve(expr.getCallee());
        expr.getArguments().forEach(this::resolve);
        return null;
    }

    @Override
    public Void visitExpr(Expr.Grouping expr) {
        resolve(expr.getExpression());
        return null;
    }

    @Override
    public Void visitExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitExpr(Expr.Get expr) {
        resolve(expr.getObject());
        return null;
    }

    @Override
    public Void visitExpr(Expr.Variable expr) {
        if (!scopes.isEmpty()) {
            Map<String, Boolean> scope = scopes.get(scopes.size() - 1);
            if (scope.containsKey(expr.getName().getLexeme()) && scope.get(expr.getName().getLexeme()) == Boolean.FALSE) {
                throw new ResolverError(expr.getName(), "cannot read variable from its initializer");
            }
        }
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Void visitExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) throw new ResolverError(expr.getKeyword(), "can not use 'this' outside a class");
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Expression stmt) {
        resolve(stmt.getExpr());
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Print stmt) {
        resolve(stmt.getExpr());
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visitStmt(Stmt.If stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getThenBranch());
        if (stmt.getElseBranch() != null) resolve(stmt.getElseBranch());
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Var stmt) {
        declare(stmt.getName());
        Expr initializer = stmt.getInitializer();
        if (initializer != null) resolve(initializer);
        define(stmt.getName());
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Class stmt) {
        ClassType encloseType = this.currentClass;
        this.currentClass = ClassType.CLASS;
        declare(stmt.getName());
        define(stmt.getName());
        beginScope();
        scopes.get(scopes.size() - 1).put("this", true);
        stmt.getMethods().forEach(method -> resolveFunc(method, method.getName().getLexeme().equals("init") ? FunctionType.INITIALIZER : FunctionType.METHOD));
        endScope();
        this.currentClass = encloseType;
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Fun stmt) {
        declare(stmt.getName());
        define(stmt.getName());
        resolveFunc(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitStmt(Stmt.While stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getBody());
        return null;
    }

    @Override
    public Void visitStmt(Stmt.Return stmt) {
        if (currentFunc == FunctionType.NONE) throw new ResolverError(stmt.getKeyword(), "can not return from top-level");
        if (stmt.getValue() != null) {
            if (currentFunc == FunctionType.INITIALIZER) throw new ResolverError(stmt.getKeyword(), "can not return value from initializer");
            resolve(stmt.getValue());
        }
        return null;
    }

    /**
     * offer a new scope
     */
    private void beginScope() {
        scopes.add(new HashMap<>());
    }

    /**
     * delete a scope
     */
    private void endScope() {
        scopes.remove(scopes.size() - 1);
    }

    private void declare(Token name) {
        if (this.scopes.isEmpty()) return;
        Map<String, Boolean> scope = this.scopes.get(scopes.size() - 1);
        if (scope.containsKey(name.getLexeme())) throw new ResolverError(name, "variable " + name.getLexeme() + " has already defined");
        scope.put(name.getLexeme(), false);
    }

    private void define(Token name) {
        if (this.scopes.isEmpty()) return;
        this.scopes.get(scopes.size() - 1).put(name.getLexeme(), true);
    }

    private void resolveFunc(Stmt.Fun stmt, FunctionType type) {
        FunctionType encloseType = this.currentFunc;
        this.currentFunc = type;
        beginScope();
        for (Token parameter : stmt.getParameters()) {
            declare(parameter);
            define(parameter);
        }
        resolve(stmt.getBody());
        endScope();
        this.currentFunc = encloseType;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, Boolean> scope = scopes.get(i);
            if (scope.containsKey(name.getLexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }
}
