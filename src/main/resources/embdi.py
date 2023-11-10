'''
Uses EmbDI scripts to generate Schema matchings of given data.
Code mainly taken and modified from: https://gitlab.eurecom.fr/cappuzzo/embdi
'''

import sys
import os


# We use stdout to communicate with java library and therefore don't want libraries to mess
# that up.
OLD_STDOUT = sys.stdout
sys.stdout = open(os.devnull, 'w')
sys.stderr = open(os.devnull, 'w')


try:
    import argparse
    import pickle
    import datetime
    import pandas as pd
    import gensim.models as models
    from numpy import dot
    from operator import itemgetter
    from gensim import matutils
    from pathlib import Path
    from EmbDI.embeddings import learn_embeddings
    from EmbDI.graph import graph_generation
    import EmbDI.utils as eutils
    from EmbDI.data_preprocessing import data_preprocessing, write_info_file, get_unique_string_values
    from EmbDI.edgelist import EdgeList
    from EmbDI.sentence_generation_strategies import random_walks_generation
    from EmbDI.utils import (TIME_FORMAT, read_edgelist)
    from EmbDI.schema_matching import _produce_match_results
except Exception as e:
    print(e)
    sys.exit(2)

PREFIXES = ["3#__tn", "3$__tt", "5$__idx", "1$__cid"]

# default parameters for embdi
PARAMS = {
    'ntop': 10,
    'ncand': 1,
    'max_rank': 3,
    'follow_sub': False,
    'smoothing_method': 'no',
    'backtrack': True,
    'training_algorithm': 'word2vec',
    'write_walks': True,
    'flatten': 'all',
    'indexing': 'basic',
    'epsilon': 0.1,
    'num_trees': 250,
    'compression': False,
    'n_sentences': 'default',
    'walks_strategy': 'basic',
    'learning_method': 'skipgram',
    'sentence_length': 60,
    'window_size': '3',
    'n_dimensions': '300',
    'experiment_type': 'SM',
    'intersection': False,
    'walks_file': None,
    'mlflow': False,
    'repl_numbers': False,
    'repl_strings': False,
    'sampling_factor': 0.001,
    'output_file': 'small_example',
    'concatenate': 'horizon',
    'missing_value': 'nan,ukn,none,unknown,',
    'missing_value_strategy': '',
    'round_number': 0,
    'round_columns': 'price',
    'auto_merge': False,
    'tokenize_shared': True,
    'run-tag': 'something_random',
    'follow_replacement': False
}

def read_csv(file_path: str) -> pd.DataFrame:
    df = pd.read_csv(file_path)
    return df

def write_csv(df: pd.DataFrame, file_path: str):
    Path(file_path).parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(file_path, index=False)


def embeddings_generation(walks, dictionary, embeddings_file_name):
    """
    Take the generated walks and train embeddings using the walks as training corpus.
    :param walks:
    :param dictionary:
    :return:
    """
    learn_embeddings(embeddings_file_name, walks, write_walks=PARAMS['write_walks'],
                     dimensions=int(PARAMS['n_dimensions']),
                     window_size=int(PARAMS['window_size']))

    if PARAMS['compression']:
        newf = eutils.clean_embeddings_file(embeddings_file_name, dictionary)
    else:
        newf = embeddings_file_name
    PARAMS['embeddings_file'] = newf

    return PARAMS

def generate_similarity_matrix(wv, dataset, source_columns, target_columns):
    similarity_matrix = [[0.0 for _ in target_columns] for __ in source_columns]
    for i, source_column in enumerate(source_columns):
        for j, target_column in enumerate(target_columns):
            source_embedding_string, target_embedding_string = f"cid__{source_column}", f"cid__{target_column}"
            if source_embedding_string in wv and target_embedding_string in wv:
                similarity_matrix[i][j] = dot(matutils.unitvec(wv[source_embedding_string]), matutils.unitvec(wv[target_embedding_string]))

    return similarity_matrix

def schema_matching(embeddings_file, dataset, source_columns, target_columns):

    # clean embeddings keeps only embeddings of columns that are actually in the ground truth, to save some memory.
    # We could look for all existing/required keys, but so far simply loading all embeddings was no issue.
    #emb_file = _clean_embeddings(embeddings_file, {c: [c] for c in dataset.columns})

    wv = models.KeyedVectors.load_word2vec_format(embeddings_file, unicode_errors='ignore')

    return generate_similarity_matrix(wv, dataset, source_columns, target_columns)

def parse_args():
    """Simple argument parser invoked on startup.

    Returns:
        Namespace: Argument parser
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('-i_1', '--input_1', action='store', default=None, required=False)
    parser.add_argument('-i_2', '--input_2', action='store', default=None, required=False)
    built_args = parser.parse_args()
    return built_args

def prepare_csv(df_1, df_2):
    # overrides default values to schema matching values
    # might need adjustment for new datasets
    return data_preprocessing([df_1, df_2], PARAMS)

def generate_edgelist(df, info_file):
    return EdgeList(df, EDGELIST_FP, PREFIXES, info_file, flatten=True)

def generate_random_walks():
    prefixes, edgelist = read_edgelist(EDGELIST_FP)
    graph = graph_generation(PARAMS, edgelist, prefixes, dictionary=None)
    #  Compute the number of sentences according to the rule of thumb.
    if PARAMS['n_sentences'] == 'default':
        PARAMS['n_sentences'] = graph.compute_n_sentences(int(PARAMS['sentence_length']))
    PARAMS["write_walks"] = False
    walks = random_walks_generation(PARAMS, graph)

    return walks

def table_identifier(csv_path):
    path_parts = os.path.normpath(csv_path).split(os.path.sep)
    table = path_parts[-1][:-4]
    dataset = path_parts[-3]
    return f"{dataset}_{table}"

def filter_embeddings(embeddings_path):
    with open(embeddings_path, "r") as emb_f:
        unfiltered = emb_f.readlines()
    dimension = unfiltered[0].split(" ")[1].strip("\n")
    filtered = [line for line in unfiltered if line.startswith("cid__")]
    with open(embeddings_path, "w") as emb_f:
        emb_f.write(f"{len(filtered)} {dimension}\n")
        emb_f.writelines(filtered)

if __name__ == "__main__":
    args = parse_args()

    INFO_FILE_FP = f"target/embdi_cache/info_file.csv"
    PREPROCESSED_FP = f"target/embdi_cache/{table_identifier(args.input_1)}-{table_identifier(args.input_2)}_preprocessed.csv"
    EDGELIST_FP = f"target/embdi_cache/edgelist"
    EMBEDDINGS_FP = f"target/embdi_cache/{table_identifier(args.input_1)}-{table_identifier(args.input_2)}_embeddings"

    df_1 = read_csv(args.input_1)
    df_2 = read_csv(args.input_2)

    PARAMS["expand_columns"] = ','.join(list(set(list(df_1.columns) + list(df_2.columns))))
    preprocessed = prepare_csv(df_1, df_2)
    Path(INFO_FILE_FP).parent.mkdir(parents=True, exist_ok=True)
    write_info_file([df_1 ,df_2], INFO_FILE_FP, [args.input_1, args.input_2])
    if not os.path.exists(EMBEDDINGS_FP):
        edgelist = generate_edgelist(preprocessed, INFO_FILE_FP)
        walks = generate_random_walks()
        embeddings_generation(walks, None, EMBEDDINGS_FP)
        filter_embeddings(EMBEDDINGS_FP)
    matchings = schema_matching(EMBEDDINGS_FP, preprocessed, list(df_1.columns), list(df_2.columns))

    sys.stdout = OLD_STDOUT
    for col in matchings:
        print(" ".join([str(x) for x in col]))