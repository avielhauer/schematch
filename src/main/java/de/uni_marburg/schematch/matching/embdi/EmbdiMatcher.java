package de.uni_marburg.schematch.matching.embdi;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;

public class EmbdiMatcher extends Matcher {

    @Override
    public float[][] match(TablePair tablePair) {

        PythonUtils.PythonOutput embdiRun = PythonUtils.runPythonFile(
                "embdi.py",
                "-i_1", tablePair.getSourceTable().getPath(),
                "-i_2", tablePair.getTargetTable().getPath());

        float[][] simMatrix = tablePair.getEmptySimMatrix();
        if (embdiRun.success) {
            try {
                for (int i = 0; i < simMatrix.length; i++) {
                    String line = embdiRun.stdout.readLine();
                    String[] sims = line.split(" ");
                    for (int j = 0; j < simMatrix[i].length; j++) {
                        simMatrix[i][j] = Float.parseFloat(sims[j]);
                    }
                }
            } catch (Exception e) {
                System.out.println("Output of EmbDI Matcher python call could not be read correctly.");
                System.out.println("Similarity Matrix might be incomplete/faulty.");
                return simMatrix;
            }
        } else {
            System.out.printf("EmbDI run failed for %s source and %s target table. Falling back to empty matrix.",
                    tablePair.getSourceTable().getName(), tablePair.getTargetTable().getName());
        }
        return simMatrix;
    }
}
