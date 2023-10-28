package icu.buzz.lox;

import icu.buzz.lox.stmt.Stmt;
import icu.buzz.lox.token.Token;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Lox {
    private static boolean hasError = false;

    public static final int MAX_ARGS = 255;

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
            System.out.flush();
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
        if (hasError) System.exit(65);
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hasError) System.exit(65);
        Interpreter interpreter = new Interpreter(statements);
        interpreter.interpret();
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