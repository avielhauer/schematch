package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

public class CMCMatcher extends Matcher {
    public  CMCMatcher(MatchStep matchStep)
    {
        crediblityPredictorModel=new CrediblityPredictorModel(matchStep);
    }
    public CrediblityPredictorModel getCrediblityPredictorModel() {
        return crediblityPredictorModel;
    }

    public void setCrediblityPredictorModel(CrediblityPredictorModel crediblityPredictorModel) {
        this.crediblityPredictorModel = crediblityPredictorModel;
    }

    CrediblityPredictorModel crediblityPredictorModel;


    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {


        return new float[0][];
    }
}
