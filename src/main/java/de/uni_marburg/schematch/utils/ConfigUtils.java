package de.uni_marburg.schematch.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigUtils {
    private static final Logger log = LogManager.getLogger(ConfigUtils.class);

    public static boolean isEvaluateBoostingOnLine(int line) {
        Configuration config = Configuration.getInstance();
        if (line == 1) {
            return config.isEvaluateSimMatrixBoostingOnFirstLineMatchers();
        }
        if (line == 2) {
            return config.isEvaluateSimMatrixBoostingOnSecondLineMatchers();
        }
        throw new IllegalStateException("Unsupported line number: " + line);
    }

    public static boolean isEvaluateFirstLine() {
        Configuration config = Configuration.getInstance();
        return config.isEvaluateFirstLineMatchers() || config.isEvaluateSimMatrixBoostingOnFirstLineMatchers();
    }

    public static boolean isEvaluateSecondLine() {
        Configuration config = Configuration.getInstance();
        return config.isEvaluateSecondLineMatchers() || config.isEvaluateSimMatrixBoostingOnSecondLineMatchers();
    }

    public static boolean anyEvaluate() {
        Configuration config = Configuration.getInstance();
        return config.isEvaluateTablePairGeneration() || config.isEvaluateFirstLineMatchers() ||
                config.isEvaluateSecondLineMatchers() || config.isEvaluateSimMatrixBoostingOnFirstLineMatchers() ||
                config.isEvaluateSimMatrixBoostingOnSecondLineMatchers();
    }
}
