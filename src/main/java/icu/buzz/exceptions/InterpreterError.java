package icu.buzz.exceptions;

import icu.buzz.lox.Token;

public class InterpreterError extends RuntimeException {
    private Token token;

    public InterpreterError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
