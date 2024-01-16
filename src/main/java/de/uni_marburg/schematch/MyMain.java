package de.uni_marburg.schematch;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.ensemble.CrediblityPredictorModel;
import de.uni_marburg.schematch.matching.ensemble.Feature;
import de.uni_marburg.schematch.matching.ensemble.MachineLearningModel;
import de.uni_marburg.schematch.matching.similarity.label.CosineMatcher;
import de.uni_marburg.schematch.matching.similarity.label.HammingMatcher;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.utils.ModelUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyMain {
    public static void main(String[] args) throws IOException {
       /*
        try {
            ModelUtils.loadDataToModel(crediblityPredictorModel);

            crediblityPredictorModel.addFeature(new Feature("f1"));
            crediblityPredictorModel.addFeature(new Feature("f2"));
            crediblityPredictorModel.addFeature(new Feature("f3"));
            crediblityPredictorModel.addFeature(new Feature("f4"));
            crediblityPredictorModel.addMatcher(new CosineMatcher());
            crediblityPredictorModel.addMatcher(new HammingMatcher());
            crediblityPredictorModel.prepareData();
        } catch (CrediblityPredictorModel.ModelTrainedException e) {
            throw new RuntimeException(e);
        }

        */
        Matcher matcher=new CosineMatcher();
        System.out.println(matcher);
        CrediblityPredictorModel crediblityPredictorModel=new CrediblityPredictorModel(new ArrayList<>());
        crediblityPredictorModel.train();
    }
}
