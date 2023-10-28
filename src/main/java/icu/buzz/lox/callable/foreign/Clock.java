package icu.buzz.lox.callable.foreign;

import icu.buzz.lox.Interpreter;
import icu.buzz.lox.callable.LoxCallable;

import java.util.List;

public class Clock implements LoxCallable {

    // return time from January 1, 1970, 00:00:00 GMT in second
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public String toString() {
        return "<lox native function> clock";
    }
}
