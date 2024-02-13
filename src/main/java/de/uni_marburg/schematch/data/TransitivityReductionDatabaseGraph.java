package de.uni_marburg.schematch.data;

import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransitivityReductionDatabaseGraph extends DatabaseGraph {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private static Integer globalGraphCounter = 1;
    private final Database database;
    private final HashMap<Collection<Column>, Node> nodes = new HashMap<>();
    private final List<SiblingNode> siblingNodes = new ArrayList<>();

    @Getter
    private final Integer graphId; // in order to ensure that two graphs won't have identically named nodes
    private Integer uccCounter = 1;
    private Integer fdCounter = 1;
    private Integer antiDirectionalityNodeCounter = 1;
    private Set<Node> attributePairNodes = new HashSet<>();

    public TransitivityReductionDatabaseGraph(final Database database) {
        logger.info("Building graph");
        this.database = database;
        this.graphId = globalGraphCounter;
        globalGraphCounter++;
        buildFor(database);
        exportGraph();
    }

    @RequiredArgsConstructor
    static class Node {
        final Collection<Column> attributes;

        final Set<Node> inboundNodes = new HashSet<>(0);
        final Set<Node> outboundNodes = new HashSet<>(0);

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(attributes, node.attributes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attributes);
        }

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder();
            for (Column attribute : attributes) {
                output.append(attribute.toString());
                output.append("|");
            }
            output.append(" (" + inboundNodes.size() + ", " + outboundNodes.size() + ")");
            return output.toString();
        }
    }

    static class SiblingNode extends Node {
        private static int siblingNodeCounter = 0;

        public SiblingNode(Node... siblings) {
            super(Set.of());
            this.siblings.addAll(Arrays.stream(siblings).toList());
            id = siblingNodeCounter;
            siblingNodeCounter++;
        }

        final Set<Node> siblings = new HashSet<>(0);
        final int id;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SiblingNode node = (SiblingNode) o;
            return Objects.equals(siblings, node.siblings);
        }

        @Override
        public int hashCode() {
            return Objects.hash(siblings);
        }

        @Override
        public String toString() {
            return "SIBLING_CLUSTER_" + id + " (" + siblings.size() + ", " + outboundNodes.size() + ")";
        }
    }

    private void buildFor(final Database database) {
//        Collection<UniqueColumnCombination> uccs = database.getMetadata().getUniqueColumnCombinations(2);
//        uccs.forEach(this::addUcc);

//        Collection<FunctionalDependency> fds = database.getMetadata().getMeaningfulFunctionalDependencies(1, new HashSet<>());

        Collection<FunctionalDependency> fds = database.getMetadata().getMeaningfulFunctionalDependencies(1, new HashSet<>())
                .stream().map(database.getMetadata()::subsumeFunctionalDependencyViaInclusionDependency)
                .toList();

        // Enter FDs into graph
        int count = 0;
        for (FunctionalDependency fd : fds) {
            count++;
            System.out.println("Processing fd " + count + "/" + fds.size());
            Node sourceNode = nodes.computeIfAbsent(Set.copyOf(fd.getDeterminant()), (attributes) -> {
                Node node = new Node(attributes);
                if (attributes.size() == 2) {
                    attributePairNodes.add(node);
                }
                return node;
            });
            Node targetNode = nodes.computeIfAbsent(Set.of(fd.getDependant()), Node::new);
            sourceNode.outboundNodes.add(targetNode);
            targetNode.inboundNodes.add(sourceNode);

            if (fd.getDeterminant().size() > 1) {
                for (Column attribute : fd.getDeterminant()) {
                    // Trivial FDs to each attribute of the collection
                    Node trivialTargetNode = nodes.computeIfAbsent(Set.of(attribute), Node::new);
                    sourceNode.outboundNodes.add(trivialTargetNode);
                    trivialTargetNode.inboundNodes.add(sourceNode);
                }
            }
        }

        // Add missing edges to pairVertices resulting from a vertex having multiple FDs
        for (Node node: nodes.values()) {
            Set<Column> dependants = node.outboundNodes.stream().flatMap(n -> n.attributes.stream()).collect(Collectors.toSet());
            for (Node attributePairNode : attributePairNodes) {
                if (node == attributePairNode) {
                    continue;
                }

                if (dependants.containsAll(attributePairNode.attributes)) {
                    node.outboundNodes.add(attributePairNode);
                    attributePairNode.inboundNodes.add(node);
                }
            }
        }

        // determine siblings
        for (Node node: nodes.values()) {
            for (Node outboundNode: node.outboundNodes) {
                if (node.inboundNodes.contains(outboundNode)) {
                    Optional<SiblingNode> optionalSiblingNode = siblingNodes.stream()
                            .filter(sn -> sn.siblings.contains(node) || sn.siblings.contains(outboundNode)).findFirst();
                    if (optionalSiblingNode.isPresent()) {
                        optionalSiblingNode.get().siblings.add(node);
                        optionalSiblingNode.get().siblings.add(outboundNode);
                    } else {
                        siblingNodes.add(new SiblingNode(node, outboundNode));
                    }
                }
            }
        }

        // Remove connections between siblings
        for (SiblingNode siblingNode : siblingNodes) {
            for (Node sibling : siblingNode.siblings) {
                sibling.outboundNodes.removeAll(siblingNode.siblings);
                sibling.inboundNodes.removeAll(siblingNode.siblings);
            }
        }

        // Rewrite connections that siblings hat to siblingNode
        for (SiblingNode siblingNode : siblingNodes) {
            for (Node sibling : siblingNode.siblings) {
                for (Node originalSiblingOutboundNode : sibling.outboundNodes) {
                    originalSiblingOutboundNode.inboundNodes.remove(sibling);
                    originalSiblingOutboundNode.inboundNodes.add(siblingNode);
                }
                siblingNode.outboundNodes.addAll(sibling.outboundNodes);
                sibling.outboundNodes.clear();

                for (Node originalSiblingInboundNode : sibling.inboundNodes) {
                    originalSiblingInboundNode.outboundNodes.remove(sibling);
                    originalSiblingInboundNode.outboundNodes.add(siblingNode);
                }
                siblingNode.inboundNodes.addAll(sibling.inboundNodes);
                sibling.inboundNodes.clear();
            }
        }

        for (SiblingNode siblingNode : siblingNodes) {
            for (Node sibling : siblingNode.siblings) {
                siblingNode.inboundNodes.add(sibling);
                sibling.outboundNodes.add(siblingNode);

                siblingNode.outboundNodes.add(sibling);
                sibling.inboundNodes.add(siblingNode);
            }
        }

        // clean up transitive hull
        for (Node node: nodes.values()) {
            for (Node dependant: node.outboundNodes) {
                for (Node determinant: node.inboundNodes) {
                    // the determinant already implies the dependant by transitivity, no need for a direct edge
                    determinant.outboundNodes.remove(dependant);
                    dependant.inboundNodes.remove(determinant);
                }
            }
        }
        for (SiblingNode node: siblingNodes) {
            for (Node dependant: node.outboundNodes) {
                for (Node determinant: node.inboundNodes) {
                    // the determinant already implies the dependant by transitivity, no need for a direct edge
                    determinant.outboundNodes.remove(dependant);
                    dependant.inboundNodes.remove(determinant);
                }
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
        SimpleDirectedGraph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
//        graph.addVertex(graphRoot());
        for (Node node : nodes.values()) {
            graph.addVertex(vertexName(node.toString()));
//            graph.addEdge(graphRoot(), vertexName(node.toString()));
        }
        for (SiblingNode node : siblingNodes) {
            graph.addVertex(vertexName(node.toString()));
        }
        for (Node node : nodes.values()) {
            for (Node outboundNode : node.outboundNodes) {
                graph.addEdge(vertexName(node.toString()), vertexName(outboundNode.toString()));
            }
        }
        for (SiblingNode node : siblingNodes) {
            for (Node outboundNode : node.outboundNodes) {
                graph.addEdge(vertexName(node.toString()), vertexName(outboundNode.toString()));
            }
        }

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

    private String graphRoot() {
        return vertexName("ROOT");
    }

    private String vertexName(final String resourceType, final String resourceName) {
        return String.format("DB|%d|%s|%s", graphId, resourceType, resourceName);
    }

    private String vertexName(final String resourceName) {
        return String.format("DB|%d|%s", graphId, resourceName);
    }
}
