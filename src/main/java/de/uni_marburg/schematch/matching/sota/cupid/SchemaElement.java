package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SchemaElement {

    @Getter
    private List<String> categories;
    @Getter
    private String dataType;
    private List<Token> tokens;
    @Getter
    private String initialName;


    /**
     * Initiates a schema element, with the given name and data type
     * @param name name of the schema element
     * @param dataType data type of the schema element
     */
    public SchemaElement(String name, String dataType) {
        this.categories = new ArrayList<>();
        this.dataType = dataType;
        this.tokens = new ArrayList<>();
        this.initialName = name;
    }

    /**
     * Adds new category to the schema element object.
     * @param category category to be added (String)
     */
    public void addCategory(String category) {
        this.categories.add(category);
    }

    /**
     * Adds token to the schema element
     * @param token token to be added (Token)
     */
    public void addToken(Token token) {
        if (token instanceof Token) {
            this.tokens.add(token);
        } else {
            System.out.println("Incorrect token type. The type should be 'Token'");
        }
    }

    /**
     * Returns a sorted list of tokens
     * @return sorted list of token (List<Token>)
     */
    public List<Token> sortByTokenType() {
        List<Token> sortedTokens = new ArrayList<>(this.tokens);
        Collections.sort(sortedTokens, (token1, token2) -> token1.getTokenType().compareTo(token2.getTokenType()));
        return sortedTokens;
    }

    /**
     * Returns all tokens that are the given token type
     * @param tokenType token type (String)
     * @return tokens with type=tokenType (List<Token>)
     */
    public List<Token> getTokensByTokenType(String tokenType) {
        List<Token> sortedTokens = this.sortByTokenType();
        List<Token> resultTokens = new ArrayList<>();
        for (Token t : sortedTokens) {
            if (t.getTokenType().getTokenName().equals(tokenType)) {
                resultTokens.add(t);
            }
        }
        return resultTokens;
    }

    /**
     * @return all token (List<Token>)
     */
    public List<Token> getTokens() {
        return tokens;
    }
}
