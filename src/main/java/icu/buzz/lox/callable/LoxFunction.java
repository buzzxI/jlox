package icu.buzz.lox.callable;

import icu.buzz.lox.Environment;
import icu.buzz.lox.Interpreter;
import icu.buzz.lox.exceptions.Return;
import icu.buzz.lox.stmt.Stmt;
import icu.buzz.lox.token.Token;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Fun function;
    private final Environment closure;

    public LoxFunction(Stmt.Fun function, Environment closure) {
        this.function = function;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment funcScope = new Environment(closure);
        List<Token> parameters = function.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            funcScope.define(parameters.get(i).getLexeme(), arguments.get(i));
        }
        List<Stmt> body = function.getBody();
        try {
            interpreter.executeBlock(body, funcScope);
        } catch (Return ret) {
           return ret.getValue();
        }
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
