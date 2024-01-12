package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SchemaElement {

    private List<String> categories;
    @Getter
    private String dataType;
    private List<Token> tokens;
    private String initialName;


    private String longName;

    public SchemaElement(String name) {
        this.categories = new ArrayList<>();
        this.dataType = null;
        this.tokens = new ArrayList<>();
        this.initialName = name;
        this.longName = null;
    }

    public void addLongName(String tableName, String tableGuid, String columnName, String columnGuid) {
        this.longName = tableName + tableGuid + columnName + columnGuid;
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
            if (t.getTokenType().equals(tokenType)) {
                resultTokens.add(t);
            }
        }
        return resultTokens;
    }
    public List<String> getCategories() {
        return categories;
    }
    public String getLongName() {
        return longName;
    }
}
