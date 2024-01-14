package de.uni_marburg.schematch.matching.ensemble.datageneration;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.io.File;
import java.util.stream.Collectors;


public class LoadData {

    private final static String RESULT_DIR = "./results";
    private final static String GROUND_TRUTH_DIR = "./data";

    private static List<CSV_Datatype> csv_dataFiles;
    private static List<Ground_Truth_CSV> groundTruthCsvs = new ArrayList<>();
//    private final static String SUFFIX_DIR_TO_SEARCH = "overview.csv";
    public static HashMap<String,Integer> getSimilaritiesFromResults(){
        csv_dataFiles = findCSVFiles();
        processDirectory(new File(GROUND_TRUTH_DIR));
//      TODO
//          schauen, ob die liste noch in overview und summary unterteilt werden müssen.
                
        return null;
    }

    public static List<CSV_Datatype> findCSVFiles() {
        List<CSV_Datatype> csvFiles = new ArrayList<>();
        File directory = new File(RESULT_DIR);

        // Überprüfe, ob das Verzeichnis existiert
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Ungültiges Verzeichnis: " + RESULT_DIR);
            return csvFiles;
        }
        File[] files = Arrays.stream(directory.listFiles()).sorted().collect(Collectors.toList()).toArray(new File[0]);
        // Durchsuche das Verzeichnis nach .csv-Dateien
        searchCSVFiles(files[files.length-1], csvFiles);

        return csvFiles;
    }

    private static void searchCSVFiles(File directory, List<CSV_Datatype> csvFiles) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Rekursiv in Unterverzeichnissen suchen
                    searchCSVFiles(file, csvFiles);
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".csv") && file.getParentFile().getParent().toLowerCase().contains("firstline")) {
                    // .csv-Datei gefunden, füge sie zur Liste hinzu
                    csvFiles.add(new CSV_Datatype(file,file.getPath(),file.getParentFile().getParentFile().getName(),file.getParentFile().getParentFile().getParentFile().getParentFile().getName(),file.getParentFile().getParentFile().getParentFile().getName()));
                }
            }
        }
    }

    private static void processDirectory(File directory) {
        if (directory.isDirectory()) {
            // Überprüfe, ob der Ordner "groundtruth" ist
            if (directory.getName().equals("ground_truth")) {
                HashMap<String,File> hashResult = new HashMap<>();
                // Durchsuche den Ordner nach CSV-Dateien und füge sie zur HashMap hinzu
                processGroundTruthDirectory(directory, hashResult);

                groundTruthCsvs.add(new Ground_Truth_CSV(directory.getParentFile().getName(),directory.getParentFile().getParentFile().getName(),hashResult));
            } else {
                // Rekursiv durchsuche Unterordner
                File[] subdirectories = directory.listFiles(File::isDirectory);
                if (subdirectories != null) {
                    for (File subdirectory : subdirectories) {
                        processDirectory(subdirectory);
                    }
                }
            }
        }
    }

    private static void processGroundTruthDirectory(File groundTruthDirectory, HashMap<String, File> csvFilesMap) {
        File[] csvFiles = groundTruthDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles != null) {
            for (File csvFile : csvFiles) {
                // Füge CSV-Datei zur HashMap hinzu (Name als Key, Dateiobjekt als Wert)
                csvFilesMap.put(csvFile.getName(), csvFile);
            }
        }
    }


    @AllArgsConstructor
    private static class CSV_Datatype{
        File file;
        String directoryPath;
        String lineMatcherType;
        String database;
        String schemas_compared;
    }

    @AllArgsConstructor
    private static class Ground_Truth_CSV{
        String schemaName;
        String database;
        HashMap<String,File> groundTruthTableCsvFiles;
    }

}
