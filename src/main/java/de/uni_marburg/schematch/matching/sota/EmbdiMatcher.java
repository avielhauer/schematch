package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matching.Matcher;
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
    private Integer serverPort;
    private String sm_mode;

    @Override
    public float[][] match(TablePair tablePair) {
        getLogger().debug("Running EmbDI matcher for tables '{}' as source and '{}' as target.",
                tablePair.getSourceTable().getName(), tablePair.getTargetTable().getName());

        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(serverPort, List.of(
                    new ImmutablePair<>("table1", tablePair.getSourceTable().pathRelativeToDataDirectory()),
                    new ImmutablePair<>("table2", tablePair.getTargetTable().pathRelativeToDataDirectory()),
                    new ImmutablePair<>("sm_mode", sm_mode)
            ));
            if (response.statusCode() != 200) {
                getLogger().error("Running EmbDI matcher failed with status code {}", response.statusCode());
                return tablePair.getEmptySimMatrix();
            }
            return PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);
        } catch (Exception e) {
            getLogger().error("Running EmdDI matcher failed with exception. Is the EmbDI server running?", e);
            return tablePair.getEmptySimMatrix();
        }
    }
}
