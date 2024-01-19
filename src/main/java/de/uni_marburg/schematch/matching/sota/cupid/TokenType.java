package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

@Getter
public class TokenType {
    private final String tokenName;
    private final double weight;

    public TokenType(String tokenName, double weight) {
        this.tokenName = tokenName;
        this.weight = weight;
    }

}
