package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.InputReader;
import de.uni_marburg.schematch.utils.OutputWriter;
import de.uni_marburg.schematch.utils.ResultsUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class MatchingStep extends MatchStep {
    private final static Logger log = LogManager.getLogger(MatchingStep.class);

    private final int line;
    private final List<Matcher> matchers;

    public MatchingStep(boolean doSave, boolean doEvaluate, int line, List<Matcher> matchers) {
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

        for (Matcher matcher : this.matchers) {
            float[][] simMatrix = null;
            if ((line == 1 && Configuration.getInstance().isReadCacheFirstLineMatchers()) ||
                    line == 2 && Configuration.getInstance().isReadCacheSecondLineMatchers()) {
                simMatrix = InputReader.readCache(matchTask,this, matcher);
            }
            if (simMatrix == null) {
                log.debug("Processing " + this.line + ". line matcher " + matcher.toString());
                simMatrix = matcher.match(matchTask, this);
            }
            matchTask.setSimMatrix(this, matcher, simMatrix);
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        // write cache
        if ((line == 1 && Configuration.getInstance().isWriteCacheFirstLineMatchers()) ||
                line == 2 && Configuration.getInstance().isWriteCacheSecondLineMatchers()) {
            log.debug("Caching  " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

            Path outputMatchStepPath = ResultsUtils.getCachePathForMatchStepInScenario(matchTask, this);

            for (Matcher matcher : this.matchers) {
                float[][] simMatrix = matchTask.getSimMatrix(this, matcher);
                OutputWriter.writeSimMatrix(outputMatchStepPath, matchTask, matcher.toString(), simMatrix);
                matchTask.incrementCacheWrite();
            }
        }

        // write results
        if ((line == 1 && Configuration.getInstance().isSaveOutputFirstLineMatchers()) ||
                line == 2 && Configuration.getInstance().isSaveOutputSecondLineMatchers()) {
            log.debug("Saving " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

            Path outputMatchStepPath = ResultsUtils.getOutputPathForMatchStepInScenario(matchTask, this);

            for (Matcher matcher : this.matchers) {
                float[][] simMatrix = matchTask.getSimMatrix(this, matcher);
                OutputWriter.writeSimMatrix(outputMatchStepPath, matchTask, matcher.toString(), simMatrix);
            }
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isEvaluateFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isEvaluateSecondLineMatchers()) {
            return;
        }

        log.debug("Evaluating " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

        for (Matcher matcher : this.matchers) {
            Map<Metric, Performance> performances = matchTask.getEvaluator().evaluate(matchTask.getSimMatrix(this, matcher));
            for (Metric metric : performances.keySet()) {
                matchTask.setPerformanceForMatcher(metric, this, matcher, performances.get(metric));
            }
        }
    }
}
