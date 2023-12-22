package de.uni_marburg.schematch.matching;

import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import de.uni_marburg.schematch.preprocessing.tokenization.TokenizerFactory;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Data
@NoArgsConstructor
public class MatcherFactory {
    private static final Logger log = LogManager.getLogger(MatcherFactory.class);

    private Map<String, List<Tokenizer>> tokenizers;
    private int numTokenizers;

    /**
     * Instantiates all tokenizers as specified in first_line_tokenizers.yaml
     * @throws Exception when reflection goes wrong
     */
    private void createTokenizers() throws Exception {
        this.tokenizers = new HashMap<>();
        TokenizerFactory tokenizerFactory = new TokenizerFactory();

        Configuration config = Configuration.getInstance();

        for (String tokenizerName : config.getFirstLineTokenizerConfigurations().keySet()) {
            List<Tokenizer> tokenizerInstances = tokenizerFactory.createTokenizerInstances(tokenizerName);
            this.numTokenizers += tokenizerInstances.size();
            tokenizers.put(tokenizerName, tokenizerInstances);
        }

        if (this.numTokenizers == 0) {
            String e = "Using a tokenized matcher but there are no tokenizers configured.";
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiates a matcher for the given matcher configuration
     * @param matcherConfiguration Matcher configuration to use for instantiation
     * @return Matcher instance for the specified matcher configuration
     * @throws Exception when reflection goes wrong
     */
    public Matcher createMatcherInstance(Configuration.MatcherConfiguration matcherConfiguration) throws Exception {
        String name = matcherConfiguration.getName();
        String packageName = matcherConfiguration.getPackageName();
        Class<?> matcherClass = Class.forName(Configuration.MATCHING_PACKAGE_NAME + "." + packageName + "." + name);

        Matcher matcher = (Matcher) matcherClass.getConstructor().newInstance();
        matcher.configure(matcherConfiguration);

        return matcher;
    }

    /**
     * Instantiates a tokenized matcher for the given matcher configuration and tokenizer
     * @param matcherConfiguration Matcher configuration to use for instantiation
     * @param tokenizer Tokenizer to use for instantiation
     * @return Matcher instance for the specified matcher configuration and tokenizer
     * @throws Exception when reflection goes wrong
     */
    public Matcher createTokenizedMatcherInstance(Configuration.MatcherConfiguration matcherConfiguration, Tokenizer tokenizer) throws Exception {
        String name = matcherConfiguration.getName();
        String packageName = matcherConfiguration.getPackageName();
        Class<?> matcherClass = Class.forName(Configuration.MATCHING_PACKAGE_NAME + "." + packageName + "." + name);

        TokenizedTablePairMatcher matcher = (TokenizedTablePairMatcher) matcherClass.getConstructor().newInstance();
        matcher.configure(matcherConfiguration);
        matcher.setTokenizer(tokenizer);

        return matcher;
    }

    /**
     * Instantiates all configurations for a given matcher as specified in first_line_matchers.yaml
     * @param matcherName Name of the matcher to create instances for
     * @return List of all configured matcher instances
     * @throws Exception when reflection goes wrong
     */
    public List<Matcher> createMatcherInstances(String matcherName, int line) throws Exception {
        Configuration config = Configuration.getInstance();

        List<Configuration.MatcherConfiguration> matcherConfigurations =
                switch(line) {
                    case 1 -> config.getFirstLineMatcherConfigurations().get(matcherName);
                    case 2 -> config.getSecondLineMatcherConfigurations().get(matcherName);
                    default -> throw new IllegalStateException("Unexpected value: " + line);
                };
        int numConfigs = matcherConfigurations.size();

        String name = matcherConfigurations.get(0).getName();
        String packageName = matcherConfigurations.get(0).getPackageName();
        Class<?> matcherClass = Class.forName(Configuration.MATCHING_PACKAGE_NAME + "." + packageName + "." + name);

        List<Matcher> matcherInstances = new ArrayList<>();
        // TODO: find better way to figure out if current matcher is tokenized
        Matcher m = (Matcher) matcherClass.getConstructor().newInstance();
        if (m instanceof TokenizedTablePairMatcher) {
            // first tokenized matcher, create tokenizers first
            if (this.tokenizers == null) {
                createTokenizers();
            }
            // for every tokenizer, initialize matcher for all matcher configurations
            log.info("Instantiating tokenized matcher " + matcherName + " with " + numConfigs + " different configurations and " +
                    this.numTokenizers + " different tokenizers (" + numConfigs*this.numTokenizers + " total)");
            for (String tokenizerName : this.tokenizers.keySet()) {
                for (Tokenizer tokenizer : this.tokenizers.get(tokenizerName)) {
                    for (Configuration.MatcherConfiguration matcherConfiguration : matcherConfigurations) {
                        matcherInstances.add(createTokenizedMatcherInstance(matcherConfiguration, tokenizer));
                    }
                }
            }
        } else {
            // initialize matcher for all matcher configurations
            log.info("Instantiating matcher " + matcherName + " with " + numConfigs + " different configurations");
            for (Configuration.MatcherConfiguration matcherConfiguration : matcherConfigurations) {
                matcherInstances.add(createMatcherInstance(matcherConfiguration));
            }
        }

        return matcherInstances;
    }

    /**
     * Instantiates all matchers as specified in the respective config file
     * @return Map of matcher names to list of all configured matcher instances
     * @throws Exception when reflection goes wrong
     */
    public Map<String, List<Matcher>> createMatchersFromConfig(int line) throws Exception {
        Map<String, List<Matcher>> matchers = new HashMap<>();
        Configuration config = Configuration.getInstance();

        Set<String> matcherNames;
        if (line == 1) {
            matcherNames = config.getFirstLineMatcherConfigurations().keySet();
        } else { // line == 2
            matcherNames = config.getSecondLineMatcherConfigurations().keySet();
        }

        for (String matcherName : matcherNames) {
            matchers.put(matcherName, createMatcherInstances(matcherName, line));
        }

        return matchers;
    }
}
