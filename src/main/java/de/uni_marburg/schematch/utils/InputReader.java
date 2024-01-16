package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.DatabaseMetadata;
import de.uni_marburg.schematch.data.metadata.ScenarioMetadata;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class InputReader {
    private static final Logger log = LogManager.getLogger(InputReader.class);

    public static List<Table> readDataDir(String inputPath) {
        return readDataDir(inputPath, Configuration.getInstance().getDefaultSeparator());
    }

    public static List<Table> readDataDir(String inputPath, String separator) {
        List<Table> tables = new ArrayList<>();

        File dir = new File(inputPath);
        File[] listOfFiles = dir.listFiles();
        Arrays.sort(listOfFiles);

        for (File file : listOfFiles) {
            if (file.isFile()) {
                Table table = readDataFile(file.getAbsolutePath(), separator);
                tables.add(table);
            }
        }
        return tables;
    }

    public static Table readDataFile(String inputPath) {
        return readDataFile(inputPath, Configuration.getInstance().getDefaultSeparator());
    }

    public static Table readDataFile(String inputPath, String separator) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setDelimiter(separator)
                .setAllowMissingColumnNames(true)
                .build();

        File file = new File(inputPath);
        String fileName = StringUtils.getFileName(file);
        log.trace("Reading file " + file.getAbsolutePath());

        List<String> labels;
        List<Column> columns = new ArrayList<>();

        try (Reader reader = new FileReader(inputPath)) {
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            // parse labels
            // TODO: set labels to null if they are all empty strings
            /*if (csvParser.getHeaderMap().keySet().size() == 1 &&
                csvParser.getHeaderNames().get(0) == "") {
                labels = null;
            } else {
                labels = csvParser.getHeaderNames();
            }*/
            labels = csvParser.getHeaderNames();
            // parse records
            int numColumns = csvParser.getHeaderNames().size();
            List<List<String>> valueLists = new ArrayList<>();
            for (int j = 0; j < numColumns; j++) {
                valueLists.add(new ArrayList<>());
            }
            for (CSVRecord csvRecord : csvParser) {
               for (int j = 0; j < numColumns; j++) {
                   valueLists.get(j).add(csvRecord.get(j));
               }
            }
            // TODO: work with null labels instead of empty strings
            /*if (labels == null) {
                for (int j = 0; j < valueLists.size(); j++) {
                    columns.add(new Column(null, valueLists.get(j)));
                }
            } else {
                for (int j = 0; j < valueLists.size(); j++) {
                    columns.add(new Column(labels.get(j), valueLists.get(j)));
                }
            }*/
            for (int j = 0; j < valueLists.size(); j++) {
                columns.add(new Column(labels.get(j), valueLists.get(j)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Table(fileName, labels, columns, inputPath);
    }

    public static List<String> fetchGroundTruthTablePairNames(String inputPath) {
        List<String> gtTablePairNames = new ArrayList<>();

        File dir = new File(inputPath);
        File[] listOfFiles = dir.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String fileName = StringUtils.getFileName(file);
                gtTablePairNames.add(fileName);
            }
        }

        return gtTablePairNames;
    }

    public static int[][] readGroundTruthFile(String inputPath) {
        return readGroundTruthFile(inputPath, Configuration.getInstance().getDefaultTablePairSeparator(),
                Configuration.getInstance().getDefaultSeparator());
    }
    private static int[][] readGroundTruthFile(String inputPath, String tablePairSeparator, String csvSeparator) {
        File file = new File(inputPath);

        // FIXME: find better way to skip table pairs without ground truth
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            String line;
            int numLines = 0;
            int numColumns = 0;
            while ((line = br.readLine()) != null) {
                numLines += 1;
                numColumns = line.split(String.valueOf(csvSeparator)).length;
            }
            int[][] scores = new int[numLines][numColumns];
            br.close();
            // TODO: find better way to reset buffered reader
            BufferedReader br2 = new BufferedReader(new FileReader(file.getAbsolutePath()));
            int i = 0;
            int j = 0;
            int numAssignments = 0;
            String[] values;
            while ((line = br2.readLine()) != null) {
                values = line.split(String.valueOf(csvSeparator));
                for (j = 0; j < values.length; j++) {
                    int value = Integer.parseInt(values[j]);
                    numAssignments += value;
                    scores[i][j] = value;
                }
                i += 1;
            }
            // we only add table pairs with at least one ground truth assignment
            if (numAssignments > 0) {
                String fileName = StringUtils.getFileName(file);
                String[] tableNames = fileName.split(tablePairSeparator);
                String sourceTableName = tableNames[0];
                String targetTableName = tableNames[1];
                return scores;
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DatabaseMetadata readDatabaseMetadata(Database database) {
        try {
            String inputPath = database.getPath();
            String folderName = StringUtils.getFolderName(inputPath);
            Path metadataFolderPath = Paths.get(new File(inputPath).getParent(), "metadata", folderName);
            Path indFilePath = metadataFolderPath.resolve("inds.txt");

            Map<Column, Collection<InclusionDependency>> indMap = new HashMap<>();
            Map<Column, Collection<FunctionalDependency>> fdMap = new HashMap<>();
            Map<Column, Collection<UniqueColumnCombination>> uccMap = new HashMap<>();

            Collection<InclusionDependency> inds = readINDFile(indFilePath, database, database, indMap);
            Collection<FunctionalDependency> fds = new ArrayList<>();
            Collection<UniqueColumnCombination> uccs = new ArrayList<>();

            for (Table table : database.getTables()) {
                Path fdFilePath = metadataFolderPath.resolve(table.getName()).resolve("FD_results.txt");
                Path uccFilePath = metadataFolderPath.resolve(table.getName()).resolve("UCC_results.txt");

                Collection<FunctionalDependency> datasetFDs = readFDFile(fdFilePath, table, fdMap);
                for (FunctionalDependency fd : datasetFDs) {
                    fd.setPdepTuple(MetadataUtils.getPdep(fd));
                }
                Collection<UniqueColumnCombination> datasetUCCs = readUCCFile(uccFilePath, table, uccMap);

                fds.addAll(datasetFDs);
                uccs.addAll(datasetUCCs);
            }
            DatabaseMetadata metadata = new DatabaseMetadata(uccs, fds, inds);
            metadata.getIndMap().putAll(indMap);
            metadata.getFdMap().putAll(fdMap);
            metadata.getUccMap().putAll(uccMap);
            return metadata;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static ScenarioMetadata readScenarioMetadata(String inputPath, Database sourceDatabase, Database targetDatabase){
        try {
            Path metadataFolderPath = Paths.get(inputPath, "metadata");
            Path sourceFilePath = metadataFolderPath.resolve("source-to-target-inds.txt");
            Path targetFilePath = metadataFolderPath.resolve("target-to-source-inds.txt");

            Map<Column, Collection<InclusionDependency>> sourceContentMap = new HashMap<>();
            Map<Column, Collection<InclusionDependency>> targetContentMap = new HashMap<>();

            Collection<InclusionDependency> sourceContent = readINDFile(sourceFilePath, sourceDatabase, targetDatabase, sourceContentMap);
            Collection<InclusionDependency> targetContent = readINDFile(targetFilePath, targetDatabase, sourceDatabase, targetContentMap);

            ScenarioMetadata metadata = new ScenarioMetadata(sourceContent, targetContent);
            metadata.getSourceToTargetMap().putAll(sourceContentMap);
            metadata.getTargetToSourceMap().putAll(targetContentMap);
            return metadata;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Collection<FunctionalDependency> readFDFile(Path filePath, Table table, Map<Column, Collection<FunctionalDependency>> map) throws IOException{
        Set<FunctionalDependency> fds = new HashSet<>();
        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            String[] split = line.split(" --> ");
            if(split[0].equalsIgnoreCase("[]"))
                continue;
            Collection<Column> leftCC = (Collection<Column>) extractColumnsFromString(split[0], table);
            for(String right : split[1].split(",")){
                Column rightC = table.getColumn(table.getLabels().indexOf(right.trim().split(".csv.")[1]));
                FunctionalDependency fd = new FunctionalDependency(leftCC, rightC);
                fds.add(fd);
                map.computeIfAbsent(rightC, k -> new ArrayList<>()).add(fd);
                for (Column left: leftCC){
                    map.computeIfAbsent(left, k -> new ArrayList<>()).add(fd);
                }
            }
        }
        return fds;
    }

    private static Collection<UniqueColumnCombination> readUCCFile(Path filePath, Table table, Map<Column, Collection<UniqueColumnCombination>> map) throws IOException{
        Set<UniqueColumnCombination> uccs = new HashSet<>();
        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            if(line.equalsIgnoreCase("[]"))
                continue;
            Collection<Column> columns = (Collection<Column>) extractColumnsFromString(line, table);
            UniqueColumnCombination ucc = new UniqueColumnCombination(columns);
            uccs.add(ucc);
            for (Column left: columns){
                map.computeIfAbsent(left, k -> new ArrayList<>()).add(ucc);
            }
        }
        return uccs;
    }

    private static Collection<InclusionDependency> readINDFile(Path filePath, Database leftDatabase, Database rightDatabase, Map<Column, Collection<InclusionDependency>> map) throws IOException{
        Set<InclusionDependency> inds = new HashSet<>();
        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            String[] split = line.split(" --> ");
            Collection<String[]> supersetCCString = (Collection<String[]>) extractColumnsFromString(split[0], null);
            Collection<Column> supersetCC = new ArrayList<>();
            for(String[] tableColumnPair: supersetCCString){
                Table table = leftDatabase.getTableByName(tableColumnPair[0]);
                if(table == null)
                    throw new RuntimeException("While reading in metadata from " + filePath + " an error occurred, table " + tableColumnPair[0] + " cannot be found!");
                supersetCC.add(table.getColumn(table.getLabels().indexOf(tableColumnPair[1])));
            }
            for(String right : split[1].split("], ")){
                right = right.trim();
                if(right.charAt(right.length()-1) != ']')
                    right = right + "]";
                Collection<String[]> subsetCCString = (Collection<String[]>) extractColumnsFromString(right, null);
                Collection<Column> subsetCC = new ArrayList<>();
                for(String[] tableColumnPair: subsetCCString){
                    Table table = rightDatabase.getTableByName(tableColumnPair[0]);
                    if(table == null)
                        throw new RuntimeException("While reading in metadata from " + filePath + " an error occurred, table " + tableColumnPair[0] + " cannot be found!");
                    subsetCC.add(table.getColumn(table.getLabels().indexOf(tableColumnPair[1])));
                }
                InclusionDependency ind = new InclusionDependency(subsetCC, supersetCC);
                inds.add(ind);
                for (Column left: subsetCC){
                    map.computeIfAbsent(left, k -> new ArrayList<>()).add(ind);
                }
                for (Column left: supersetCC){
                    map.computeIfAbsent(left, k -> new ArrayList<>()).add(ind);
                }
            }
        }
        return inds;
    }

    private static Collection<?> extractColumnsFromString(String input, Table table) {
        List<Object> columnList = new ArrayList<>();
        while (input.contains("]")) {
            int start = 0;
            int end = -1;

            if (input.contains("[")) {
                start = input.indexOf("[");
            }

            if (input.contains(",")) {
                end = input.indexOf(",");
            } else {
                end = input.indexOf("]");
            }

            String output = input.substring(start + 1, end);
            input = input.substring(end + 1);

            String[] tableColumnString = output.split(".csv.");
            if (table == null) {
                columnList.add(tableColumnString);
            } else {
                Column column = table.getColumn(table.getLabels().indexOf(tableColumnString[1]));
                columnList.add(column);
            }
        }
        return columnList;
    }

    public static float[][] readCache(MatchTask matchTask, MatchStep matchStep, Matcher matcher, String separator) {
        float[][] simMatrix = matchTask.getEmptySimMatrix();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setDelimiter(separator)
                .setAllowMissingColumnNames(true)
                .build();

        File file = ResultsUtils.getCachePathForMatchStepInScenario(matchTask, matchStep).resolve(matcher.toString() + ".csv").toFile();

        try (Reader reader = new FileReader(file.getAbsolutePath())) {
            log.trace("Reading cache file " + file.getAbsolutePath());
            matchTask.incrementCacheRead();
            CSVParser csvParser = new CSVParser(reader, csvFormat);
            // parse records
            int i = 0;
            for (CSVRecord csvRecord : csvParser) {
                for (int j = 0; j < simMatrix[0].length; j++) {
                    simMatrix[i][j] = Float.parseFloat(csvRecord.get(j));
                }
                i += 1;
            }
        } catch (IOException e) {
            // no cache for matcher in matchstep
            return null;
        }

        return simMatrix;
    }

    public static float[][] readCache(MatchTask matchTask, MatchStep matchStep, Matcher matcher) {
        return readCache(matchTask, matchStep, matcher, Configuration.getInstance().getDefaultSeparator());
    }
}
