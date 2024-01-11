package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ModelUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrediblityPredictorModel implements Serializable {


    public List<ColumnPair> colomnPairs=new ArrayList<>();
    List<Matcher> matchers=new ArrayList<>();
    public void addMatcher(Matcher matcher)
    {
        matchers.add(matcher);
    }

     public class ModelTrainedException extends Exception{
        public ModelTrainedException(){
            super("\"Model is Already Trained\"");
        }
    }
    boolean isTrained=false;
    List<TablePair> tablePairs=new ArrayList<>();
    public void addTablePair(TablePair tablePair) throws ModelTrainedException {
        if(!isTrained){
            tablePairs.add(tablePair);

        }
        else throw new ModelTrainedException();

    }

    List<Feature> features=new ArrayList<>();
    List<List<Double>> scores=new ArrayList<>();
    public void generateScores()
    {
        for(Feature feature:features)
        {
            List<Double> score=new ArrayList<>();
            for(ColumnPair columnPair:colomnPairs)
            {
                score.add(feature.calculateScore(columnPair));
            }
        }
    }
    public void addFeature(Feature feature) throws ModelTrainedException {
        if(!isTrained){
            features.add(feature);

        }
        else throw new ModelTrainedException();

    }
    public void generateColumnPairs() throws ModelTrainedException {
        if(isTrained)
            throw new ModelTrainedException();
        else {

            for (TablePair tp:tablePairs)
            {
                Table source=tp.getSourceTable();
                Table target=tp.getTargetTable();
                for (int i = 0; i < source.getNumColumns() ; i++) {
                    Column x=source.getColumn(i);
                    for (int j = 0; j < target.getNumColumns(); j++) {
                        Column y=target.getColumn(j);
                        colomnPairs.add(new ColumnPair(x,y));
                    }
                }
            }
        }
    }
    Map<ColumnPair,Map<Matcher,Double>> accuracy=new HashMap<>();
    public void generateAccuracy()
    {
        for (ColumnPair columnPair:colomnPairs)
        {
            Map<Matcher,Double> map=new HashMap<>();
            for (Matcher matcher :matchers)
            {
                map.put(matcher, ModelUtils.getMSE(columnPair,matcher));
            }
            accuracy.put(columnPair,map);
        }

    }



}
