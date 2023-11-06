package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.evaluation.Evaluator;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.EvalWriter;
import de.uni_marburg.schematch.utils.OutputWriter;
import de.uni_marburg.schematch.utils.ResultsUtils;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SimMatrixBoostingStep extends MatchStep {
    final static Logger log = LogManager.getLogger(SimMatrixBoostingStep.class);

    @Getter
    private final int line;
    @Getter
    private final SimMatrixBoosting simMatrixBoosting;

    public SimMatrixBoostingStep(boolean doRun, boolean doSave, boolean doEvaluate, int line, SimMatrixBoosting simMatrixBoosting) {
        super(doRun, doSave, doEvaluate);
        this.line = line;
        this.simMatrixBoosting = simMatrixBoosting;
    }

    @Override
    public void run(MatchTask matchTask) {
        log.debug("Running similarity matrix boosting (line=" + this.line + ") for scenario: " + matchTask.getScenario().getPath());
        for (TablePair tablePair : matchTask.getTablePairs()) {
            if (this.line == 1) {
                for (Matcher matcher : tablePair.getFirstLineMatcherResults().keySet()) {
                    float[][] simMatrix = tablePair.getResultsForFirstLineMatcher(matcher);
                    tablePair.addBoostedResultsForFirstLineMatcher(matcher, this.simMatrixBoosting.run(simMatrix));
                }
            } else {
                for (Matcher matcher : tablePair.getSecondLineMatcherResults().keySet()) {
                    float[][] simMatrix = tablePair.getResultsForSecondLineMatcher(matcher);
                    tablePair.addBoostedResultsForSecondLineMatcher(matcher, this.simMatrixBoosting.run(simMatrix));
                }
            }
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isSaveOutputSimMatrixBoostingOnFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isSaveOutputSimMatrixBoostingOnSecondLineMatchers()) {
            return;
        }
        log.debug("Saving similarity matrix boosting (line=" + this.line + ") output for scenario: " + matchTask.getScenario().getPath());

        String basePath = ResultsUtils.getBaseResultsPathForScenario(matchTask);
        basePath += File.separator + ResultsUtils.getDirNameForMatchStep(this) + File.separator + Configuration.getInstance().getOutputDir();
        for (TablePair tablePair : matchTask.getTablePairs()) {
            Map<Matcher, float[][]> boostedResults;
            if (this.line == 1) {
                boostedResults = tablePair.getBoostedFirstLineMatcherResults();
            } else {
                boostedResults = tablePair.getBoostedSecondLineMatcherResults();
            }
            for (Matcher matcher : boostedResults.keySet()) {
                String matcherInfo = matcher.toString();
                float[][] simMatrix = boostedResults.get(matcher);
                String path = basePath + File.separator + matcherInfo + File.separator +
                        tablePair.toString() + ".csv";
                OutputWriter.writeSimMatrix(path, simMatrix);
            }
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isEvaluateSimMatrixBoostingOnFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isEvaluateSimMatrixBoostingOnSecondLineMatchers()) {
            return;
        }
        log.debug("Evaluating similarity matrix boosting (line=" + this.line + ") output for scenario: " + matchTask.getScenario().getPath());

        List<TablePair> tablePairs = matchTask.getTablePairs();

        Set<Matcher> matchers = switch (this.line) {
            case 1 -> tablePairs.get(0).getFirstLineMatcherPerformances().keySet();
            case 2 -> tablePairs.get(0).getSecondLineMatcherResults().keySet();
            default -> throw new RuntimeException("Illegal matcher line set for similarity matrix boosting");
        };

        for (TablePair tablePair : tablePairs) {
            int[][] gtMatrix = tablePair.getGroundTruth();
            for (Matcher matcher : matchers) {
                float[][] simMatrix;
                switch (this.line) {
                    case 1:
                        simMatrix = tablePair.getBoostedResultsForFirstLineMatcher(matcher);
                        tablePair.addBoostedPerformanceForFirstLineMatcher(matcher, Evaluator.evaluateMatrix(simMatrix, gtMatrix));
                        break;
                    case 2:
                        simMatrix = tablePair.getBoostedResultsForSecondLineMatcher(matcher);
                        tablePair.addBoostedPerformanceForSecondLineMatcher(matcher, Evaluator.evaluateMatrix(simMatrix, gtMatrix));
                        break;
                }
            }
        }

        EvalWriter evalWriter = new EvalWriter(matchTask, this);
        evalWriter.writeMatchStepPerformance();
    }
}
