package de.uni_marburg.schematch.data;

import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.Metanome;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import de.uni_marburg.schematch.evaluation.Evaluator;
import de.uni_marburg.schematch.utils.MetadataUtils;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.graphml.GraphMLExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class MetaNodesDatabaseGraph extends DatabaseGraph {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private static Integer globalGraphCounter = 1;

    private final Integer MAX_UCC_SIZE = 3;
    private final Integer MAX_FD_SIZE = 3;

    private final Database database;
    private final SimpleDirectedGraph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
    @Getter
    private final Integer graphId; // in order to ensure that two graphs won't have identically named nodes
    private Integer uccCounter = 1;
    private Integer fdCounter = 1;
    private Integer antiDirectionalityNodeCounter = 1;

    private List<String> columnsToExclude = List.of(
//          "phone",
//          "ptmxztdd" // author_fname
    );

    public MetaNodesDatabaseGraph(final Database database) {
        logger.info("Building graph");
        this.database = database;
        this.graphId = globalGraphCounter;
        globalGraphCounter++;

        graphBuildingTime = Evaluator.profileRuntime(() -> {
            buildFor(database);
            exportGraph();
            return 0;
        }).getRight();
    }

    private void buildFor(final Database database) {
        graph.addVertex(graphRoot());
//        graph.addVertex(uccMetaNode());
//        graph.addVertex(fdMetaNode());

        for (Table table : database.getTables()) {
            graph.addVertex(tableNode(table));
            addEdge(graphRoot(), tableNode(table), true);

            for (Column column : table.getColumns()) {
                graph.addVertex(columnNode(column));

                if (columnsToExclude.contains(column.getLabel())) {
                    continue;
                }

                addEdge(tableNode(table), columnNode(column), true);
            }
        }

        int maxConstraintsSize = graph.vertexSet().size() * 100;

//        int maxUccSize = MAX_UCC_SIZE;
//        Collection<UniqueColumnCombination> uccs;
//        do {
//            uccs = database.getMetadata().getUniqueColumnCombinations(maxUccSize);
//            maxUccSize--;
//        } while (uccs.size() > maxConstraintsSize && maxUccSize >= 1);
//        uccs.forEach(this::addUcc);

        int maxFdSize = MAX_FD_SIZE;
        Collection<FunctionalDependency> fds;
        fds = database.getMetadata().getMeaningfulFunctionalDependencies(maxFdSize, maxConstraintsSize, new HashSet<>());
//        fds = fds.stream()
//                .sorted(Comparator.comparingDouble((FunctionalDependency fd) -> fd.getPdepTuple().gpdep).reversed())
//                .limit(fds.size() / 2)
//                .filter(fd -> fd.getPdepTuple().gpdep > 0)
//                .toList();
        fds.forEach(this::addFd);
//        fds.stream().map(database.getMetadata()::subsumeFunctionalDependencyViaInclusionDependency).forEach(this::addFd);

        // We might have computed some new pdep scores and want to save those.
        if (Metanome.SAVE) {
            for (Table table : database.getTables()) {
                MetadataUtils.saveFDs(
                        MetadataUtils.getMetadataPathFromTable(Path.of(table.getPath())),
                        database.getMetadata().getTableFDs(table)
                );
            }
        }
    }

    public Path exportPath() {
        return Path.of("target/graphs")
                .resolve(database.getScenario().getDataset().getName())
                .resolve(database.getScenario().getName())
                .resolve(database.getName() + ".gml");
    }

    private void exportGraph() {
        GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>(Function.identity());
        try {
            Files.createDirectories(exportPath().getParent());
            Writer sourceGraphWriter = new FileWriter(exportPath().toString());
            exporter.exportGraph(graph, sourceGraphWriter);
        } catch (IOException e) {
            logger.error("Could not open file " + graph);
            throw new RuntimeException(e);
        }
    }

    private void addUcc(UniqueColumnCombination ucc) {
        String thisUccNode = vertexName("UCC", String.valueOf(uccCounter));
        graph.addVertex(thisUccNode);
//        addEdge(thisUccNode, uccMetaNode());
        for (Column c : ucc.getColumnCombination()) {
            addEdge(columnNode(c), thisUccNode, true);
        }

        uccCounter++;
    }

    private void addFd(FunctionalDependency fd) {
        if (fd.getDeterminant().stream().anyMatch((c) -> columnsToExclude.contains(c.getLabel())) ||
                columnsToExclude.contains(fd.getDependant().getLabel())) {
            return;
        }

        String thisFdNode = vertexName("FD", String.valueOf(fdCounter));
        graph.addVertex(thisFdNode);
//        addEdge(thisFdNode, fdMetaNode());
        for (Column c : fd.getDeterminant()) {
            addEdge(columnNode(c), thisFdNode);
        }
        addEdge(thisFdNode, columnNode(fd.getDependant()));

        fdCounter++;
    }

    private void addEdge(final String sourceVertex, final String targetVertex) {
        addEdge(sourceVertex, targetVertex, false);
    }

    private void addEdge(final String sourceVertex, final String targetVertex, final Boolean bothDirections) {
        graph.addEdge(sourceVertex, targetVertex);
        if (bothDirections) {
            graph.addEdge(targetVertex, sourceVertex);
        }
//        String antiDirectionNode = vertexName("ANTI_DIRECTION", String.valueOf(antiDirectionalityNodeCounter));
//        graph.addVertex(antiDirectionNode);
//        graph.addEdge(targetVertex, antiDirectionNode);
//        graph.addEdge(antiDirectionNode, sourceVertex);
//
//        antiDirectionalityNodeCounter++;
    }

    private String graphRoot() {
        return vertexName("ROOT", "");
    }
    private String uccMetaNode() {
        return vertexName("UCC", "");
    }
    private String fdMetaNode() {
        return vertexName("FD", "");
    }
    private String tableNode(final Table table) {
        return vertexName("TABLE", table.getName());
    }
    private String columnNode(final Column column) {
        return vertexName("COLUMN", column.getTable().getName() + "|" + column.getLabel());
    }
    private String vertexName(final String resourceType, final String resourceName) {
        return String.format("DB|%d|%s|%s", graphId, resourceType, resourceName);
    }
}
