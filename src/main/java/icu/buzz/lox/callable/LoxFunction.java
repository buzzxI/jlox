package icu.buzz.lox.callable;

import icu.buzz.lox.Environment;
import icu.buzz.lox.Interpreter;
import icu.buzz.lox.exceptions.Return;
import icu.buzz.lox.oop.LoxInstance;
import icu.buzz.lox.stmt.Stmt;
import icu.buzz.lox.token.Token;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Fun function;
    private final Environment closure;
    private final boolean initializer;

    public LoxFunction(Stmt.Fun function, Environment closure, boolean initializer) {
        this.function = function;
        this.closure = closure;
        this.initializer = initializer;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(function, environment, initializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment funcScope = new Environment(closure);
        List<Token> parameters = function.getParameters();
        for (int i = 0; i < parameters.size(); i++) funcScope.define(parameters.get(i).getLexeme(), arguments.get(i));
        List<Stmt> body = function.getBody();
        try {
            interpreter.executeBlock(body, funcScope);
        } catch (Return ret) {
            if (initializer) return closure.get("this", 0);
            return ret.getValue();
        }
        if (initializer) return closure.get("this", 0);
        return null;
    }

    @Override
    public int arity() {
        return function.getParameters().size();
    }

    @Override
    public String toString() {
        return "<lox function> " + function.getName().getLexeme();
    }
}
