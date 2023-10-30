package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.evaluation.Evaluator;
import de.uni_marburg.schematch.evaluation.performance.MatcherPerformance;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TokenizedMatcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.EvalWriter;
import de.uni_marburg.schematch.utils.ResultsWriter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class FirstLineMatchingStep implements MatchStep {
    final static Logger log = LogManager.getLogger(FirstLineMatchingStep.class);

    private final Map<String, List<Matcher>> firstLineMatchers;

    @Override
    public void run(MatchTask matchTask) {
        log.debug("Running first line matching on scenario: " + matchTask.getScenario().getPath());

        var tablePairs = matchTask.getTablePairs();

        for (String matcherName : this.firstLineMatchers.keySet()) {
            for (Matcher matcher : this.firstLineMatchers.get(matcherName)) {
                log.trace("Processing first line matcher " + matcherName);
                for (TablePair tablePair : tablePairs) {
                    tablePair.addResultsForFirstLineMatcher(matcher, matcher.match(tablePair));
                }
            }
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        if (!Configuration.getInstance().isSaveOutputFirstLineMatchers()) {
            return;
        }

        log.debug("Saving first line matching output for scenario: " + matchTask.getScenario().getPath());

        String basePath = ResultsWriter.getBaseResultsPathForScenario(matchTask);
        basePath += File.separator + this.getClass().getSimpleName() + File.separator + Configuration.getInstance().getOutputDir();
        for (TablePair tablePair : matchTask.getTablePairs()) {
            for (Matcher matcher : tablePair.getFirstLineMatcherResults().keySet()) {
                StringBuilder matcherInfo = new StringBuilder(matcher.toString());
                if (matcher instanceof TokenizedMatcher) {
                    matcherInfo.append("___").append(((TokenizedMatcher) matcher).getTokenizer().toString());
                }
                float[][] simMatrix = tablePair.getResultsForFirstLineMatcher(matcher);
                String path = basePath + File.separator + matcherInfo + File.separator +
                        tablePair.toString() + ".csv";
                ResultsWriter.writeSimMatrix(path, simMatrix);
            }
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if (!Configuration.getInstance().isEvaluateFirstLineMatchers()) {
           return;
        }

        log.debug("Evaluating first line matching output for scenario: " + matchTask.getScenario().getPath());
    }
}
