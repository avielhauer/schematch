package de.uni_marburg.schematch.tools;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Dataset;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.utils.AdditionalInformationWriter;
import de.uni_marburg.schematch.utils.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Class to generate numeric metadata for given datasets.
 */
public class AdditionalMetadataGenerator {
    final static Logger log = LogManager.getLogger(AdditionalMetadataGenerator.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting additional metadata generation.");

        Configuration config = Configuration.getInstance();

        // loop over datasets
        for (Configuration.DatasetConfiguration datasetConfiguration : config.getDatasetConfigurations()) {
            Dataset dataset = new Dataset(datasetConfiguration);
            log.info("Starting generation for dataset " + dataset.getName() + " with " + dataset.getScenarioNames().size() + " scenarios");
            // loop over scenarios
            for (String scenarioName : dataset.getScenarioNames()) {
                Scenario scenario = new Scenario(dataset.getPath() + File.separator + scenarioName);
                log.debug("Starting generation for dataset " + dataset.getName() + ", scenario: " + scenario.getPath());
                for (Table table: scenario.getSourceDatabase().getTables()) {
                    String name = table.getName();

                    Map<Column, Map<String, String>> typeMetadata = generateTypeMetadata(table);
                    Map<Column, Map<String, String>> numMetadata = generateNumMetadata(table);
                    String typePath = scenario.getPath() + "/metadata/source/" + name + "/type.csv";
                    String numPath = scenario.getPath() + "/metadata/source/" + name + "/num.csv";
                    AdditionalInformationWriter.writeMetadata(typeMetadata, typePath);
                    AdditionalInformationWriter.writeMetadata(numMetadata, numPath);
                }

                for (Table table: scenario.getTargetDatabase().getTables()) {
                    String name = table.getName();
                    Map<Column, Map<String, String>> typeMetadata = generateTypeMetadata(table);
                    Map<Column, Map<String, String>> numMetadata = generateNumMetadata(table);
                    String typePath = scenario.getPath() + "/metadata/target/" + name + "/type.csv";
                    String numPath = scenario.getPath() + "/metadata/target/" + name + "/num.csv";
                    AdditionalInformationWriter.writeMetadata(typeMetadata, typePath);
                    AdditionalInformationWriter.writeMetadata(numMetadata, numPath);
                }
            }

        }
    }

    private static Map<Column, Map<String, String>> generateTypeMetadata(Table table){
        Map<Column, Map<String, String>> result = new HashMap<>();
        for(Column column : table.getColumns()) {
            fetchDatatype(column, null);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("datatype", column.getDatatype().toString());
            result.put(column, metadata);
        }
        return result;
    }
    private static Map<Column, Map<String, String>> generateNumMetadata(Table table){
        Map<Column, Map<String, String>> result = new HashMap<>();
        for(Column column : table.getColumns()){
            Map<String, String> metadata = new HashMap<>();
            switch(column.getDatatype()) {
                case INTEGER, FLOAT:
                    metadata.put("mean", String.valueOf(computeMean(column.getValues())));
                    metadata.put("maximum", String.valueOf(computeMaximum(column.getValues())));
                    metadata.put("minimum", String.valueOf(computeMinimum(column.getValues())));
                    break;
                case STRING, TEXT:
                    metadata.put("mean_length", String.valueOf(computeMeanLength(column.getValues())));
                    break;
            }
            result.put(column, metadata);
        }
        return result;
    }

    private static void fetchDatatype(Column column, SimpleDateFormat dateFormat){
        Column.Datatype datatype = Column.Datatype.INTEGER;
        for(String value : column.getValues()){
            switch(datatype){
                case INTEGER:
                    try{
                        Integer.parseInt(value);
                        break;
                    }
                    catch (NumberFormatException e){
                        datatype = Column.Datatype.FLOAT;
                    }
                case FLOAT:
                    try{
                        Float.parseFloat(value);
                        break;
                    }
                    catch (NumberFormatException e){
                        datatype = Column.Datatype.DATE;
                    }
                case DATE:
                    try{
                        if(dateFormat != null){
                            dateFormat.parse(value);
                            break;
                        }
                        else{
                            datatype = Column.Datatype.STRING;
                        }
                    }
                    catch (ParseException e){
                        datatype = Column.Datatype.STRING;
                    }
                case STRING:
                    if(value.length() >= 100) {
                    datatype = Column.Datatype.TEXT;
                    }
                    else{
                        break;
                    }
                default:
                    break;
            }
        }
        column.setDatatype(datatype);
    }

    private static float computeMean(List<String> numbers){
        float sum = 0;
        for(String number : numbers){
            sum += Float.parseFloat(number);
        }
        return sum / numbers.size();
    }

    private static float computeMaximum(List<String> numbers){
        float maximum = Float.MIN_VALUE;
        for(String number : numbers){
            float floatNumber = Float.parseFloat(number);
            if(floatNumber > maximum){
                maximum = floatNumber;
            }
        }
        return maximum;
    }

    private static float computeMinimum(List<String> numbers){
        float minimum = Float.MAX_VALUE;
        for(String number : numbers){
            float floatNumber = Float.parseFloat(number);
            if(floatNumber < minimum){
                minimum = floatNumber;
            }
        }
        return minimum;
    }

    private static float computeMeanLength(List<String> strings){
        float sum = 0;
        for(String string : strings){
            sum += string.length();
        }
        return sum / strings.size();
    }
}
