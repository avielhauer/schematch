package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AdditionalInformationReader {
    private static final Logger log = LogManager.getLogger(AdditionalInformationReader.class);

    public static void readNUMFile(Path path, Table table) throws IOException{
        char separator = Configuration.getInstance().getDefaultSeparator();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setDelimiter(separator)
                .setAllowMissingColumnNames(true)
                .build();

        Reader reader = Files.newBufferedReader(path);
        CSVParser csvParser = new CSVParser(reader, csvFormat);

        List<String> headerNames = csvParser.getHeaderNames();

        for (CSVRecord record : csvParser) {
            String rowKey = record.get(0); // Get the first column for the row key
            for (int i = 1; i < record.size(); i++){
                if(record.get(i) == ""){
                    continue;
                }
                Column column = extractColumnFromString(headerNames.get(i), table);
                column.getMetadata().getNumMetaMap().put(rowKey, Float.parseFloat(record.get(i)));
            }
        }
        csvParser.close();
    }

    public static void readTYPEFile(Path path, Table table) throws IOException{
        char separator = Configuration.getInstance().getDefaultSeparator();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setDelimiter(separator)
                .setAllowMissingColumnNames(true)
                .build();

        Reader reader = Files.newBufferedReader(path);
        CSVParser csvParser = new CSVParser(reader, csvFormat);

        List<String> headerNames = csvParser.getHeaderNames();

        for (CSVRecord record : csvParser) {
            String rowKey = record.get(0); // Get the first column for the row key
            for (int i = 1; i < record.size(); i++){
                if(record.get(i) == ""){
                    continue;
                }
                Column column = extractColumnFromString(headerNames.get(i), table);
                column.getMetadata().getStringMetaMap().put(rowKey, record.get(i));
                if(rowKey == "datatype"){
                    column.setDatatype(column.getMetadata().getDatatype());
                }
            }
        }
        csvParser.close();
    }
    private static Column extractColumnFromString(String input, Table table) {
        try{
            return  table.getColumn(table.getLabels().indexOf(input));
        }
        catch(Exception e)
        {
            log.error("Failed to extract column label  " + input);
            throw new RuntimeException("Failed to extract column label  " + input);
        }
    }
}
