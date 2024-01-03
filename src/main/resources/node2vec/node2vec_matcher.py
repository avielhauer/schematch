import re

import networkx as nx
import matplotlib.pyplot as plt
from node2vec import Node2Vec

def visualize_graph(graph):
    pos = nx.spring_layout(graph)
    nx.draw(graph, with_labels=True, node_color='skyblue', font_color='black',
            font_size=10, edge_color='gray', linewidths=1, alpha=0.7)
    plt.title("Graph")

    # Display the plots
    plt.show()

def match(source_graph_file, source_graph_id, target_graph_file, target_graph_id):
    graphA: nx.Graph = nx.read_graphml(source_graph_file, node_type=str)
    graphB: nx.Graph = nx.read_graphml(target_graph_file, node_type=str)

    combinedGraph: nx.Graph = nx.compose(graphA, graphB)

    # combinedGraph.add_edge(f"DB_{source_graph_id}_ROOT_", f"DB_{target_graph_id}_ROOT_")
    # combinedGraph.add_edge(f"DB_{source_graph_id}_UCC_", f"DB_{target_graph_id}_UCC_")
    # combinedGraph.add_edge(f"DB_{source_graph_id}_FD_", f"DB_{target_graph_id}_FD_")


    P = [1]  # [4, 2, 1, 0.5, 0.25]
    Q = [2]  # [4, 2, 1, 0.5, 0.25]
    DIMENSIONS = [128, 64, 32, 256, 16]
    NUM_WALKS = [500]  # [160, 80, 40, 20]
    WALK_LENGTH = [30]  # [320, 160, 80, 40, 20]
    for dimensions in DIMENSIONS:
        for walk_length in WALK_LENGTH:
            for num_walks in NUM_WALKS:
                for p in P:
                    for q in Q:
                        model: Node2Vec = Node2Vec(combinedGraph, dimensions=dimensions, p=p, q=q, num_walks=num_walks, walk_length=walk_length, quiet=False, workers=8)
                        fitted_model = model.fit()
                        most_similar = fitted_model.wv.most_similar("DB_1_TABLE_authors", topn=50)
                        most_similar_other_db = [x for x in fitted_model.wv.most_similar("DB_1_TABLE_authors", topn=400) if not x[0].startswith("DB_1")]
                        similarity_correct = fitted_model.wv.similarity("DB_1_TABLE_authors", "DB_2_TABLE_target_authors")
                        similarity_rank = len([x for x in most_similar_other_db if x[1] > similarity_correct]) + 1
                        print(f"Dimensions: {dimensions:3d}, Walk length: {walk_length:3d}, Number of walks: {num_walks:3d}, p: {p:.2f}, q: {q:.2f}, rank: {similarity_rank:3d}, similarity: {similarity_correct}")

    alignment_matrix = []
    return "\n".join([
        " ".join(
            [str(x) for x in row]
        )
        for row in alignment_matrix
    ]
    )