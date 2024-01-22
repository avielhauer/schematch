import re
import json

import networkx as nx
import matplotlib.pyplot as plt
from node2vec import Node2Vec
import numpy as np

import xnetmf as REGAL_xnetmf
import config as REGAL_config

import unsup_align
EMBEDDINGS_CACHE = {}
EMBEDDING_GENERATION = "XNET" # "N2V" "XNET"

def embed_xnetmf(graph, features):
    repmethod = REGAL_config.RepMethod()
    graph = REGAL_config.Graph(adj=nx.adjacency_matrix(graph), node_attributes=features)
    return REGAL_xnetmf.get_representations(graph, repmethod)


def one_hot_encoding_lookup(labels):
    labels = set(labels)
    lookup = {}
    for label in labels:
        if not label in lookup:
            feature = np.zeros(len(labels))
            feature[len(lookup)] = 1.0
            lookup[label] = feature
    return lookup


def encode(labels, lookup):
    return np.asarray([lookup[label] for label in labels])


def extract_features_from_names(graphA, graphB):
    labelsA = [node.split("|")[2] for node in graphA.nodes]
    labelsB = [node.split("|")[2] for node in graphB.nodes]
    lookup = one_hot_encoding_lookup(set(labelsA + labelsB))
    return encode(labelsA, lookup), encode(labelsB, lookup)

def visualize_graph(graph):
    pos = nx.spring_layout(graph)
    nx.draw(graph, with_labels=True, node_color='skyblue', font_color='black',
            font_size=10, edge_color='gray', linewidths=1, alpha=0.7)
    plt.title("Graph")

    # Display the plots
    plt.show()

def align_shapes(embed1, embed2):
    assert(embed1.shape[1] == embed2.shape[1])
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
    dim_align_matrix, corr_mat = unsup_align.align(embed1, embed2, init_sim, bsz=min(10, min_shape), nmax=min(10,min_shape))
    return norm(embed1.dot(dim_align_matrix))


def is_column_node(node):
    if len(node.split("|")) >= 3:
        if node.split("|")[2] == "COLUMN":
            return True
    return False

def extract_column_embeddings(all_embeddings, graph, table_name):
    embeddings = []
    for i, node in enumerate(graph.nodes):
        if is_column_node(node):
            if "|".join(node.split("|")[3:]).startswith(table_name):
                embeddings.append(all_embeddings[i])
    return np.asarray(embeddings)


def norm(embed):
    norms = np.linalg.norm(embed, axis=1).reshape((embed.shape[0], 1))
    norms[norms == 0] = 1
    embed = embed / norms
    return embed


def get_sm(source, target):
    sm = np.zeros((source.shape[0], target.shape[0]))
    for i in range(source.shape[0]):
        for j in range(target.shape[0]):
            sm[i][j] = np.dot(source[i], target[j]) / (np.linalg.norm(source[i]) * np.linalg.norm(target[j]))
    return sm

def generate_n2v_embeddings(graph):
    P = 1
    Q = 2
    DIMENSIONS = 300
    NUM_WALKS = 500
    WALK_LENGTH = 20
    model: Node2Vec = Node2Vec(graph, dimensions=DIMENSIONS, p=P, q=Q, num_walks=NUM_WALKS, walk_length=WALK_LENGTH,
                                quiet=False, workers=8)
    fitted_model = model.fit()
    return fitted_model.wv

def load_feature_dict(path):
    with open(path, "r") as fp:
        dict = json.loads(json.load(fp))
    return dict

def align_feature_dict(graph, feature_dict):
    # TODO: make this more elegant & robust.
    features = []
    feature_vector_length = len(list(list(feature_dict.values())[0].values())[0])
    for node in graph.nodes:
        if is_column_node(node):
            table = node.split("|")[3]
            column = node.split("|")[4]
            features.append(feature_dict[table][column])
        else:
            features.append([0.0 for _ in range(feature_vector_length)])
    print(features)
    return np.asarray(features)

def read_additional_features(graphA, graphB, features_dir):
    source_features = align_feature_dict(graphA,  load_feature_dict("/home/fabian/Desktop/MP/repos/schematch/" + features_dir+"/source.json"))
    target_features = align_feature_dict(graphB, load_feature_dict("/home/fabian/Desktop/MP/repos/schematch/" + features_dir+"/target.json"))
    return source_features, target_features

def get_embeddings(graphA, source_graph_file, graphB, target_graph_file, features_dir):
    if (source_graph_file, target_graph_file) in EMBEDDINGS_CACHE:
        embeddings_source, embeddings_target = EMBEDDINGS_CACHE[(source_graph_file, target_graph_file)]
    else:
        if EMBEDDING_GENERATION == "N2V":
            embeddings_source = generate_n2v_embeddings(graphA)
            embeddings_source = np.asarray([embeddings_source[node] for node in graphA.nodes])
            embeddings_target = generate_n2v_embeddings(graphB)
            embeddings_target = np.asarray([embeddings_target[node] for node in graphB.nodes])
            embeddings_source = align_embeddings(norm(embeddings_source), norm(embeddings_target))
        elif EMBEDDING_GENERATION == "XNET":

            featuresA, featuresB = extract_features_from_names(graphA, graphB)
            addFeaturesA, addFeaturesB = read_additional_features(graphA, graphB, features_dir)
            featuresA = np.concatenate((featuresA, addFeaturesA), axis=1)
            featuresB = np.concatenate((featuresB, addFeaturesB), axis=1)
            combined_graph = nx.compose(graphA, graphB)
            embeddings_combined = embed_xnetmf(combined_graph, np.concatenate((featuresA, featuresB), axis=0))
            embeddings_source = embeddings_combined[: len(graphA.nodes)]
            embeddings_target = embeddings_combined[len(graphA.nodes):]
        else:
            print("get_embeddings failed, ", EMBEDDING_GENERATION, " not recognized")
            assert(False)
        EMBEDDINGS_CACHE[(source_graph_file, target_graph_file)] = (embeddings_source, embeddings_target)
    return embeddings_source, embeddings_target

def match(source_graph_file, source_table, target_graph_file, target_table, features_dir):
    graphA: nx.Graph = nx.read_graphml(source_graph_file, node_type=str)
    graphB: nx.Graph = nx.read_graphml(target_graph_file, node_type=str)


    embeddings_source, embeddings_target = get_embeddings(graphA, source_graph_file, graphB, target_graph_file, features_dir)

    graphA_emb = extract_column_embeddings(embeddings_source, graphA, source_table)
    graphB_emb = extract_column_embeddings(embeddings_target, graphB, target_table)

    alignment_matrix = get_sm(graphA_emb, graphB_emb)

    print(alignment_matrix)
    return "\n".join([
        " ".join(
            [str(x) for x in row]
        )
        for row in alignment_matrix
    ]
    )
