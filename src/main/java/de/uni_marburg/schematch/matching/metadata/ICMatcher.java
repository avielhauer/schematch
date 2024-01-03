package de.uni_marburg.schematch.matching.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
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
    private Map<Integer, Integer> alignmentSeeds = new HashMap<>();
    private Integer serverPortGALib = 5003;
    private Integer serverPortGraspologic = 5004;
    private String alignMethod = "GRASP"; //"REGAL"
    private List<List<Integer>> graphBaseNodes = new ArrayList<>();
    private final Integer maxUCCSize = 5;
    private final Integer maxFDSize = 5;
    private final Float seedRatio = 0.3F;
    private final Float seedCertainty = 0.9F;

    private SimpleDirectedGraph<Integer, DefaultEdge> buildGraph(List<Column> columns, DatabaseMetadata metadata) {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        int tableNode = nodeCounter++;
        int uccNode = nodeCounter++;
        int fdNode = nodeCounter++;
        List<Integer> baseNodes = new ArrayList<>();
        baseNodes.add(tableNode);
        baseNodes.add(uccNode);
        baseNodes.add(fdNode);
        graph.addVertex(tableNode);
        graph.addVertex(uccNode);
        graph.addVertex(fdNode);

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
            graph.addEdge(uccNode, ucc_id);
        }

        for (FunctionalDependency fd : fds_filtered) {
            Integer fd_id = nodeCounter++;
            graph.addVertex(fd_id);
            for (Column c : fd.getDeterminant()) {
                graph.addEdge(columnToID.get(c), fd_id);
            }
            graph.addEdge(fd_id, columnToID.get(fd.getDependant()));
            graph.addEdge(fdNode, fd_id);
        }

        graphBaseNodes.add(baseNodes);
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
        GraphMLExporter<Integer, DefaultEdge> exporter = new GraphMLExporter<>(String::valueOf);
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

    private void addSM(float[][] sm1, float[][] sm2) {
        assert (sm1.length == sm2.length);

        for (int i = 0; i < sm1.length; i++) {
            assert (sm1[i].length == sm2[i].length);

            for (int j = 0; j < sm1[i].length; j++) {
                sm1[i][j] += sm2[i][j];
            }
        }
    }

    private void divideSM(float[][] sm, float divisor) {
        for (int i = 0; i < sm.length; i++) {

            for (int j = 0; j < sm[i].length; j++) {
                sm[i][j] /= divisor;
            }
        }
    }

    private boolean containsValueOrHigher(float[][] sm, float val) {
        for (float[] sims : sm) {
            for (float sim : sims) {
                if (sim >= val) {
                    return true;
                }
            }
        }
        return false;
    }

    private Pair<Integer, Integer> getMaximumsCoordinates(float[][] sm) {
        float max = 0.0F;
        Pair<Integer, Integer> maxCoordinates = Pair.of(0, 0);
        for (int i = 0; i < sm.length; i++) {
            for (int j = 0; j < sm[i].length; j++) {
                if (sm[i][j] > max) {
                    max = sm[i][j];
                    maxCoordinates = Pair.of(i, j);
                }
            }
        }
        return maxCoordinates;
    }

    private void setFoundAlignmentSeeds(TablePair tablePair, Integer minNumColumns) {
        float[][] aggSM = tablePair.getEmptySimMatrix();
        tablePair.getFirstLineMatcherResults().values().forEach((sm) -> addSM(aggSM, sm));
        divideSM(aggSM, tablePair.getFirstLineMatcherResults().size());

        while (Math.ceil(seedRatio * minNumColumns) > alignmentSeeds.size() || containsValueOrHigher(aggSM, seedCertainty)) {
            Pair<Integer, Integer> maxCoordinate = getMaximumsCoordinates(aggSM);
            if (aggSM[maxCoordinate.getLeft()][maxCoordinate.getRight()] == 0.0F) {
                // no more seeds to be found in similarity matrix.
                break;
            }
            aggSM[maxCoordinate.getLeft()][maxCoordinate.getRight()] = 0.0F;
            int sourceGraphID = columnToID.get(tablePair.getSourceTable().getColumns().get(maxCoordinate.getLeft()));
            if (!alignmentSeeds.containsKey(sourceGraphID)) {
                alignmentSeeds.put(sourceGraphID, columnToID.get(tablePair.getTargetTable().getColumns().get(maxCoordinate.getRight())));
            }
        }
        assert(graphBaseNodes.size() == 2);
        assert(graphBaseNodes.get(0).size() == graphBaseNodes.get(1).size());
        for(int i = 0; i < graphBaseNodes.get(0).size(); i++){
            alignmentSeeds.put(graphBaseNodes.get(0).get(i), graphBaseNodes.get(1).get(i));
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

        setFoundAlignmentSeeds(tablePair, Math.min(sourceTable.getNumberOfColumns(), targetTable.getNumberOfColumns()));
        String alignmentSeedsPath = "target/ic/" + scenario.getName() + "/" + sourceTable.getName() + "_" + targetTable.getName() + "_seeds";
        exportAlignmentSeeds(alignmentSeeds, alignmentSeedsPath);
        // TODO: Make sure empty nodes are actually written to the export file
        // (not only added isolated nodes can be empty, but also nodes with no ICs)
        float[][] sm = executeGraphAlignment(sourceGraphPath, targetGraphPath, nNodesSourceGraph, nNodesTargetGraph, tablePair, alignmentSeedsPath);

        resetMatchingState();

        return sm;
    }

    private void exportAlignmentSeeds(Map<Integer, Integer> alignmentSeeds, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Use Jackson's ObjectMapper to write the collection to a JSON file
            objectMapper.writeValue(new File(filePath), alignmentSeeds);
        } catch (IOException e) {
            getLogger().error("Alignment could not be written for " + filePath);
        }
    }

    private float[][] executeGraphAlignment(String sourceGraphPath, String targetGraphPath, int nNodesSourceGraph,
                                            int nNodesTargetGraph, TablePair tablePair, String alignmentSeedPath) {
        float[][] alignment_matrix;
        try {
            HttpResponse<String> response = PythonUtils.sendMatchRequest(
                    (Objects.equals(alignMethod, "GRASP"))
                            ? serverPortGraspologic
                            : serverPortGALib,
                    List.of(
                            new ImmutablePair<>("align_method", alignMethod),
                            new ImmutablePair<>("source_graph_path", sourceGraphPath),
                            new ImmutablePair<>("target_graph_path", targetGraphPath),
                            new ImmutablePair<>("alignment_seeds", alignmentSeedPath)
                    ));

            alignment_matrix = PythonUtils.readMatcherOutput(
                    Arrays.stream(response.body().split("\n")).toList(), nNodesSourceGraph, nNodesTargetGraph);

        } catch (Exception e) {
            getLogger().error("Running IC Matcher's Graph Alignment failed, is the server running?", e);
            return tablePair.getEmptySimMatrix();
        }

        return extractSimilarityMatrix(alignment_matrix, tablePair, nNodesSourceGraph);
    }

    private void resetMatchingState() {
        nodeCounter = 0;
        columnToID.clear();
        alignmentSeeds.clear();
        graphBaseNodes.clear();
    }
}
