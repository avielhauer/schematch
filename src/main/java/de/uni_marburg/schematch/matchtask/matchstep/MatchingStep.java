package de.uni_marburg.schematch.matchtask.matchstep;

import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
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
            log.trace("Processing " + this.line + ". line matcher " + matcher.toString());
            float[][] simMatrix = matcher.match(matchTask, this);
            matchTask.setSimMatrix(this, matcher, simMatrix);
        }
    }

    @Override
    public void save(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isSaveOutputFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isSaveOutputSecondLineMatchers()) {
            return;
        }

        log.debug("Saving " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

        Path outputMatchStepPath = ResultsUtils.getOutputPathForMatchStepInScenario(matchTask, this);

        for (Matcher matcher : this.matchers) {
            float[][] simMatrix = matchTask.getSimMatrix(this, matcher);
            OutputWriter.writeSimMatrix(outputMatchStepPath.resolve(matcher.toString() + ".csv"), simMatrix);
        }
    }

    @Override
    public void evaluate(MatchTask matchTask) {
        if ((line == 1 && !Configuration.getInstance().isSaveOutputFirstLineMatchers()) ||
                line == 2 && !Configuration.getInstance().isSaveOutputSecondLineMatchers()) {
            return;
        }

        log.debug("Evaluating " + this.line + ". line matching output for scenario: " + matchTask.getScenario().getPath());

        for (Matcher matcher : this.matchers) {
            Map<Metric, Performance> performances = matchTask.getEvaluator().evaluate(matchTask.getSimMatrix(this, matcher));
            for (Metric metric : performances.keySet()) {
                matchTask.setPerformanceForMatcher(metric, this, matcher, performances.get(metric));
            }
        }
        //EvalWriter evalWriter = new EvalWriter(matchTask, this);
        //evalWriter.writeMatchStepPerformance();
    }
}
