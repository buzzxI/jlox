package icu.buzz.lox.exceptions;

import icu.buzz.lox.token.Token;

public class ParserError extends RuntimeException {

    private final Token token;

    public ParserError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}