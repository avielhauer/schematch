package de.uni_marburg.schematch.matching.sota.cupid;

public class Token {
    private boolean ignore;
    private String data;
    private TokenTypes tokenType;

    public Token() {
        this.ignore = false;
        this.data = null;
        this.tokenType = null;
    }

    public Token addData(String data) {
        this.data = data;
        return this;
    }

    public String toString() {
        return this.data;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public String getData() {
        return data;
    }

    public TokenTypes getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenTypes tokenType) {
        this.tokenType = tokenType;
    }
}
