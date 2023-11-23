package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeapmeMatcher extends Matcher {
    private Integer serverPort;
    private String information;
    private String features;

    @Override
    public float[][] match(TablePair tablePair) {
        getLogger().debug("Running LEAPME matcher for tables '{}' as source and '{}' as target.",
                tablePair.getSourceTable().getPath(), tablePair.getTargetTable().getPath());
        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(serverPort, List.of(
                    new ImmutablePair<>("table1", tablePair.getSourceTable().pathRelativeToDataDirectory()),
                    new ImmutablePair<>("table2", tablePair.getTargetTable().pathRelativeToDataDirectory()),
                    new ImmutablePair<>("prediction_used_info_types", information),
                    new ImmutablePair<>("prediction_used_features", features)
            ));
            if (response.statusCode() != 200) {
                getLogger().error("Running LEAPME matcher failed with status code {}", response.statusCode());
                return tablePair.getEmptySimMatrix();
            }

            return PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);
        } catch (Exception e) {
            getLogger().error("Running LEAPME matcher failed with exception. Is the LEAPME server running?", e);
            return tablePair.getEmptySimMatrix();
        }
    }
}
