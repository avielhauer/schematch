# Copied from https://github.com/GemsLab/REGAL/blob/master/alignments.py

import numpy as np
import scipy.io as sio
import sklearn.metrics.pairwise
from scipy.sparse import csr_matrix, coo_matrix
from sklearn.neighbors import KDTree
import scipy.sparse as sp
from scipy.spatial.distance import cosine
import matching
import matching.games

def get_embedding_similarities(embed, embed2 = None, sim_measure = "euclidean", top_k_row = None, top_k_col=None):
    if embed2 is None:
        embed2 = embed

    # if top_k_row is not None or top_k_col is not None: #KD tree with only top similarities computed
    #     kd_sim_source_target = kd_align(embed, embed2, distance_metric=sim_measure, num_top=top_k_row if top_k_row is not None else 0)
    #     kd_sim_target_source = kd_align(embed2, embed, distance_metric=sim_measure, num_top=top_k_col if top_k_col is not None else 0)
    #     return kd_sim_target_source.transpose().maximum(kd_sim_source_target)

    #All pairwise distance computation
    if sim_measure == "cosine":
        similarity_matrix = sklearn.metrics.pairwise.cosine_similarity(embed, embed2)
    else:
        similarity_matrix = sklearn.metrics.pairwise.euclidean_distances(embed, embed2)
        similarity_matrix = np.exp(-similarity_matrix)

    if top_k_row is not None or top_k_col is not None:
        row_players = {
            i: matching.Player(i) for i in range(similarity_matrix.shape[0])
        }
        column_players = {
            i: matching.Player(i) for i in range(similarity_matrix.shape[1])
        }
        for i, row in enumerate(similarity_matrix):
            preferences = np.flip(np.argsort(row))
            row_players[i].prefs = [
                column_players[p] for p in preferences
            ]

        for i in range(similarity_matrix.shape[1]):
            column = similarity_matrix[:, i]
            preferences = np.flip(np.argsort(column))
            column_players[i].prefs = [
                row_players[p] for p in preferences
            ]

        row_players = list(row_players.values())
        column_players = list(column_players.values())

        fill_up_players(row_players, column_players)

        marriage_problem = matching.games.StableMarriage(row_players, column_players)
        player_matching = marriage_problem.solve("suitors" if len(row_players) < len(column_players) else "reviewers")

        filter_matrix = np.zeros(similarity_matrix.shape)
        for row_player in player_matching:
            # Filter out dummy players
            if row_player.name >= 1_000_000 or row_player.matching.name >= 1_000_000:
                continue
            filter_matrix[row_player.name][row_player.matching.name] = 1
        similarity_matrix = np.minimum(similarity_matrix, filter_matrix)

    return csr_matrix(similarity_matrix)

def fill_up_players(row_players, column_players):
    if len(row_players) > len(column_players):
        row_players, column_players = column_players, row_players

    # Create dummy players
    for i in range(len(column_players) - len(row_players)):
        new_player = matching.Player(i + 1_000_000)
        new_player.prefs = column_players
        row_players.append(new_player)

        for player in column_players:
            player.prefs.append(new_player)



#Split embeddings in half
#Right now asssume graphs are same size (as done in paper's experiments)
#NOTE: to handle graphs of different sizes, pass in an arbitrary split index
#Similarly, to embed >2 graphs, change to pass in a list of splits and return list of embeddings
def get_embeddings(combined_embed, graph_split_idx = None):
    if graph_split_idx is None:
        graph_split_idx = int(combined_embed.shape[0] / 2)
    dim = combined_embed.shape[1]
    embed1 = combined_embed[:graph_split_idx]
    embed2 = combined_embed[graph_split_idx:]

    return embed1, embed2

#alignments are dictionary of the form node_in_graph 1 : node_in_graph2
#rows of alignment matrix are nodes in graph 1, columns are nodes in graph2
def score(alignment_matrix, true_alignments = None):
    if true_alignments is None: #assume it's just identity permutation
        return np.sum(np.diagonal(alignment_matrix))
    else:
        nodes_g1 = [int(node_g1) for node_g1 in true_alignments.keys()]
        nodes_g2 = [int(true_alignments[node_g1]) for node_g1 in true_alignments.keys()]
        return np.sum(alignment_matrix[nodes_g1, nodes_g2])


def score_embeddings_matrices(embed1, embed2, topk = None, similarity_threshold = None, normalize = False, true_alignments = None, sim="cosine"):
    similarity_matrix = get_embedding_similarities(embed1, embed2, sim_measure = sim)
    alignment_matrix = get_estimated_alignment_matrix(similarity_matrix, similarity_threshold, normalize)
    score = score_alignment_matrix(alignment_matrix, topk = topk, true_alignments = true_alignments)
    return score

def kd_align(emb1, emb2, normalize=False, distance_metric = "euclidean", num_top = 50):
    kd_tree_source_target = KDTree(emb2, metric = distance_metric)

    dist, ind = kd_tree_source_target.query(emb1, k = num_top)
    row = np.array([])
    for i in range(emb1.shape[0]):
        row = np.concatenate((row, np.ones(num_top)*i))
    col = ind.flatten()
    data = np.exp(-dist).flatten()
    sparse_align_matrix = coo_matrix((data, (row, col)), shape=(emb1.shape[0], emb2.shape[0]))
    return sparse_align_matrix.tocsr()


def score_alignment_matrix(alignment_matrix, topk = None, topk_score_weighted = False, true_alignments = None):
    n_nodes = alignment_matrix.shape[0]
    correct_nodes = []

    if topk is None:
        row_sums = alignment_matrix.sum(axis=1)
        row_sums[row_sums == 0] = 1e-6 #shouldn't affect much since dividing 0 by anything is 0
        alignment_matrix = alignment_matrix / row_sums[:, np.newaxis] #normalize

        alignment_score = score(alignment_matrix, true_alignments = true_alignments)
    else:
        alignment_score = 0
        if not sp.issparse(alignment_matrix):
            sorted_indices = np.argsort(alignment_matrix)

        for node_index in range(n_nodes):
            target_alignment = node_index #default: assume identity mapping, and the node should be aligned to itself
            if true_alignments is not None: #if we have true alignments (which we require), use those for each node
                target_alignment = int(true_alignments[node_index])
            if sp.issparse(alignment_matrix):
                row, possible_alignments, possible_values = sp.find(alignment_matrix[node_index])
                node_sorted_indices = possible_alignments[possible_values.argsort()]
            else:
                node_sorted_indices = sorted_indices[node_index]
            if target_alignment in node_sorted_indices[-topk:]:
                if topk_score_weighted:
                    alignment_score += 1.0 / (n_nodes - np.argwhere(sorted_indices[node_index] == target_alignment)[0])
                else:
                    alignment_score += 1
                correct_nodes.append(node_index)
        alignment_score /= float(n_nodes)

    return alignment_score, set(correct_nodes)