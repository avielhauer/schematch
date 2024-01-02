package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Column;
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
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ICMatcher extends Matcher {

    private Integer nodeCounter = 0;
    private Map<Column, Integer> columnToID = new HashMap<>();
    private Integer serverPort = 5003;
    private final Integer maxUCCSize = 5;
    private final Integer maxFDSize = 5;

    private SimpleDirectedGraph<Integer, DefaultEdge> buildGraph(List<Column> columns, DatabaseMetadata metadata) {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        int tableNode = nodeCounter++;
        graph.addVertex(tableNode);

        for (Column column : columns) {
            Integer columnID = nodeCounter++;
            columnToID.put(column, columnID);
            graph.addVertex(columnID);
            graph.addEdge(tableNode, columnID);
        }
        // TODO: perhaps go over the ucc and fd collections on the metadata and simply filter out the ones that do not
        HashSet<Column> columnLookup = new HashSet<>(columns);
        Collection<UniqueColumnCombination> uccs_unfiltered = metadata.getUccs();
        Collection<UniqueColumnCombination> uccs_filtered = uccs_unfiltered.stream().filter((ucc) -> columnLookup.containsAll(ucc.getColumnCombination()) && ucc.getColumnCombination().size() <= maxUCCSize).toList();

        HashSet<UniqueColumnCombination> uccLookup = new HashSet<>(uccs_filtered);
        Collection<FunctionalDependency> fds_unfiltered = metadata.getFds();
        Collection<FunctionalDependency> fds_filtered = fds_unfiltered.stream().filter((fd) -> (!uccLookup.contains(new UniqueColumnCombination(fd.getDeterminant()))) && columnLookup.containsAll(fd.getDeterminant()) && columnLookup.contains(fd.getDependant()) && fd.getDeterminant().size() < maxFDSize).toList();

        int cutoff = maxFDSize - 1;
        while (fds_filtered.size() > 2000 && cutoff > 1) {
            int finalCutoff = cutoff--;
            fds_filtered = fds_filtered.stream().filter((fd) -> fd.getDeterminant().size() < finalCutoff).toList();
        }
        cutoff = maxUCCSize;
        while (uccs_filtered.size() > 2000 && cutoff >= 1) {
            int finalCutoff1 = cutoff--;
            uccs_filtered = uccs_filtered.stream().filter((ucc) -> ucc.getColumnCombination().size() <= finalCutoff1).toList();
        }

        for (UniqueColumnCombination ucc : uccs_filtered) {
            Integer ucc_id = nodeCounter++;
            graph.addVertex(ucc_id);
            for (Column c : ucc.getColumnCombination()) {
                graph.addEdge(columnToID.get(c), ucc_id);
            }
        }

        for (FunctionalDependency fd : fds_filtered) {
            Integer fd_id = nodeCounter++;
            graph.addVertex(fd_id);
            for (Column c : fd.getDeterminant()) {
                graph.addEdge(columnToID.get(c), fd_id);
            }
            graph.addEdge(fd_id, columnToID.get(fd.getDependant()));
        }

        return graph;
    }

    private float[][] removeAddedVertices(float[][] alignment_matrix, int diff, boolean sourceBigger) {
        // alignment_matrix is a square, so dimensions are equal.
        int dim = alignment_matrix.length;
        float[][] newMatrix;
        if (sourceBigger) {
            // remove diff last columns
            newMatrix = new float[dim][dim - diff];

            for (int i = 0; i < dim; i++) {
                System.arraycopy(alignment_matrix[i], 0, newMatrix[i], 0, dim - diff);
            }
        } else {
            // remove diff last rows
            newMatrix = new float[dim - diff][dim];
            for (int i = 0; i < dim - diff; i++) {
                System.arraycopy(alignment_matrix[i], 0, newMatrix[i], 0, dim);
            }
        }
        return newMatrix;
    }

    private float[][] extractSimilarityMatrix(float[][] alignment_matrix, TablePair tablePair, Integer sourceNodeCount) {
        float[][] sm = tablePair.getEmptySimMatrix();
        List<Integer> sourceColumns = tablePair.getSourceTable().getColumns().stream().map((c) -> columnToID.get(c)).toList();
        List<Integer> targetColumns = tablePair.getTargetTable().getColumns().stream().map((c) -> columnToID.get(c)).toList();

        for (int i = 0; i < sourceColumns.size(); i++) {
            for (int j = 0; j < targetColumns.size(); j++) {
                sm[i][j] = alignment_matrix[sourceColumns.get(i)][targetColumns.get(j) - sourceNodeCount];
            }
        }

        return sm;
    }

    private void exportGraph(String path, SimpleDirectedGraph<Integer, DefaultEdge> graph) {
        GraphMLExporter<Integer, DefaultEdge> exporter = new GraphMLExporter<>();
        Path filePath = Paths.get(path);
        try {
            Files.createDirectories(filePath.getParent());
            Writer sourceGraphWriter = new FileWriter(path);
            exporter.exportGraph(graph, sourceGraphWriter);
        } catch (IOException e) {
            getLogger().error("Could not open file " + graph);
            throw new RuntimeException(e);
        }
    }

    @Override
    public float[][] match(TablePair tablePair) {

        // Extract Tables
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        // Extract and load scenario meta data
        Scenario scenario = tablePair.getScenario();

        SimpleDirectedGraph<Integer, DefaultEdge> sourceGraph = buildGraph(sourceTable.getColumns(), scenario.getSourceDatabase().getMetadata());
        SimpleDirectedGraph<Integer, DefaultEdge> targetGraph = buildGraph(targetTable.getColumns(), scenario.getTargetDatabase().getMetadata());

        // NA expects both graph to have the same number of nodes - we will add isolated nodes to the smaller graph
        int nNodesSourceGraph = sourceGraph.vertexSet().size();
        int nNodesTargetGraph = targetGraph.vertexSet().size();

        String targetGraphPath = "target/ic/" + scenario.getName() + "/" + sourceTable.getName() + "_" + targetTable.getName() + "_target";
        String sourceGraphPath = "target/ic/" + scenario.getName() + "/" + sourceTable.getName() + "_" + targetTable.getName() + "_source";
        exportGraph(targetGraphPath, targetGraph);
        exportGraph(sourceGraphPath, sourceGraph);

        // TODO: Make sure empty nodes are actually written to the export file
        // (not only added isolated nodes can be empty, but also nodes with no ICs)
        float[][] sm = executeGraphAlignment(sourceGraphPath, targetGraphPath, nNodesSourceGraph, nNodesTargetGraph, tablePair);

        resetMatchingState();

        return sm;
    }

    private float[][] executeGraphAlignment(String sourceGraphPath, String targetGraphPath, int nNodesSourceGraph, int nNodesTargetGraph, TablePair tablePair) {
        float[][] alignment_matrix;
        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(serverPort, List.of(
                    new ImmutablePair<>("source_graph_path", sourceGraphPath),
                    new ImmutablePair<>("target_graph_path", targetGraphPath)
            ));

            alignment_matrix = PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), nNodesSourceGraph, nNodesTargetGraph);

        } catch (Exception e) {
            getLogger().error("Running IC Matcher's Graph Alignment failed, is the server running?", e);
            return tablePair.getEmptySimMatrix();
        }

        return extractSimilarityMatrix(alignment_matrix, tablePair, nNodesSourceGraph);
    }

    private void resetMatchingState() {
        nodeCounter = 0;
        columnToID.clear();
    }
}
