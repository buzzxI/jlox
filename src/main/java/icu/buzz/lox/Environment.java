package icu.buzz.lox;

import icu.buzz.exceptions.ExecuteError;
import icu.buzz.lox.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> map;

    public Environment() {
        this.map = new HashMap<>();
    }

    public void define(String name, Object value) {
        map.put(name, value);
    }

    public Object get(Token name) {
        if (map.containsKey(name.getLexeme())) {
            return map.get(name.getLexeme());
        }

        throw new ExecuteError(name, "variable: " + name.getLexeme() + " is undefined");
    }

}