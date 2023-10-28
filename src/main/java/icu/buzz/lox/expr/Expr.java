package icu.buzz.lox.expr;

import icu.buzz.lox.token.Token;
import java.util.List;

public abstract class Expr {
    public abstract <R> R accept(ExprVisitor<R> visitor);

    public static class Assign extends Expr {
        private final Token name;

        private final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Token getName() {
            return this.name;
        }

        public Expr getValue() {
            return this.value;
        }

    }

    public static class Logical extends Expr {
        private final Expr left;

        private final Token operator;

        private final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Expr getLeft() {
            return this.left;
        }

        public Token getOperator() {
            return this.operator;
        }

        public Expr getRight() {
            return this.right;
        }

    }

    public static class Binary extends Expr {
        private final Expr left;

        private final Token operator;

        private final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Expr getLeft() {
            return this.left;
        }

        public Token getOperator() {
            return this.operator;
        }

        public Expr getRight() {
            return this.right;
        }

    }

    public static class Unary extends Expr {
        private final Token operator;

        private final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Token getOperator() {
            return this.operator;
        }

        public Expr getRight() {
            return this.right;
        }

    }

    public static class Call extends Expr {
        private final Expr callee;

        private final List<Expr> arguments;

        private final Token paren;

        public Call(Expr callee, List<Expr> arguments, Token paren) {
            this.callee = callee;
            this.arguments = arguments;
            this.paren = paren;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Expr getCallee() {
            return this.callee;
        }

        public List<Expr> getArguments() {
            return this.arguments;
        }

        public Token getParen() {
            return this.paren;
        }

    }

    public static class Grouping extends Expr {
        private final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Expr getExpression() {
            return this.expression;
        }

    }

    public static class Literal extends Expr {
        private final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Object getValue() {
            return this.value;
        }

    }

    public static class Variable extends Expr {
        private final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitExpr(this);
        }

        // getters
        public Token getName() {
            return this.name;
        }

    }

}
