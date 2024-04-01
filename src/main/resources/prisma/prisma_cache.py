import networkx as nx
import numpy as np

from prisma_graph import EmbedGraph
from prisma_graph import extract_node_type, extract_table
from REGAL_alignments import get_embedding_similarities


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
        xNetMFGammaAttrStruc
    ):
        self.key = (source_graph_file, target_graph_file, dropColumns, dropConstraints, xNetMFGammaAttrStruc)

        self.source_table_nodes_sorted_as_given = graphA.mappings["COLUMN"]
        self.source_all_original_table_nodes_sorted_as_given = graphA.original_mappings[
            "COLUMN"
        ]
        self.target_table_nodes_sorted_as_given = graphB.mappings["COLUMN"]
        self.target_all_original_table_nodes_sorted_as_given = graphB.original_mappings[
            "COLUMN"
        ]
        self.column_embeddings_source_lookup = get_column_lookup(embeddingsA, graphA.graph)
        self.column_embeddings_target_lookup = get_column_lookup(embeddingsB, graphB.graph)
        self.source_column_embeddings = np.asarray([self.column_embeddings_source_lookup[node] for node in self.source_table_nodes_sorted_as_given])
        self.target_column_embeddings = np.asarray([self.column_embeddings_target_lookup[node] for node in self.target_table_nodes_sorted_as_given])
        self.csr_k_similar_sm_cache = {}

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
            self.column_embeddings_source_lookup,
        )

    def get_target_embeddings(self, table):
        return self.get_embeddings(
            table,
            self.target_table_nodes_sorted_as_given,
            self.column_embeddings_target_lookup,
        )

    def cosine_distance(self, source_node, target_node):
        source_emb = self.column_embeddings_source_lookup[source_node]
        target_emb = self.column_embeddings_target_lookup[target_node]
        return np.dot(source_emb, target_emb) / (
            np.linalg.norm(source_emb) * np.linalg.norm(target_emb)
        )

    def get_filtered_sm(self, source_table, target_table, distance="cosine", top_k_row=2, top_k_col=2, top_k_by_union=True):
        source_indices_lookup = {node: i for i, node in enumerate(self.source_table_nodes_sorted_as_given) if extract_table(node) == source_table}
        target_indices_lookup = {node: i for i, node in enumerate(self.target_table_nodes_sorted_as_given) if extract_table(node) == target_table}
        filtered_original_source_column_nodes = [node for node in self.source_all_original_table_nodes_sorted_as_given if extract_table(node) == source_table]
        filtered_original_target_column_nodes = [node for node in self.target_all_original_table_nodes_sorted_as_given if extract_table(node) == target_table]
        sm = np.zeros(
            (
                len(filtered_original_source_column_nodes),
                len(filtered_original_target_column_nodes),
            )
        )

        if (top_k_row, top_k_col, top_k_by_union) not in self.csr_k_similar_sm_cache:
            self.csr_k_similar_sm_cache[(top_k_row, top_k_col, top_k_by_union)] = get_embedding_similarities(self.source_column_embeddings, self.target_column_embeddings, top_k_row=top_k_row, top_k_col=top_k_col, top_k_by_union=top_k_by_union)

        csr_k_similar_sm = self.csr_k_similar_sm_cache[(top_k_row, top_k_col, top_k_by_union)]

        for sm_i, source_node in enumerate(filtered_original_source_column_nodes):
            for sm_j, target_node in enumerate(filtered_original_target_column_nodes):
                if source_node in source_indices_lookup and target_node in target_indices_lookup:
                    csr_value = csr_k_similar_sm.getrow(source_indices_lookup[source_node]).getcol(target_indices_lookup[target_node]).toarray()[0][0]
                    if csr_value != 0:
                        # TODO: make this distinction when generation the sparse lookup.
                        # the csr_matrix is built based on euclidean distance. If the ordering based on the euclidean distance is identical to the ordering
                        # generated using cosine distance, we can simply get the top k euclidean matchings and calculate the cosine sim, to get the top k
                        # cosine identical matchings.
                        # I think is is only kind of correct (for normalized vectors), but for sure very inefficient.
                        if distance == "euclidean":
                            sm[sm_i][sm_j] = csr_k_similar_sm.getrow(source_indices_lookup[source_node]).getcol(target_indices_lookup[target_node]).toarray()[0][0]
                        elif distance == "cosine":
                            sm[sm_i][sm_j] = self.cosine_distance(source_node, target_node)
                else:
                    sm[sm_i][sm_j] = -404.0

        return sm