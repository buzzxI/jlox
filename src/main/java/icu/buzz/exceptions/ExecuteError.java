package icu.buzz.exceptions;

import icu.buzz.lox.token.Token;

public class ExecuteError extends RuntimeException {
    private final Token token;

    public ExecuteError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
