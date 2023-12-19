package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class MatchingStep extends MatchStep {
    private final static Logger log = LogManager.getLogger(MatchingStep.class);

    private final int line;
    private final Map<String, List<Matcher>> matchers;

    public MatchingStep(boolean doRun, boolean doSave, boolean doEvaluate, int line, Map<String, List<Matcher>> matchers) {
        super(doRun, doSave, doEvaluate);
        this.line = line;
        this.matchers = matchers;
    }

    @Override
    public String toString() {
        return super.toString() + "Line" + line;
    }

    @Override
    public void run(MatchTask matchTask) {
        log.debug("Running " + this.line + ". line matching on scenario: " + matchTask.getScenario().getPath());

        for (String matcherName : this.matchers.keySet()) {
            for (Matcher matcher : this.matchers.get(matcherName)) {
                log.trace("Processing " + this.line + ". line matcher " + matcherName);
                float[][] simMatrix = matcher.match(matchTask, this);
                matchTask.setSimMatrix(this, matcher, simMatrix);
            }
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        if (!Configuration.getInstance().isSaveOutputFirstLineMatchers()) {
            return;
        }

        log.debug("Saving " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

        /*Path basePath = ResultsUtils.getOutputBaseResultsPathForMatchStepInScenario(matchTask, this);
        for (TablePair tablePair : matchTask.getTablePairs()) {
            for (Matcher matcher : tablePair.getFirstLineMatcherResults().keySet()) {
                float[][] simMatrix = tablePair.getResultsForFirstLineMatcher(matcher);
                OutputWriter.writeSimMatrix(basePath.resolve(matcher.toString()).resolve(tablePair + ".csv"), simMatrix);
            }
        }*/
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if (!Configuration.getInstance().isEvaluateFirstLineMatchers()) {
           return;
        }

        log.debug("Evaluating " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

        /*List<TablePair> tablePairs = matchTask.getTablePairs();

        for (TablePair tablePair : tablePairs) {
            int[][] gtMatrix = tablePair.getGroundTruth();
            for (String matcherName : this.matchers.keySet()) {
                for (Matcher matcher : this.matchers.get(matcherName)) {
                    float[][] simMatrix = tablePair.getResultsForFirstLineMatcher(matcher);
                    tablePair.addPerformanceForFirstLineMatcher(matcher, EvaluatorOld.evaluateMatrix(simMatrix, gtMatrix));
                }
            }
        }

        EvalWriter evalWriter = new EvalWriter(matchTask, this);
        evalWriter.writeMatchStepPerformance();*/
    }
}
