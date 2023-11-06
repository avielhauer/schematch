package de.uni_marburg.schematch.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class Configuration {
    private static final Logger log = LogManager.getLogger(Configuration.class);

    private static final String GENERAL_CFG = "general.yaml";
    private static final String DATASETS_CFG = "datasets.yaml";
    private static final String MATCHERS_CFG = "first_line_matchers.yaml";
    private static final String TOKENIZERS_CFG = "first_line_tokenizers.yaml";
    public static final String MATCHING_PACKAGE_NAME = "de.uni_marburg.schematch.matching"; // TODO: fetch from reflection so it is easy to refactor
    public static final String TOKENIZATION_PACKAGE_NAME = "de.uni_marburg.schematch.preprocessing.tokenization"; // TODO: fetch from reflection so it is easy to refactor
    public static final String TIMESTAMP_PATTERN = "MM-dd-yyyy_HH-mm-ss";

    private static Configuration instance = null;

    private String defaultDatasetBasePath;

    private char defaultSeparator;
    private String defaultTablePairSeparator;
    private String defaultSourceDatabaseDir;
    private String defaultTargetDatabaseDir;
    private String defaultGroundTruthDir;

    private String resultsDir;
    private String performanceDir;
    private String performanceMatchStepOverviewFileSuffix;
    private String performanceMatchStepSummaryFileSuffix;
    private String performanceScenarioSummaryFileSuffix;
    private String performanceDatasetSummaryFileSuffix;
    private String performanceOverallSummaryFile;
    private String outputDir;

    // FIXME: refactor to use log4j custom log level instead
    private int logLevelResults;

    // Step 1
    private boolean saveOutputTablePairGeneration;
    private boolean evaluateTablePairGeneration;

    // Step 2
    private boolean saveOutputFirstLineMatchers;
    private boolean evaluateFirstLineMatchers;

    // Step 3
    private boolean runSimMatrixBoostingOnFirstLineMatchers;
    private boolean saveOutputSimMatrixBoostingOnFirstLineMatchers;
    private boolean evaluateSimMatrixBoostingOnFirstLineMatchers;

    // Step 4
    private boolean runSecondLineMatchers;
    private boolean saveOutputSecondLineMatchers;
    private boolean evaluateSecondLineMatchers;

    // Step 5
    private boolean runSimMatrixBoostingOnSecondLineMatchers;
    private boolean saveOutputSimMatrixBoostingOnSecondLineMatchers;
    private boolean evaluateSimMatrixBoostingOnSecondLineMatchers;

    private List<DatasetConfiguration> datasetConfigurations = new ArrayList<>();
    private Map<String, List<MatcherConfiguration>> matcherConfigurations = new HashMap<>();
    private Map<String, List<TokenizerConfiguration>> tokenizerConfigurations = new HashMap<>();

    // FIXME: read dependencies on demand, atm this boolean gets set to true whenenver runSimMatrixBoosting is set to true
    private boolean readDependencies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasetConfiguration {
        private String name;
        private String path;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatcherConfiguration {
        private String name;
        private String packageName;
        private Map<String, Object> params;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenizerConfiguration {
        private String name;
        private Map<String, Object> params;
    }

    /**
     * @return Central configuration instance
     */
    public static Configuration getInstance() {
        if (Configuration.instance == null) {
            Configuration.instance = Configuration.loadConfig();
        }
        return Configuration.instance;
    }

    private static Configuration loadConfig() {
        Yaml yaml = null;
        InputStream inputStream = null;

        // load general configuration
        yaml = new Yaml(new Constructor(Configuration.class, new LoaderOptions()));
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(GENERAL_CFG);
        if (inputStream == null) {
            log.error("Unable to load general configuration file from: " + GENERAL_CFG);
            throw new RuntimeException();
        }
        Configuration config = yaml.load(inputStream);
        // FIXME: read dependencies on demand
        config.setReadDependencies(config.isRunSimMatrixBoostingOnFirstLineMatchers() || config.isRunSimMatrixBoostingOnSecondLineMatchers());
        log.debug("General configuration: " + config.toString());

        // load datasets configuration
        yaml = new Yaml(new Constructor(Configuration.DatasetConfiguration.class, new LoaderOptions()));
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(DATASETS_CFG);
        if (inputStream == null) {
            log.error("Unable to load datasets configuration file from: " + DATASETS_CFG);
            throw new RuntimeException();
        }
        Iterable<Object> datasetConfigs = yaml.loadAll(inputStream);

        for (Object datasetConfig : datasetConfigs) {
            DatasetConfiguration datasetConfiguration = (DatasetConfiguration) datasetConfig;
            // ensure absolute path in path field
            if (!(datasetConfiguration.getPath().startsWith(File.separator))) {
                datasetConfiguration.setPath(config.getDefaultDatasetBasePath() + File.separator + datasetConfiguration.getPath());
            }
            config.datasetConfigurations.add(datasetConfiguration);
            log.debug(datasetConfiguration.toString());
        }

        // load matchers configuration
        yaml = new Yaml();
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(MATCHERS_CFG);
        if (inputStream == null) {
            log.error("Unable to load matchers configuration file from: " + MATCHERS_CFG);
            throw new RuntimeException();
        }
        Iterable<Object> matcherConfigs = yaml.loadAll(inputStream);

        CombinationGenerator<Object> combinationGenerator = new CombinationGenerator<>();
        for (Object object : matcherConfigs) {
            Map<String, Object> matcherConfig = (Map<String, Object>) object;
            String name = (String) matcherConfig.get("name");
            String packageName = (String) matcherConfig.get("packageName");
            List<MatcherConfiguration> matcherConfigurationList = new ArrayList<>();
            if (matcherConfig.get("params") instanceof List) {
                String err = "Parsing of parameter lists not implemented yet. Use dictionary of possible parameter values" +
                        "and the program will deduce configurations for all possible parameter value combinations.";
                log.error(err);
                throw new NotImplementedException(err);
            } else {
                Map<String, Object> paramSpacesMap = (Map<String, Object>) matcherConfig.get("params");
                List<List<Object>> paramSpaces = new ArrayList<>();
                List<String> paramNames = new ArrayList<>();
                if (paramSpacesMap != null) {
                    for (String key : paramSpacesMap.keySet()) {
                        List<Object> paramSpace;
                        if (paramSpacesMap.get(key) instanceof List) {
                            paramSpace = (List<Object>) paramSpacesMap.get(key);
                        } else {
                            paramSpace = new ArrayList<>();
                            paramSpace.add(paramSpacesMap.get(key));
                        }
                        paramSpaces.add(paramSpace);
                        paramNames.add(key);
                    }
                }
                List<List<Object>> paramLists = combinationGenerator.generateCombinations(paramSpaces);
                for (List<Object> paramList : paramLists) {
                    Map<String, Object> params = new HashMap<>();
                    for (int i = 0; i < paramList.size(); i++) {
                        params.put(paramNames.get(i), paramList.get(i));
                    }
                    MatcherConfiguration matcherConfiguration = new MatcherConfiguration(name, packageName, params);
                    matcherConfigurationList.add(matcherConfiguration);
                    log.debug(matcherConfiguration.toString());
                }
            }
            config.matcherConfigurations.put(name, matcherConfigurationList);

        }

        // load tokenizers configuration
        yaml = new Yaml();
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(TOKENIZERS_CFG);
        if (inputStream == null) {
            log.error("Unable to load tokenizers configuration file from: " + TOKENIZERS_CFG);
            throw new RuntimeException();
        }
        Iterable<Object> tokenizersConfigs = yaml.loadAll(inputStream);

        for (Object object : tokenizersConfigs) {
            Map<String, Object> tokenizerConfig = (Map<String, Object>) object;
            String name = (String) tokenizerConfig.get("name");
            List<TokenizerConfiguration> tokenizerConfigurationList = new ArrayList<>();
            if (tokenizerConfig.get("params") instanceof List) {
                log.error("Parsing of parameter lists not implemented yet. Use dictionary of possible parameter values" +
                        "and the program will deduce configurations for all possible parameter value combinations.");
                throw new NotImplementedException();
            } else {
                Map<String, Object> paramSpacesMap = (Map<String, Object>) tokenizerConfig.get("params");
                List<List<Object>> paramSpaces = new ArrayList<>();
                List<String> paramNames = new ArrayList<>();
                if (paramSpacesMap != null) {
                    for (String key : paramSpacesMap.keySet()) {
                        List<Object> paramSpace;
                        if (paramSpacesMap.get(key) instanceof List) {
                            paramSpace = (List<Object>) paramSpacesMap.get(key);
                        } else {
                            paramSpace = new ArrayList<>();
                            paramSpace.add(paramSpacesMap.get(key));
                        }
                        paramSpaces.add(paramSpace);
                        paramNames.add(key);
                    }
                }
                List<List<Object>> paramLists = combinationGenerator.generateCombinations(paramSpaces);
                for (List<Object> paramList : paramLists) {
                    Map<String, Object> params = new HashMap<>();
                    for (int i = 0; i < paramList.size(); i++) {
                        params.put(paramNames.get(i), paramList.get(i));
                    }
                    TokenizerConfiguration tokenizerConfiguration = new TokenizerConfiguration(name, params);
                    tokenizerConfigurationList.add(tokenizerConfiguration);
                    log.debug(tokenizerConfiguration.toString());
                }
            }
            config.tokenizerConfigurations.put(name, tokenizerConfigurationList);

        }

        return config;
    }
}