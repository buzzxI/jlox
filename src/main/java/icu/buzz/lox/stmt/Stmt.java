package icu.buzz.lox.stmt;

import icu.buzz.lox.expr.Expr;
import icu.buzz.lox.token.Token;

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

}
