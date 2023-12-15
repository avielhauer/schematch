package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.similarity.string.Levenshtein;

import java.util.*;

public class LinguisticMatching {
    private String snakeCase(String name) {
        return "";
    }

    public SchemaElement normalization(String element, SchemaElement schemaElement) {


        return null;
    }

    private TokenType addTokenType(Token token) {

        return null;
    }

    public Map<Pair<String, String>, Float> computeCompatibility(Set<String> categories) {

        return null;
    }

    public void comparison() {

    }

    private List<Pair<SchemaElementNode, SchemaElementNode>> generateParallelLsimInput(SchemaTree sourceTree,
                                      SchemaTree targetTree,
                                      Map<Pair<String, String>, Float> compatibilityTable,
                                      float th_ns) {

        List<Pair<SchemaElementNode, SchemaElementNode>> result = new ArrayList<>();

        List<SchemaElementNode> allNodesS = levelOrderTraversal(sourceTree.getRoot());
        List<SchemaElementNode> allNodesT = levelOrderTraversal(targetTree.getRoot());
        List<Pair<SchemaElementNode, SchemaElementNode>> allNodes = new ArrayList<>();
        for (SchemaElementNode nodeS : allNodesS) {
            for (SchemaElementNode nodeT : allNodesT) {
                allNodes.add(new Pair<>(nodeS, nodeT));
            }
        }

        CompatibilityTable dataTypeCompatibilityTable = new CompatibilityTable();

        for (Pair<SchemaElementNode, SchemaElementNode> pair : allNodes) {
            try {
                dataTypeCompatibilityTable.table.get(pair.getFirst().getCurrent().getCategories().get(0));
                dataTypeCompatibilityTable.table.get(pair.getSecond().getCurrent().getCategories().get(0));
                dataTypeCompatibilityTable.table.get(pair.getFirst().getCurrent().getCategories().get(0));
            } catch (Exception e) {
                continue;
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

    private Pair<Pair<String, String>, Float> lsimProc(Pair<SchemaElement, SchemaElement> pair, Map<Pair<String, String>, Float> compatibilityTable) {
        SchemaElement s = pair.getFirst();
        SchemaElement t = pair.getSecond();

        List<String> s_cat = s.getCategories();
        List<String> t_cat = t.getCategories();

        //double max_s
        return null;
    }

    private double dataTypeSimilarity(List<Token> tokenSet1, List<Token> tokenSet2) {
        double sum1 = 0;
        double sum2 = 0;

        for (TokenTypes tt : TokenTypes.allTokenTypes()) {
            if (tt == TokenTypes.SYMBOLS) {
                continue;
            }

            List t1 = new ArrayList();
            for (Token token : tokenSet1) {
                if (token.getTokenType().equals(tt)) {
                    t1.add(token);
                }
            }

            List t2 = new ArrayList();
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

        return null;
    }

    private float computeSimilarityWordnet(String word1, String word2) {

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

    private float computeLsim(SchemaElement element1, SchemaElement element2) {

        return 0;
    }

    private float getMaxNsCategory(ArrayList<String> categoriesE1, ArrayList<String> categoriesE2) {

        return 0;
    }


}
