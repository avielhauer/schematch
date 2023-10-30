package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.ResultsWriter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

@RequiredArgsConstructor
public class SimMatrixBoostingStep implements MatchStep {
    final static Logger log = LogManager.getLogger(SimMatrixBoostingStep.class);

    private final int line;
    private final SimMatrixBoosting simMatrixBoosting;

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

        String basePath = ResultsWriter.getBaseResultsPathForScenario(matchTask);
        basePath += File.separator + this.getClass().getSimpleName() + "Line" + this.line + File.separator + Configuration.getInstance().getOutputDir();
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
                ResultsWriter.writeSimMatrix(path, simMatrix);
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
    }
}
