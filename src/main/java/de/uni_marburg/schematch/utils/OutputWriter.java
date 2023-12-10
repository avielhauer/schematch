package de.uni_marburg.schematch.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OutputWriter {
    private final static Logger log = LogManager.getLogger(OutputWriter.class);

    public static void writeSimMatrix(Path path, float[][] simMatrix) {
        try {
            Files.createDirectories(path.getParent());
            BufferedWriter writer = new BufferedWriter(new FileWriter(path.toString()));
            for (float[] scoreList : simMatrix) {
                StringBuilder line = new StringBuilder();
                for (float score : scoreList) {
                    line.append(score).append(Configuration.getInstance().getDefaultSeparator());
                }
                writer.write(line.substring(0, line.length() - 1));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
