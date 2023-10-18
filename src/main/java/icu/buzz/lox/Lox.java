package icu.buzz.lox;

import icu.buzz.exceptions.InterpreterError;
import icu.buzz.test.AstPrinter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Lox {
    private static boolean hasError = false;

    /**
     * parse lox source file from path @param: path
     */
    private static void parseFile(String path) throws IOException {
        byte[] buffer = Files.readAllBytes(Path.of(path));
        // source file will be considered as coded in UTF-8
        runLox(new String(buffer, StandardCharsets.UTF_8));
    }

    /**
     * parse lox script in prompt
     */
    private static void parsePrompt() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        for (;;) {
            System.out.print(">");
            String script = reader.readLine();
            // user types Ctrl + D => EOF, command line terminates
            if (script == null) break;
            runLox(script);
        }
    }

    /**
     * source will be considered as lox source file to execute
     */
    private static void runLox(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        System.out.println(tokens);
        if (hasError) System.exit(65);
        Parser parser = new Parser(tokens);
        Expr expr = parser.parseExpr();
        if (hasError) System.exit(65);
        Interpreter interpreter = new Interpreter(expr);
        try {
            System.out.println(interpreter.interpret());
        } catch (InterpreterError e) {
            System.err.println("Running lox script error:" + e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: java Lox [script file]");
            System.exit(64);
        } else if (args.length == 1) parseFile(args[0]);
        else parsePrompt();
    }

    /**
     * report an error
     * @param token token with error
     * @param message error message
     */
    public static void errorReport(Token token, String message) {
        errorReport(token.getLocationInfo().getLine(), token.getLocationInfo().getOffset(), message);
    }

    /**
     * report an error
     * @param line line number for error message
     * @param column not use for now
     * @param message error message
     */
    public static void errorReport(int line, int column, String message) {
        System.err.println("line [" + line + "] column [" + column + "] has Error: " + message);
        hasError = true;
    }
}