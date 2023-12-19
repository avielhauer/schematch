package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.*;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
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

    private static String summaryHeader;

    private static Map<MatchStep, Map<Matcher, Float>> avgTablePairPerformance = new HashMap<>();
    private static Map<MatchStep, Map<Matcher, Float>> sumScenarioPerformance = new HashMap<>();
    private static Map<MatchStep, Map<Matcher, Float>> sumDatasetPerformance = new HashMap<>();

    private final MatchTask matchTask;
    private final MatchStep matchStep;
    private List<Matcher> matchersSortedByName;
    private String overviewHeader;
    private Path path;

    public EvalWriter(MatchTask matchTask, MatchStep matchStep) {
        this.matchTask = matchTask;
        this.matchStep = matchStep;
        // set matchers
        // FIXME: refactor
        /*Set<Matcher> matchers = null;
        TablePair tp = matchTask.getTablePairs().get(0);
        if (matchStep instanceof MatchingStep && ((MatchingStep) matchStep).getLine() == 1) {
            matchers = tp.getFirstLineMatcherPerformances().keySet();
        } else if (matchStep instanceof MatchingStep && ((MatchingStep) matchStep).getLine() == 2) {
            matchers = tp.getSecondLineMatcherResults().keySet();
        } else if (matchStep instanceof SimMatrixBoostingStep) {
            if (((SimMatrixBoostingStep) matchStep).getLine() == 1) {
                matchers = tp.getBoostedFirstLineMatcherPerformances().keySet();
            } else {
                matchers = tp.getBoostedSecondLineMatcherPerformances().keySet();
            }
        }
        this.matchersSortedByName = MatcherUtils.sortMatchersByName(matchers);
        // set header
        StringBuilder sb = new StringBuilder();
        for (Matcher matcher : matchersSortedByName) {
            String matcherDesc = matcher.toString();
            sb.append(Configuration.getInstance().getDefaultSeparator()).append(matcherDesc);
        }
        this.overviewHeader = sb.toString();
        // set path
        Path basePath = ResultsUtils.getBaseResultsPathForScenario(matchTask);
        this.path = basePath.resolve(matchStep.toString()).resolve(Configuration.getInstance().getPerformanceDir());*/
    }

    private static String getSummaryHeader() {
        if (summaryHeader == null) {
            Configuration config = Configuration.getInstance();

            summaryHeader = config.getDefaultSeparator() + "BestMatcher" +
                    config.getDefaultSeparator() + "BestScore" +
                    config.getDefaultSeparator() + "numBestMatchers" +
                    config.getDefaultSeparator() + "ListBestMatchers";
        }

        return summaryHeader;
    }

    public static void writeOverallPerformance(List<MatchStep> matchSteps) {
        Configuration config = Configuration.getInstance();

        Path summaryPath = ResultsUtils.getBaseResultsPath().resolve(config.getApplicationName() + config.getPerformanceSummaryFileSuffix());

        try {
            Files.createDirectories(summaryPath.getParent());
            BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(summaryPath.toString()));
            summaryWriter.write("MatchStep\\Summary" + EvalWriter.getSummaryHeader());

            log.info("Best overall performance (average over all datasets):");

            for (MatchStep matchStep : matchSteps) {
                // FIXME: add evaluation for table pair generation
                if (matchStep instanceof TablePairGenerationStep) {
                    continue;
                }
                if (!matchStep.isDoEvaluate()) {
                    continue;
                }

                Matcher bestMatcherForMatchStep = null;
                float bestNonBinaryPrecisionForMatchStep = -1f;
                int numBestMatchersForMatchStep = 0;
                List<Matcher> listBestMatchersForMatchStep = new ArrayList<>();

                Map<Matcher, Float> matcherPerformances = sumDatasetPerformance.get(matchStep);
                for (Matcher matcher : matcherPerformances.keySet()) {
                    float score = matcherPerformances.get(matcher);
                    if (score == bestNonBinaryPrecisionForMatchStep) {
                        numBestMatchersForMatchStep += 1;
                        listBestMatchersForMatchStep.add(matcher);
                    } else if (score > bestNonBinaryPrecisionForMatchStep) {
                        numBestMatchersForMatchStep = 1;
                        bestNonBinaryPrecisionForMatchStep = score;
                        bestMatcherForMatchStep = matcher;
                        listBestMatchersForMatchStep = new ArrayList<>();
                        listBestMatchersForMatchStep.add(matcher);
                    }
                }
                bestNonBinaryPrecisionForMatchStep = bestNonBinaryPrecisionForMatchStep / config.getDatasetConfigurations().size();

                String matchStepInfo = matchStep.getClass().getSimpleName();
                if (matchStep instanceof SimMatrixBoostingStep) {
                    matchStepInfo += "Line" + ((SimMatrixBoostingStep) matchStep).getLine();
                }
                String matcherInfo = bestMatcherForMatchStep.toString();
                String summaryLine = matchStepInfo + config.getDefaultSeparator() +
                        matcherInfo + // BestMatcher
                        config.getDefaultSeparator() +
                        bestNonBinaryPrecisionForMatchStep + // BestScore
                        config.getDefaultSeparator() +
                        numBestMatchersForMatchStep + // numBestMatchers
                        config.getDefaultSeparator() +
                        "\"" + listBestMatchersForMatchStep + "\""; // ListBestMatchers

                summaryWriter.newLine();
                summaryWriter.write(summaryLine);

                Formatter formatter = new Formatter();
                formatter.format("  %s best matcher: %s (non-binary precision: %.3f)", matchStepInfo, matcherInfo, bestNonBinaryPrecisionForMatchStep);
                log.info(formatter);
            }
            summaryWriter.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void writeDatasetPerformance(Dataset dataset, List<MatchStep> matchSteps) {
        Configuration config = Configuration.getInstance();

        Path summaryPath = ResultsUtils.getBaseResultsPathForDataset(dataset)
                .resolve(dataset.getName() + config.getPerformanceSummaryFileSuffix());

        try {
            Files.createDirectories(summaryPath.getParent());
            BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(summaryPath.toString()));
            summaryWriter.write("MatchStep\\Summary" + EvalWriter.getSummaryHeader());

            log.info("Best performance for dataset " + dataset.getName() + " (average over all scenarios):");

            for (MatchStep matchStep : matchSteps) {
                // FIXME: add evaluation for table pair generation
                if (matchStep instanceof TablePairGenerationStep) {
                    continue;
                }
                if (!matchStep.isDoEvaluate()) {
                    continue;
                }

                sumDatasetPerformance.computeIfAbsent(matchStep, k -> new HashMap<>());

                Matcher bestMatcherForMatchStep = null;
                float bestNonBinaryPrecisionForMatchStep = -1f;
                int numBestMatchersForMatchStep = 0;
                List<Matcher> listBestMatchersForMatchStep = new ArrayList<>();

                Map<Matcher, Float> matcherPerformances = sumScenarioPerformance.get(matchStep);
                for (Matcher matcher : matcherPerformances.keySet()) {
                    float score = matcherPerformances.get(matcher) / dataset.getScenarioNames().size();
                    if (score == bestNonBinaryPrecisionForMatchStep) {
                        numBestMatchersForMatchStep += 1;
                        listBestMatchersForMatchStep.add(matcher);
                    } else if (score > bestNonBinaryPrecisionForMatchStep) {
                        numBestMatchersForMatchStep = 1;
                        bestNonBinaryPrecisionForMatchStep = score;
                        bestMatcherForMatchStep = matcher;
                        listBestMatchersForMatchStep = new ArrayList<>();
                        listBestMatchersForMatchStep.add(matcher);
                    }
                    sumDatasetPerformance.get(matchStep).putIfAbsent(matcher, 0f);
                    sumDatasetPerformance.get(matchStep).put(matcher, score + sumDatasetPerformance.get(matchStep).get(matcher));
                }

                String matchStepInfo = matchStep.getClass().getSimpleName();
                if (matchStep instanceof SimMatrixBoostingStep) {
                    matchStepInfo += "Line" + ((SimMatrixBoostingStep) matchStep).getLine();
                }
                String matcherInfo = bestMatcherForMatchStep.toString();
                String summaryLine = matchStepInfo + config.getDefaultSeparator() +
                        matcherInfo + // BestMatcher
                        config.getDefaultSeparator() +
                        bestNonBinaryPrecisionForMatchStep + // BestScore
                        config.getDefaultSeparator() +
                        numBestMatchersForMatchStep + // numBestMatchers
                        config.getDefaultSeparator() +
                        "\"" + listBestMatchersForMatchStep + "\""; // ListBestMatchers

                summaryWriter.newLine();
                summaryWriter.write(summaryLine);

                Formatter formatter = new Formatter();
                formatter.format("  %s best matcher: %s (non-binary precision: %.3f)", matchStepInfo, matcherInfo, bestNonBinaryPrecisionForMatchStep);
                log.info(formatter);
            }
            summaryWriter.close();
            sumScenarioPerformance = new HashMap<>();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void writeScenarioPerformance(Dataset dataset, Scenario scenario, List<MatchStep> matchSteps) {
        Configuration config = Configuration.getInstance();

        Path summaryPath = ResultsUtils.getBaseResultsPathForDataset(dataset)
                .resolve(scenario.getName())
                .resolve(scenario.getName() + config.getPerformanceSummaryFileSuffix());

        try {
            Files.createDirectories(summaryPath.getParent());
            BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(summaryPath.toString()));
            summaryWriter.write("MatchStep\\Summary" + EvalWriter.getSummaryHeader());

            for (MatchStep matchStep : matchSteps) {
                // FIXME: add evaluation for table pair generation
                if (matchStep instanceof TablePairGenerationStep) {
                    continue;
                }
                if (!matchStep.isDoEvaluate()) {
                    continue;
                }

                sumScenarioPerformance.computeIfAbsent(matchStep, k -> new HashMap<>());

                Matcher bestMatcherForMatchStep = null;
                float bestNonBinaryPrecisionForMatchStep = -1f;
                int numBestMatchersForMatchStep = 0;
                List<Matcher> listBestMatchersForMatchStep = new ArrayList<>();

                Map<Matcher, Float> matcherPerformances = avgTablePairPerformance.get(matchStep);
                for (Matcher matcher : matcherPerformances.keySet()) {
                    float score = matcherPerformances.get(matcher);
                    if (score == bestNonBinaryPrecisionForMatchStep) {
                        numBestMatchersForMatchStep += 1;
                        listBestMatchersForMatchStep.add(matcher);
                    } else if (score > bestNonBinaryPrecisionForMatchStep) {
                        numBestMatchersForMatchStep = 1;
                        bestNonBinaryPrecisionForMatchStep = score;
                        bestMatcherForMatchStep = matcher;
                        listBestMatchersForMatchStep = new ArrayList<>();
                        listBestMatchersForMatchStep.add(matcher);
                    }
                    sumScenarioPerformance.get(matchStep).putIfAbsent(matcher, 0f);
                    sumScenarioPerformance.get(matchStep).put(matcher, score + sumScenarioPerformance.get(matchStep).get(matcher));
                }

                String summaryLine = matchStep.getClass().getSimpleName();
                if (matchStep instanceof SimMatrixBoostingStep) {
                    summaryLine += "Line" + ((SimMatrixBoostingStep) matchStep).getLine();
                }
                String matcherInfo = bestMatcherForMatchStep.toString();
                summaryLine = summaryLine + config.getDefaultSeparator() +
                        matcherInfo + // BestMatcher
                        config.getDefaultSeparator() +
                        bestNonBinaryPrecisionForMatchStep + // BestScore
                        config.getDefaultSeparator() +
                        numBestMatchersForMatchStep + // numBestMatchers
                        config.getDefaultSeparator() +
                        "\"" + listBestMatchersForMatchStep + "\""; // ListBestMatchers

                summaryWriter.newLine();
                summaryWriter.write(summaryLine);
            }
            summaryWriter.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void writeMatchStepPerformance() {
        /*Configuration config = Configuration.getInstance();
        List<TablePair> tablePairs = this.matchTask.getTablePairs();

        String matchStepInfo = this.matchStep.toString();

        Path overviewPath = this.path.resolve(matchStepInfo + config.getPerformanceOverviewFileSuffix());
        Path summaryPath = this.path.resolve(matchStepInfo + config.getPerformanceSummaryFileSuffix());

        try {
            Files.createDirectories(overviewPath.getParent());
            BufferedWriter overviewWriter = new BufferedWriter(new FileWriter(overviewPath.toString()));
            overviewWriter.write("TablePair\\Matcher" + this.overviewHeader);

            Files.createDirectories(summaryPath.getParent());
            BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(summaryPath.toString()));
            summaryWriter.write("TablePair\\Summary" + EvalWriter.getSummaryHeader());

            Map<Matcher, Float> sumMatcherPerformanceForMatchStep = new HashMap<>();
            for (Matcher matcher : matchersSortedByName) {
                sumMatcherPerformanceForMatchStep.put(matcher, 0f);
            }

            for (TablePair tablePair : tablePairs) {
                Matcher bestMatcherForTablePair = null;
                float bestNonBinaryPrecisionForTablePair = -1f;
                int numBestMatchersForTablePair = 0;
                List<Matcher> listBestMatchersForTablePair = new ArrayList<>();

                StringBuilder sbOverviewLine = new StringBuilder();
                sbOverviewLine.append(tablePair);
                for (Matcher matcher : matchersSortedByName) {
                    sbOverviewLine.append(config.getDefaultSeparator());
                    float nonBinaryPrecision = tablePair.getPerformance(matchStep, matcher).calculateNonBinaryPrecision();
                    sbOverviewLine.append(nonBinaryPrecision);
                    if (nonBinaryPrecision == bestNonBinaryPrecisionForTablePair) {
                        numBestMatchersForTablePair += 1;
                        listBestMatchersForTablePair.add(matcher);
                    } else if (nonBinaryPrecision > bestNonBinaryPrecisionForTablePair) {
                        numBestMatchersForTablePair = 1;
                        bestNonBinaryPrecisionForTablePair = nonBinaryPrecision;
                        bestMatcherForTablePair = matcher;
                        listBestMatchersForTablePair = new ArrayList<>();
                        listBestMatchersForTablePair.add(matcher);
                    }
                    sumMatcherPerformanceForMatchStep.put(matcher, sumMatcherPerformanceForMatchStep.get(matcher) + nonBinaryPrecision);
                }

                overviewWriter.newLine();
                overviewWriter.write(sbOverviewLine.toString());

                String matcherInfo = bestMatcherForTablePair.toString();

                String sbSummaryLine = String.valueOf(tablePair) +
                        config.getDefaultSeparator() +
                        matcherInfo + // BestMatcher
                        config.getDefaultSeparator() +
                        bestNonBinaryPrecisionForTablePair + // BestScore
                        config.getDefaultSeparator() +
                        numBestMatchersForTablePair + // numBestMatchers
                        config.getDefaultSeparator() +
                        "\"" + listBestMatchersForTablePair + "\""; // ListBestMatchers

                summaryWriter.newLine();
                summaryWriter.write(sbSummaryLine);
            }

            Map<Matcher, Float> avgMatcherPerformanceForMatchStep = new HashMap<>();
            for (Matcher matcher : sumMatcherPerformanceForMatchStep.keySet()) {
                avgMatcherPerformanceForMatchStep.put(matcher, sumMatcherPerformanceForMatchStep.get(matcher)/tablePairs.size());
            }

            EvalWriter.avgTablePairPerformance.put(this.matchStep, avgMatcherPerformanceForMatchStep);

            overviewWriter.close();
            summaryWriter.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }*/
    }
}

