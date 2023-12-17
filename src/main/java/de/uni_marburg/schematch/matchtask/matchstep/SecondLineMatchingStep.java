package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.evaluation.Evaluator;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.EvalWriter;
import de.uni_marburg.schematch.utils.OutputWriter;
import de.uni_marburg.schematch.utils.ResultsUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SecondLineMatchingStep extends MatchStep {
    private final static Logger log = LogManager.getLogger(SecondLineMatchingStep.class);

    @Getter
    private final List<Matcher> secondLineMatchers;

    public SecondLineMatchingStep(boolean doRun, boolean doSave, boolean doEvaluate, List<Matcher> secondLineMatchers) {
        super(doRun, doSave, doEvaluate);
        this.secondLineMatchers = secondLineMatchers;
    }

    @Override
    public void run(MatchTask matchTask) {
        log.debug("Running second line matching on scenario: " + matchTask.getScenario().getPath());

        var tablePairs = matchTask.getTablePairs();

        for (Matcher matcher : this.secondLineMatchers) {
            log.trace("Processing second line matcher " + matcher.getClass().getSimpleName());
            for (TablePair tablePair : tablePairs) {
                tablePair.addResultsForSecondLineMatcher(matcher, matcher.match(tablePair));
            }
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        if (!Configuration.getInstance().isSaveOutputSecondLineMatchers()) {
            return;
        }
        log.debug("Saving second line matching output for scenario: " + matchTask.getScenario().getPath());

        String basePath = ResultsUtils.getBaseResultsPathForScenario(matchTask);
        basePath += File.separator + ResultsUtils.getDirNameForMatchStep(this) + File.separator + Configuration.getInstance().getOutputDir();
        for (TablePair tablePair : matchTask.getTablePairs()) {
            for (Matcher matcher : tablePair.getSecondLineMatcherResults().keySet()) {
                String matcherInfo = matcher.toString();
                float[][] simMatrix = tablePair.getResultsForSecondLineMatcher(matcher);
                String path = basePath + File.separator + matcherInfo + File.separator +
                        tablePair.toString() + ".csv";
                OutputWriter.writeSimMatrix(path, simMatrix);
            }
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if (!Configuration.getInstance().isEvaluateSecondLineMatchers()) {
           return;
        }
        log.debug("Evaluating second line matching output for scenario: " + matchTask.getScenario().getPath());

        List<TablePair> tablePairs = matchTask.getTablePairs();

        for (TablePair tablePair : tablePairs) {
            int[][] gtMatrix = tablePair.getGroundTruth();
            for (Matcher matcher : this.secondLineMatchers) {
                float[][] simMatrix = tablePair.getResultsForSecondLineMatcher(matcher);
                tablePair.addPerformanceForSecondLineMatcher(matcher, Evaluator.evaluateMatrix(simMatrix, gtMatrix));
            }
        }

        EvalWriter evalWriter = new EvalWriter(matchTask, this);
        evalWriter.writeMatchStepPerformance();
    }
}
