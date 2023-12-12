'''
Uses NATool scripts to generate alignment matrices of given graph(s).
Code mainly taken and modified from: https://github.com/wyy-code/NATool/blob/main/main.py
'''

import sys
import numpy as np
import networkx as nx
import time
from encoder.REGAL.xnetmf_config import *
from scipy.linalg import block_diag
import encoder.REGAL.xnetmf as xnetmf
import encoder.REGAL.regal_utils as regal_utils
from encoder.FINAL.FINAL import FINAL
from encoder.CONE.CONE import CONE
from encoder.Grampa.Grampa import Grampa
from encoder.IsoRank.IsoRank import IsoRank
from encoder.BigAlign.BigAlign import BigAlign
from encoder.NSD.NSD import NSD
from encoder.LREA.LREA import LREA
from encoder.Grasp.Grasp import Grasp
from encoder.gwl import gwl_model
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
def match(graph_file, alignmethod="REGAL", embmethod="xnetMF"):

    ##################### Load data ######################################
    # running normal graph alignment methods
    combined_graph_name = graph_file
    graph = nx.read_graphml(combined_graph_name, node_type=int)
    #graph = nx.read_edgelist(combined_graph_name, nodetype=int, comments="%")
    adj = nx.adjacency_matrix(graph, nodelist = range(graph.number_of_nodes()) ).todense().astype(float)
    node_num = int(adj.shape[0] / 2)
    adjA = np.array(adj[:node_num, :node_num])
    split_idx = adjA.shape[0]
    adjB = np.array(adj[node_num:, node_num:])

    # print statistics data
    print("---------------")
    print(f"The number of nodes in a single graph is {node_num}")
    print(f"The number of edges in a the graph A is {nx.from_numpy_matrix(adjA).number_of_edges()}")
    print(f"The number of edges in a the graph B is {nx.from_numpy_matrix(adjB).number_of_edges()}")
    print("---------------")

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
    if alignmethod == 'REGAL':
        emb1, emb2 = regal_utils.get_embeddings(embed, graph_split_idx=split_idx)
        alignment_matrix = regal_utils.get_embedding_similarities(emb1, emb2, num_top = None)
    elif alignmethod == 'FINAL':
        encoder = FINAL(adjA, adjB)
        alignment_matrix = encoder.align()
    elif alignmethod == 'IsoRank':
        encoder = IsoRank(adjA, adjB)
        alignment_matrix = encoder.align()
    elif alignmethod == 'BigAlign':
        encoder = BigAlign(adjA, adjB)
        alignment_matrix = encoder.align()
    elif alignmethod == 'CONE':
        encoder = CONE(adjA, adjB)
        alignment_matrix = encoder.align()
    elif alignmethod == 'Grampa':
        encoder = Grampa(adjA, adjB)
        alignment_matrix = encoder.align()
    elif alignmethod == 'NSD':
        encoder = NSD(adjA, adjB)
        alignment_matrix = encoder.align()
    elif alignmethod == 'LREA':
        encoder = LREA(adjA, adjB)
        alignment_matrix = encoder.align()
    elif alignmethod == 'Grasp':
        encoder = Grasp(adjA, adjB)
        alignment_matrix = encoder.align()

    elif alignmethod == "gwl":
        result_folder = 'gwl_test'
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

    print(alignment_matrix.shape, file=sys.stderr)
    return "\n".join([
                        " ".join(
                            [str(x) for x in row]
                        )
                        for row in alignment_matrix
                    ]
    )

if __name__ == "__main__":
    match("test")