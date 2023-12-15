package de.uni_marburg.schematch.matching.sota.cupid;

public class TokenType {
    private String tokenName;
    private double weight;

    public TokenType(String tokenName, double weight) {
        this.tokenName = tokenName;
        this.weight = weight;
    }

    public String getTokenName() {
        return tokenName;
    }

    public double getWeight() {
        return weight;
    }

    public int compareTo(TokenType tokenType) {
        return 0;
    }
}
