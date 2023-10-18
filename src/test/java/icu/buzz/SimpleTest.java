package icu.buzz;

import icu.buzz.test.AstPrinter;
import icu.buzz.lox.Expr;
import icu.buzz.lox.Token;
import icu.buzz.lox.TokenType;
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
    public void nested_class() {
        TestImpl impl = new TestImpl();
        OuterClass.Impl_1 inner_1 = new OuterClass.Impl_1();
        inner_1.op_1(impl);
        OuterClass.Impl_2 inner_2 = new OuterClass.Impl_2();
        inner_2.op_1(impl);
    }

    @Test
    public void astPrint() {
        Expr expr = new Expr.Binary(
                new Expr.Unary(new Token("-", TokenType.MINUS, -1, 0), new Expr.Literal(123)),
                new Token("*", TokenType.STAR, -1, 0),
                new Expr.Grouping(new Expr.Literal(45.67)));
        AstPrinter printer = new AstPrinter(expr);
        System.out.println(printer.print());
    }

    @Test
    public void visitorTest() {
        OperationA operation = new OperationA();
        ExtentA a = new ExtentA();
        ExtentB b = new ExtentB();
        a.accept(operation);
        b.accept(operation);
    }

    @Test
    public void methodRef() {
        Consumer<Integer> consumer = this::voidFunWithParameter;
        consumer.accept(1);
    }

    private void testFun(Supplier<?> supplier) {
        supplier.get();
    }

    private void testFun(Runnable runnable) {
        runnable.run();
    }

    private void voidFun() {
        System.out.println("call void function");
    }

    private void voidFunWithParameter(int arg) {
        System.out.println(arg);
    }

    private int intFunWithParameter(int arg1, int arg2) {
        System.out.println("arg1:" + arg1 + ":arg2:" + arg2);
        return arg1 + arg2;
    }

    private int retIntFun() {
        System.out.println("call int ret function");
        return 0;
    }

    private void variableMethod(String... strs) {
        System.out.println(strs[0]);
    }
}

interface Visitor {
    void visit(TopAbstract obj);
}

class OperationA implements Visitor {

    @Override
    public void visit(TopAbstract obj) {
        if (obj instanceof ExtentA) System.out.println("instance:A");
        else if (obj instanceof ExtentB) System.out.println("instance:B");
        else System.out.println("instance:none");
    }
}

abstract class TopAbstract {
    abstract void accept(Visitor visitor);
}

class ExtentA extends TopAbstract {

    @Override
    void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

class ExtentB extends TopAbstract {

    @Override
    void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

class TestImpl implements InterfaceTest {
    @Override
    public void fun(OuterClass arg) {
        if (arg instanceof OuterClass.Impl_1) System.out.println("this is impl1");
        else if (arg instanceof OuterClass.Impl_2) System.out.println("this is impl2");
    }
}

interface InterfaceTest {
    <T extends OuterClass> void fun(T arg);
}

abstract class OuterClass {
    public abstract void op_1(InterfaceTest interfaceTest);

    public static class Impl_1 extends OuterClass {
        @Override
        public void op_1(InterfaceTest interfaceTest) {
            interfaceTest.fun(this);
        }
    }

    public static class Impl_2 extends OuterClass {
        @Override
        public void op_1(InterfaceTest interfaceTest) {
            interfaceTest.fun(this);
        }
    }
}