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
    private static final String METRICS_CFG = "metrics.yaml";
    private static final String FIRST_LINE_MATCHERS_CFG = "first_line_matchers.yaml";
    private static final String FIRST_LINE_TOKENIZERS_CFG = "first_line_tokenizers.yaml";
    private static final String SECOND_LINE_MATCHERS_CFG = "second_line_matchers.yaml";
    public static final String METRIC_PACKAGE_NAME = "de.uni_marburg.schematch.evaluation.metric"; // TODO: fetch from reflection so it is easy to refactor
    public static final String MATCHING_PACKAGE_NAME = "de.uni_marburg.schematch.matching"; // TODO: fetch from reflection so it is easy to refactor
    public static final String TOKENIZATION_PACKAGE_NAME = "de.uni_marburg.schematch.preprocessing.tokenization"; // TODO: fetch from reflection so it is easy to refactor
    public static final String TIMESTAMP_PATTERN = "MM-dd-yyyy_HH-mm-ss";

    private static Configuration instance = null;

    private String defaultDatasetBasePath;

    private String applicationName;
    private String defaultSeparator;
    private String defaultTablePairSeparator;
    private String defaultSourceDatabaseDir;
    private String defaultTargetDatabaseDir;
    private String defaultGroundTruthDir;

    private String cacheDir;
    private String resultsDir;
    private String performanceDir;
    private String performanceOverviewFilePrefix;
    private String performanceSummaryFileName;
    private String outputDir;

    private float partialFDMaxError;
    private boolean recomputeAllFDs;

    // FIXME: refactor to use log4j custom log level instead
    private int logLevelResults;

    private boolean evaluateAttributes;
    private boolean saveOutputPerTablePair;
    private boolean saveOutputVerbose;

    // Step 1
    private boolean saveOutputTablePairGeneration;
    private boolean evaluateTablePairGeneration;

    // Step 2
    private boolean saveOutputFirstLineMatchers;
    private boolean evaluateFirstLineMatchers;
    private boolean readCacheFirstLineMatchers;
    private boolean writeCacheFirstLineMatchers;
    private boolean writeFirstLineGroundTruth;

    // Step 3
    private boolean runSimMatrixBoostingOnFirstLineMatchers;
    private boolean saveOutputSimMatrixBoostingOnFirstLineMatchers;
    private boolean evaluateSimMatrixBoostingOnFirstLineMatchers;
    private boolean readCacheSimMatrixBoostingOnFirstLineMatchers;
    private boolean writeCacheSimMatrixBoostingOnFirstLineMatchers;

    // Step 4
    private boolean runSecondLineMatchers;
    private boolean saveOutputSecondLineMatchers;
    private boolean evaluateSecondLineMatchers;
    private boolean readCacheSecondLineMatchers;
    private boolean writeCacheSecondLineMatchers;

    // Step 5
    private boolean runSimMatrixBoostingOnSecondLineMatchers;
    private boolean saveOutputSimMatrixBoostingOnSecondLineMatchers;
    private boolean evaluateSimMatrixBoostingOnSecondLineMatchers;
    private boolean readCacheSimMatrixBoostingOnSecondLineMatchers;
    private boolean writeCacheSimMatrixBoostingOnSecondLineMatchers;

    private List<DatasetConfiguration> datasetConfigurations = new ArrayList<>();
    private List<MetricConfiguration> metricConfigurations = new ArrayList<>();
    private Map<String, List<MatcherConfiguration>> firstLineMatcherConfigurations = new HashMap<>();
    private Map<String, List<TokenizerConfiguration>> firstLineTokenizerConfigurations = new HashMap<>();
    private Map<String, List<MatcherConfiguration>> secondLineMatcherConfigurations = new HashMap<>();

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
    public static class MetricConfiguration {
        private String name;
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

    private static void loadMatcherConfig(InputStream matchersCfgStream, Map<String, List<MatcherConfiguration>> matcherConfigurations) {
        Yaml yaml = new Yaml();

        Iterable<Object> matcherConfigs = yaml.loadAll(matchersCfgStream);

        CombinationGenerator<Object> combinationGenerator = new CombinationGenerator<>();
        for (Object object : matcherConfigs) {
            Map<String, Object> matcherConfig = (Map<String, Object>) object;
            if (!(Boolean) matcherConfig.get("active")) {
                log.info("Skipped loading first line matcher '{}' because it is marked as inactive.", matcherConfig.get("name"));
                continue;
            }

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
            matcherConfigurations.put(name, matcherConfigurationList);
        }
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

        // load metrics configuration
        yaml = new Yaml(new Constructor(Configuration.MetricConfiguration.class, new LoaderOptions()));
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(METRICS_CFG);
        if (inputStream == null) {
            log.error("Unable to load metrics configuration file from: " + METRICS_CFG);
            throw new RuntimeException();
        }
        Iterable<Object> metricConfigs = yaml.loadAll(inputStream);

        for (Object metricConfig : metricConfigs) {
            MetricConfiguration metricConfiguration = (MetricConfiguration) metricConfig;
            config.metricConfigurations.add(metricConfiguration);
            log.debug(metricConfiguration.toString());
        }

        // load matchers configuration
        // first line
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(FIRST_LINE_MATCHERS_CFG);
        if (inputStream == null) {
            log.error("Unable to load matchers configuration file from: " + FIRST_LINE_MATCHERS_CFG);
            throw new RuntimeException();
        }
        loadMatcherConfig(inputStream, config.firstLineMatcherConfigurations);
        // second line
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(SECOND_LINE_MATCHERS_CFG);
        if (inputStream == null) {
            log.error("Unable to load matchers configuration file from: " + SECOND_LINE_MATCHERS_CFG);
            throw new RuntimeException();
        }
        loadMatcherConfig(inputStream, config.secondLineMatcherConfigurations);

        // load tokenizers configuration
        yaml = new Yaml();
        inputStream = Configuration.class.getClassLoader().getResourceAsStream(FIRST_LINE_TOKENIZERS_CFG);
        if (inputStream == null) {
            log.error("Unable to load tokenizers configuration file from: " + FIRST_LINE_TOKENIZERS_CFG);
            throw new RuntimeException();
        }
        Iterable<Object> tokenizersConfigs = yaml.loadAll(inputStream);

        CombinationGenerator<Object> combinationGenerator = new CombinationGenerator<>();

        for (Object object : tokenizersConfigs) {
            Map<String, Object> tokenizerConfig = (Map<String, Object>) object;
            String name = (String) tokenizerConfig.get("name");
            List<TokenizerConfiguration> tokenizerConfigurationList = new ArrayList<>();
            if (tokenizerConfig.get("params") instanceof List) {
                log.error("Parsing of parameter lists not implemented yet. Use dictionary of possible parameter values" +
                        "and the program will deduce configurations for all possible parameter value combinations.");
                throw new NotImplementedException("");
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
            config.firstLineTokenizerConfigurations.put(name, tokenizerConfigurationList);

        }

        return config;
    }
}
