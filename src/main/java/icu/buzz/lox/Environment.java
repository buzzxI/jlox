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
        if (map.containsKey(name.getLexeme())) return map.get(name.getLexeme());
        throw new ExecuteError(name, "variable: " + name.getLexeme() + " is undefined");
    }

    /**
     * get variables from distance
     * @param name variable name
     * @param distance distance from current environment
     * @return variable value
     */
    public Object get(Token name, int distance) {
        if (distance == 0) return this.get(name);
        if (enclose != null) return this.enclose.get(name, distance - 1);
        return null;
    }

    public void assign(Token name, Object value) {
        if (map.containsKey(name.getLexeme())) map.put(name.getLexeme(), value);
        else throw new ExecuteError(name, "assign variable: " + name.getLexeme() + " has not been defined");
    }

    /**
     * assign variable with distance
     * @param name variable name
     * @param value new value
     * @param distance distance from current environment
     */
    public void assign(Token name, Object value, int distance) {
        if (distance == 0) this.assign(name, value);
        else if (enclose != null) this.enclose.assign(name, value, distance - 1);
    }
}