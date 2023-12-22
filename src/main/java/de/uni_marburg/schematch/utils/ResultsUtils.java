package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.Main;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;

import java.nio.file.Path;

public class ResultsUtils {
    private final static Configuration config = Configuration.getInstance();

    public static Path getBaseResultsPath() {
        Configuration config = Configuration.getInstance();
        return Path.of(config.getResultsDir(), StringUtils.dateToString(Main.START_TIMESTAMP));
    }

    public static Path getBaseResultsPathForDataset(Dataset dataset) {
        return getBaseResultsPath().resolve(dataset.getName());
    }

    public static Path getBaseResultsPathForScenario(MatchTask matchTask) {
        return getBaseResultsPathForDataset(matchTask.getDataset()).resolve(matchTask.getScenario().getName());
    }

    public static Path getPerformanceBaseResultsPathForMatchStepInScenario(MatchTask matchTask, MatchStep matchStep) {
        return getBaseResultsPathForScenario(matchTask).resolve(matchStep.toString()).resolve(config.getPerformanceDir());
    }

    public static Path getOutputBaseResultsPathForMatchStepInScenario(MatchTask matchTask, MatchStep matchStep) {
        return getBaseResultsPathForScenario(matchTask).resolve(matchStep.toString()).resolve(config.getOutputDir());
    }

    public static Path getOutputScenarioResultsPathForMatchStepInScenario(MatchTask matchTask, MatchStep matchStep) {
        return getOutputBaseResultsPathForMatchStepInScenario(matchTask, matchStep).resolve(config.getResultsDirScenario());
    }
}
