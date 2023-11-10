package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Column;
import org.apache.commons.csv.CSVFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdditionalInformationWriter {
    private final static Logger log = LogManager.getLogger(AdditionalInformationWriter.class);

    public static void writeMetadata(Map<Column, Map<String, String>> output, String path){
        Set<String> allRows = new HashSet<>();
        for (Map<String, String> innerMap : output.values()){
            allRows.addAll(innerMap.keySet());
        }

        try(
                BufferedWriter writer = new BufferedWriter(new FileWriter(path));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
                ) {
            csvPrinter.print(null);
            csvPrinter.printRecord(output.keySet());

            for (String rowKey : allRows){
                csvPrinter.print(rowKey);
                for (Column columnKey : output.keySet()){
                    Map<String, String> column = output.get(columnKey);
                    csvPrinter.print(column.getOrDefault(rowKey, null));
                }
                csvPrinter.println();
            }
        }
        catch(IOException e){
            log.info("Error on writing metadata: " + e.getMessage());
        }
    }
}
