package icu.buzz.lox;

import java.util.List;
import icu.buzz.exceptions.ParserError;

public class Parser {
    private final List<Token> tokenList;
    private int current;

    public Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
        this.current = 0;
    }

    public Expr parseExpr() {
        try {
           return expression();
        } catch (ParserError error) {
            return null;
        }
    }


    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.MINUS, TokenType.BANG)) {
            Token operator = previous();
            return new Expr.Unary(operator, unary());
        }
        return primary();
    }

    private Expr primary() {
        Token currenToken = advance();

        return switch (currenToken.getType()) {
            case FALSE -> new Expr.Literal(false);
            case TRUE -> new Expr.Literal(true);
            case NIL -> new Expr.Literal(null);
            case NUMBER, STRING -> new Expr.Literal(currenToken.getValue());
            case LEFT_PAREN -> {
                Expr expr = expression();
                consume(TokenType.RIGHT_PAREN, "a ')' is expected");
                yield new Expr.Grouping(expr);
            }
            default -> throw reportAndRaiseParserError(peek(), "unexpected token");
        };
    }

    private boolean match(TokenType ... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isEnd()) return false;
        return peek().getType() == type;
    }

    private Token peek() {
        return tokenList.get(current);
    }

    private Token advance() {
        if (!isEnd()) current++;
        return previous();
    }

    private boolean isEnd() {
        return tokenList.get(current).getType() == TokenType.EOF;
    }

    private Token previous() {
        return tokenList.get(current - 1);
    }

    /**
     * consume a token if matches or report an error
     * @param type target token type
     * @param errorMessage error message
     * @return target token
     */
    private Token consume(TokenType type, String errorMessage) {
        if (check(type)) return advance();

        throw reportAndRaiseParserError(peek(), errorMessage);
    }

    /**
     * report an error and throws an exception
     * @param token token (maybe) with error
     * @param message error message
     * @return parse error
     */
    private ParserError reportAndRaiseParserError(Token token, String message) {
        Lox.errorReport(token, message);
        return new ParserError(message);
    }

    /**
     * discard tokens until the next statement (for now)
     */
    private void synchronize() {
        advance();
        while (!isEnd()) {
            if (previous().getType() == TokenType.SEMICOLON) return;
            switch (peek().getType()) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }
            advance();
        }
    }
}
