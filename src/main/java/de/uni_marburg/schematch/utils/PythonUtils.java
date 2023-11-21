package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;


public class PythonUtils {
    static Logger log = LogManager.getLogger(PythonUtils.class);

    public static Process runPythonFile(Boolean withPoetry, String folderName, String fileName, String... command) throws IOException {
        List<String> processCommand = new ArrayList<>();
        if (withPoetry) {
           processCommand.add("poetry");
           processCommand.add("run");
        }
        processCommand.add("python");
        processCommand.add(fileName);
        processCommand.addAll(List.of(command));


        ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
        processBuilder.directory(new File("src/main/resources", folderName));

        return processBuilder.start();
    }

    protected static List<String> readInputStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            return reader.lines().toList();
        } catch (UncheckedIOException e) {
            log.warn("An error occurred while reading the external python matcher logging output.");
        }
        return List.of();
    }

    public static float[][] runPythonMatcher(TablePair tablePair, Boolean withPoetry, String folderName, String fileName, String... command) {
        int exitCode;
        List<String> matcherOutput;
        String matcherLoggingOutput;

        try {
            Process process = runPythonFile(withPoetry, folderName, fileName, command);
            matcherOutput = readInputStream(process.getInputStream());
            matcherLoggingOutput = String.join("\n", readInputStream(process.getErrorStream()));
            exitCode = process.waitFor();
        } catch (Exception e) {
            log.error("Python file " + fileName + " could not be run.");
            log.error("Falling back to empty similarity matrix.");
            return tablePair.getEmptySimMatrix();
        }

        switch (exitCode) {
            case 0 -> {
                log.debug(matcherLoggingOutput);
                return readMatcherOutput(matcherOutput, tablePair);
            }
            case 1 -> log.error(matcherLoggingOutput);
            case 2 -> {
                log.error(matcherLoggingOutput);
                log.error("Python was not able to resolve imports. Are all requirements installed for the used interpreter?");
            }
        }

        log.error("Falling back to empty similarity matrix for external matcher.");
        return tablePair.getEmptySimMatrix();
    }

    public static float[][] readMatcherOutput(final List<String> output, final TablePair tablePair) {
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        try {
            for (int i = 0; i < simMatrix.length; i++) {
                String line = output.get(i);
                String[] sims = line.split(" ");
                for (int j = 0; j < simMatrix[i].length; j++) {
                    simMatrix[i][j] = Float.parseFloat(sims[j]);
                }
            }
        } catch (Exception e) {
            log.warn("Output of external matcher python call could not be read correctly.");
            log.warn("Similarity Matrix might be incomplete/faulty.");
            return simMatrix;
        }

        return simMatrix;
    }
}
