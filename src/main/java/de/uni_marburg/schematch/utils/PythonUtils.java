package de.uni_marburg.schematch.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PythonUtils {
    public static PythonUtils.PythonOutput runPythonFile(String fileName, String... command) {
        String filePath = Paths.get("target", "classes", fileName).toAbsolutePath().toString();
        ProcessBuilder processBuilder = new ProcessBuilder(
                Stream.concat(Stream.of("python3", filePath),
                        Arrays.stream(command)).collect(Collectors.toList()));
        int exitCode;
        BufferedReader reader = null;
        try {
            Process process = processBuilder.redirectErrorStream(true).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            exitCode = process.waitFor();
        } catch (Exception e) {
            System.out.println("Python file " + fileName + " could not be run.");
            return new PythonUtils.PythonOutput(false, reader);
        }


        switch (exitCode) {
            case 0 -> {
                return new PythonOutput(true, reader);
            }
            case 2 -> System.out.println("Python was not able to resolve imports. Are all requirements installed for the used Interpreter?");
        }

        return new PythonUtils.PythonOutput(false, reader);

    }

    public static class PythonOutput {
        public boolean success;
        public BufferedReader stdout;

        public PythonOutput(boolean success, BufferedReader stdout) {
            this.success = success;
            this.stdout = stdout;
        }
    }
}
