package de.uni_marburg.schematch.matching.ensemble;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CMCMatcher extends TablePairMatcher {
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

    public float matchColumnPair(ColumnPair columnPair) throws CrediblityPredictorModel.ModelTrainedException {
        List<Matcher> matchers=crediblityPredictorModel.matchers;
        for(Matcher m:crediblityPredictorModel.matchers)
        {
            if(crediblityPredictorModel.isTrained)
            {
                //here generate the similarity
            }
            else {
                throw new CrediblityPredictorModel.ModelTrainedException();
            }
        }
        return 0;
    }

    @Override
    public float[][] match(TablePair tablePair) throws CrediblityPredictorModel.ModelTrainedException {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                {

                    simMatrix[i][j] = matchColumnPair(new ColumnPair(sourceTable.getColumn(i),targetTable.getColumn(j)));
                }
            }
        }
        return simMatrix;
    }

}
