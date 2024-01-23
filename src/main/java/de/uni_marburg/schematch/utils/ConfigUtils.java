package de.uni_marburg.schematch.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigUtils {
    private static final Logger log = LogManager.getLogger(ConfigUtils.class);

    public static boolean isEvaluateBoostingOnLine(int line) {
        Configuration config = Configuration.getInstance();
        if (line == 1) {
            return config.isRunSimMatrixBoostingOnFirstLineMatchers() && config.isEvaluateSimMatrixBoostingOnFirstLineMatchers();
        }
        if (line == 2) {
            return config.isRunSimMatrixBoostingOnSecondLineMatchers() && config.isEvaluateSimMatrixBoostingOnSecondLineMatchers();
        }
        throw new IllegalStateException("Unsupported line number: " + line);
    }

    public static boolean isEvaluateFirstLine() {
        Configuration config = Configuration.getInstance();
        return config.isEvaluateFirstLineMatchers() ||
                (config.isRunSimMatrixBoostingOnFirstLineMatchers() && config.isEvaluateSimMatrixBoostingOnFirstLineMatchers());
    }

    public static boolean isEvaluateSecondLine() {
        Configuration config = Configuration.getInstance();
        return (config.isRunSecondLineMatchers() && config.isEvaluateSecondLineMatchers()) ||
                (config.isRunSimMatrixBoostingOnSecondLineMatchers() && config.isEvaluateSimMatrixBoostingOnSecondLineMatchers());
    }

    public static boolean anyEvaluate() {
        Configuration config = Configuration.getInstance();
        return config.isEvaluateTablePairGeneration() || config.isEvaluateFirstLineMatchers() ||
                (config.isRunSecondLineMatchers() && config.isEvaluateSecondLineMatchers()) ||
                (config.isRunSimMatrixBoostingOnFirstLineMatchers() && config.isEvaluateSimMatrixBoostingOnFirstLineMatchers()) ||
                (config.isRunSimMatrixBoostingOnSecondLineMatchers() && config.isEvaluateSimMatrixBoostingOnSecondLineMatchers());
    }

    public static boolean anyReadCache() {
        Configuration config = Configuration.getInstance();
        return config.isReadCacheFirstLineMatchers() ||
                (config.isRunSecondLineMatchers() && config.isReadCacheSecondLineMatchers()) ||
                (config.isRunSimMatrixBoostingOnFirstLineMatchers() && config.isReadCacheSimMatrixBoostingOnFirstLineMatchers()) ||
                (config.isRunSimMatrixBoostingOnSecondLineMatchers() && config.isReadCacheSimMatrixBoostingOnSecondLineMatchers());
    }

    public static boolean anyWriteCache() {
        Configuration config = Configuration.getInstance();
        return config.isWriteCacheFirstLineMatchers() ||
                (config.isRunSecondLineMatchers() && config.isWriteCacheSecondLineMatchers()) ||
                (config.isRunSimMatrixBoostingOnFirstLineMatchers() && config.isWriteCacheSimMatrixBoostingOnFirstLineMatchers()) ||
                (config.isRunSimMatrixBoostingOnSecondLineMatchers() && config.isWriteCacheSimMatrixBoostingOnSecondLineMatchers());
    }
}
