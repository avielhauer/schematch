package de.uni_marburg.schematch.matching.ensemble;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class CMCMatcher extends Matcher {
    public  CMCMatcher(MatchingStep matchStep, ArrayList<Feature> featureList, ArrayList<Matcher> matchers,ArrayList<MatchTask> matchTasks) {
        try {


            crediblityPredictorModel = new CrediblityPredictorModel(matchStep);
            for (Feature f : featureList)
                crediblityPredictorModel.addFeature(f);
            for (Matcher m:matchers)
                crediblityPredictorModel.addMatcher(m);

            for (MatchTask matchTask:matchTasks)
                crediblityPredictorModel.matchTasks.add(matchTask);

            crediblityPredictorModel.train();
        }
        catch (CrediblityPredictorModel.ModelTrainedException e){

        }
    }
    public CrediblityPredictorModel getCrediblityPredictorModel() {
        return crediblityPredictorModel;
    }

    public void setCrediblityPredictorModel(CrediblityPredictorModel crediblityPredictorModel) {
        this.crediblityPredictorModel = crediblityPredictorModel;
    }

    CrediblityPredictorModel crediblityPredictorModel;

    public float match(ColumnPair columnPair,MatchingStep matchStep)
    {
        return 0;
    }

    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {

        return new float[0][];
    }
}
