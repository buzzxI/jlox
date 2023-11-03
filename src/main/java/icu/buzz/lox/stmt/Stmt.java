package icu.buzz.lox.stmt;

import icu.buzz.lox.expr.Expr;
import icu.buzz.lox.token.Token;
import java.util.List;

public abstract class Stmt {
    public abstract <R> R accept(StmtVisitor<R> visitor);

    public static class Expression extends Stmt {
        private final Expr expr;

        public Expression(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Expr getExpr() {
            return this.expr;
        }

    }

    public static class Print extends Stmt {
        private final Expr expr;

        public Print(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Expr getExpr() {
            return this.expr;
        }

    }

    public static class Block extends Stmt {
        private final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public List<Stmt> getStatements() {
            return this.statements;
        }

    }

    public static class If extends Stmt {
        private final Expr condition;

        private final Stmt thenBranch;

        private final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Expr getCondition() {
            return this.condition;
        }

        public Stmt getThenBranch() {
            return this.thenBranch;
        }

        public Stmt getElseBranch() {
            return this.elseBranch;
        }

    }

    public static class Var extends Stmt {
        private final Token name;

        private final Expr initializer;

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Token getName() {
            return this.name;
        }

        public Expr getInitializer() {
            return this.initializer;
        }

    }

    public static class Class extends Stmt {
        private final Token name;

        private final List<Stmt.Fun> methods;

        public Class(Token name, List<Stmt.Fun> methods) {
            this.name = name;
            this.methods = methods;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Token getName() {
            return this.name;
        }

        public List<Stmt.Fun> getMethods() {
            return this.methods;
        }

    }

    public static class Fun extends Stmt {
        private final Token name;

        private final List<Token> parameters;

        private final List<Stmt> body;

        public Fun(Token name, List<Token> parameters, List<Stmt> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Token getName() {
            return this.name;
        }

        public List<Token> getParameters() {
            return this.parameters;
        }

        public List<Stmt> getBody() {
            return this.body;
        }

    }

    public static class While extends Stmt {
        private final Expr condition;

        private final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Expr getCondition() {
            return this.condition;
        }

        public Stmt getBody() {
            return this.body;
        }

    }

    public static class Return extends Stmt {
        private final Token keyword;

        private final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitStmt(this);
        }

        // getters
        public Token getKeyword() {
            return this.keyword;
        }

        public Expr getValue() {
            return this.value;
        }

    }

}
