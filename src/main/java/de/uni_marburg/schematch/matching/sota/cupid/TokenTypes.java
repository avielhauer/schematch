package de.uni_marburg.schematch.matching.sota.cupid;
import java.util.EnumSet;
public enum TokenTypes {
    SYMBOLS("symbols", 0),
    NUMBER("number", 0.1),
    COMMON_WORDS("common words", 0.1),
    CONTENT("content", 0.8);

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

    public TokenType getValue() {
        return value;
    }

    public static EnumSet<TokenTypes> allTokenTypes() {
        return EnumSet.allOf(TokenTypes.class);
    }
}
