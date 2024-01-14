package de.uni_marburg.schematch;

import de.uni_marburg.schematch.matching.ensemble.CrediblityPredictorModel;
import de.uni_marburg.schematch.matching.ensemble.Feature;
import de.uni_marburg.schematch.matching.similarity.label.CosineMatcher;
import de.uni_marburg.schematch.matching.similarity.label.HammingMatcher;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.utils.ModelUtils;

public class MyMain {
    public static void main(String[] args) {
        CrediblityPredictorModel crediblityPredictorModel=new CrediblityPredictorModel();
        try {
            ModelUtils.loadDataToModel(crediblityPredictorModel);

            crediblityPredictorModel.addFeature(new Feature("f1"));
            crediblityPredictorModel.addFeature(new Feature("f2"));
            crediblityPredictorModel.addFeature(new Feature("f3"));
            crediblityPredictorModel.addFeature(new Feature("f4"));
            crediblityPredictorModel.addMatcher(new CosineMatcher());
            crediblityPredictorModel.addMatcher(new HammingMatcher());
            crediblityPredictorModel.prepareData();
            crediblityPredictorModel.train();
        } catch (CrediblityPredictorModel.ModelTrainedException e) {
            throw new RuntimeException(e);
        }
    }
}
