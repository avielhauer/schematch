package de.uni_marburg.schematch;

import de.uni_marburg.schematch.boosting.SimFloodingSimMatrixBoosting;
import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.metric.MetricFactory;
import de.uni_marburg.schematch.matchtask.matchstep.*;
import de.uni_marburg.schematch.matchtask.tablepair.generators.NaiveTablePairsGenerator;
import de.uni_marburg.schematch.matchtask.tablepair.generators.TablePairsGenerator;
import de.uni_marburg.schematch.data.*;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.MatcherFactory;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.utils.*;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class Main {
    final static Logger log = LogManager.getLogger(Main.class);
    public final static Date START_TIMESTAMP = new Date();

    public static void main(String[] args) throws Exception {
        log.info("Starting Schematch");

        Configuration config = Configuration.getInstance();
        MatcherFactory matcherFactory = new MatcherFactory();
        // FIXME: make sim matrix boosting configurable via .yaml files
        // Configure similarity matrix boosting here for now
        SimMatrixBoosting firstLineSimMatrixBoosting = new SimFloodingSimMatrixBoosting();
        SimMatrixBoosting secondLineSimMatrixBoosting = new SimFloodingSimMatrixBoosting();

        log.info("Instantiating metrics");
        MetricFactory metricFactory = new MetricFactory();
        List<Metric> metrics = metricFactory.createMetricsFromConfig();

        log.info("Setting up matching steps as specified in config");
        List<MatchStep> matchSteps = new ArrayList<>();
        // Step 1: generate candidate table pairs to match
        log.info("Instantiating table pair generation");
        TablePairsGenerator tablePairsGenerator = new NaiveTablePairsGenerator();
        matchSteps.add(new TablePairGenerationStep(
                config.isSaveOutputTablePairGeneration(),
                config.isEvaluateTablePairGeneration(),
                tablePairsGenerator));
        // Step 2: run first line matchers (i.e., matchers that use table data to match)
        log.info("Instantiating first-line matchers");
        List<Matcher> firstLineMatchers = matcherFactory.createMatchersFromConfig(1);
        matchSteps.add(new MatchingStep(
                config.isSaveOutputFirstLineMatchers(),
                config.isEvaluateFirstLineMatchers(),
                1,
                firstLineMatchers));
        // Step 3: run similarity matrix boosting on the output of first line matchers
        if (config.isRunSimMatrixBoostingOnFirstLineMatchers()) {
            log.info("Instantiating sim matrix boosting for first-line");
            matchSteps.add(new SimMatrixBoostingStep(
                    config.isSaveOutputSimMatrixBoostingOnFirstLineMatchers(),
                    config.isEvaluateSimMatrixBoostingOnFirstLineMatchers(),
                    1,
                    firstLineSimMatrixBoosting));
        }
        // Step 4: run second line matchers (ensemble matchers and other matchers using output of first line matchers)
        if (config.isRunSecondLineMatchers()) {
            log.info("Instantiating second-line matchers");
            List<Matcher> secondLineMatchers = matcherFactory.createMatchersFromConfig(2);
            matchSteps.add(new MatchingStep(
                    config.isSaveOutputSecondLineMatchers(),
                    config.isEvaluateSecondLineMatchers(),
                    2,
                    secondLineMatchers));
            // Step 5: run similarity matrix boosting on the output of second line matchers
            if (config.isRunSimMatrixBoostingOnSecondLineMatchers()) {
                log.info("Instantiating sim matrix boosting for second-line");
                matchSteps.add(new SimMatrixBoostingStep(
                        config.isSaveOutputSimMatrixBoostingOnSecondLineMatchers(),
                        config.isEvaluateSimMatrixBoostingOnSecondLineMatchers(),
                        2,
                        secondLineSimMatrixBoosting));
            }
        }


        EvalWriter evalWriter = null;
        if (ConfigUtils.anyEvaluate()) {
            evalWriter = new EvalWriter(matchSteps, metrics);
        }

        // loop over datasets
        for (Configuration.DatasetConfiguration datasetConfiguration : config.getDatasetConfigurations()) {
            Dataset dataset = new Dataset(datasetConfiguration);
            log.info("Starting experiments for dataset " + dataset.getName() + " with " + dataset.getScenarioNames().size() + " scenarios");
            // loop over scenarios
            for (String scenarioName : dataset.getScenarioNames()) {
                Scenario scenario = new Scenario(dataset.getPath() + File.separator + scenarioName);
                log.debug("Starting experiments for dataset " + dataset.getName() + ", scenario: " + scenario.getPath());

                MatchTask matchTask = new MatchTask(dataset, scenario, matchSteps, metrics);
                matchTask.runSteps();
                if (ConfigUtils.anyEvaluate()) {
                    assert evalWriter != null;
                    evalWriter.writeScenarioPerformance(matchTask);
                }
            }

            if (ConfigUtils.anyEvaluate()) {
                assert evalWriter != null;
                evalWriter.writeDatasetPerformance(dataset);
            }
        }

        if (ConfigUtils.anyEvaluate()) {
            assert evalWriter != null;
            evalWriter.writeOverallPerformance(config.getDatasetConfigurations().size());
        }

        log.info("See results directory for more detailed performance and similarity matrices results.");

        Date END_TIMESTAMP = new Date();
        long durationInMillis =  END_TIMESTAMP.getTime() - START_TIMESTAMP.getTime();
        log.info("Total time: " + DurationFormatUtils.formatDuration(durationInMillis, "HH:mm:ss:SSS"));

        log.info("Ending Schematch");
    }
}