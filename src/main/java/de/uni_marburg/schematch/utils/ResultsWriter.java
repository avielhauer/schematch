package de.uni_marburg.schematch.utils;

import de.uni_marburg.schematch.Main;
import de.uni_marburg.schematch.matchtask.MatchTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ResultsWriter {
    final static Logger log = LogManager.getLogger(ResultsWriter.class);

    /*public static void writeResults(Matcher matcher, Scenario scenario, Dataset dataset,
                                    Map<ImmutablePair<String,String>, Scoring> scorings) {
        String matcherInfo = matcher.toString();
        if (matcher instanceof TokenizedMatcher) {
            matcherInfo += "___" + ((TokenizedMatcher) matcher).getTokenizer().toString();
        }
        String path = Configuration.getInstance().getResultsDir() + File.separator +
                StringUtils.dateToString(Main.START_TIMESTAMP) + File.separator +
                Configuration.getInstance().getMatcherOutputDir() + File.separator +
                dataset.getName() + File.separator + scenario.getName() + File.separator + matcherInfo;
        for (Scoring scoring : scorings.values()) {
            scoring.writeToFile(path);
        }
    }*/

    public static void writeSimMatrix(String path, float[][] simMatrix) {
        File file = new File(path);
        file.mkdirs();
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            for (float[] scoreList : simMatrix) {
                StringBuilder line = new StringBuilder();
                for (float score : scoreList) {
                    line.append(score).append(Configuration.getInstance().getDefaultSeparator());
                }
                writer.write(line.toString().substring(0, line.length() - 1));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static String getBaseResultsPathForScenario(MatchTask matchTask) {
        Configuration config = Configuration.getInstance();
        return config.getResultsDir() + File.separator + StringUtils.dateToString(Main.START_TIMESTAMP) +
                File.separator + matchTask.getDataset().getName() + File.separator + matchTask.getScenario().getName();
    }
}
