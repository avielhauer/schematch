package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.similarity.string.Levenshtein;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.item.ISynset;
import edu.stanford.nlp.ling.CoreLabel;
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
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LinguisticMatching {

    public static void main(String[] args) throws IOException {
        WordNetFunctionalities wnf = new WordNetFunctionalities();
        LinguisticMatching lm = new LinguisticMatching(wnf);
        double a = lm.computeSimilarityWordnet("book","person");
        double b = lm.computeSimilarityWordnet("id","person");
        double c = a+b;
        System.out.println(a+ ","+b+","+c);
    }
    private WordNetFunctionalities wordNetFunctionalities = null;

    private WuPalmer wuPalmer = null;

    private final Levenshtein levenshtein = new Levenshtein();

    public LinguisticMatching(WordNetFunctionalities wordNetFunctionalities) {
        this.wordNetFunctionalities = wordNetFunctionalities;
        WS4JConfiguration.getInstance().setMemoryDB(false);
        WS4JConfiguration.getInstance().setMFS(true);
        ILexicalDatabase db = new MITWordNet(new RAMDictionary(wordNetFunctionalities.dict, 2));
        this.wuPalmer = new WuPalmer(db);
    }

    /**
     * Konvertiert den gegebenen Zeichenfolgen-`name` in das "snake_case"-Format.
     *
     * @param name Die Eingabezeichenfolge, die in das "snake_case"-Format konvertiert werden soll.
     * @return Die Zeichenfolge `name` im "snake_case"-Format.
     */
    private static String snakeCase(String name) {
        String s1 = name.replaceAll("(.)([A-Z][a-z]+)", "$1_$2");
        return s1.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Normalizes the given `element` and updates the `schemaElement` accordingly.
     * Normalization involves tokenizing the element, categorizing tokens into different types
     * (e.g., symbols, numbers, common words, content), and adding them to the `schemaElement`.
     *
     * @param element       The input string to be normalized.
     * @param schemaElement The SchemaElement object to be updated with normalized tokens.
     *                      If null, a new SchemaElement is created with the given `element`.
     * @return The SchemaElement object containing the normalized tokens.
     */
    public static SchemaElement normalization(String element,
                                       SchemaElement schemaElement) {
        if (schemaElement == null) {
            schemaElement = new SchemaElement(element, schemaElement.getDataType());
        }


        Tokenizer tokenizer = new PTBTokenizer(new StringReader(element), new CoreLabelTokenFactory(), "");
        List<CoreLabel> tokens = tokenizer.tokenize();
        StopWords stopWords = new StopWords();

        for (CoreLabel tokenCoreLabel : tokens) {
            String token = tokenCoreLabel.originalText();
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
                        tokenSnake = tokenSnake.replace("_", " ");
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

    /**
     * Determines the TokenType for the given Token based on its data.
     * If the Token data can be parsed as a Float, it is considered a number; otherwise, it is considered content.
     *
     * @param token The Token object for which the TokenType is to be determined.
     * @return The TokenType of the Token based on its data.
     */
    private TokenTypes addTokenType(Token token) {
        try {
            Float.parseFloat(token.getData());
            return TokenTypes.NUMBER;
        } catch (Exception e) {
            return TokenTypes.CONTENT;
        }
    }


    /**
     * Computes the compatibility between categories based on their similarity in data types and semantic meaning.
     *
     * @param categories A set of categories for which compatibility is to be computed.
     * @return A map representing the compatibility table between categories, where each entry
     *         contains a map of compatibility scores between category pairs.
     */
    public Map<String, Map<String, Double>> computeCompatibility(Set<String> categories) {
        Map<String, Map<String, Double>> compatibilityTable = new HashMap<>();
        List<StringPair> combinations = new ArrayList<>();
        Map<String, Map<String, Double>> dataCompatibilityTable = new DataCompatibilityTable().table;

        for (String s: categories) {
            for (String t: categories) {
                StringPair p = new StringPair(s,t);
                combinations.add(p);
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
                List<CoreLabel> tokenStrings = tokenizer.tokenize();
                List<Token> tokens1 = new ArrayList<>();
                for (CoreLabel tokenStringCoreLabel : tokenStrings) {
                    String tokenString = tokenStringCoreLabel.originalText();
                    if (StringUtils.isAlphanumeric(tokenString)) {
                        tokens1.add(new Token().addData(tokenString));
                    }
                }

                for (Token token : tokens1) {
                    token.setTokenType(addTokenType(token));
                }

                Tokenizer tokenizer2 = new PTBTokenizer(new StringReader(categoryPair.getSecond()), new CoreLabelTokenFactory(), "");
                List<CoreLabel> tokenStrings2 = tokenizer2.tokenize();
                List<Token> tokens2 = new ArrayList<>();
                for (CoreLabel tokenStringCoreLabel : tokenStrings2) {
                    String tokenString = tokenStringCoreLabel.originalText();
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

    /**
     * Compares schema elements between the source and target schema trees using parallel processing,
     * computes their similarity, and returns a map of similarity scores.
     *
     * @param sourceTree          The source schema tree.
     * @param targetTree          The target schema tree.
     * @param compatibilityTable A compatibility table containing precomputed compatibility scores between categories.
     * @param thNs                The threshold for considering similarity between schema elements.
     * @param parallelism         The level of parallelism for concurrent processing.
     * @return A map containing similarity scores between schema element pairs.
     */
    public Map<StringPair, Float> comparison(SchemaTree sourceTree,
                                              SchemaTree targetTree,
                                              Map<String, Map<String, Double>> compatibilityTable,
                                              double thNs,
                                              int parallelism) {
        List<Pair<SchemaElementNode, SchemaElementNode>> elementsToCompare = generateParallelLsimInput(sourceTree, targetTree, compatibilityTable, thNs);
        Map<StringPair, Float> lsims = new HashMap<>();

        if (parallelism == 1) {
            for (Pair<SchemaElementNode, SchemaElementNode> pairNode : elementsToCompare) {
                Pair<SchemaElement, SchemaElement> pair = new Pair<>(pairNode.getFirst().current, pairNode.getSecond().current);
                Pair<StringPair, Double> lsimProcVal = lsimProc(pair, compatibilityTable);
                lsims.put(lsimProcVal.getFirst(), lsimProcVal.getSecond().floatValue());
            }
        } else {
            List<Future<Pair<StringPair, Double>>> futures = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(parallelism);
            for (Pair<SchemaElementNode, SchemaElementNode> pairNode : elementsToCompare) {
                Pair<SchemaElement, SchemaElement> pair = new Pair<>(pairNode.getFirst().current, pairNode.getSecond().current);
                Future<Pair<StringPair, Double>> future = executor.submit(() -> lsimProc(pair, compatibilityTable));
                futures.add(future);
            }
            try {
                for (Future<Pair<StringPair, Double>> future: futures) {
                    Pair<StringPair, Double> pair = future.get();
                    lsims.put(pair.getFirst(),pair.getSecond().floatValue());
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            executor.shutdown();
        }
        return lsims;
    }

    /**
     * Generates pairs of SchemaElementNode from source and target schema trees for parallel LSIM computation,
     * based on their compatibility scores and a given threshold.
     *
     * @param sourceTree          The source schema tree.
     * @param targetTree          The target schema tree.
     * @param compatibilityTable A compatibility table containing precomputed compatibility scores between categories.
     * @param thNs                The threshold for considering compatibility between schema elements.
     * @return A list of pairs of SchemaElementNode representing elements to be compared for LSIM computation.
     */
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

    /**
     * Performs level-order traversal on a schema tree starting from the given root node.
     *
     * @param root The root node of the schema tree.
     * @return A list of SchemaElementNode objects traversed in level-order.
     */
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

    /**
     * Computes the LSIM score for a pair of schema elements based on their compatibility and name similarity.
     *
     * @param pair              A Pair of SchemaElement objects representing the schema elements to be compared.
     * @param compatibilityTable A compatibility table containing precomputed compatibility scores between categories.
     * @return A Pair containing the names of the schema elements and their computed LSIM score.
     */
    private Pair<StringPair, Double> lsimProc(Pair<SchemaElement, SchemaElement> pair, Map<String, Map<String, Double>> compatibilityTable) {
        SchemaElement s = pair.getFirst();
        SchemaElement t = pair.getSecond();

        List<String> s_cat = s.getCategories();
        List<String> t_cat = t.getCategories();

        double maxScore = 0.0;

        for (String c1: s_cat)
            if (maxScore == 1.0)
                break;
            else if (compatibilityTable.containsKey(c1))
                for (String c2: t_cat)
                    if (maxScore == 1.0)
                        break;
                    else if (compatibilityTable.get(c1).containsKey(c2))
                        if (maxScore<compatibilityTable.get(c1).get(c2))
                            maxScore = compatibilityTable.get(c1).get(c2);



        double nameSimilarityScore = nameSimilarityElements(s, t);

        return new Pair<>(new StringPair(s.getInitialName(), t.getInitialName()), nameSimilarityScore * maxScore);
    }

    /**
     * Computes the similarity between two sets of tokens based on their data types.
     *
     * @param tokenSet1 The first set of tokens.
     * @param tokenSet2 The second set of tokens.
     * @return The similarity score between the two sets of tokens based on their data types.
     */
    private double dataTypeSimilarity(List<Token> tokenSet1, List<Token> tokenSet2) {
        double sum1 = 0;
        double sum2 = 0;

        for (TokenTypes tt : TokenTypes.allTokenTypes()) {
            if (tt == TokenTypes.SYMBOLS) {
                continue;
            }

            List<Token> t1 = new ArrayList<>();
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
            if (t1.isEmpty() || t2.isEmpty()) {
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

    /**
     * Computes the similarity between two sets of tokens based on their names.
     *
     * @param tokenSet1 The first set of tokens.
     * @param tokenSet2 The second set of tokens.
     * @return The similarity score between the two sets of tokens based on their names.
     */
    private double nameSimilarityTokens(List<Token> tokenSet1, List<Token> tokenSet2) {
        try {
            double sum1 = getPartialSimilarity(tokenSet1, tokenSet2);
            double sum2 = getPartialSimilarity(tokenSet2, tokenSet1);
            return (sum1 + sum2) / (tokenSet1.size() + tokenSet2.size());
        } catch (Exception ignored) {

        }
        return 0.0;
    }

    /**
     * Computes the partial similarity between two sets of tokens based on their data.
     *
     * @param tokenSet1 The first set of tokens.
     * @param tokenSet2 The second set of tokens.
     * @return The partial similarity score between the two sets of tokens.
     */
    private double getPartialSimilarity(List<Token> tokenSet1, List<Token> tokenSet2) {

        double totalSum = 0;
        for (Token t1 : tokenSet1) {
            double max_sim = 0;
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

    /**
     * Retrieves synonyms of a given word using WordNet functionalities.
     *
     * @param word The word for which synonyms are to be retrieved.
     * @return A set of ISynset objects representing synonym sets for the given word.
     * @throws IOException If an I/O error occurs while accessing WordNet data.
     */
    public Set<ISynset> getSynonyms(String word) throws IOException {
        return wordNetFunctionalities.getAllSynonymsets(word);
    }

    /**
     * Computes the similarity between two words using WordNet-based functionalities.
     *
     * @param word1 The first word for comparison.
     * @param word2 The second word for comparison.
     * @return The computed similarity score between the two words.
     *         Returns NaN if either word has no synonyms or an error occurs during computation.
     */
    private double computeSimilarityWordnet(String word1, String word2) {
        // check (word1 & word2 not in lemmas) is not nessecary, bc for every IWord, the method .getLemma() is never null
        try {
            Set<ISynset> allSyns1 = getSynonyms(word1);
            Set<ISynset> allSyns2 = getSynonyms(word2);

            if (allSyns1.isEmpty() || allSyns2.isEmpty()) {
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

            for (Pair<ISynset, ISynset> pair : productOfBothISynsetSets) {
                ISynset s1 = pair.getFirst();
                ISynset s2 = pair.getSecond();
                double score = wuPalmer.calcRelatednessOfWords(s1.getWords().get(0).getLemma(), s2.getWords().get(0).getLemma());
                //System.out.println(index + ". " + s1.getWords().get(0).getLemma() + ", " + s2.getWords().get(0).getLemma() + ": " + score);
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

    /**
     * Computes the similarity between two words using the Levenshtein distance algorithm.
     *
     * @param word1 The first word for comparison.
     * @param word2 The second word for comparison.
     * @return The computed similarity score between the two words based on Levenshtein distance.
     */
    private float computeSimilarityLeven(String word1, String word2) {
        return levenshtein.compare(word1, word2);
    }

    /**
     * Computes the similarity between two schema elements based on their token types and token names.
     *
     * @param element1 The first schema element for comparison.
     * @param element2 The second schema element for comparison.
     * @return The computed similarity score between the two schema elements based on token types and names.
     *         Returns 0 if either schema element has no tokens or if no similarity is found.
     */
    private double nameSimilarityElements(SchemaElement element1, SchemaElement element2) {
        double sum1 = 0;
        double sum2 = 0;

        for (TokenTypes tt : TokenTypes.allTokenTypes()) {
            if (tt == TokenTypes.SYMBOLS) {
                continue;
            }
            List<Token> t1 = element1.getTokensByTokenType(tt.getTokenName());
            List<Token> t2 = element2.getTokensByTokenType(tt.getTokenName());
            if (t1.isEmpty() || t2.isEmpty()) {
                continue;
            }
            double sim = nameSimilarityTokens(t1, t2);
            double weight = tt.getWeight();
            sum1 += (weight * sim);
            sum2 += weight;

        }

        if (sum1 == 0 || sum2 == 0) {
            return 0;
        }
        return sum1 / sum2;
    }

    /**
     * Computes the Linguistic Similarity (LSim) between two schema elements.
     *
     * @param element1 The first schema element for comparison.
     * @param element2 The second schema element for comparison.
     * @return The computed Linguistic Similarity (LSim) score between the two schema elements.
     */
    public double computeLsim(SchemaElement element1, SchemaElement element2) {
        double nameSimilarity = nameSimilarityElements(element1, element2);
        double maxCategory = getMaxNsCategory(element1.getCategories(), element2.getCategories());
        return nameSimilarity * maxCategory;
    }

    /**
     * Computes the maximum similarity score among categories of two schema elements.
     *
     * @param categoriesE1 The list of categories for the first schema element.
     * @param categoriesE2 The list of categories for the second schema element.
     * @return The maximum similarity score among categories of the two schema elements.
     */
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
}
