package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Setter;

public class Token {
    @Setter
    private boolean ignore;
    private String data;
    @Setter
    private TokenTypes tokenType;

    /**
     * Initiates empty Token object.
     */
    public Token() {
        this.ignore = false;
        this.data = null;
        this.tokenType = null;
    }

    /**
     * Sets data to the Token object
     * @param data data which should be set as the tokens data
     * @return this token object
     */
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

    public String getData() {
        return data;
    }

    /**
     * @return this token objects token type
     */
    public TokenTypes getTokenType() {
        return tokenType;
    }

}
