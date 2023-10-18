package icu.buzz.lox;

public class Token {
    // token name
    private final String lexeme;
    private final TokenType type;
    // literal value if exists, for most lexemes, this field should be null
    private final Object value;
    private final LocationInfo locationInfo;

    /**
     * inner class LocationInfo
     * this class is used to record token position
     */
    static class LocationInfo {
        // line number in source code
        private final int line;
        // column number in source code (offset is measured in characters not tokens)
        private final int offset;

        public LocationInfo(int line) {
            this(line, -1);
        }

        public LocationInfo(int line, int offset) {
            this.line = line;
            this.offset = offset;
        }

        public int getLine() {
            return line;
        }

        public int getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            String rst = "line number:" + this.line;
            if (offset >= 0) rst += " " + "column offset:" + this.offset;
            return rst;
        }
    }

    public Token(String lexeme, TokenType type, Object value, int line) {
        this.lexeme = lexeme;
        this.type = type;
        this.value = value;
        this.locationInfo = new LocationInfo(line);
    }

    public Token(String lexeme, TokenType type, Object value, int line, int offset) {
        this.lexeme = lexeme;
        this.type = type;
        this.value = value;
        locationInfo = new LocationInfo(line, offset);
    }

    @Override
    public String toString() {
        return "lexeme:" + this.lexeme + " " +
                "type:" + type + " " +
                "value:" + value + " " +
                this.locationInfo;
    }

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }
}
