package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ModelUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrediblityPredictorModel implements Serializable {

    public List<MatchTask> matchTasks=new ArrayList<>();
    public List<ColumnPair> colomnPairs=new ArrayList<>();
    List<Matcher> matchers=new ArrayList<>();
    List<MatchStep> matchSteps;
    public CrediblityPredictorModel(List<MatchStep> matchSteps) {
        this.matchSteps=matchSteps;
    }

    public void addMatcher(Matcher matcher)
    {
        matchers.add(matcher);
    }

    public void train() {
        // TODO
        throw new NotImplementedException();
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
    public void prepareData2() throws ModelTrainedException {
        generateColumnPairs2();
        generateAccuracy2();
        String header= "Pair";
        for(Feature feature:features){
            header+=","+feature.getName();
        }
        header+=","+"Matcher";
        header+=","+"Accuracy";
        List<String> data=new ArrayList<>();
        data.add(header);
        for (int i = 0; i < colomnPairs.size(); i++) {
            String line= colomnPairs.get(i).toString();
            for(Feature feature:features){
                line+=","+feature.calculateScore(colomnPairs.get(i));
            }

            Map<Matcher,Double> map=accuracy.get(colomnPairs.get(i));
            for (Matcher matcher:matchers)
            {
                String instance=line;
                instance+=","+matcher.getClass().getName();
                instance+=","+map.get(matcher);

                data.add(instance);
            }

        }
        saveListToFile("output.csv",data);


        System.out.println("heere header"+header);
    }

    private void generateAccuracy2() {
        for (ColumnPair columnPair:colomnPairs)
        {
            Map<Matcher,Double> map=new HashMap<>();
            for (Matcher matcher :matchers)
            {
                map.put(matcher, getMSE(columnPair,matcher));
            }
            accuracy.put(columnPair,map);
        }

    }
    public  double getSimilarity(ColumnPair columnPair, Matcher matcher)

    {
        //TODO Crommc
        return 0;
    }
    public  double getGroundTruth(ColumnPair columnPair)
    {
        //TODO Crommc

        return 0;
    }
    public  double getMSE(ColumnPair columnPair,Matcher matcher)
    {

        return Math.pow((getSimilarity(columnPair,matcher)-getGroundTruth(columnPair)),2);
    }

    private void generateColumnPairs2() throws ModelTrainedException {
        for (MatchTask matchTask:matchTasks){
            tablePairs.addAll( matchTask.getTablePairs());

        }
        generateColumnPairs();
    }

    List<Feature> features=new ArrayList<>();
    /* List<List<Double>> scores=new ArrayList<>();
    *public void generateScores()
     {
         for(Feature feature:features)
         {
             List<Double> score=new ArrayList<>();
             for(ColumnPair columnPair:colomnPairs)
             {
                 score.add(feature.calculateScore(columnPair));
             }
             scores.add(score);
         }
     }

      */
    public void prepareData() throws ModelTrainedException {
        generateColumnPairs();
        generateAccuracy();
        String header= "Pair";
        for(Feature feature:features){
            header+=","+feature.getName();
        }
        header+=","+"Matcher";
        header+=","+"Accuracy";
        List<String> data=new ArrayList<>();
        data.add(header);
        for (int i = 0; i < colomnPairs.size(); i++) {
            String line= colomnPairs.get(i).toString();
            for(Feature feature:features){
                line+=","+feature.calculateScore(colomnPairs.get(i));
            }

            Map<Matcher,Double> map=accuracy.get(colomnPairs.get(i));
            for (Matcher matcher:matchers)
            {
                String instance=line;
                instance+=","+matcher.getClass().getName();
                instance+=","+map.get(matcher);

                data.add(instance);
            }

        }
        saveListToFile("output.csv",data);


        System.out.println("heere header"+header);
    }
    private  void saveListToFile(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write each string as a new line in the file
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Data saved to " + filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
