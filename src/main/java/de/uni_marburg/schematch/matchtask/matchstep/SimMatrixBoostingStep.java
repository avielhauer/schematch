package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.OutputWriter;
import de.uni_marburg.schematch.utils.ResultsUtils;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SimMatrixBoostingStep extends MatchStep {
    final static Logger log = LogManager.getLogger(SimMatrixBoostingStep.class);

    @Getter
    private final int line;
    @Getter
    private final SimMatrixBoosting simMatrixBoosting;

    public SimMatrixBoostingStep(boolean doSave, boolean doEvaluate, int line, SimMatrixBoosting simMatrixBoosting) {
        super(doSave, doEvaluate);
        this.line = line;
        this.simMatrixBoosting = simMatrixBoosting;
    }

    @Override
    public String toString() {
        return super.toString() + "Line" + line;
    }

    @Override
    public void run(MatchTask matchTask) {
        log.debug("Running similarity matrix boosting (line=" + this.line + ") for scenario: " + matchTask.getScenario().getPath());
        Map<String, List<Matcher>> matchers = matchTask.getMatchersForLine(this.line);
        for (String matcherName : matchers.keySet()) {
            for (Matcher matcher : matchers.get(matcherName)) {
                float[][] boostedSimMatrix = this.simMatrixBoosting.run(matchTask, this, matchTask.getSimMatrixFromPreviousMatchStep(this, matcher));
                matchTask.setSimMatrix(this, matcher, boostedSimMatrix);
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

        Path scenarioPath = ResultsUtils.getOutputScenarioResultsPathForMatchStepInScenario(matchTask, this);

        for (Matcher matcher : matchTask.getSimMatrices().get(this).keySet()) {
            float[][] simMatrix = matchTask.getSimMatrix(this, matcher);
            OutputWriter.writeSimMatrix(scenarioPath.resolve(matcher.toString() + ".csv"), simMatrix);
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isEvaluateSimMatrixBoostingOnFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isEvaluateSimMatrixBoostingOnSecondLineMatchers()) {
            return;
        }
        log.debug("Evaluating similarity matrix boosting (line=" + this.line + ") output for scenario: " + matchTask.getScenario().getPath());

        /*List<TablePair> tablePairs = matchTask.getTablePairs();

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
                        tablePair.addBoostedPerformanceForFirstLineMatcher(matcher, EvaluatorOld.evaluateMatrix(simMatrix, gtMatrix));
                        break;
                    case 2:
                        simMatrix = tablePair.getBoostedResultsForSecondLineMatcher(matcher);
                        tablePair.addBoostedPerformanceForSecondLineMatcher(matcher, EvaluatorOld.evaluateMatrix(simMatrix, gtMatrix));
                        break;
                }
            }
        }

        EvalWriter evalWriter = new EvalWriter(matchTask, this);
        evalWriter.writeMatchStepPerformance();*/
    }
}
