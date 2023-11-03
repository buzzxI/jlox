package icu.buzz.lox.oop;

import java.util.HashMap;
import java.util.Map;

import icu.buzz.lox.callable.LoxFunction;
import icu.buzz.lox.exceptions.ExecuteError;
import icu.buzz.lox.token.Token;

public class LoxInstance {
    private final LoxClass klass;
    private final Map<String, Object> field;

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
        this.field = new HashMap<>();
    }

    public Object get(Token name) {
        if (field.containsKey(name.getLexeme())) return field.get(name.getLexeme());

        LoxFunction method = klass.getMethod(name.getLexeme());
        if (method != null) return method.bind(this);

        throw new ExecuteError(name, "Undefined property " + name.getLexeme());
    }

    public void set(Token name, Object value) {
        field.put(name.getLexeme(), value);
    }

    @Override
    public String toString() {
        return "<lox " + klass.getName() +  " instance> " + field;
    }
}
