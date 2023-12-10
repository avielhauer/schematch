package de.uni_marburg.schematch.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigUtils {
    private static final Logger log = LogManager.getLogger(ConfigUtils.class);

    public static boolean anyEvaluate() {
        Configuration config = Configuration.getInstance();
        return config.isEvaluateTablePairGeneration() || config.isEvaluateFirstLineMatchers() ||
                config.isEvaluateSecondLineMatchers() || config.isEvaluateSimMatrixBoostingOnFirstLineMatchers() ||
                config.isEvaluateSimMatrixBoostingOnSecondLineMatchers();
    }
}
