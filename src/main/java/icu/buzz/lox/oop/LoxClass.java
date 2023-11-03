package icu.buzz.lox.oop;

import icu.buzz.lox.Interpreter;
import icu.buzz.lox.callable.LoxCallable;
import icu.buzz.lox.callable.LoxFunction;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {

    private final String name;
    private final Map<String, LoxFunction> methods;

    public LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    public LoxFunction getMethod(String name) {
        return methods.getOrDefault(name, null);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "<lox class> " + this.name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = getMethod("init");
        if (initializer != null) initializer.bind(instance).call(interpreter, arguments);
        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = getMethod("init");
        if (initializer != null) return initializer.arity();
        return 0;
    }
}
