package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.similarity.string.Levenshtein;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.item.ISynset;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.MITWordNet;
import edu.uniba.di.lacam.kdde.ws4j.similarity.WuPalmer;
import edu.uniba.di.lacam.kdde.ws4j.util.WS4JConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
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
            schemaElement = new SchemaElement(element, schemaElement.getDataType());
        }


        Tokenizer tokenizer = new PTBTokenizer(new StringReader(element), new CoreLabelTokenFactory(), "");
        List<String> tokens = tokenizer.tokenize();

        StopWords stopWords = new StopWords();

        for (String token : tokens) {
            Token tokenObj = new Token();

            String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
            if (punctuation.contains(token)) {
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
                    } else if (stopWords.isStopWord(token)) {
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

                Tokenizer tokenizer = new PTBTokenizer(new StringReader(categoryPair.getFirst()), new CoreLabelTokenFactory(), "");
                List<String> tokenStrings = tokenizer.tokenize();
                List<Token> tokens1 = new ArrayList<>();
                for (String tokenString : tokenStrings) {
                    if (StringUtils.isAlphanumeric(tokenString)) {
                        tokens1.add(new Token().addData(tokenString));
                    }
                }

                for (Token token : tokens1) {
                    token.setTokenType(addTokenType(token));
                }

                Tokenizer tokenizer2 = new PTBTokenizer(new StringReader(categoryPair.getSecond()), new CoreLabelTokenFactory(), "");
                List<String> tokenStrings2 = tokenizer2.tokenize();
                List<Token> tokens2 = new ArrayList<>();
                for (String tokenString : tokenStrings2) {
                    if (StringUtils.isAlphanumeric(tokenString)) {
                        tokens2.add(new Token().addData(tokenString));
                    }
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

        double maxScore = s_cat.stream()
                .mapToDouble(c -> compatibilityTable.getOrDefault(c, Map.of()).entrySet().stream()
                        .filter(e -> t_cat.contains(e.getKey()))
                        .mapToDouble(Map.Entry::getValue)
                        .max().orElse(0.0))
                .max().orElse(0.0);

        double nameSimilarityScore = nameSimilarityElements(s, t);

        return new Pair<>(new StringPair(s.getInitialName(), t.getInitialName()), nameSimilarityScore * maxScore);
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
        try {
            double sum1 = getPartialSimilarity(tokenSet1, tokenSet2);
            double sum2 = getPartialSimilarity(tokenSet2, tokenSet1);
            return (sum1 + sum2) / (tokenSet1.size() + tokenSet2.size());
        } catch (Exception ignored) {

        }
        return 0.0;
    }

    private double getPartialSimilarity(List<Token> tokenSet1, List<Token> tokenSet2) throws IOException {

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

    private Set<ISynset> getSynonyms(String word) throws IOException {
        WordNetFunctionalities wordNet = new WordNetFunctionalities();
        return wordNet.getAllSynonymsets(word);
    }

    private double computeSimilarityWordnet(String word1, String word2) {
        // check (word1 & word2 not in lemmas) is not nessecary, bc for every IWord, the method .getLemma() is never null
        try {
            Set<ISynset> allSyns1 = getSynonyms(word1);
            Set<ISynset> allSyns2 = getSynonyms(word2);

            if (allSyns1.size() == 0 || allSyns2.size() == 0) {
                return Double.NaN;
            }

            List<Pair<ISynset, ISynset>> productOfBothISynsetSets = new ArrayList<>();
            for (ISynset iSynset1 : allSyns1) {
                for (ISynset iSynset2 : allSyns2) {
                    productOfBothISynsetSets.add(new Pair<>(iSynset1, iSynset2));
                }
            }

            double max = -1.0;
            int index = 1;
            WordNetFunctionalities wordNetFunctionalities = new WordNetFunctionalities();
            WS4JConfiguration.getInstance().setMemoryDB(false);
            WS4JConfiguration.getInstance().setMFS(true);
            ILexicalDatabase db = new MITWordNet(new RAMDictionary(wordNetFunctionalities.dict, 2));
            WuPalmer wuPalmer = new WuPalmer(db);

            for (Pair<ISynset, ISynset> pair : productOfBothISynsetSets) {
                ISynset s1 = pair.getFirst();
                ISynset s2 = pair.getSecond();
                double score = wuPalmer.calcRelatednessOfWords(s1.getWords().get(0).getLemma(), s2.getWords().get(0).getLemma());
                System.out.println(index + ". " + s1.getWords().get(0).getLemma() + ", " + s2.getWords().get(0).getLemma() + ": " + score);
                index++;

                if (score > max) {
                    max = score;
                }

            }
            if (max == -1.0) {
                return Double.NaN;
            }
            return max;


        } catch (
                Exception e) {
            return Double.NaN;
        }

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
            List<Token> t2 = element2.getTokensByTokenType(tt.getTokenName());
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

        for (String c1 : categoriesE1) {

            Tokenizer tokenizer = new PTBTokenizer(new StringReader(c1), new CoreLabelTokenFactory(), "");
            List<String> c1TokenStrings = tokenizer.tokenize();
            List<Token> c1Token = new ArrayList<>();
            for (String tokenString : c1TokenStrings) {
                c1Token.add(new Token().addData(tokenString));
            }

            for (String c2 : categoriesE2) {

                Tokenizer tokenizer2 = new PTBTokenizer(new StringReader(c2), new CoreLabelTokenFactory(), "");
                List<String> c2TokenStrings = tokenizer2.tokenize();
                List<Token> c2Token = new ArrayList<>();
                for (String tokenString : c2TokenStrings) {
                    c2Token.add(new Token().addData(tokenString));
                }

                double nameSimilarityCategories = nameSimilarityTokens(c1Token, c2Token);

                if (nameSimilarityCategories > maxCategory) {
                    maxCategory = nameSimilarityCategories;
                }
            }
        }
        return maxCategory;
    }

    public static void main(String[] args) throws IOException {
        LinguisticMatching linguisticMatching = new LinguisticMatching();

        System.out.println(linguisticMatching.computeSimilarityWordnet("ship", "boat"));

    }
}
