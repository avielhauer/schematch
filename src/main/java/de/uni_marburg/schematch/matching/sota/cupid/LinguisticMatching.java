package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.similarity.string.Levenshtein;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LinguisticMatching {

    private String snakeCase(String name) {
        String s1 = name.replaceAll("(.)([A-Z][a-z]+)", "$1_$2");
        return s1.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    public SchemaElement normalization(String element,
                                       SchemaElement schemaElement) {
        if (schemaElement == null) {
            schemaElement = new SchemaElement(element);
        }
        try {
            // TODO: 17.12.2023 nltk.word_tokenize

        } catch (Exception e) {
            // TODO: 17.12.2023 same library
        }


        // Dummy
        List<String> tokens = new ArrayList<>();

        for (String token : tokens) {
            Token tokenObj = new Token();

            // TODO: token in string.punctuation
            if (true) {
                tokenObj.setIgnore(true);
                tokenObj.addData(token);
                // welche bedeutung hat die Klassen TokenType?
                tokenObj.setTokenType(TokenTypes.SYMBOLS);
                schemaElement.addToken(tokenObj);
            } else {
                try {
                    Float.parseFloat(token);
                    tokenObj.addData(token);
                    tokenObj.setTokenType(TokenTypes.NUMBER);
                    schemaElement.addToken(tokenObj);
                } catch (Exception e) {
                    String tokenSnake = snakeCase(token);

                    if (tokenSnake.contains("_")) {
                        tokenSnake.replace("_", " ");
                        schemaElement = normalization(tokenSnake, schemaElement);
                    } else if (true) { // token.lower() in stopwords.words('english'):
                        tokenObj.addData(token.toLowerCase(Locale.ROOT));
                        tokenObj.setIgnore(true);
                        tokenObj.setTokenType(TokenTypes.COMMON_WORDS);
                        schemaElement.addToken(tokenObj);
                    } else {
                        tokenObj.addData(token.toLowerCase(Locale.ROOT));
                        tokenObj.setTokenType(TokenTypes.CONTENT);
                        schemaElement.addToken(tokenObj);
                    }
                }
            }
        }

        return schemaElement;
    }

    private TokenTypes addTokenType(Token token) {
        try {
            Float.parseFloat(token.getData());
            return TokenTypes.NUMBER;
        } catch (Exception e) {
            return TokenTypes.CONTENT;
        }
    }

    public Map<String, Map<String, Double>> computeCompatibility(List<String> categories) {
        Map<String, Map<String, Double>> compatibilityTable = new HashMap<>();
        List<StringPair> combinations = new ArrayList<>();
        Map<String, Map<String, Double>> dataCompatibilityTable = new DataCompatibilityTable().table;

        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < categories.size(); j++) {
                StringPair s = new StringPair(categories.get(i), categories.get(j));
                combinations.add(s);
            }
        }

        for (StringPair categoryPair : combinations) {
            if (!compatibilityTable.containsKey(categoryPair.getFirst())) {
                compatibilityTable.put(categoryPair.getFirst(), new HashMap<>());
            }

            if (!compatibilityTable.containsKey(categoryPair.getSecond())) {
                compatibilityTable.put(categoryPair.getSecond(), new HashMap<>());
            }

            if (categoryPair.getFirst().equals(categoryPair.getSecond())) {
                compatibilityTable.get(categoryPair.getFirst()).put(categoryPair.getSecond(), 1.0);
                compatibilityTable.get(categoryPair.getSecond()).put(categoryPair.getFirst(), 1.0);
            } else if (dataCompatibilityTable.containsKey(categoryPair.getFirst()) &&
                    dataCompatibilityTable.get(categoryPair.getFirst()).containsKey(categoryPair.getSecond())) {
                compatibilityTable.get(categoryPair.getFirst())
                        .put(categoryPair.getSecond(), dataCompatibilityTable.get(categoryPair.getFirst())
                                .get(categoryPair.getSecond()));

                compatibilityTable.get(categoryPair.getSecond())
                        .put(categoryPair.getFirst(), dataCompatibilityTable.get(categoryPair.getSecond())
                                .get(categoryPair.getFirst()));
            } else {
                // TODO: 17.12.2023 nltk.world library
                List<Token> tokens1 = new ArrayList<>();
                List<Token> tokens2 = new ArrayList<>();

                for (Token token : tokens1) {
                    token.setTokenType(addTokenType(token));
                }

                for (Token token : tokens2) {
                    token.setTokenType(addTokenType(token));
                }

                double compatibility = dataTypeSimilarity(tokens1, tokens2);
                compatibilityTable.get(categoryPair.getFirst()).put(categoryPair.getSecond(), compatibility);
                compatibilityTable.get(categoryPair.getSecond()).put(categoryPair.getFirst(), compatibility);
            }
        }
        return compatibilityTable;
    }

    public Map<StringPair, Double> comparison(SchemaTree sourceTree,
                                                  SchemaTree targetTree,
                                                  Map<String, Map<String, Double>> compatibilityTable,
                                                  double thNs,
                                                  int parallelism) {
        List<Pair<SchemaElementNode, SchemaElementNode>> elementsToCompare = generateParallelLsimInput(sourceTree, targetTree, compatibilityTable, thNs);
        Map<StringPair, Double> lsims = new HashMap<>();

        if (parallelism == 1) {
            for (Pair<SchemaElementNode, SchemaElementNode> pairNode : elementsToCompare) {
                Pair<SchemaElement, SchemaElement> pair = new Pair<>(pairNode.getFirst().current, pairNode.getFirst().current);
                Pair<StringPair, Double> lsimProcVal = lsimProc(pair, compatibilityTable);
                lsims.put(lsimProcVal.getFirst(), lsimProcVal.getSecond());
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(parallelism);
            for (Pair<SchemaElementNode, SchemaElementNode> pairNode : elementsToCompare) {
                Pair<SchemaElement, SchemaElement> pair = new Pair<>(pairNode.getFirst().current, pairNode.getFirst().current);
                executor.submit(() -> {
                    Pair<StringPair, Double> lsimProcVal = lsimProc(pair, compatibilityTable);
                    lsims.put(lsimProcVal.getFirst(), lsimProcVal.getSecond());
                });
            }
            executor.shutdown();
        }
        return lsims;
    }

    private List<Pair<SchemaElementNode, SchemaElementNode>> generateParallelLsimInput(SchemaTree sourceTree,
                                                                                       SchemaTree targetTree,
                                                                                       Map<String, Map<String, Double>> compatibilityTable,
                                                                                       double thNs) {

        List<Pair<SchemaElementNode, SchemaElementNode>> result = new ArrayList<>();

        List<SchemaElementNode> allNodesS = levelOrderTraversal(sourceTree.getRoot());
        List<SchemaElementNode> allNodesT = levelOrderTraversal(targetTree.getRoot());
        List<Pair<SchemaElementNode, SchemaElementNode>> allNodes = new ArrayList<>();
        for (SchemaElementNode nodeS : allNodesS) {
            for (SchemaElementNode nodeT : allNodesT) {
                allNodes.add(new Pair<>(nodeS, nodeT));
            }
        }

        for (Pair<SchemaElementNode, SchemaElementNode> pair : allNodes) {
            try {
                if (compatibilityTable.containsKey(pair.getFirst().getCurrent().getCategories().get(0)) &&
                    compatibilityTable.containsKey(pair.getSecond().getCurrent().getCategories().get(0)) &&
                        compatibilityTable.get(pair.getFirst().getCurrent().getCategories().get(0))
                        .get(pair.getSecond().getCurrent().getCategories().get(0)) > thNs) {
                    result.add(pair);
                }
            } catch (Exception ignored) {
            }
        }


        return result;
    }

    private static List<SchemaElementNode> levelOrderTraversal(SchemaElementNode root) {
        if (root == null) {
            return null;
        }
        List<SchemaElementNode> nodes = new ArrayList<>();

        Queue<SchemaElementNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            SchemaElementNode current = queue.poll();
            nodes.add(current);
            for (SchemaElementNode child : current.children) {
                queue.offer(child);
            }
        }
        return nodes;
    }

    private Pair<StringPair, Double> lsimProc(Pair<SchemaElement, SchemaElement> pair, Map<String, Map<String, Double>> compatibilityTable) {
        SchemaElement s = pair.getFirst();
        SchemaElement t = pair.getSecond();

        List<String> s_cat = s.getCategories();
        List<String> t_cat = t.getCategories();

        // TODO: 17.12.2023 Tf ist diese Listcomprehension
        return null;
    }

    private double dataTypeSimilarity(List<Token> tokenSet1, List<Token> tokenSet2) {
        double sum1 = 0;
        double sum2 = 0;

        for (TokenTypes tt : TokenTypes.allTokenTypes()) {
            if (tt == TokenTypes.SYMBOLS) {
                continue;
            }

            List<Token> t1 = new ArrayList();
            for (Token token : tokenSet1) {
                if (token.getTokenType().equals(tt)) {
                    t1.add(token);
                }
            }

            List<Token> t2 = new ArrayList();
            for (Token token : tokenSet2) {
                if (token.getTokenType().equals(tt)) {
                    t2.add(token);
                }
            }
            if (t1.size() == 0 || t2.size() == 0) {
                continue;
            }
            double sim = nameSimilarityTokens(t1, t2);
            sum1 += tt.getWeight() * sim;
            sum2 += tt.getWeight();
        }
        if (sum1 == 0 || sum2 == 0) {
            return 0;
        }
        return sum1 / sum2;
    }

    private double nameSimilarityTokens(List<Token> tokenSet1, List<Token> tokenSet2) {
        double sum1 = getPartialSimilarity(tokenSet1, tokenSet2);
        double sum2 = getPartialSimilarity(tokenSet2, tokenSet1);
        return (sum1 + sum2) / (tokenSet1.size() + tokenSet2.size());
    }

    private double getPartialSimilarity(List<Token> tokenSet1, List<Token> tokenSet2) {

        double totalSum = 0;
        for (Token t1 : tokenSet1) {
            double max_sim = Double.MIN_VALUE;
            double sim;
            for (Token t2 : tokenSet2) {
                if (t1.getData().equals(t2.getData())) {
                    sim = 1.0;
                } else {
                    sim = computeSimilarityWordnet(t1.getData(), t2.getData());
                    if (Double.isNaN(sim)) {
                        sim = computeSimilarityLeven(t1.getData(), t2.getData());
                    }
                }
                if (sim > max_sim) {
                    max_sim = sim;
                }
            }
            totalSum += max_sim;
        }
        return totalSum;
    }

    private Set<String> getSynonyms(String word) {
        // TODO: 17.12.2023  
        
        return null;
    }

    private float computeSimilarityWordnet(String word1, String word2) {
        // TODO: 17.12.2023  
        return 0;
    }

    private float computeSimilarityLeven(String word1, String word2) {
        Levenshtein levenshtein = new Levenshtein();
        return levenshtein.compare(word1, word2);
    }

    private double nameSimilarityElements(SchemaElement element1, SchemaElement element2) {
        int sum1 = 0;
        int sum2 = 0;

        for (TokenTypes tt : TokenTypes.allTokenTypes()) {
            if (tt == TokenTypes.SYMBOLS) {
                continue;
            }
            List<Token> t1 = element1.getTokensByTokenType(tt.getTokenName());
            List<Token> t2 = element1.getTokensByTokenType(tt.getTokenName());
            if (t1.size() == 0 || t2.size() == 0) {
                continue;
            }
            double sim = nameSimilarityTokens(t1, t2);
            sum1 += tt.getWeight() * sim;
            sum2 += tt.getWeight();

        }

        if (sum1 == 0 || sum2 == 0) {
            return 0;
        }
        return sum1 / sum2;
    }

    private double computeLsim(SchemaElement element1, SchemaElement element2) {
        double nameSimilarity = nameSimilarityElements(element1, element2);
        double maxCategory = getMaxNsCategory(element1.getCategories(), element2.getCategories());
        return nameSimilarity * maxCategory;
    }

    private double getMaxNsCategory(List<String> categoriesE1, List<String> categoriesE2) {
        double maxCategory = Double.MIN_VALUE;

        // TODO: 17.12.2023 nltk.word_tokens for c1Tokens and c2Tokens
        for (String c1 : categoriesE1) {
            List<Token> c1Token = new ArrayList<>();
            for (String c2 : categoriesE2) {
                List<Token> c2Token = new ArrayList<>();
                double nameSimilarityCategories = nameSimilarityTokens(c1Token, c2Token);
                
                if (nameSimilarityCategories > maxCategory) {
                    maxCategory = nameSimilarityCategories;
                }
            }
        }
        return maxCategory;
    }


}
