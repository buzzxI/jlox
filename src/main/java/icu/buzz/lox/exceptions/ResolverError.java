package icu.buzz.lox.exceptions;

import icu.buzz.lox.token.Token;

public class ResolverError extends RuntimeException {
    private final Token token;

    public ResolverError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
