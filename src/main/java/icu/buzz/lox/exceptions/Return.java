package icu.buzz.lox.exceptions;

/**
 * lox returns with throwing an exception
 */
public class Return extends RuntimeException{
    private final Object value;
    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
