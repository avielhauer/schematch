package de.uni_marburg.schematch.matching.sota.cupid;
import lombok.Getter;

import java.util.EnumSet;
@Getter
public enum TokenTypes {
    SYMBOLS("SYMBOLS", 0),
    NUMBER("NUMBER", 0.1),
    COMMON_WORDS("COMMON WORDS", 0.1),
    CONTENT("CONTENT", 0.8);

    private final TokenType value;

    TokenTypes(String tokenName, double weight) {
        this.value = new TokenType(tokenName, weight);
    }

    public double getWeight() {
        return value.getWeight();
    }

    public String getTokenName() {
        return value.getTokenName();
    }

    public static EnumSet<TokenTypes> allTokenTypes() {
        return EnumSet.allOf(TokenTypes.class);
    }
}
