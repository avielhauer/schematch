package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmbdiMatcher extends Matcher {
    private Integer serverPort = 5001;
    private String sm_mode = "binary_from_embdi"; // dot_product_similarity

    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep){
        getLogger().debug("Running EmbDI matcher for scenario '{}'.",
                matchTask.getScenario().getName());

        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(serverPort, List.of(
                    new ImmutablePair<>("scenario_path", matchTask.getScenario().getPath()),
                    new ImmutablePair<>("scenario_name", matchTask.getScenario().getName()),
                    new ImmutablePair<>("sm_mode", sm_mode)
            ));
            if (response.statusCode() != 200) {
                getLogger().error("Running EmbDI matcher failed with status code {}", response.statusCode());
                return matchTask.getEmptySimMatrix();
            }
            return PythonUtils.parseOutputIntoMatrix(Arrays.stream(response.body().split("\n")).toList(), matchTask.getEmptySimMatrix());
        } catch (Exception e) {
            getLogger().error("Running EmdDI matcher failed with exception. Is the EmbDI server running?", e);
            return matchTask.getEmptySimMatrix();
        }
    }
}
