package icu.buzz.lox;

public abstract class Expr {
    public abstract <R> R accept(ExprVisitor<R> visitor);

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

}
