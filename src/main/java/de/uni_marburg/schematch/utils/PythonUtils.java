package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


public class PythonUtils {
    static Logger log = LogManager.getLogger(PythonUtils.class);
    static HttpClient httpClient = HttpClient.newHttpClient();

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

    public static HttpResponse<String> sendMatchRequest(Integer serverPort, List<Pair<String, String>> parameters) throws IOException, InterruptedException {

        StringBuilder uri_builder = new StringBuilder();
        uri_builder.append("http://127.0.0.1:").append(serverPort).append("/match");
        if (!parameters.isEmpty()) {
            uri_builder.append("?");
            StringJoiner parameterJoiner = new StringJoiner("&");
            for (Pair<String, String> parameter : parameters) {
                parameterJoiner.add(URLEncoder.encode(parameter.getLeft(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(parameter.getRight(), StandardCharsets.UTF_8));
            }
            uri_builder.append(parameterJoiner);
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri_builder.toString())).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
