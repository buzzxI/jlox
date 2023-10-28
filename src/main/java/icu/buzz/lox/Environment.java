package icu.buzz.lox;

import icu.buzz.lox.exceptions.ExecuteError;
import icu.buzz.lox.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> map;
    private final Environment enclose;

    public Environment() {
        this.map = new HashMap<>();
        this.enclose = null;
    }

    public Environment(Environment enclose) {
        this.map = new HashMap<>();
        this.enclose = enclose;
    }


    public void define(String name, Object value) {
        map.put(name, value);
    }

    public Object get(Token name) {
        if (map.containsKey(name.getLexeme())) {
            return map.get(name.getLexeme());
        }

        if (enclose != null) return enclose.get(name);

        throw new ExecuteError(name, "variable: " + name.getLexeme() + " is undefined");
    }
    public void assign(Token name, Object value) {
        if (map.containsKey(name.getLexeme())) {
            map.put(name.getLexeme(), value);
            return;
        }

        if (enclose != null) {
            enclose.assign(name, value);
            return;
        }

        throw new ExecuteError(name, "assign variable: " + name.getLexeme() + " has not been defined");
    }

}