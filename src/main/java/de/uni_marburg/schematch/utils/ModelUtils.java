package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.ensemble.CrediblityPredictorModel;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.matchtask.tablepair.generators.GroundTruthTablePairsGenerator;
import de.uni_marburg.schematch.matchtask.tablepair.generators.TablePairsGenerator;

import java.io.File;
import java.util.List;

public class ModelUtils {
    public static double getSimilarity(ColumnPair columnPair, Matcher matcher)

    {
        //TODO Crommc
        return 0;
    }
    public static double getGroundTruth(ColumnPair columnPair)
    {
        //TODO Crommc
        return 0;
    }
    public static double getMSE(ColumnPair columnPair,Matcher matcher)
    {

        return Math.pow((getSimilarity(columnPair,matcher)-getGroundTruth(columnPair)),2);
    }


    public static void loadDataToModel(CrediblityPredictorModel crediblityPredictorModel) throws CrediblityPredictorModel.ModelTrainedException {
        Configuration config = Configuration.getInstance();
        TablePairsGenerator tablePairsGenerator = new GroundTruthTablePairsGenerator();

        // Step 1: generate candidate table pairs to match

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
    }
}
