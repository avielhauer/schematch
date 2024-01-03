import networkx as nx
import numpy as np
from graspologic.utils import import_graph
from graspologic.match import graph_match
import json

def alignment_matrix_to_string(am):
    return "\n".join([
        " ".join(
            [str(x) for x in row]
        )
        for row in am
    ]
    )

def load_seeds(seed_file_path):
    with open(seed_file_path, "r") as seed_file:
        return json.load(seed_file)

def rebuild_complete_matrix(seed_row_indexes, seed_column_indexes, convex_solution):
    sm = np.copy(convex_solution)

    # TODO: make this efficient (currently the whole array is copied seed times * 2)
    for row in sorted(seed_row_indexes):
        sm = np.insert(sm,row, 0, axis=0)
    for column in sorted(seed_column_indexes):
        sm = np.insert(sm,column, 0, axis=1)

    for row, col in zip(seed_row_indexes, seed_column_indexes):
        sm[row][col] = 1.0
    return sm

def execute_grasp(graphA, graphB, seed_file_path):
    lookup_graphA_name = list(graphA.nodes)
    lookup_graphB_name = list(graphB.nodes)

    lookup_graphA_index = {node: i for i, node in enumerate(lookup_graphA_name)}
    lookup_graphB_index = {node: i for i, node in enumerate(lookup_graphB_name)}
    graphA = nx.to_numpy_array(graphA, dtype=np.float_)
    graphB = nx.to_numpy_array(graphB, dtype=np.float_)


    known_matches = [(int(a),b) for a,b in load_seeds(seed_file_path).items()]
    seed = np.array(
        [[lookup_graphA_index[a], lookup_graphB_index[b]] for a, b in known_matches]
    )
    # graphs are converted to numpy arrays, the nodes are sorted before they are translated into indices
    # TODO: perhaps realign matrix based on new sortings?
    res = graph_match(graphA, graphB, n_init=10, partial_match=seed)

    print(res)
    print(sorted(res.misc, key=lambda x: x["score"], reverse=True)[0]["score"])
    print(sorted(res.misc, key=lambda x: x["score"], reverse=True)[0]["convex_solution"].shape)
    return rebuild_complete_matrix(seed[:,0], seed[:,1],sorted(res.misc, key=lambda x: x["score"], reverse=True)[0]["convex_solution"])

def match(source_graph_file, target_graph_file, seed_file=""):
    graphA = nx.read_graphml(source_graph_file, node_type=int)
    graphB = nx.read_graphml(target_graph_file, node_type=int)
    return alignment_matrix_to_string(execute_grasp(graphA, graphB, seed_file))

if __name__ == "__main__":
    print(match("article_publication_source", "article_publication_target", "article_publication_seeds"))
