import networkx as nx
import numpy as np

from embedAlign_graph import EmbedGraph
from embedAlign_graph import extract_node_type, extract_table


def get_column_lookup(embeddings, graph: nx.Graph):
    lookup = {}
    for i, node in enumerate(graph.nodes):
        if extract_node_type(node) == "COLUMN":
            lookup[node] = embeddings[i]
    return lookup


class RepresentationCache:
    def __init__(
        self,
        source_graph_file,
        target_graph_file,
        graphA: EmbedGraph,
        embeddingsA,
        graphB: EmbedGraph,
        embeddingsB,
        dropColumns,
        dropConstraints,
    ):
        self.key = (source_graph_file, target_graph_file, dropColumns, dropConstraints)

        self.source_table_nodes_sorted_as_given = graphA.mappings["COLUMN"]
        self.source_all_original_table_nodes_sorted_as_given = graphA.original_mappings[
            "COLUMN"
        ]
        self.target_table_nodes_sorted_as_given = graphB.mappings["COLUMN"]
        self.target_all_original_table_nodes_sorted_as_given = graphB.original_mappings[
            "COLUMN"
        ]
        self.column_embeddings_source = get_column_lookup(embeddingsA, graphA.graph)
        self.column_embeddings_target = get_column_lookup(embeddingsB, graphB.graph)

    def get_embeddings(self, table, nodes, lookup):
        embeddings = []
        for node in nodes:
            if extract_table(node) == table:
                embeddings.append(lookup[node])
        return np.asarray(embeddings)

    def get_source_embeddings(self, table):
        return self.get_embeddings(
            table,
            self.source_table_nodes_sorted_as_given,
            self.column_embeddings_source,
        )

    def get_target_embeddings(self, table):
        return self.get_embeddings(
            table,
            self.target_table_nodes_sorted_as_given,
            self.column_embeddings_target,
        )
