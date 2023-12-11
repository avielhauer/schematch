package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.*;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

@Data
public class EvalWriter {
    private static final Logger log = LogManager.getLogger(EvalWriter.class);
    private static final Configuration config = Configuration.getInstance();

    public static void writeOverallPerformance(List<Dataset> datasets, List<MatchStep> matchSteps) {
        Map<MatchStep, Map<Matcher, Float>> matcherPerformanceSumAcrossAllDatasets = new HashMap<>();

        // While writing performances, collect performances across all datasets
        for (Dataset dataset : datasets) {
            writeDatasetPerformance(dataset, matchSteps, matcherPerformanceSumAcrossAllDatasets);
        }

        // Write performances for this granularity level
        Function<MatchStep, List<Pair<Matcher, Float>>> matcherPerformancesForMatchStep = (matchStep) -> {
            return EvalWriter.matcherPerformancesForAggregationUnit(matcherPerformanceSumAcrossAllDatasets, matchStep, datasets.size());
        };
        TriConsumer<MatchStep, Matcher, Float> addMatcherScoreToSum = (matchStep, matcher, score) -> {};
        log.info("Best overall performance (average over all datasets):");
        writePerformanceSummary(
                ResultsUtils.getBaseResultsPath(), config.getApplicationName(),
                true,
                matchSteps, matcherPerformancesForMatchStep, addMatcherScoreToSum
        );
        writePerformanceOverview(
                ResultsUtils.getBaseResultsPath(), config.getApplicationName(),
                matchSteps, matcherPerformancesForMatchStep
        );
    }

    public static void writeDatasetPerformance(Dataset dataset, List<MatchStep> matchSteps,
                                               Map<MatchStep, Map<Matcher, Float>> matcherPerformancesAcrossAllDatasets) {
        Map<MatchStep, Map<Matcher, Float>> matcherPerformanceSumForWholeDataset = new HashMap<>();

        // While writing performances, collect performances across all scenarios
        for (MatchTask scenarioMatchTask : dataset.getScenarioMatchTasks()) {
            writeScenarioPerformance(scenarioMatchTask, matcherPerformanceSumForWholeDataset);
        }

        // Write performances for this granularity level
        Function<MatchStep, List<Pair<Matcher, Float>>> matcherPerformancesForMatchStep = (matchStep) -> {
            return EvalWriter.matcherPerformancesForAggregationUnit(matcherPerformanceSumForWholeDataset, matchStep, dataset.getScenarioMatchTasks().size());
        };
        TriConsumer<MatchStep, Matcher, Float> addMatcherScoreToSum = (matchStep, matcher, score) -> {
            EvalWriter.addMatcherScoreToPerformanceSum(matcherPerformancesAcrossAllDatasets, matchStep, matcher, score);
        };
        log.info("Best performance for dataset " + dataset.getName() + " (average over all scenarios):");
        writePerformanceSummary(
                ResultsUtils.getBaseResultsPathForDataset(dataset), dataset.getName(),
                true,
                matchSteps, matcherPerformancesForMatchStep, addMatcherScoreToSum
        );
        writePerformanceOverview(
                ResultsUtils.getBaseResultsPathForDataset(dataset), dataset.getName(),
                matchSteps, matcherPerformancesForMatchStep
        );
    }

    public static void writeScenarioPerformance(MatchTask matchTask,
                                                Map<MatchStep, Map<Matcher, Float>> matcherPerformanceSumForWholeDataset) {
        Map<MatchStep, Map<Matcher, Float>> matcherPerformanceSumForWholeScenario = new HashMap<>();

        // While writing performances, collect performances across all match steps
        for (MatchStep matchStep : matchTask.getMatchSteps()) {
            if (matchStep.isDoEvaluate()) {
                writeMatchStepPerformance(matchTask, matchStep, matcherPerformanceSumForWholeScenario);
            }
        }

        // Write performances for this granularity level
        Function<MatchStep, List<Pair<Matcher, Float>>> matcherPerformancesForMatchStep = (matchStep) -> {
            return EvalWriter.matcherPerformancesForAggregationUnit(matcherPerformanceSumForWholeScenario, matchStep, matchTask.getTablePairs().size());
        };
        TriConsumer<MatchStep, Matcher, Float> addMatcherScoreToSum = (matchStep, matcher, score) -> {
            EvalWriter.addMatcherScoreToPerformanceSum(matcherPerformanceSumForWholeDataset, matchStep, matcher, score);
        };
        writePerformanceSummary(
                ResultsUtils.getBaseResultsPathForScenario(matchTask), matchTask.getScenario().getName(),
                false,
                matchTask.getMatchSteps(), matcherPerformancesForMatchStep, addMatcherScoreToSum
        );
        writePerformanceOverview(
                ResultsUtils.getBaseResultsPathForScenario(matchTask), matchTask.getScenario().getName(),
                matchTask.getMatchSteps(), matcherPerformancesForMatchStep
        );
    }

    public static void writeMatchStepPerformance(MatchTask matchTask, MatchStep matchStep,
                                                 Map<MatchStep, Map<Matcher, Float>> matcherPerformanceSumForWholeScenario) {
        // Write performances for this granularity level
        Function<TablePair, List<Pair<Matcher, Float>>> matcherPerformancesForTablePair = (tablePair) -> {
            return tablePair.getPerformances(matchStep).entrySet().stream().map(
                    e -> Pair.of(e.getKey(), e.getValue().calculateNonBinaryPrecision())
            ).toList();
        };
        TriConsumer<TablePair, Matcher, Float> addMatcherScoreToSum = (ignored, matcher, score) -> {
            EvalWriter.addMatcherScoreToPerformanceSum(matcherPerformanceSumForWholeScenario, matchStep, matcher, score);
        };

        writePerformanceSummary(
                ResultsUtils.getPerformanceBaseResultsPathForMatchStepInScenario(matchTask, matchStep), matchStep.toString(),
                false,
                matchTask.getTablePairs(), matcherPerformancesForTablePair, addMatcherScoreToSum
        );
        writePerformanceOverview(
                ResultsUtils.getPerformanceBaseResultsPathForMatchStepInScenario(matchTask, matchStep), matchStep.toString(),
                matchTask.getTablePairs(), matcherPerformancesForTablePair
        );
    }

    private static <T> List<Pair<Matcher, Float>> matcherPerformancesForAggregationUnit(Map<T, Map<Matcher, Float>> matcherPerformanceSums, T aggregationUnit,
                                                                                        Integer numberOfAggregationUnitsOnNextLowestGranularityLevel) {
        return matcherPerformanceSums.get(aggregationUnit).entrySet().stream().map(
                e -> Pair.of(
                        e.getKey(),
                        // Dividing by the number of aggregation units to obtain the average performance instead of the
                        // performance sum
                        e.getValue() / numberOfAggregationUnitsOnNextLowestGranularityLevel
                )).toList();
    }

    private static <T> void addMatcherScoreToPerformanceSum(Map<T, Map<Matcher, Float>> matcherPerformanceSums, T aggregationUnit,
                                                            Matcher matcher, Float score) {
        matcherPerformanceSums.computeIfAbsent(aggregationUnit, k -> new HashMap<>());
        matcherPerformanceSums.get(aggregationUnit).putIfAbsent(matcher, 0f);
        matcherPerformanceSums.get(aggregationUnit).put(matcher, score + matcherPerformanceSums.get(aggregationUnit).get(matcher));
    }

    public static <T> void writePerformanceSummary(Path directory,
                                                   String fileNameWithoutSuffix,
                                                   Boolean logSummaryResult,
                                                   List<T> aggregationUnits, // can be MatchStep or TablePair
                                                   Function<T, List<Pair<Matcher, Float>>> performancesForCurrentGranularityLevel,
                                                   TriConsumer<T, Matcher, Float> addMatcherPerformanceForNextHighestGranularityLevel) {
        try {
            CsvEvaluationWriter summaryWriter = new CsvEvaluationWriter(
                    directory.resolve(fileNameWithoutSuffix + config.getPerformanceSummaryFileSuffix())
            );
            summaryWriter.addSummaryHeader();

            for (T aggregationUnit : aggregationUnits) {
                if (aggregationUnit instanceof MatchStep && !((MatchStep)aggregationUnit).isDoEvaluate()) {
                    continue;
                }
                // FIXME: add evaluation for table pair generation
                if (aggregationUnit instanceof TablePairGenerationStep) {
                    continue;
                }

                Pair<Float, List<Matcher>> bestMatchers = bestMatchersForAggregationUnit(
                        aggregationUnit, performancesForCurrentGranularityLevel, addMatcherPerformanceForNextHighestGranularityLevel
                );

                summaryWriter.addSummary(aggregationUnit, bestMatchers.getLeft(), bestMatchers.getRight());

                if (logSummaryResult) {
                    Formatter formatter = new Formatter();
                    formatter.format(
                            "  %s best matcher: %s (non-binary precision: %.3f)",
                            aggregationUnit,
                            bestMatchers.getRight().get(0),
                            bestMatchers.getLeft()
                    );
                    log.info(formatter);
                }
            }

            summaryWriter.write();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static <T> Pair<Float, List<Matcher>> bestMatchersForAggregationUnit(T aggregationUnit,
                                                                                 Function<T, List<Pair<Matcher, Float>>> performancesForCurrentGranularityLevel,
                                                                                 TriConsumer<T, Matcher, Float> addMatcherPerformanceForNextHighestGranularityLevel) {
        float bestPrecision = -1f;
        List<Matcher> bestMatchers = new ArrayList<>();
        for (Pair<Matcher, Float> matcherPerformance :
                performancesForCurrentGranularityLevel.apply(aggregationUnit)) {
            if (matcherPerformance.getValue() == bestPrecision) {
                bestMatchers.add(matcherPerformance.getKey());
            } else if (matcherPerformance.getValue() > bestPrecision) {
                bestPrecision = matcherPerformance.getValue();
                bestMatchers.clear();
                bestMatchers.add(matcherPerformance.getKey());
            }
            addMatcherPerformanceForNextHighestGranularityLevel.accept(aggregationUnit, matcherPerformance.getKey(), matcherPerformance.getValue());
        }
        return Pair.of(bestPrecision, bestMatchers);
    }

    public static <T> void writePerformanceOverview(Path directory,
                                                    String fileNameWithoutSuffix,
                                                    List<T> aggregationUnits,
                                                    Function<T, List<Pair<Matcher, Float>>> performancesForCurrentGranularityLevel) {
        try {
            CsvEvaluationWriter overviewWriter = new CsvEvaluationWriter(
                    directory.resolve(fileNameWithoutSuffix + config.getPerformanceOverviewFileSuffix())
            );

            List<T> activeAggregationUnits = new ArrayList<>();
            Map<Matcher, Float[]> matcherResults = new HashMap<>();
            int numIgnoredAggregationUnits = 0;
            for (int i = 0; i < aggregationUnits.size(); i++) {
                T aggregationUnit = aggregationUnits.get(i);
                if (aggregationUnit instanceof MatchStep && !((MatchStep)aggregationUnit).isDoEvaluate()) {
                    numIgnoredAggregationUnits += 1;
                    continue;
                }
                // FIXME: add evaluation for table pair generation
                if (aggregationUnit instanceof TablePairGenerationStep) {
                    numIgnoredAggregationUnits += 1;
                    continue;
                }

                activeAggregationUnits.add(aggregationUnit);
                for (Pair<Matcher, Float> matcherPrecision: performancesForCurrentGranularityLevel.apply(aggregationUnit)) {
                    matcherResults.computeIfAbsent(matcherPrecision.getKey(), k -> new Float[aggregationUnits.size()]);
                    matcherResults.get(matcherPrecision.getKey())[i - numIgnoredAggregationUnits] = matcherPrecision.getValue();
                }
            }

            overviewWriter.addOverviewInformation(activeAggregationUnits, matcherResults);
            overviewWriter.write();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    static class CsvEvaluationWriter {
        private final BufferedWriter writer;
        private final List<String[]> lines = new ArrayList<>();

        public CsvEvaluationWriter(Path evaluationPath) throws IOException {
            Files.createDirectories(evaluationPath.getParent());
            writer = new BufferedWriter(new FileWriter(evaluationPath.toString()));
        }

        public void addSummaryHeader() throws IOException {
            addCsvLine(
                    "AggregationUnit\\Summary",
                    "BestMatcher",
                    "BestScore",
                    "numBestMatchers",
                    "ListBestMatchers"
            );
        }

        public <T> void addSummary(T aggregationUnit, Float bestPrecision, List<Matcher> bestMatchers) throws IOException {
            Matcher bestMatcherForMatchStep = bestMatchers.get(0);

            addCsvLine(
                    aggregationUnit.toString(),
                    bestMatcherForMatchStep.toString(),
                    String.valueOf(bestPrecision) ,
                    String.valueOf(bestMatchers.size()),
                    bestMatchers.toString()
            );
        }

        public <T> void addOverviewInformation(List<T> aggregationUnits,
                                               Map<Matcher, Float[]> matcherResults) throws IOException {
            String[] matcherHeaders = new String[aggregationUnits.size() + 1];
            matcherHeaders[0] = "Matcher\\AggregationUnit";
            for (int i = 0; i < aggregationUnits.size(); i++) {
                matcherHeaders[i + 1] = aggregationUnits.get(i).toString();
            }
            this.addCsvLine(matcherHeaders);

            List<Pair<Matcher, Float[]>> sortedMatchers = new ArrayList<>(
                    matcherResults.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList()
            );
            sortedMatchers.sort(Comparator.comparing(pair -> pair.getKey().toString()));

            for (Pair<Matcher, Float[]> matcherResult : sortedMatchers) {
                String[] matcherResultLine = new String[aggregationUnits.size() + 1];
                matcherResultLine[0] = matcherResult.getKey().toString();
                for (int i = 0; i < aggregationUnits.size(); i++) {
                    matcherResultLine[i + 1] = String.valueOf(matcherResult.getValue()[i]);
                }
                this.addCsvLine(matcherResultLine);
            }
        }

        private void addCsvLine(String... line) {
            lines.add(line);
        }

        public void write() throws IOException {
            for (String[] line : lines) {
                List<String> quotedCells = Arrays.stream(line).map(cell -> {
                    if (cell.contains(config.getDefaultSeparator())) {
                        return '"' + cell + '"';
                    }
                    return cell;
                }).toList();
                writer.write(String.join(config.getDefaultSeparator(), quotedCells));
                writer.newLine();
            }
            writer.close();
        }
    }
}

