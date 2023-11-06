package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.Main;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ResultsUtils {
    private final static Logger log = LogManager.getLogger(ResultsUtils.class);

    public static String getDirNameForMatchStep(MatchStep matchStep) {
        String dirName = matchStep.getClass().getSimpleName();
        if (matchStep instanceof SimMatrixBoostingStep) {
            dirName += "Line" + ((SimMatrixBoostingStep) matchStep).getLine();
        }
        return dirName;
    }
    public static String getBaseResultsPathForScenario(MatchTask matchTask) {
        return getBaseResultsPathForDataset(matchTask.getDataset()) + File.separator + matchTask.getScenario().getName();
    }

    public static String getBaseResultsPathForDataset(Dataset dataset) {
       return getBaseResultsPath() + File.separator + dataset.getName();
    }

    public static String getBaseResultsPath() {
        Configuration config = Configuration.getInstance();
        return config.getResultsDir() + File.separator + StringUtils.dateToString(Main.START_TIMESTAMP);
    }
}
