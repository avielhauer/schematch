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



    public SchemaElement(String name, String dataType) {
        this.categories = new ArrayList<>();
        this.dataType = dataType;
        this.tokens = new ArrayList<>();
        this.initialName = name;
    }

    public SchemaElement(String name, String dataType, List<String> categories) {
        this.categories = categories;
        this.dataType = dataType;
        this.tokens = new ArrayList<>();
        this.initialName = name;
    }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    public void addToken(Token token) {
        if (token instanceof Token) {
            this.tokens.add(token);
        } else {
            System.out.println("Incorrect token type. The type should be 'Token'");
        }
    }

    public List<String> getTokensData(List<Token> tokens) {
        if (tokens == null) {
            List<String> tokenData = new ArrayList<>();
            for (Token t : this.tokens) {
                tokenData.add(t.getData());
            }
            return tokenData;
        } else {
            List<String> tokenData = new ArrayList<>();
            for (Token t : tokens) {
                tokenData.add(t.getData());
            }
            return tokenData;
        }
    }

    public List<Pair<String, TokenTypes>> getTokensDataAndType(List<Token> tokens) {
        if (tokens == null) {
            List<Pair<String, TokenTypes>> tokenDataAndType = new ArrayList<>();
            for (Token t : this.tokens) {
                tokenDataAndType.add(new Pair<>(t.getData(), t.getTokenType()));
            }
            return tokenDataAndType;
        } else {
            List<Pair<String, TokenTypes>> tokenDataAndType = new ArrayList<>();
            for (Token t : tokens) {
                tokenDataAndType.add(new Pair<>(t.getData(), t.getTokenType()));
            }
            return tokenDataAndType;
        }
    }

    public List<Token> sortByTokenType() {
        List<Token> sortedTokens = new ArrayList<>(this.tokens);
        Collections.sort(sortedTokens, (token1, token2) -> token1.getTokenType().compareTo(token2.getTokenType()));
        return sortedTokens;
    }

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

    public List<Token> getTokens() {
        return tokens;
    }
}
