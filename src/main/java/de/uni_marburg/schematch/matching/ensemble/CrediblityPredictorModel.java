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
import lombok.SneakyThrows;
import org.apache.commons.lang3.NotImplementedException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.classifier.sgd.L2;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

import java.io.IOException;

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
    String dataPath="output.csv";
    public void train() throws IOException {
        // TODO
        CSVParser csvParser;
        csvParser = new CSVParser(new FileReader("path/to/your/dataset.csv"), CSVFormat.DEFAULT.withHeader());
        OnlineLogisticRegression model = trainMahoutModel(csvParser);

        throw new NotImplementedException();
    }
    private  OnlineLogisticRegression trainMahoutModel(CSVParser csvParser) throws IOException {
        OnlineLogisticRegression model = new OnlineLogisticRegression(1, 3, new L2(1));

        for (CSVRecord record : csvParser) {
            // Extract numerical features
            List<Double>  numericalFeatures=new ArrayList<>();
            for(Feature f:features)
            {
                numericalFeatures.add(Double.parseDouble(record.get(f.Name)));
            }

            // Extract categorical feature (assuming the last column is categorical)
            String categoricalFeature = record.get("Matcher");

            // Map categorical feature to a numerical value (you might need a proper mapping strategy)
            double mappedCategory = mapCategoryToNumber(categoricalFeature);

            // Extract target label (assuming the last column is the target label)
            double target = Double.parseDouble(record.get("target"));

            // Train the model
            //model.train(target, new DenseVector(), mappedCategory);
        }

        return model;
    }

    private double mapCategoryToNumber(String categoricalFeature) {
    }

    public class ModelTrainedException extends Exception{
        public ModelTrainedException(){
            super("\"Model is Already Trained\"");
        }
    }

    public class SimilarityNotFoundExeption extends Exception{
        public SimilarityNotFoundExeption(){
            super("\"Similarty was not found in MatchTasks With the given column labels\"");
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
        saveListToFile(dataPath,data);


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
    @SneakyThrows
    public double getSimilarity(ColumnPair columnPair, Matcher matcher){
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





}
