package icu.buzz.lox;

import icu.buzz.lox.token.Token;
import icu.buzz.lox.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private static final Map<String, TokenType> keywords;
    private final String source;
    private final List<Token> tokenList;
    // start position of a token
    private int start;
    // current position while scanning
    private int current;
    // current line while scanning
    private int line;
    // valid column starts from 1
    // start column of a token
    private int column;
    // current column while scanning
    private int cur_column;

    static {
        // "while" is the last keyword token, "and" is the first keyword token
        keywords = new HashMap<>(TokenType.WHILE.ordinal() - TokenType.AND.ordinal() + 1);
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fun", TokenType.FUN);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    public Scanner(String source) {
        this.source = source;
        tokenList = new ArrayList<>();
        this.start = 0;
        this.current = 0;
        this.line = 1;
        this.column = 1;
        this.cur_column = 1;
    }

    public List<Token> scanTokens() {
        for (; notEnd(); start = current, column = cur_column) scanToken();
        tokenList.add(new Token("", TokenType.EOF, null, line, column));
        return tokenList;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // these cases are used for single character (except for '/')
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);

            // these cases are used for single or double characters
            case '!' -> {
                if (match('=')) addToken(TokenType.BANG_EQUAL);
                else addToken(TokenType.BANG);
            }
            case '=' -> {
                if (match('=')) addToken(TokenType.EQUAL_EQUAL);
                else addToken(TokenType.EQUAL);
            }
            case '>' -> {
                if (match('=')) addToken(TokenType.GREATER_EQUAL);
                else addToken(TokenType.GREATER);
            }
            case '<' -> {
                if (match('=')) addToken(TokenType.LESS_EQUAL);
                else addToken(TokenType.LESS);
            }

            // case for slash symbol, slash may construct a comment
            case '/' -> scanSlash();

            // blank character
            case ' ', '\r', '\t' -> {}
            // newline character
            case '\n' -> {
                line++;
                cur_column = 0;
            }

            // string literal
            case '"' -> scanString();

            default -> {
                // number literal
                if (isDigit(c)) scanNumber();
                else if (isAlpha(c)) scanIdentifier();

                else Lox.errorReport(line, column, "Unexpected character");
            }
        }

    }

    private void scanString() {
        char next_char;
        while ((next_char = peek(0)) != '\0' && next_char != '"')  {
            if (next_char == '\n') {
                line++;
                cur_column = 0;
            }
            advance();
        }
        // error report
        if (next_char == '\0') {
            Lox.errorReport(line, column, "Unterminated string");
            return;
        }
        advance();
        // trim the source without ""
        addToken(TokenType.STRING, source.substring(start + 1, current - 1));
    }

    private void scanNumber() {
        char next_char;
        while (isDigit(next_char = peek(0))) advance();

        if (next_char == '.') {
            if (isDigit(peek(1))) {
                advance();
                while (isDigit(peek(0))) advance();
            }
            else {
                Lox.errorReport(line, column, "Unexpected symbol '.' without tailing digital number");
                return;
            }
        }
        // parse all number token into double value
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void scanSlash() {
        if (match('/')) {
            // single line comment
            while (notEnd() && peek(0) != '\n') advance();
            if (notEnd()) advance();
            line++;
            cur_column = 0;
        } else if (match('*')) {
            // multiple line comment
            int flag = 0;
            for (char next_char = peek(0), next_next_char = peek(1);
                 next_char != '\0' && next_next_char != '\0';
                 advance(), next_char = peek(0), next_next_char = peek(1)) {
                if (next_char == '*' && next_next_char == '/') {
                    flag = 1;
                    break;
                }
                if (next_char == '\n') {
                    line++;
                    cur_column = 0;
                }
            }
            if (flag == 1) {
                // flip to next_char => '*'
                advance();
                // flip to next_next_char => '/'
                advance();
            } else Lox.errorReport(line, column, "Unterminated comment");
        } else addToken(TokenType.SLASH);
    }

    private void scanIdentifier() {
        while (isAlphaNumeric(peek(0))) advance();

        String value = source.substring(start, current);
        addToken(keywords.getOrDefault(value, TokenType.IDENTIFIER));
    }

    /**
     * find if @param: c is digit number
     * @param c target character
     * @return comparison result
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * find if @param: c is an alpha => letter or underscore
     * @param c target character
     * @return comparison result
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * find if @param: c is an alpha or number
     * @param c target character
     * @return comparison result
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * find if current has reach the end
     * @return comparison result
     */
    private boolean notEnd() {
        return current != source.length();
    }

    /**
     * find if current character is the same as @param: target
     * @param target target character
     * @return comparison result
     */
    private boolean match(char target) {
        if (current == source.length()) return false;
        if (source.charAt(current) != target) return false;
        current++;
        cur_column++;
        return true;
    }

    /**
     * peek next with bias
     * @param offset offset from current
     * @return peek the character at current + @param: offset
     */
    private char peek(int offset) {
        return current + offset >= source.length() ? '\0' : source.charAt(current + offset);
    }

    /**
     * fetch character and flip current
     * @return character at current
     */
    private char advance() {
        cur_column++;
        return source.charAt(current++);
    }

    /**
     * add a token to token list with @param: type
     * @param type token type
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * add a token to token list with @param: type, @param: literal
     * @param type token type
     * @param literal token literal => generally literal is null, except for literal-type token
     */
    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokenList.add(new Token(lexeme, type, literal, line, column));
    }
}
