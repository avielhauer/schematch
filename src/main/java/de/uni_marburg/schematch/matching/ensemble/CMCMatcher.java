package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.ensemble.features.Feature;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class CMCMatcher extends Matcher {
    private float c;
    public  CMCMatcher(MatchingStep matchStep, ArrayList<Feature> featureList, ArrayList<Matcher> matchers, ArrayList<MatchTask> matchTasks,float c) {
        try {
            this.c=c;
            crediblityPredictorModel = new CrediblityPredictorModel();
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
    public CMCMatcher(CrediblityPredictorModel model)
    {
        crediblityPredictorModel=model;

    }
    public CrediblityPredictorModel getCrediblityPredictorModel() {
        return crediblityPredictorModel;
    }

    public void setCrediblityPredictorModel(CrediblityPredictorModel crediblityPredictorModel) {
        this.crediblityPredictorModel = crediblityPredictorModel;
    }

    CrediblityPredictorModel crediblityPredictorModel;




    public float[][] match(TablePair tablePair, MatchTask matchTask, MatchingStep matchStep) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                {

                    try {
                        double sumAcc=0;
                        double nominator=0;


                        for (Matcher m : matchTask.getFirstLineMatchers()) {
                            float sim = matchTask.getSimMatrixFromPreviousMatchStep(matchStep,m)[i][j];
                            float acc= (float) crediblityPredictorModel.predictaccuracy(new ColumnPair(sourceTable.getColumn(i),targetTable.getColumn(j)),m);
                            acc= (float) Math.exp(-1*c*acc);
                            nominator+=sim*acc;

                            sumAcc+=acc;
                        }
                        simMatrix[i][j] = (float) (nominator/sumAcc);
                    } catch (CrediblityPredictorModel.ModelTrainedException e) {
                        System.out.println("Sim Not Found");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return simMatrix;
    }

    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {
        List<TablePair> tablePairs = matchTask.getTablePairs();

        float[][] simMatrix = matchTask.getEmptySimMatrix();

        for (TablePair tablePair : tablePairs) {
            float[][] tablePairSimMatrix;
            tablePairSimMatrix = this.match(tablePair,matchTask,matchStep);
            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(tablePairSimMatrix, simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }
}
