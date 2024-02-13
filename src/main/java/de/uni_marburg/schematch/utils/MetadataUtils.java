package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.PdepTuple;
import de.uni_marburg.schematch.data.metadata.dependency.Dependency;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class MetadataUtils {

    public static PdepTuple getPdep(FunctionalDependency fd){
        Collection<Column> determinant = fd.getDeterminant();
        Column dependant = fd.getDependant();
        int N = dependant.getValues().size();
        Map<String, Integer> frequencyMapDep = createFrequencyMap(dependant);
        Map<String, Integer> frequencyMapDet = createFrequencyMap(determinant, N);
        Map<String, Map<String, Integer>> frequencyMapDepByDet = createFrequencyMapGroupedByDeterminant(determinant, dependant, N);

        double pdep = pdepAB(frequencyMapDet, frequencyMapDepByDet, N);
        double gpdep = gpdep(pdep, frequencyMapDet, frequencyMapDep,N);
        return new PdepTuple(pdep, gpdep);
    }


    public static double epdep(int dA, Map<String, Integer> valuesB, int N) {
        double pdepB = pdep(valuesB, N);

        return pdepB + (dA - 1.0) / (N - 1.0) * (1.0 - pdepB);
    }

    private static double pdep(Map<String, Integer> valuesB, int N) {
        double result = 0;
        for (Integer count : valuesB.values()) {
            result += (count*count);
        }
        return result / (N*N);
    }

    private static double pdepAB(Map<String, Integer> valuesA, Map<String, Map<String, Integer>> valuesBByA, int N) {
        double result = 0;
        for (Map.Entry<String, Integer> aValue : valuesA.entrySet()) {
            for (Integer count : valuesBByA.get(aValue.getKey()).values()) {
                result += (double) (count * count) / aValue.getValue();
            }
        }
        return result / N;
    }

    public static double gpdep(double pdepAB, Map<String, Integer> valuesA, Map<String, Integer> valuesB, int N) {
        double epdepAB = epdep(valuesA.size(), valuesB, N);

        return pdepAB - epdepAB;
    }

    public static Map<String, Integer> createFrequencyMap(Column column) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        for (String value : column.getValues()) {
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }

        return frequencyMap;
    }

    public static Map<String, Integer> createFrequencyMap(Collection<Column> columns, int size) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            StringBuilder concatenatedValue = new StringBuilder();
            for (Column col : columns) {
                concatenatedValue.append(col.getValues().get(i));
            }
            String key = concatenatedValue.toString();
            frequencyMap.put(key, frequencyMap.getOrDefault(key, 0) + 1);
        }

        return frequencyMap;
    }


    public static Map<String, Map<String, Integer>> createFrequencyMapGroupedByDeterminant(Collection<Column> determinant, Column dependant, int size) {
        Map<String, Map<String, Integer>> frequencyMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            StringBuilder concatenatedValue = new StringBuilder();
            for (Column col : determinant) {
                concatenatedValue.append(col.getValues().get(i));
            }
            String key = concatenatedValue.toString();
            Map<String, Integer> frequencyMapByDet = frequencyMap.computeIfAbsent(key, k -> new HashMap<>());
            String dependantKey = dependant.getValues().get(i);
            frequencyMapByDet.put(dependantKey, frequencyMapByDet.getOrDefault(dependantKey, 0) + 1);
        }

        return frequencyMap;
    }

    public static boolean metadataExists(String filePath, String dep) {
        Path path = Paths.get(filePath);
        String fileNameWithoutExtension = path.getFileName().toString().replaceFirst("[.][^.]+$", "");

        Path parentDirectory = path.getParent();

        if (parentDirectory != null) {

            String folderName = parentDirectory.getFileName().toString();
            Path metadataFolder = parentDirectory.resolve("metadata");
            Path stFolder = metadataFolder.resolve(folderName);

            if (Files.exists(stFolder) && Files.isDirectory(stFolder)) {
                Path targetFolder = stFolder.resolve(fileNameWithoutExtension);
                Path indPath  = stFolder.resolve("inds.txt");
                Path fdPath = targetFolder.resolve("FD_results.txt");
                Path uccPath = targetFolder.resolve("UCC_results.txt");

                return switch (dep) {
                    case "UCC" -> fileContainsContent(uccPath);
                    case "FD" -> fileContainsContent(fdPath);
                    case "IND" -> fileContainsContent(indPath);
                    default -> false;
                };
            }
        }

        return false;
    }
    private static boolean fileContainsContent(Path filePath) {
        try {
            BufferedReader reader = Files.newBufferedReader(filePath);
            String line = reader.readLine();
            reader.close();
            return line != null && !line.trim().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    public static Path getMetadataPathFromTable(Path path) {
        return getMetadataPath(path, true);
    }

    public static Path getMetadataRootPathFromTable(Path path) {
        return getMetadataPath(path, false);
    }

    public static Path getMetadataPath(Path path, boolean includeFileName) {
        String fileNameWithoutExtension = path.getFileName().toString().replaceFirst("[.][^.]+$", "");

        Path parentDirectory = path.getParent();

        if (parentDirectory != null) {
            String folderName = parentDirectory.getFileName().toString();

            Path metadataFolder = parentDirectory.getParent().resolve("metadata");

            Path stFolder = metadataFolder.resolve(folderName);

            if (Files.exists(stFolder) && Files.isDirectory(stFolder)) {
                return includeFileName ? stFolder.resolve(fileNameWithoutExtension) : stFolder;
            }
        }
        return null;
    }

    public static void saveDeps(Path path, Collection<? extends Dependency> objects, String fileName) {
        Path filePath = path.resolve(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (Dependency object : objects) {
                writer.write(object.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveINDs(Path path, Collection<? extends InclusionDependency> inds) {
        saveDeps(path, inds, "inds.txt");
    }

    public static void saveUCCs(Path path, Collection<? extends UniqueColumnCombination> uccs) {
        saveDeps(path, uccs, "UCC_results.txt");
    }

    public static void saveFDs(Path path, Collection<? extends FunctionalDependency> fDs) {
        saveDeps(path, fDs, "FD_results.txt");
    }
}
