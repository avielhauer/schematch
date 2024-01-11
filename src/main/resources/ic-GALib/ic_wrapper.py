'''
Uses GALib scripts to generate alignment matrices of given graph(s).
Code mainly taken and modified from: https://github.com/wyy-code/GALib/blob/main/run.py
'''

import sys
import os

# Add GALib to path, so that import resolcing works
sys.path.append(os.path.join(os.path.dirname(__file__), 'GALib'))

import networkx as nx
import time
from GALib.encoder.REGAL.xnetmf_config import *
from scipy.linalg import block_diag
import GALib.encoder.REGAL.xnetmf as xnetmf
import GALib.encoder.REGAL.regal_utils as regal_utils
from GALib.encoder.CONE.CONE import CONE
from GALib.encoder.Grampa.Grampa import Grampa
from GALib.encoder.IsoRank.IsoRank import IsoRank
from GALib.encoder.BigAlign.BigAlign import BigAlign
from GALib.encoder.NSD.NSD import NSD
from GALib.encoder.LREA.LREA import LREA
from GALib.encoder.Grasp.Grasp import Grasp
from GALib.encoder.gwl import gwl_model
import torch.optim as optim
from torch.optim import lr_scheduler



attributes=None
attrvals=2
k=10
untillayer=2
alpha=0.01
gammastruc=1
gammaattr=1
buckets=2

def align_node_count(graphA, graphB):
    numNodesA = graphA.number_of_nodes()
    numNodesB = graphB.number_of_nodes()
    totalNodesCount = numNodesA + numNodesB
    diff = abs(numNodesA - numNodesB)
    if(numNodesB > numNodesA):
        for i in range(totalNodesCount + 1, totalNodesCount +1 + diff):
            graphA.add_node(i)
    else:
        for i in range(totalNodesCount + 1, totalNodesCount +1 + diff):
            graphB.add_node(i)
    return numNodesB > numNodesA, diff

def remove_added_nodes(alignment_matrix, addedToSource, numNodesAdded):
    if numNodesAdded == 0:
        return alignment_matrix
    if addedToSource:
        return alignment_matrix[:-numNodesAdded,:]
    else:
        return alignment_matrix[:,:-numNodesAdded]

def alignment_matrix_to_string(am):
    return "\n".join([
        " ".join(
            [str(x) for x in row]
        )
        for row in am
    ]
    )

def match(source_graph_file, target_graph_file, align_method="REGAL", embmethod="xnetMF"):

    ##################### Load data ######################################
    graphA = nx.read_graphml(source_graph_file, node_type=int)
    graphB = nx.read_graphml(target_graph_file, node_type=int)
    addedToGraphA, numNodesAdded = align_node_count(graphA, graphB)
    node_num = graphA.number_of_nodes()
    adjA = nx.adjacency_matrix(graphA, nodelist = range(graphA.number_of_nodes()) ).todense().astype(float)
    adjB = nx.adjacency_matrix(graphB, nodelist = range(graphB.number_of_nodes()) ).todense().astype(float)
    split_idx = adjA.shape[0]
    assert(graphA.number_of_nodes() == graphB.number_of_nodes())
    print(f"shape of adjA {adjA.shape} and adjB {adjB.shape}")
    # print statistics data
    print("---------------")
    print(f"The number of nodes in a single graph is {node_num}")
    print(f"The number of edges in a the graph A is {nx.from_numpy_matrix(adjA).number_of_edges()}")
    print(f"The number of edges in a the graph B is {nx.from_numpy_matrix(adjB).number_of_edges()}")
    print("---------------")

    if (align_method == "gwl"):
        # parse the data to be gwl readable format
        print("Parse the data to be gwl readable format")
        data_gwl = {}
        data_gwl['src_index'] = {}
        data_gwl['tar_index'] = {}
        data_gwl['src_interactions'] = []
        data_gwl['tar_interactions'] = []
        data_gwl['mutual_interactions'] = []
        for i in range(adjA.shape[0]):
            data_gwl['src_index'][float(i)] = i
        for i in range(adjB.shape[0]):
            data_gwl['tar_index'][float(i)] = i
        ma,mb = adjA.nonzero()
        for i in range(ma.shape[0]):
            data_gwl['src_interactions'].append([ma[i], mb[i]])
        ma,mb = adjB.nonzero()
        for i in range(ma.shape[0]):
            data_gwl['tar_interactions'].append([ma[i], mb[i]])

    ##################### Proprocess if needed ######################################
    if (embmethod == "xnetMF"):
        print("Generating xnetMF embeddings for REGAL")
        adj = block_diag(adjA, adjB)
        graph = Graph(adj, node_attributes = attributes)
        max_layer = untillayer
        if untillayer == 0:
            max_layer = None
        #if buckets == 1:
        #    buckets = None
        rep_method = RepMethod(max_layer = max_layer, alpha = alpha, k = k, num_buckets = buckets, #BASE OF LOG FOR LOG SCALE
                               normalize = True, gammastruc = gammastruc, gammaattr = gammaattr)
        if max_layer is None:
            max_layer = 1000
        print("Learning representations with max layer %d and alpha = %f" % (max_layer, alpha))
        embed = xnetmf.get_representations(graph, rep_method)
        # if (args.store_emb):
        #     np.save(args.embeddingA, embed, allow_pickle=False)
        #     np.save(args.embeddingB, embed, allow_pickle=False)
    elif (embmethod == "gwl"):
        # parse the data to be gwl readable format
        print("Parse the data to be gwl readable format")
        data_gwl = {}
        data_gwl['src_index'] = {}
        data_gwl['tar_index'] = {}
        data_gwl['src_interactions'] = []
        data_gwl['tar_interactions'] = []
        data_gwl['mutual_interactions'] = []
        for i in range(adjA.shape[0]):
            data_gwl['src_index'][float(i)] = i
        for i in range(adjB.shape[0]):
            data_gwl['tar_index'][float(i)] = i
        ma,mb = adjA.nonzero()
        for i in range(ma.shape[0]):
            data_gwl['src_interactions'].append([ma[i], mb[i]])
        ma,mb = adjB.nonzero()
        for i in range(ma.shape[0]):
            data_gwl['tar_interactions'].append([ma[i], mb[i]])
        after_emb = time.time()
    else:
        print("No preprocessing needed for FINAL")
        after_emb = time.time()

    ##################### Alignment ######################################
    before_align = time.time()
    # step2 and 3: align embedding spaces and match nodes with similar embeddings
    if align_method == 'REGAL':
        emb1, emb2 = regal_utils.get_embeddings(embed, graph_split_idx=split_idx)
        alignment_matrix = regal_utils.get_embedding_similarities(emb1, emb2, num_top = None)
    elif align_method == 'IsoRank':
        encoder = IsoRank(adjA, adjB)
        alignment_matrix = encoder.align()
    elif align_method == 'BigAlign':
        encoder = BigAlign(adjA, adjB)
        alignment_matrix = encoder.align()
    elif align_method == 'CONE':
        encoder = CONE(adjA, adjB)
        alignment_matrix = encoder.align()
    elif align_method == 'Grampa':
        encoder = Grampa(adjA, adjB)
        alignment_matrix = encoder.align()
    elif align_method == 'NSD':
        encoder = NSD(adjA, adjB)
        alignment_matrix = encoder.align()
    elif align_method == 'LREA':
        encoder = LREA(adjA, adjB)
        alignment_matrix = encoder.align()
    elif align_method == 'Grasp':
        encoder = Grasp(adjA, adjB)
        alignment_matrix = encoder.align()

    elif align_method == "gwl":
        result_folder = 'gwl_test'
        try:
            os.mkdir(result_folder)
        except:
            pass
        cost_type = ['cosine']
        method = ['proximal']
        opt_dict = {'epochs': 30,
                    'batch_size': 57000,
                    'use_cuda': False,
                    'strategy': 'soft',
                    'beta': 1e-2,
                    'outer_iteration': 200,
                    'inner_iteration': 1,
                    'sgd_iteration': 500,
                    'prior': False,
                    'prefix': result_folder,
                    'display': False}
        for m in method:
            for c in cost_type:
                hyperpara_dict = {'src_number': len(data_gwl['src_index']),
                                  'tar_number': len(data_gwl['tar_index']),
                                  'dimension': 256,
                                  'loss_type': 'L2',
                                  'cost_type': c,
                                  'ot_method': m}
                gwd_model = gwl_model.GromovWassersteinLearning(hyperpara_dict)

                # initialize optimizer
                optimizer = optim.Adam(gwd_model.gwl_model.parameters(), lr=1e-2)
                scheduler = lr_scheduler.ExponentialLR(optimizer, gamma=0.8)

                # Gromov-Wasserstein learning
                gwd_model.train_without_prior(data_gwl, optimizer, opt_dict, scheduler=None)
                # save model
                gwd_model.save_model('{}/model_{}_{}.pt'.format(result_folder, m, c))
                gwd_model.save_recommend('{}/result_{}_{}.pkl'.format(result_folder, m, c))
                alignment_matrix = gwd_model.trans

    alignment_matrix = remove_added_nodes(alignment_matrix, addedToGraphA, numNodesAdded)

    return alignment_matrix_to_string(alignment_matrix)

if __name__ == "__main__":
    match("author_author_source", "author_author_target")