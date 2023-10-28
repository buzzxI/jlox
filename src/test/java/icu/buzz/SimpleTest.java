package icu.buzz;

import icu.buzz.test.AstPrinter;
import icu.buzz.lox.expr.Expr;
import icu.buzz.lox.token.Token;
import icu.buzz.lox.token.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    public void switch_test(int rst) {
        switch (rst) {
            case 0 -> System.out.println("0");
            case 1 -> System.out.println("1");
            case 2 -> {
                // this is a empty block
            }
            case 3 -> System.out.println("3");
        }
    }

    @Test
    public void string_trim() {
        List<String> types = Arrays.asList("Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right");
        String type = "Binary   : Expr left, Token operator, Expr right";
        String className = type.split(":")[0].trim();
        String fields = type.split(":")[1].trim();
        System.out.printf("class name: %s, fields: %s\n", className, fields);
    }

    @Test
    public void SingleTon() {
        SingleTon instance = SingleTon.get();
        System.out.println(instance);
    }
}

class SingleTon {
    private int value;
    private SingleTon() {}


    private static class SingleTonHolder {
        private static final SingleTon instance = new SingleTon();
    }

    public static SingleTon get() {
        return SingleTonHolder.instance;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

