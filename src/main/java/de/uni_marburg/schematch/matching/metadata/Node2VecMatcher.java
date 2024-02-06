package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import lombok.*;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Field;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class Node2VecMatcher extends TablePairMatcher {
    private Integer serverPort = 5004;
    @Getter
    @Setter
    public double dropColumns = 0.0;
    @Getter
    @Setter
    public double dropConstraints = 0.0;
    public Integer xNetMFGammaStruc = 1;
    public Integer xNetMFGammaAttr = 1;

    @Override
    public float[][] match(TablePair tablePair) {

        // Extract Tables
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        //Extract Columns
        // Extract and load scenario meta data
        Scenario scenario = tablePair.getScenario();
        // Extract and load database meta data
        Database source = scenario.getSourceDatabase();
        Database target = scenario.getTargetDatabase();

        float[][] alignment_matrix;
        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(serverPort, List.of(
                    new ImmutablePair<>("source_graph_path", source.getGraph().exportPath().toString()),
                    new ImmutablePair<>("source_table", sourceTable.getName()),
                    new ImmutablePair<>("target_graph_path", target.getGraph().exportPath().toString()),
                    new ImmutablePair<>("target_table", targetTable.getName()),
                    new ImmutablePair<>("features_dir", "target/features/" + scenario.getDataset().getName() + "/" + scenario.getName()),
                    new ImmutablePair<>("get_k_highest_sm", "True"),
                    new ImmutablePair<>("dropColumns", String.valueOf(dropColumns)),
                    new ImmutablePair<>("dropConstraints", String.valueOf(dropConstraints)),
                    new ImmutablePair<>("xNetMFGammaStruc", String.valueOf(xNetMFGammaStruc)),
                    new ImmutablePair<>("xNetMFGammaAttr", String.valueOf(xNetMFGammaAttr))
            ));
            alignment_matrix = PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);

        } catch (Exception e){
            getLogger().error("Running Node2Vec Matcher failed, is the server running?", e);
            return  tablePair.getEmptySimMatrix();
        }

        return alignment_matrix;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());
        result.append("(");
        for (Field field : getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                result.append(field.getName()).append("=").append(field.get(this)).append(": ");
                field.setAccessible(false);
            } catch (IllegalAccessException ignored) {} // Cannot happen, we have set the field to be accessible
        }
        String res = result.toString();
        if (getClass().getDeclaredFields().length > 0) {
            res = res.substring(0, res.length() - 2);
        }
        return res + ")";
    }
}
