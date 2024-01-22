package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchStep;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputWriter {
    private final static Logger log = LogManager.getLogger(OutputWriter.class);

    public static void writeSimMatrix(Path path, MatchTask matchTask, String matcherInfo, float[][] simMatrix, boolean writeGroundTruth) {
        Path pathToFile = path.resolve(matcherInfo + ".csv");
        boolean isCacheFile = path.toString().startsWith(Configuration.getInstance().getCacheDir());
        int[][] groundTruth = writeGroundTruth? matchTask.getGroundTruthMatrix() : null;
        if(writeGroundTruth && groundTruth == null){
            log.info("Could not get ground truth for " + matchTask.getScenario().getName() + " - not writing ground truth to sim Matrix csv");
            writeGroundTruth = false;
        }
        try {
            Files.createDirectories(pathToFile.getParent());
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathToFile.toString()));
            if (Configuration.getInstance().isSaveOutputVerbose() &&
                    !isCacheFile) { // always cache plain sim matrices
                StringBuilder line = new StringBuilder();
                line.append("Source\\Target");
                for (Table table : matchTask.getScenario().getTargetDatabase().getTables()) {
                    for (String targetLabel : table.getLabels()) {
                        line.append(Configuration.getInstance().getDefaultSeparator());
                        line.append(table.getName() + "." + targetLabel);
                    }
                }
                writer.write(line.toString());
                writer.newLine();
            }
            for (int i = 0; i < simMatrix.length; i++) {
                float[] scoreList = simMatrix[i];
                StringBuilder line = new StringBuilder();
                if (Configuration.getInstance().isSaveOutputVerbose() &&
                        !isCacheFile) { // always cache plain sim matrices
                    line.append(matchTask.getScenario().getSourceDatabase().getFullColumnNameByIndex(i));
                    line.append(Configuration.getInstance().getDefaultSeparator());
                }
                for (int j = 0; j < scoreList.length; j++) {
                    if(writeGroundTruth){
                        line.append(groundTruth[i][j]).append(" / ");
                    }
                    line.append(scoreList[j]).append(Configuration.getInstance().getDefaultSeparator());
                }
                writer.write(line.substring(0, line.length() - 1));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (Configuration.getInstance().isSaveOutputPerTablePair()) {
            writeTablePairSimMatrices(path, matchTask, matcherInfo, simMatrix);
        }
    }

    private static void writeTablePairSimMatrices(Path path, MatchTask matchTask, String matcherInfo, float[][] simMatrix) {
        Path pathToTablePairs = path.resolve("tablepairs");
        Path pathToMatcher = pathToTablePairs.resolve(matcherInfo);

        for (TablePair tablePair : matchTask.getTablePairs()) {
            Path pathToTablePair = pathToMatcher.resolve(tablePair.toString() + ".csv");
            try {
                Files.createDirectories(pathToTablePair.getParent());
                BufferedWriter tpWriter = new BufferedWriter(new FileWriter(pathToTablePair.toString()));
                Table sourceTable = tablePair.getSourceTable();
                Table targetTable = tablePair.getTargetTable();
                int sourceOffset = sourceTable.getOffset();
                int targetOffset = targetTable.getOffset();
                int numSourceColumns = sourceTable.getNumColumns();
                int numTargetColumns = targetTable.getNumColumns();
                if (Configuration.getInstance().isSaveOutputVerbose()) {
                    StringBuilder line = new StringBuilder();
                    line.append("Source\\Target");
                    for (String targetLabel : targetTable.getLabels()) {
                        line.append(Configuration.getInstance().getDefaultSeparator());
                        line.append(targetLabel);
                    }
                    tpWriter.write(line.toString());
                    tpWriter.newLine();
                }
                for (int i = sourceOffset; i < (sourceOffset+numSourceColumns); i++) {
                    StringBuilder line = new StringBuilder();
                    if (Configuration.getInstance().isSaveOutputVerbose()) {
                        line.append(sourceTable.getColumn(i-sourceOffset).getLabel());
                        line.append(Configuration.getInstance().getDefaultSeparator());
                    }
                    for (int j = targetOffset; j < (targetOffset+numTargetColumns); j++) {
                        line.append(simMatrix[i][j]).append(Configuration.getInstance().getDefaultSeparator());
                    }
                    tpWriter.write(line.substring(0, line.length() - 1));
                    tpWriter.newLine();
                }
                tpWriter.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
