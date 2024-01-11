package de.uni_marburg.schematch;

import de.uni_marburg.schematch.boosting.IdentitySimMatrixBoosting;
import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.matching.ensemble.AverageEnsembleMatcher;
import de.uni_marburg.schematch.matching.ensemble.CrediblityPredictorModel;
import de.uni_marburg.schematch.matching.ensemble.RandomEnsembleMatcher;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.*;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.matchtask.tablepair.generators.GroundTruthTablePairsGenerator;
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

        log.info("Instantiating matchers and similarity matrix boosting");
        MatcherFactory matcherFactory = new MatcherFactory();
        Map<String, List<Matcher>> firstLineMatchers = matcherFactory.createMatchersFromConfig();
        // FIXME: make sim matrix boosting and second line matching configurable via .yaml files
        // Configure second line matchers and similarity matrix boosting here for now
        TablePairsGenerator tablePairsGenerator = new GroundTruthTablePairsGenerator();

        log.info("Setting up matching steps as specified in config");
        List<MatchStep> matchSteps = new ArrayList<>();
        // Step 1: generate candidate table pairs to match
        matchSteps.add(new TablePairGenerationStep(true,
                config.isSaveOutputTablePairGeneration(),
                config.isEvaluateTablePairGeneration(),
                tablePairsGenerator));

        CrediblityPredictorModel crediblityPredictorModel=new CrediblityPredictorModel();
        // loop over datasets
        for (Configuration.DatasetConfiguration datasetConfiguration : config.getDatasetConfigurations()) {
            Dataset dataset = new Dataset(datasetConfiguration);
            // loop over scenarios
            for (String scenarioName : dataset.getScenarioNames()) {
                Scenario scenario = new Scenario(dataset.getPath() + File.separator + scenarioName);
                List<TablePair> tablePairs = tablePairsGenerator.generateCandidates(scenario);
                for(TablePair tp :tablePairs) {

                    crediblityPredictorModel.addTablePair(tp);
                }
       }

        }

        crediblityPredictorModel.generateColumnPairs();
        for (ColumnPair columnPair:crediblityPredictorModel.colomnPairs)
        {
            System.out.println(columnPair);
        }
        log.info("See results directory for more detailed performance and similarity matrices results.");

        Date END_TIMESTAMP = new Date();
        long durationInMillis =  END_TIMESTAMP.getTime() - START_TIMESTAMP.getTime();
        log.info("Total time: " + DurationFormatUtils.formatDuration(durationInMillis, "HH:mm:ss:SSS"));

        log.info("Ending Schematch");
    }
}