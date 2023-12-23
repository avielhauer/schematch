package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Data
public class EvalWriter {
    private static final Logger log = LogManager.getLogger(EvalWriter.class);

    private final String SUMMARY_HEADER
            = "MatchStep\\Summary,BestMatcher,BestScore,numBestMatchers,ListBestMatchers";

    private final List<MatchStep> matchSteps;
    private final List<Metric> metrics;
    private final Map<Metric, Map<MatchStep, Map<Matcher, Performance>>> datasetPerformances = new HashMap<>();
    private final Map<Metric, Map<MatchStep, Map<Matcher, Performance>>> overallPerformances = new HashMap<>();

    private enum EvaluationLevel {
        SCENARIO,
        DATASET,
        OVERALL
    }

    public EvalWriter(List<MatchStep> matchSteps, List<Metric> metrics) {
        this.matchSteps = matchSteps;
        this.metrics = metrics;
        // Initialize dataset and overall performance for metrics, matchsteps and matchers with 0.0 global score
        // Writing scenario performance will add up global scores to dataset performances
        // Writing dataset performance will divide the sum by the number of scenarios before writing to disk,
        //      it will then add the dataset performances to overall performances and reset the current dataset performance
        initializePerformances(this.datasetPerformances);
        initializePerformances(this.overallPerformances);
    }

    private void initializePerformances(Map<Metric, Map<MatchStep, Map<Matcher, Performance>>> performances) {
        for (Metric metric : this.metrics) {
            performances.put(metric, new HashMap<>());

            for (MatchStep matchStep : this.matchSteps) {
                int line;
                if (matchStep instanceof MatchingStep) {
                    line = ((MatchingStep) matchStep).getLine();
                } else if (matchStep instanceof SimMatrixBoostingStep) {
                    line = ((SimMatrixBoostingStep) matchStep).getLine();
                } else {
                    continue;
                }

                performances.get(metric).put(matchStep, new HashMap<>());

                List<Matcher> matchers = ((MatchingStep) getMatchStep(new HashSet<>(this.matchSteps), line, false)).getMatchers();
                for (Matcher matcher : matchers) {
                    performances.get(metric).get(matchStep).put(matcher, new Performance(0f));
                }
            }
        }
    }

    private void averagePerformances(Map<Metric, Map<MatchStep, Map<Matcher, Performance>>> performances, int n) {
        for (Metric metric : performances.keySet()) {
            for (MatchStep matchStep : performances.get(metric).keySet()) {
                for (Matcher matcher : performances.get(metric).get(matchStep).keySet()) {
                    float score = performances.get(metric).get(matchStep).get(matcher).getGlobalScore();
                    performances.get(metric).get(matchStep).get(matcher).setGlobalScore(score/n);
                }
            }
        }
    }

    private void writePerformance(EvaluationLevel evaluationLevel, Path path, Map<Metric, Map<MatchStep, Map<Matcher, Performance>>> performances) {
        for (Metric metric : performances.keySet()) {
            Map<MatchStep, Map<Matcher, Performance>> performance = performances.get(metric);
            if (ConfigUtils.isEvaluateFirstLine()) {
                writePerformanceOverview(evaluationLevel, path, metric, 1, performance);
            }
            if (ConfigUtils.isEvaluateSecondLine()) {
                writePerformanceOverview(evaluationLevel, path, metric, 2, performance);
            }
            writePerformanceSummary(path, metric, performance);
        }
    }

    public void writeScenarioPerformance(MatchTask matchTask) {
        Path scenarioPerformancePath = ResultsUtils.getPerformancePathForScenario(matchTask);
        writePerformance(EvaluationLevel.SCENARIO, scenarioPerformancePath, matchTask.getPerformances());

        if (Configuration.getInstance().isEvaluateAttributes()) {
            // FIXME: find a better place to do the mapping of column indices to column labels
            SortedMap<Integer, String> sourceAttributes = new TreeMap<>();
            SortedMap<Integer, String> targetAttributes = new TreeMap<>();
            int[][] groundTruthMatrix = matchTask.getGroundTruthMatrix();
            for (int i = 0; i < groundTruthMatrix.length; i++) {
                for (int j = 0; j < groundTruthMatrix[0].length; j++) {
                    if (groundTruthMatrix[i][j] == 1) {
                        sourceAttributes.put(i, matchTask.getScenario().getSourceDatabase().getFullColumnNameByIndex(i));
                        targetAttributes.put(j, matchTask.getScenario().getTargetDatabase().getFullColumnNameByIndex(j));
                    }
                }
            }

            Map<Metric, Map<MatchStep, Map<Matcher, Performance>>> performances = matchTask.getPerformances();
            for (Metric metric : matchTask.getMetrics()) {
                for (MatchStep matchStep : matchTask.getMatchSteps()) {
                    if (matchStep.isDoEvaluate()) {
                        Path matchStepPath = scenarioPerformancePath.resolve(metric.toString()).resolve(matchStep.toString());
                        List<Matcher> matchers = matchTask.getMatchersForMatchStep(matchStep);
                        writeAttributePerformance(matchStepPath, matchTask, matchers,
                                performances.get(metric).get(matchStep), sourceAttributes, targetAttributes);
                    }
                }
            }
        }
    }

    private void writeAttributePerformance(Path matchStepPath, MatchTask matchTask, List<Matcher> matchers,
                                           Map<Matcher, Performance> performances,
                                           SortedMap<Integer, String> sourceAttributes,
                                           SortedMap<Integer, String> targetAttributes) {
        Path sourceAttributesFilePath = matchStepPath.resolve("performance_source_attributes.csv");
        Path targetAttributesFilePath = matchStepPath.resolve("performance_target_attributes.csv");
        Path attributePairsFilePath = matchStepPath.resolve("performance_attribute_pairs.csv");

        try {
            Files.createDirectories(sourceAttributesFilePath.getParent());
            BufferedWriter sourceAttributesWriter = new BufferedWriter(new FileWriter(sourceAttributesFilePath.toString()));
            BufferedWriter targetAttributesWriter = new BufferedWriter(new FileWriter(targetAttributesFilePath.toString()));
            BufferedWriter attributePairsWriter = new BufferedWriter(new FileWriter(attributePairsFilePath.toString()));

            List<String> linesSourceAttributes = new ArrayList<>();
            List<String> linesTargetAttributes = new ArrayList<>();
            List<String> linesAttributePairs = new ArrayList<>();
            StringBuilder generalHeader = new StringBuilder();

            for (Matcher matcher : matchers) {
                generalHeader.append(",").append(matcher.toString());
            }

            for (Map.Entry<Integer, String> sourceAttribute : sourceAttributes.entrySet()) {
                StringBuilder lineSourceAttribute = new StringBuilder();
                lineSourceAttribute.append(sourceAttribute.getValue());
                for (Matcher matcher : matchers) {
                    lineSourceAttribute.append(",")
                            .append(performances.get(matcher).getSourceAttributeScores().get(sourceAttribute.getKey()));
                }
                linesSourceAttributes.add(lineSourceAttribute.toString());
            }

            for (Map.Entry<Integer, String> targetAttribute : targetAttributes.entrySet()) {
                StringBuilder lineTargetAttribute = new StringBuilder();
                lineTargetAttribute.append(targetAttribute.getValue());
                for (Matcher matcher : matchers) {
                    lineTargetAttribute.append(",")
                            .append(performances.get(matcher).getTargetAttributeScores().get(targetAttribute.getKey()));
                }
                linesTargetAttributes.add(lineTargetAttribute.toString());
            }

            int numSourceColumns = matchTask.getNumSourceColumns();
            int numTargetColumns = matchTask.getNumTargetColumns();;
            int numTotalColumns = numSourceColumns + numTargetColumns;

            for (Map.Entry<Integer, String> sourceAttribute : sourceAttributes.entrySet()) {
                for (Map.Entry<Integer, String> targetAttribute : targetAttributes.entrySet()) {
                    StringBuilder lineAttributePair = new StringBuilder();
                    lineAttributePair.append(sourceAttribute.getValue()).append("___").append(targetAttribute.getValue());
                    for (Matcher matcher : matchers) {
                        float sourceScore = performances.get(matcher).getSourceAttributeScores().get(sourceAttribute.getKey());
                        float targetScore = performances.get(matcher).getTargetAttributeScores().get(targetAttribute.getKey());
                        float weightedAvgScore = ((sourceScore * numSourceColumns) + (targetScore * numTargetColumns))/numTotalColumns;
                        lineAttributePair.append(",").append(weightedAvgScore);
                    }
                    linesAttributePairs.add(lineAttributePair.toString());
                }
            }

            sourceAttributesWriter.write("SourceAttribute\\Matcher" + generalHeader);
            targetAttributesWriter.write("TargetAttribute\\Matcher" + generalHeader);
            attributePairsWriter.write("AttributePairs\\Matcher" + generalHeader);

            for (String line : linesSourceAttributes) {
                sourceAttributesWriter.newLine();
                sourceAttributesWriter.write(line);
            }

            for (String line : linesTargetAttributes) {
                targetAttributesWriter.newLine();
                targetAttributesWriter.write(line);
            }

            for (String line : linesAttributePairs) {
                attributePairsWriter.newLine();
                attributePairsWriter.write(line);
            }

            sourceAttributesWriter.close();
            targetAttributesWriter.close();
            attributePairsWriter.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void writeDatasetPerformance(Dataset dataset) {
        averagePerformances(this.datasetPerformances, dataset.getScenarioNames().size());
        Path datasetPerformancePath = ResultsUtils.getPerformancePathForDataset(dataset);
        writePerformance(EvaluationLevel.DATASET, datasetPerformancePath, this.datasetPerformances);
        initializePerformances(this.datasetPerformances);
    }

    public void writeOverallPerformance(int numDatasets) {
        averagePerformances(this.overallPerformances, numDatasets);
        Path overallPerformancePath = ResultsUtils.getPerformancePathForOverall();
        writePerformance(EvaluationLevel.OVERALL, overallPerformancePath, this.overallPerformances);
    }

    private void writePerformanceOverview(EvaluationLevel evaluationLevel, Path path, Metric metric, int line, Map<MatchStep, Map<Matcher, Performance>> performances) {
        Path pathToOverviewFile = path.resolve(metric.toString())
                .resolve(Configuration.getInstance().getPerformanceOverviewFilePrefix() + "line" + line + ".csv");

        try {
            Files.createDirectories(pathToOverviewFile.getParent());
            BufferedWriter overviewWriter = new BufferedWriter(new FileWriter(pathToOverviewFile.toString()));

            MatchingStep matchingStep = (MatchingStep) getMatchStep(performances.keySet(), line, false);
            List<Matcher> matchers = matchingStep.getMatchers();

            // Overview Header
            StringBuilder overviewHeader = new StringBuilder();
            overviewHeader.append("MatchStep\\Matcher");
            for (Matcher matcher : matchers) {
                String matcherDesc = matcher.toString();
                overviewHeader.append(Configuration.getInstance().getDefaultSeparator()).append(matcherDesc);
            }
            overviewWriter.write(overviewHeader.toString());

            // Results for matching step
            if (matchingStep.isDoEvaluate()) {
                StringBuilder sbMatching = new StringBuilder();
                sbMatching.append(matchingStep);
                Map<Matcher, Performance> matchingPerformance = performances.get(matchingStep);
                for (Matcher matcher : matchers) {
                    float score = matchingPerformance.get(matcher).getGlobalScore();
                    sbMatching.append(Configuration.getInstance().getDefaultSeparator()).append(score);
                    if (evaluationLevel == EvaluationLevel.SCENARIO) {
                        this.datasetPerformances.get(metric).get(matchingStep).get(matcher).addToGlobalScore(score);
                    } else if (evaluationLevel == EvaluationLevel.DATASET) {
                        this.overallPerformances.get(metric).get(matchingStep).get(matcher).addToGlobalScore(score);
                    }
                }
                overviewWriter.newLine();
                overviewWriter.write(sbMatching.toString());
            }

            // Results for boosting step
            if (ConfigUtils.isEvaluateBoostingOnLine(line)) {
                SimMatrixBoostingStep boostingStep = (SimMatrixBoostingStep) getMatchStep(performances.keySet(), line, true);
                StringBuilder sbBoosting = new StringBuilder();
                sbBoosting.append(boostingStep);
                Map<Matcher, Performance> boostingPerformance = performances.get(boostingStep);
                for (Matcher matcher : matchers) {
                    float score = boostingPerformance.get(matcher).getGlobalScore();
                    sbBoosting.append(Configuration.getInstance().getDefaultSeparator()).append(score);
                    if (evaluationLevel == EvaluationLevel.SCENARIO) {
                        this.datasetPerformances.get(metric).get(boostingStep).get(matcher).addToGlobalScore(score);
                    } else if (evaluationLevel == EvaluationLevel.DATASET) {
                        this.overallPerformances.get(metric).get(boostingStep).get(matcher).addToGlobalScore(score);
                    }
                }
                overviewWriter.newLine();
                overviewWriter.write(sbBoosting.toString());
            }

            overviewWriter.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Data
    @AllArgsConstructor
    private static class Summary {
        private Matcher bestMatcher;
        private float bestScore;
        private int numBestMatchers;
        private List<Matcher> listBestMatchers;

        @Override
        public String toString() {
            return bestMatcher + "," + bestScore + "," + numBestMatchers + ",\"" + listBestMatchers + "\"";
        }
    }

    private Summary summarizePerformance(Map<Matcher, Performance> performances) {
        Matcher bestMatcher = null;
        float bestScore = -1f;
        int numBestMatchers = 0;
        List<Matcher> listBestMatchers = new ArrayList<>();

        for (Matcher matcher : performances.keySet()) {
            float score = performances.get(matcher).getGlobalScore();
            if (score == bestScore) {
                numBestMatchers += 1;
                listBestMatchers.add(matcher);
            } else if (score > bestScore) {
                numBestMatchers = 1;
                bestScore = score;
                bestMatcher = matcher;
                listBestMatchers = new ArrayList<>();
                listBestMatchers.add(matcher);
            }
        }

        return new Summary(bestMatcher, bestScore, numBestMatchers, listBestMatchers);
    }

    private void writePerformanceSummary(Path path, Metric metric, Map<MatchStep, Map<Matcher, Performance>> performances) {
        Path pathToSummaryFile = path.resolve(metric.toString())
                .resolve(Configuration.getInstance().getPerformanceSummaryFileName());
        Configuration config = Configuration.getInstance();

        try {
            Files.createDirectories(pathToSummaryFile.getParent());
            BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(pathToSummaryFile.toString()));

            summaryWriter.write(SUMMARY_HEADER);

            Set<MatchStep> matchSteps = performances.keySet();
            if (config.isEvaluateFirstLineMatchers()) {
                MatchStep matchStep = getMatchStep(matchSteps, 1, false);
                Summary summary = summarizePerformance(performances.get(matchStep));
                summaryWriter.newLine();
                summaryWriter.write(matchStep + "," + summary);
            }

            if (config.isEvaluateSimMatrixBoostingOnFirstLineMatchers()) {
                MatchStep matchStep = getMatchStep(matchSteps, 1, true);
                Summary summary = summarizePerformance(performances.get(matchStep));
                summaryWriter.newLine();
                summaryWriter.write(matchStep + "," + summary);
            }

            if (config.isEvaluateSecondLineMatchers()) {
                MatchStep matchStep = getMatchStep(matchSteps, 2, false);
                Summary summary = summarizePerformance(performances.get(matchStep));
                summaryWriter.newLine();
                summaryWriter.write(matchStep + "," + summary);
            }

            if (config.isEvaluateSecondLineMatchers()) {
                MatchStep matchStep = getMatchStep(matchSteps, 2, true);
                Summary summary = summarizePerformance(performances.get(matchStep));
                summaryWriter.newLine();
                summaryWriter.write(matchStep + "," + summary);
            }

            summaryWriter.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private MatchStep getMatchStep(Set<MatchStep> matchSteps, int line, boolean boosting) {
        for (MatchStep matchStep : matchSteps) {
            if (!boosting && matchStep instanceof MatchingStep ms) {
                if (ms.getLine() == line) {
                    return matchStep;
                }
            }
            if (boosting && matchStep instanceof SimMatrixBoostingStep smbs) {
                if (smbs.getLine() == line) {
                    return matchStep;
                }
            }
        }
        throw new IllegalStateException("Couldn't find matchstep for line " + line + " and boosting="+boosting);
    }
}

