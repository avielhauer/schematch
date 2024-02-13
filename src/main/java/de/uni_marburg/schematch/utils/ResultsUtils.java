package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.Main;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import lombok.Setter;

import java.nio.file.Path;

public class ResultsUtils {
    private final static Configuration config = Configuration.getInstance();
    @Setter
    private static String runName = "";

    // Base result paths
    public static Path getBaseResultsPath() {
        Configuration config = Configuration.getInstance();
        return Path.of(config.getResultsDir(), StringUtils.dateToString(Main.START_TIMESTAMP) + " " + runName);
    }
    public static Path getBaseResultsPathForDataset(Dataset dataset) {
        return getBaseResultsPath().resolve(dataset.getName());
    }
    public static Path getBaseResultsPathForScenario(MatchTask matchTask) {
        return getBaseResultsPathForDataset(matchTask.getDataset()).resolve(matchTask.getScenario().getName());
    }

    // Output paths
    public static Path getOutputPathForScenario(MatchTask matchTask) {
        return getBaseResultsPathForScenario(matchTask).resolve(config.getOutputDir());
    }
    public static Path getOutputPathForMatchStepInScenario(MatchTask matchTask, MatchStep matchStep) {
        return getOutputPathForScenario(matchTask).resolve(matchStep.toString());
    }

    // Cache paths
    public static Path getCachePathForMatchStepInScenario(MatchTask matchTask, MatchStep matchingStep) {
        return Path.of(config.getCacheDir())
                .resolve(matchTask.getDataset().getName())
                .resolve(matchTask.getScenario().getName())
                .resolve(matchingStep.toString());
    }

    // Performance paths
    public static Path getPerformancePathForOverall() {
        return getBaseResultsPath().resolve(config.getPerformanceDir());
    }
    public static Path getPerformancePathForDataset(Dataset dataset) {
        return getBaseResultsPathForDataset(dataset).resolve(config.getPerformanceDir());
    }
    public static Path getPerformancePathForScenario(MatchTask matchTask) {
        return getBaseResultsPathForScenario(matchTask).resolve(config.getPerformanceDir());
    }
}
