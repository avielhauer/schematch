package de.uni_marburg.schematch;

import de.uni_marburg.schematch.matching.ensemble.CrediblityPredictorModel;
import de.uni_marburg.schematch.matching.ensemble.Feature;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.utils.ModelUtils;

public class MyMain {
    public static void main(String[] args) {
        CrediblityPredictorModel crediblityPredictorModel=new CrediblityPredictorModel();
        try {
            ModelUtils.loadDataToModel(crediblityPredictorModel);

            crediblityPredictorModel.generateColumnPairs();
            crediblityPredictorModel.addFeature(new Feature());
            crediblityPredictorModel.addFeature(new Feature());
            crediblityPredictorModel.addFeature(new Feature());
            crediblityPredictorModel.addFeature(new Feature());
            crediblityPredictorModel.generateScores();
        } catch (CrediblityPredictorModel.ModelTrainedException e) {
            throw new RuntimeException(e);
        }
        for (ColumnPair columnPair:crediblityPredictorModel.colomnPairs)
        {
            System.out.println(columnPair);
        }
    }
}
