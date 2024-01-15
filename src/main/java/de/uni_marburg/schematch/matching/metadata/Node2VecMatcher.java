package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class Node2VecMatcher extends TablePairMatcher {
    private Integer serverPort = 5004;
    private float[][] extractSimilarityMatrix(float[][] alignment_matrix, TablePair tablePair, Integer sourceNodeCount){
        float[][] sm = tablePair.getEmptySimMatrix();
//        List<Integer> sourceColumns = tablePair.getSourceTable().getColumns().stream().map((c) -> columnToID.get(c)).toList();
//        List<Integer> targetColumns = tablePair.getTargetTable().getColumns().stream().map((c) -> columnToID.get(c)).toList();
//
//        for(int i = 0; i < sourceColumns.size(); i++){
//            for(int j = 0; j < targetColumns.size(); j++){
//                sm[i][j] = alignment_matrix[sourceColumns.get(i)][targetColumns.get(j) - sourceNodeCount];
//            }
//        }

        return sm;
    }

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
                    new ImmutablePair<>("target_table", targetTable.getName())
            ));

            alignment_matrix = PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);

        } catch (Exception e){
            getLogger().error("Running Node2Vec Matcher failed, is the server running?", e);
            return  tablePair.getEmptySimMatrix();
        }

//        float[][] sm = extractSimilarityMatrix(alignment_matrix, tablePair, nNodesSourceGraph);
        return alignment_matrix;
    }
}
