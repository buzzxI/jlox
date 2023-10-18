package icu.buzz.exceptions;

public class ParserError extends RuntimeException {
    public ParserError() {
        super();
    }

    public ParserError(String message) {
        super(message);
    }
}