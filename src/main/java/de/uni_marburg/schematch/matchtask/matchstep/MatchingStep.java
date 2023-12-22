package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.OutputWriter;
import de.uni_marburg.schematch.utils.ResultsUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class MatchingStep extends MatchStep {
    private final static Logger log = LogManager.getLogger(MatchingStep.class);

    private final int line;
    private final Map<String, List<Matcher>> matchers;

    public MatchingStep(boolean doSave, boolean doEvaluate, int line, Map<String, List<Matcher>> matchers) {
        super(doSave, doEvaluate);
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
        if ((line == 1 && !Configuration.getInstance().isSaveOutputFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isSaveOutputSecondLineMatchers()) {
            return;
        }

        log.debug("Saving " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

        Path scenarioPath = ResultsUtils.getOutputScenarioResultsPathForMatchStepInScenario(matchTask, this);

        for (String matcherName : this.matchers.keySet()) {
            for (Matcher matcher : this.matchers.get(matcherName)) {
                float[][] simMatrix = matchTask.getSimMatrix(this, matcher);
                OutputWriter.writeSimMatrix(scenarioPath.resolve(matcher.toString() + ".csv"), simMatrix);
            }
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isSaveOutputFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isSaveOutputSecondLineMatchers()) {
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
