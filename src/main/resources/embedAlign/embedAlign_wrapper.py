import re
import json

import networkx as nx
import matplotlib.pyplot as plt
from node2vec import Node2Vec
import numpy as np

import xnetmf as REGAL_xnetmf
import config as REGAL_config
from embedAlign_graph import EmbedGraph, extract_table
from embedAlign_cache import RepresentationCache
import unsup_align

EMBEDDINGS_CACHE = {}
EMBEDDING_GENERATION = "XNET"  # "N2V" "XNET"


def embed_xnetmf(graph, features):
    repmethod = REGAL_config.RepMethod()
    graph = REGAL_config.Graph(adj=nx.adjacency_matrix(graph), node_attributes=features)
    return REGAL_xnetmf.get_representations(graph, repmethod)


def align_shapes(embed1, embed2):
    assert embed1.shape[1] == embed2.shape[1]
    delta_shape = abs(embed1.shape[0] - embed2.shape[0])
    zeros_extension = np.zeros((delta_shape, embed1.shape[1]))
    if embed1.shape[0] < embed2.shape[0]:
        embed1 = np.concatenate((embed1, zeros_extension), axis=0)
    else:
        embed2 = np.concatenate((embed2, zeros_extension), axis=0)
    return embed1, embed2


def align_embeddings(
    embed1, embed2, adj1=None, adj2=None, struc_embed=None, struc_embed2=None
):
    embed1, embed2 = align_shapes(embed1, embed2)

    # Convex Initialization
    init_sim, corr_mat = unsup_align.convex_init(
        embed1, embed2, apply_sqrt=False, niter=30, reg=1.0
    )

    min_shape = min(embed1.shape[0], embed2.shape[0])
    dim_align_matrix, corr_mat = unsup_align.align(
        embed1, embed2, init_sim, bsz=min(10, min_shape), nmax=min(10, min_shape)
    )
    return norm(embed1.dot(dim_align_matrix))


def is_column_node(node):
    if len(node.split("|")) >= 3:
        if node.split("|")[2] == "COLUMN":
            return True
    return False


def extract_column_embeddings(all_embeddings, graph, table_name):
    embeddings = []
    for i, node in enumerate(graph.graph.nodes):
        if is_column_node(node):
            if "|".join(node.split("|")[3:]).startswith(table_name):
                embeddings.append(all_embeddings[i])
    return np.asarray(embeddings)


def norm(embed):
    norms = np.linalg.norm(embed, axis=1).reshape((embed.shape[0], 1))
    norms[norms == 0] = 1
    embed = embed / norms
    return embed


def get_sm(source_table: str, target_table: str, rep_cache: RepresentationCache):
    all_original_source_nodes = [
        node
        for node in rep_cache.source_all_original_table_nodes_sorted_as_given
        if extract_table(node) == source_table
    ]
    all_original_target_nodes = [
        node
        for node in rep_cache.target_all_original_table_nodes_sorted_as_given
        if extract_table(node) == target_table
    ]

    sm = np.zeros(
        (
            len(all_original_source_nodes),
            len(all_original_target_nodes),
        )
    )
    for i, source_node in enumerate(all_original_source_nodes):
        for j, target_node in enumerate(all_original_target_nodes):
            if (
                source_node in rep_cache.column_embeddings_source
                and target_node in rep_cache.column_embeddings_target
            ):
                source_emb = rep_cache.column_embeddings_source[source_node]
                target_emb = rep_cache.column_embeddings_target[target_node]
                sm[i][j] = np.dot(source_emb, target_emb) / (
                    np.linalg.norm(source_emb) * np.linalg.norm(target_emb)
                )
            else:
                sm[i][j] = -404.0
    return sm


def generate_n2v_embeddings(graph):
    P = 1
    Q = 2
    DIMENSIONS = 300
    NUM_WALKS = 500
    WALK_LENGTH = 20
    model: Node2Vec = Node2Vec(
        graph,
        dimensions=DIMENSIONS,
        p=P,
        q=Q,
        num_walks=NUM_WALKS,
        walk_length=WALK_LENGTH,
        quiet=False,
        workers=8,
    )
    fitted_model = model.fit()
    return fitted_model.wv


def get_embeddings(
    graphA: EmbedGraph,
    source_graph_file,
    graphB: EmbedGraph,
    target_graph_file,
    dropColumns,
    dropConstraints,
):
    if EMBEDDING_GENERATION == "N2V":
        embeddings_source = generate_n2v_embeddings(graphA)
        embeddings_source = np.asarray(
            [embeddings_source[node] for node in graphA.nodes]
        )
        embeddings_target = generate_n2v_embeddings(graphB)
        embeddings_target = np.asarray(
            [embeddings_target[node] for node in graphB.nodes]
        )
        embeddings_source = align_embeddings(
            norm(embeddings_source), norm(embeddings_target)
        )
    elif EMBEDDING_GENERATION == "XNET":
        combined_graph = nx.compose(graphA.graph, graphB.graph)
        embeddings_combined = embed_xnetmf(
            combined_graph,
            np.concatenate((graphA.get_features(), graphB.get_features()), axis=0),
        )
        assert embeddings_combined.shape[0] == len(graphA.graph.nodes) + len(
            graphB.graph.nodes
        )
        embeddings_source = embeddings_combined[: len(graphA.graph.nodes)]
        embeddings_target = embeddings_combined[len(graphA.graph.nodes) :]
    else:
        print("get_embeddings failed, ", EMBEDDING_GENERATION, " not recognized")
        assert False
    EMBEDDINGS_CACHE[(source_graph_file, target_graph_file)] = (
        embeddings_source,
        embeddings_target,
    )
    return RepresentationCache(
        source_graph_file,
        target_graph_file,
        graphA,
        embeddings_source,
        graphB,
        embeddings_target,
        dropColumns,
        dropConstraints,
    )


def match(
    source_graph_file,
    source_table,
    target_graph_file,
    target_table,
    features_dir,
    config,
):
    key = (
        source_graph_file,
        target_graph_file,
        config["dropColumns"],
        config["dropConstraints"],
    )
    if key in EMBEDDINGS_CACHE:
        representationCache: RepresentationCache = EMBEDDINGS_CACHE[
            (
                source_graph_file,
                target_graph_file,
                config["dropColumns"],
                config["dropConstraints"],
            )
        ]
    else:
        graphA = EmbedGraph(
            nx.read_graphml(source_graph_file, node_type=str),
            "/home/fabian/Desktop/MP/repos/schematch/" + features_dir + "/source.json",
        )
        graphA.remove_random_ics(float(config["dropConstraints"]))
        graphA.remove_random_columns(float(config["dropColumns"]))
        graphB = EmbedGraph(
            nx.read_graphml(target_graph_file, node_type=str),
            "/home/fabian/Desktop/MP/repos/schematch/" + features_dir + "/target.json",
        )

        representationCache: RepresentationCache = get_embeddings(
            graphA,
            source_graph_file,
            graphB,
            target_graph_file,
            config["dropColumns"],
            config["dropConstraints"],
        )
        EMBEDDINGS_CACHE[key] = representationCache

    # graphA_emb = representationCache.get_source_embeddings(source_table)
    # graphB_emb = representationCache.get_target_embeddings(target_table)

    alignment_matrix = get_sm(source_table, target_table, representationCache)

    print(alignment_matrix)
    return "\n".join([" ".join([str(x) for x in row]) for row in alignment_matrix])