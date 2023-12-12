package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.DatabaseMetadata;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jgrapht.Graphs;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.Writer;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class ICMatcher extends Matcher {

    private Integer nodeCounter = 0;
    private Map<Column, Integer> columnToID = new HashMap<>();
    private Integer serverPort = 5003;
    private SimpleDirectedGraph<Integer, DefaultEdge> buildGraph(List<Column> columns, Map<Column, Collection<UniqueColumnCombination>> uccs, Map<Column, Collection<FunctionalDependency>> fds) {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (Column column : columns) {
            Integer column_id = nodeCounter;
            nodeCounter++;
            columnToID.put(column, column_id);
            graph.addVertex(column_id);
        }
        Collection<UniqueColumnCombination> uccs_combined = new HashSet<>();
        for (Column column : columns){
            if(uccs.containsKey(column)){
                uccs_combined.addAll(uccs.get(column));
            }
        }
        Collection<FunctionalDependency> fds_combined = new HashSet<>();
        for(Column column : columns){
            if(fds.containsKey(column)){
                fds_combined.addAll(fds.get(column));
            }
        }
        for (UniqueColumnCombination ucc : uccs_combined){
            Integer ucc_id = nodeCounter;
            nodeCounter++;
            graph.addVertex(ucc_id);
            for(Column c : ucc.getColumnCombination()){
                graph.addEdge(columnToID.get(c), ucc_id);
            }
        }

        for(FunctionalDependency fd: fds_combined){
            Integer fd_id = nodeCounter;
            nodeCounter++;
            graph.addVertex(fd_id);
            for(Column c : fd.getDeterminant()){
                graph.addEdge(columnToID.get(c), fd_id);
            }
            graph.addEdge(fd_id, columnToID.get(fd.getDependant()));
        }

        return graph;
    }

    private float[][] removeAddedVertices(float[][] alignment_matrix, int diff, boolean sourceBigger){
        // alignment_matrix is a square, so dimensions are equal.
        int dim = alignment_matrix.length;
        float[][] newMatrix;
        if(sourceBigger){
            // remove diff last columns
            newMatrix = new float[dim][dim-diff];

            for (int i = 0; i < dim; i++) {
                System.arraycopy(alignment_matrix[i], 0, newMatrix[i], 0, dim-diff);
            }
        } else {
            // remove diff last rows
            newMatrix = new float[dim-diff][dim];
            for (int i = 0; i < dim-diff; i++) {
                System.arraycopy(alignment_matrix[i], 0, newMatrix[i], 0, dim);
            }
        }
        return newMatrix;
    }

    private float[][] extractSimilarityMatrix(float[][] alignment_matrix, TablePair tablePair){
        float[][] sm = tablePair.getEmptySimMatrix();
        List<Integer> sourceColumns = tablePair.getSourceTable().getColumns().stream().map((c) -> columnToID.get(c)).toList();
        List<Integer> targetColumns = tablePair.getTargetTable().getColumns().stream().map((c) -> columnToID.get(c)).toList();

        for(int i = 0; i < sourceColumns.size(); i++){
            for(int j = 0; j < targetColumns.size(); j++){
                sm[i][j] = alignment_matrix[sourceColumns.get(i)][targetColumns.get(j)];
            }
        }

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
        DatabaseMetadata sourceMetadata = source.getMetadata();
        DatabaseMetadata targetMetadata = target.getMetadata();
        // Extract UCCs
        Map<Column, Collection<UniqueColumnCombination>> sourceUccs = sourceMetadata.getUccMap();
        Map<Column, Collection<UniqueColumnCombination>> targetUccs = targetMetadata.getUccMap();
        // Extract FDs
        Map<Column, Collection<FunctionalDependency>> sourceFds = sourceMetadata.getFdMap();
        Map<Column, Collection<FunctionalDependency>> targetFds = targetMetadata.getFdMap();

        SimpleDirectedGraph<Integer, DefaultEdge> sourceGraph = buildGraph(sourceTable.getColumns(), sourceUccs, sourceFds);
        SimpleDirectedGraph<Integer, DefaultEdge> targetGraph = buildGraph(targetTable.getColumns(), targetUccs, targetFds);

        // NA expects both graph to have the same number of nodes - we will add isolated nodes to the smaller graph
        int nNodesSourceGraph = sourceGraph.vertexSet().size();
        int nNodesTargetGraph = targetGraph.vertexSet().size();
        int diff = Math.abs(nNodesTargetGraph - nNodesSourceGraph);
        if(nNodesTargetGraph > nNodesSourceGraph){
            for(int i = 0; i < diff;i++){
                sourceGraph.addVertex(nodeCounter);
                nodeCounter++;
            }
        } else {
            for(int i = 0; i < diff; i++){
                targetGraph.addVertex(nodeCounter);
                nodeCounter++;
            }
        }
        Graphs.addGraph(sourceGraph, targetGraph);
        GraphMLExporter<Integer, DefaultEdge> exporter = new GraphMLExporter<>();
        String graphPath = "target/ic/" + scenario.getName() + "/" + tablePair.getSourceTable().getName() + "_" + tablePair.getTargetTable().getName();
        try {
            Path filePath = Paths.get(graphPath);
            Files.createDirectories(filePath.getParent());
            Writer writer = new FileWriter(graphPath);
            exporter.exportGraph(sourceGraph, writer);
        } catch (IOException e) {
            getLogger().error("Could not open file " + graphPath);
            throw new RuntimeException(e);
        }
        // TODO: Make sure empty nodes are actually written to the export file
        // (not only added isolated nodes can be empty, but also nodes with no ICs)

        float[][] alignment_matrix;
        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(serverPort, List.of(
                    new ImmutablePair<>("graph_path", graphPath)
            ));

            alignment_matrix = PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), Math.max(nNodesSourceGraph, nNodesTargetGraph));

        } catch (Exception e){
            getLogger().error("Running IC Matcher's Graph Alignment failed, is the server running?", e);
            return  tablePair.getEmptySimMatrix();
        }

        return extractSimilarityMatrix(removeAddedVertices(alignment_matrix, diff, nNodesSourceGraph > nNodesTargetGraph), tablePair);
    }
}
