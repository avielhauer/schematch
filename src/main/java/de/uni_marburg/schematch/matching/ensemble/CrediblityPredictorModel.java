package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ModelUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.NotImplementedException;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;

public class CrediblityPredictorModel implements Serializable {

    public List<MatchTask> matchTasks=new ArrayList<>();
    public List<ColumnPair> colomnPairs=new ArrayList<>();
    List<Matcher> matchers=new ArrayList<>();
    MatchStep matchSteps;
    public CrediblityPredictorModel(MatchStep matchSteps) {
        this.matchSteps=matchSteps;
    }

    public void addMatcher(Matcher matcher)
    {
        matchers.add(matcher);
    }
    String dataPath="output.csv";


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
    LinearRegression linearRegression;
    public void addTablePair(TablePair tablePair) throws ModelTrainedException {
        if(!isTrained){
            tablePairs.add(tablePair);

        }
        else throw new ModelTrainedException();

    }
    public double predictaccuracy(ColumnPair columnPair,Matcher newMatcher) throws Exception {
        Instance newInstance=new Instance(features.size()+matchers.size());
        int i =0;
        for(Feature feature:features){
            newInstance.setValue(i,feature.calculateScore(columnPair));
            i++;
        }


        for (Matcher m : matchers) {

            if (newMatcher.equals(m))

                newInstance.setValue(i, 1);
            else newInstance.setValue(i, 0);
            i++;
            }


        return linearRegression.classifyInstance(newInstance);
    }
    public void train()
    {
        try {
            Instances instances=loadData(dataPath);
            // Create the Remove filter
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndicesArray(new int[]{0});

            // Apply the filter to the dataset
            removeFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances, removeFilter);
            instances.setClassIndex(instances.numAttributes()-1);

            linearRegression = new LinearRegression();
            linearRegression.buildClassifier(instances);
            isTrained=true;
            System.out.println(linearRegression);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Instances loadData(String dataPath) throws IOException, ModelTrainedException {
        prepareData2();
        CSVLoader csvLoader=new CSVLoader();
        csvLoader.setSource(new File(dataPath));
        return csvLoader.getDataSet();
    }

    public void prepareData2() throws ModelTrainedException {
        generateColumnPairs2();
        generateAccuracy2();
        String header= "Pair";
        for(Feature feature:features){
            header+=","+feature.getName();
        }
        for (Matcher m:matchers)
        {
         header+=","+m.toString();
        }
        header+=","+"Accuracy";
        List<String> data=new ArrayList<>();
        data.add(header);
        for (int i = 0; i < colomnPairs.size(); i++) {
            String line= colomnPairs.get(i).toString();
            for(Feature feature:features){
                line+=","+feature.calculateScore(colomnPairs.get(i));
            }

            for (Matcher matcher:matchers)
            {

                String instance=line;
                for (Matcher m:matchers)
                {
                    if(matcher.equals(m))

                        instance+=","+1;
                    else
                        instance+=","+0;
                }
                instance+=","+getMSE(colomnPairs.get(i),matcher);

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
        Column srcColumn = columnPair.getSourceColumn();
        Column trgColumn = columnPair.getTargetColumn();
        int srcIndex = srcColumn.getTable().getOffset();
        int trgIndex = trgColumn.getTable().getOffset();
        for (MatchTask x : matchTasks) {
            for (TablePair tablePair : x.getTablePairs()) {
                if (tablePair.getSourceTable().equals(srcColumn.getTable())) {
                    if (tablePair.getTargetTable().equals(trgColumn.getTable())) {
                        for (MatchStep mtcStep : x.getSimMatrices().keySet()) {
                            if (mtcStep instanceof MatchingStep){

                                float[][] simsOfMatcher = x.getSimMatrices().get(mtcStep).get(matcher);
                                int i ;
                                for (i=0; i < tablePair.getSourceTable().getNumColumns(); i++) {
                                    if(srcColumn.equals(tablePair.getSourceTable().getColumn(i)))
                                        break;

                                }
                                int j ;
                                for (j = 0; j < tablePair.getTargetTable().getNumColumns(); j++) {
                                    if(trgColumn.equals(tablePair.getTargetTable().getColumn(i)))
                                        break;
                                }
                                return simsOfMatcher[srcIndex+i][trgIndex+j];
                            }
                        }
                    }
                }
            }
        }
        throw new SimilarityNotFoundExeption();
    }
    public int getGroundTruth(ColumnPair columnPair)
    {
        Column srcColumn = columnPair.getSourceColumn();
        Column trgColumn = columnPair.getTargetColumn();
        int srcIndex = srcColumn.getTable().getOffset();
        int trgIndex = trgColumn.getTable().getOffset();
        int[][] gTable = null;
        for(MatchTask x: matchTasks){
            if(srcColumn.getTable().getPath().contains(x.getDataset().getPath()) && x.getTablePairs().contains(new TablePair(srcColumn.getTable(),trgColumn.getTable()))){
                gTable = x.getGroundTruthMatrix();
            }
        }

        return gTable[srcIndex][trgIndex];
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
