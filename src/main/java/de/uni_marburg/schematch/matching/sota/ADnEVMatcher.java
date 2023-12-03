package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import de.uni_marburg.schematch.utils.OutputWriter;
import de.uni_marburg.schematch.utils.PythonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ADnEVMatcher extends Matcher {
    private Integer serverPort;

    @Override
    public float[][] match(TablePair tablePair) {
        int numSourceColumns = tablePair.getSourceTable().getNumberOfColumns();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        float[][][] firstLineSimilarityMatrices;

        if (Configuration.getInstance().isRunSimMatrixBoostingOnFirstLineMatchers()) {
            firstLineSimilarityMatrices = tablePair.getBoostedFirstLineMatcherResults().values().toArray(new float[0][][]);
        } else {
            firstLineSimilarityMatrices = tablePair.getFirstLineMatcherResults().values().toArray(new float[0][][]);
        }


        String sm_folder = "target/adnev/" + tablePair.getSourceTable().getName() + "_" + tablePair.getTargetTable().getName();
        for(int id = 0; id < firstLineSimilarityMatrices.length;id++){
            OutputWriter.writeSimMatrix(sm_folder + "/" + id, firstLineSimilarityMatrices[id]);
        }

        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(serverPort, List.of(
                    new ImmutablePair<>("sm_dir", sm_folder)
            ));
            if (response.statusCode() != 200) {
                getLogger().error("Running ADnEV failed with status code {}", response.statusCode());
                return tablePair.getEmptySimMatrix();
            }
            return PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);
        } catch (Exception e) {
            getLogger().error("Running EmdDI matcher failed with exception. Is the EmbDI server running?", e);
            return tablePair.getEmptySimMatrix();
        }
    }
}
