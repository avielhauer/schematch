package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import lombok.*;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmbedAlignMatcher extends TablePairMatcher {
    final private Integer serverPort = 5004;
    @Getter
    @Setter
    public double dropColumns = 0.0;
    @Getter
    @Setter
    public double dropConstraints = 0.0;
    @Getter
    @Setter
    public double xNetMFGammaStrucAttr = 0.5;
    @Getter
    @Setter
    public Integer topKRow = 2;
    @Getter
    @Setter
    public Integer topKCol = 2;

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
                    new ImmutablePair<>("dropColumns", String.valueOf(dropColumns)),
                    new ImmutablePair<>("dropConstraints", String.valueOf(dropConstraints)),
                    new ImmutablePair<>("xNetMFGammaStrucAttr", String.valueOf(xNetMFGammaStrucAttr)),
                    new ImmutablePair<>("top_k_row", String.valueOf(topKRow)),
                    new ImmutablePair<>("top_k_col", String.valueOf(topKCol))
            ));
            alignment_matrix = PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);

        } catch (Exception e){
            getLogger().error("Running Node2Vec Matcher failed, is the server running?", e);
            return  tablePair.getEmptySimMatrix();
        }

        return alignment_matrix;
    }
}
