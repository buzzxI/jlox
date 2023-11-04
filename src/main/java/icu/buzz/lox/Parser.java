package icu.buzz.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import icu.buzz.lox.exceptions.ParserError;
import icu.buzz.lox.expr.Expr;
import icu.buzz.lox.stmt.Stmt;
import icu.buzz.lox.token.Token;
import icu.buzz.lox.token.TokenType;

public class Parser {
    private final List<Token> tokenList;
    private int current;

    public Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
        this.current = 0;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isEnd()) {
            try {
                statements.add(declaration());
            } catch (ParserError error) {
                Lox.errorReport(error.getToken(), error.getMessage());
                synchronize();
            }
        }
        return statements;
    }

    private Stmt declaration() {
        if (match(TokenType.VAR)) return varDecl();
        if (match(TokenType.FUN)) return funDecl();
        if (match(TokenType.CLASS)) return classDecl();
        return statement();
    }

    private Stmt varDecl() {
        Token name = consume(TokenType.IDENTIFIER, "a identifier is needed for 'var'");
        Expr initializer = null;

        if (match(TokenType.EQUAL)) initializer = expression();

        consume(TokenType.SEMICOLON, "a ';' is needed at the end of a statement");
        return new Stmt.Var(name, initializer);
    }

    private Stmt funDecl() {
        return function("function");
    }

    private Stmt.Fun function(String type) {
        Token funName = consume(TokenType.IDENTIFIER, "the name of a " + type + " should be an identifier");
        consume(TokenType.LEFT_PAREN, "a '(' is needed at the beginning of " + type + " parameter list");
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= Lox.MAX_ARGS) {
                    Lox.errorReport(peek(), "cannot have more than 255 parameters in a " + type + " declaration");
                }
                Token parameter = consume(TokenType.IDENTIFIER, "expect parameter name");
                parameters.add(parameter);
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "a ')' is needed at the end of " + type + " parameter list");

        Stmt stmt = statement();
        if (!(stmt instanceof Stmt.Block body)) throw new ParserError(previous(), "body of a " + type + " should be a block");

        return new Stmt.Fun(funName, parameters, body.getStatements());
    }

    private Stmt classDecl() {
        Token className = consume(TokenType.IDENTIFIER, "the name of a class should be an identifier");

        Expr.Variable sup = null;
        if (match(TokenType.LESS)) {
            Token parentName = consume(TokenType.IDENTIFIER, "expect parent class name after '<'");
            sup =new Expr.Variable(parentName);
        }

        consume(TokenType.LEFT_BRACE, "a '{' is needed at the beginning of class declaration");
        List<Stmt.Fun> methods = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isEnd()) methods.add(function("method"));
        consume(TokenType.RIGHT_BRACE, "a '}' is needed at the end of class declaration");

        return new Stmt.Class(className, sup, methods);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStmt();
        if (match(TokenType.LEFT_BRACE)) return blockStmt();
        if (match(TokenType.IF)) return ifStmt();
        if (match(TokenType.WHILE)) return whileStmt();
        if (match(TokenType.FOR)) return forStmt();
        if (match(TokenType.RETURN)) return retStmt();
        return exprStmt();
    }

    private Stmt printStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "a ';' is needed at the end of the statement");
        return new Stmt.Print(expr);
    }

    private Stmt blockStmt() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isEnd()) stmts.add(declaration());
        consume(TokenType.RIGHT_BRACE, "a '}' is needed at the end of the block");
        return new Stmt.Block(stmts);
    }

    private Stmt ifStmt() {
        consume(TokenType.LEFT_PAREN, "a '(' is needed at the begin of 'if' condition");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "a ')' is needed at the end of 'if' condition");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) elseBranch = statement();
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStmt() {
        consume(TokenType.LEFT_PAREN, "a '(' is needed at the begin of 'while' condition");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "a ')' is needed at the end of 'while' condition");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt forStmt() {
        consume(TokenType.LEFT_PAREN, "a '(' is needed at the begin of 'for'");
        Stmt initializer = null;

        if (match(TokenType.VAR)) {
            initializer = varDecl();
        } else if (!match(TokenType.SEMICOLON)) {
            initializer = exprStmt();
        }

        Expr condition = null;
        if (!match(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "a ';' is needed after condition");

        Expr increment = null;
        if (!match(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "a ')' is needed at the end of 'for'");

        Stmt body = statement();
        if (increment != null) body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) body = new Stmt.Block(Arrays.asList(initializer, body));

        return body;
    }

    private Stmt retStmt() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) value = expression();
        consume(TokenType.SEMICOLON, "a ';' is needed at the end of return");
        return new Stmt.Return(keyword, value);
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "a ';' is needed at the end of the statement");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(TokenType.EQUAL)) {
            Token equalToken = previous();
            if (expr instanceof Expr.Variable variable) return new Expr.Assign(variable.getName(), assignment());
            else if (expr instanceof Expr.Get get) return new Expr.Set(get.getObject(), get.getName(), assignment());
            throw new ParserError(equalToken, "expect an identifier before '='");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
           Token operator = previous();
           expr = new Expr.Logical(expr, operator, and());
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            expr = new Expr.Logical(expr, operator, equality());
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
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
        return call();
    }

    private Expr call() {
       Expr expr = primary();
       while (true) {
           if (match(TokenType.LEFT_PAREN)) {
              expr = finishCall(expr);
           } else if (match(TokenType.DOT)) {
               Token name = consume(TokenType.IDENTIFIER, "an identifier is needed after '.'");
               expr = new Expr.Get(expr, name);
           } else break;
       }
       return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= Lox.MAX_ARGS) {
                    Lox.errorReport(peek(), "cannot have more than 255 arguments in a function call");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "a ')' is needed at the end of a function call");
        return new Expr.Call(callee, arguments, paren);
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
            case IDENTIFIER -> new Expr.Variable(currenToken);
            case THIS -> new Expr.This(currenToken);
            case SUPER -> {
                consume(TokenType.DOT, "a '.' is needed after 'super'");
                Token method = consume(TokenType.IDENTIFIER, "expect super class method name");
                yield new Expr.Super(currenToken, method);
            }
            default -> throw new ParserError(currenToken, "unexpected token '" + currenToken.getLexeme() + "'");
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
        else throw new ParserError(peek(), errorMessage);
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
