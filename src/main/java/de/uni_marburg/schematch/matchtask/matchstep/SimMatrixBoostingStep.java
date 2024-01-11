package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.OutputWriter;
import de.uni_marburg.schematch.utils.ResultsUtils;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SimMatrixBoostingStep extends MatchStep {
    final static Logger log = LogManager.getLogger(SimMatrixBoostingStep.class);

    private final int line;
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
        List<Matcher> matchers = matchTask.getMatchersForLine(this.line);
        for (Matcher matcher : matchers) {
            float[][] boostedSimMatrix = this.simMatrixBoosting.run(matchTask, this, matchTask.getSimMatrixFromPreviousMatchStep(this, matcher));
            matchTask.setSimMatrix(this, matcher, boostedSimMatrix);
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isSaveOutputSimMatrixBoostingOnFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isSaveOutputSimMatrixBoostingOnSecondLineMatchers()) {
            return;
         }
        log.debug("Saving similarity matrix boosting (line=" + this.line + ") output for scenario: " + matchTask.getScenario().getPath());

        Path outputMatchStepPath = ResultsUtils.getOutputPathForMatchStepInScenario(matchTask, this);

        for (Matcher matcher : matchTask.getMatchersForLine(this.line)) {
            float[][] simMatrix = matchTask.getSimMatrix(this, matcher);
            OutputWriter.writeSimMatrix(outputMatchStepPath, matchTask, matcher.toString(), simMatrix);
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isEvaluateSimMatrixBoostingOnFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isEvaluateSimMatrixBoostingOnSecondLineMatchers()) {
            return;
        }
        log.debug("Evaluating similarity matrix boosting (line=" + this.line + ") output for scenario: " + matchTask.getScenario().getPath());


        for (Matcher matcher : matchTask.getMatchersForLine(this.line)) {
            Map<Metric, Performance> performances = matchTask.getEvaluator().evaluate(matchTask.getSimMatrix(this, matcher));
            for (Metric metric : performances.keySet()) {
                matchTask.setPerformanceForMatcher(metric, this, matcher, performances.get(metric));
            }
        }
    }
}
