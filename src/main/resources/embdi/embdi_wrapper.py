'''
Uses EmbDI scripts to generate Schema matchings of given data.
Code mainly taken and modified from: https://gitlab.eurecom.fr/cappuzzo/embdi
'''

import os
import sys

import argparse
import pandas as pd
import gensim.models as models
from numpy import dot
from gensim import matutils
from pathlib import Path
from EmbDI.embeddings import learn_embeddings
from EmbDI.graph import graph_generation
import EmbDI.utils as eutils
from EmbDI.data_preprocessing import data_preprocessing, write_info_file, get_unique_string_values
from EmbDI.edgelist import EdgeList
from EmbDI.sentence_generation_strategies import random_walks_generation
from EmbDI.utils import (TIME_FORMAT, read_edgelist)
from EmbDI.schema_matching import _produce_match_results , _extract_candidates

DATA_DIRECTORY_MOUNT = "/embdi/data/"
CACHE_DIRECTORY_MOUNT = "/embdi/cache"
INFO_FILE_FP = f"{CACHE_DIRECTORY_MOUNT}/info_file.csv"
EDGELIST_FP = f"{CACHE_DIRECTORY_MOUNT}/edgelist"
PREPROCESSED_FP = lambda input_1, input_2: f"{CACHE_DIRECTORY_MOUNT}/{table_identifier(input_1)}-{table_identifier(input_2)}_preprocessed.csv"
EMBEDDINGS_FP = lambda input_1, input_2: f"{CACHE_DIRECTORY_MOUNT}/{table_identifier(input_1)}-{table_identifier(input_2)}_embeddings"

PREFIXES = ["3#__tn", "3$__tt", "5$__idx", "1$__cid"]

# default parameters for embdi
DEFAULT_PARAMS = {
    'ntop': 10,
    'ncand': 1,
    'max_rank': 3,
    'follow_sub': False,
    'smoothing_method': 'no',
    'backtrack': True,
    'training_algorithm': 'word2vec',
    'write_walks': True,
    'flatten': 'tt',
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
    'tokenize_shared': False,
    'run-tag': 'something_random',
    'follow_replacement': False
}

def read_csv(file_path: str) -> pd.DataFrame:
    df = pd.read_csv(file_path)
    return df

def write_csv(df: pd.DataFrame, file_path: str):
    Path(file_path).parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(file_path, index=False)


def embeddings_generation(walks, dictionary, embeddings_file_name, params):
    """
    Take the generated walks and train embeddings using the walks as training corpus.
    :param walks:
    :param dictionary:
    :return:
    """
    learn_embeddings(embeddings_file_name, walks, write_walks=params['write_walks'],
                     dimensions=int(params['n_dimensions']),
                     window_size=int(params['window_size']))

    if params['compression']:
        newf = eutils.clean_embeddings_file(embeddings_file_name, dictionary)
    else:
        newf = embeddings_file_name
    params['embeddings_file'] = newf

    return params

def dot_product_similarity_matrix(wv, dataset, source_columns, target_columns):
    similarity_matrix = [[0.0 for _ in target_columns] for __ in source_columns]
    for i, source_column in enumerate(source_columns):
        for j, target_column in enumerate(target_columns):
            source_embedding_string, target_embedding_string = f"cid__{source_column}", f"cid__{target_column}"
            if source_embedding_string in wv and target_embedding_string in wv:
                similarity_matrix[i][j] = dot(matutils.unitvec(wv[source_embedding_string]), matutils.unitvec(wv[target_embedding_string]))

    return similarity_matrix

def binary_similarity_matrix_from_embdi(wv, dataset, source_columns, target_columns):
    candidates = _extract_candidates(wv, dataset)
    match_results = _produce_match_results(candidates)
    sm = [[0.0 for _ in target_columns] for __ in source_columns]

    i_emb_col_names = list(enumerate([f"0_{col}" for col in source_columns])) + list(enumerate([f"1_{col}" for col in target_columns]))

    lookup = {col: i for i,col in i_emb_col_names}

    for match_result in match_results:
        sm[lookup[match_result[0]]][lookup[match_result[1]]] = 1.0

    return sm


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

def prepare_csv(df_1, df_2, params):
    # overrides default values to schema matching values
    # might need adjustment for new datasets
    return data_preprocessing([df_1, df_2], params)

def generate_edgelist(df, info_file):
    return EdgeList(df, EDGELIST_FP, PREFIXES, info_file, flatten=True)

def generate_random_walks(params):
    prefixes, edgelist = read_edgelist(EDGELIST_FP)
    graph = graph_generation(params, edgelist, prefixes, dictionary=None)
    #  Compute the number of sentences according to the rule of thumb.
    if params['n_sentences'] == 'default':
        params['n_sentences'] = graph.compute_n_sentences(int(params['sentence_length']))
    params["write_walks"] = False
    walks = random_walks_generation(params, graph)

    return walks

def table_identifier(csv_path):
    #path_parts = os.path.normpath(csv_path).split(os.path.sep)
    #table = path_parts[-1][:-4]
    #dataset = path_parts[-3]
    #return f"{dataset}_{table}"
    return csv_path

def filter_embeddings(embeddings_path):
    with open(embeddings_path, "r") as emb_f:
        unfiltered = emb_f.readlines()
    dimension = unfiltered[0].split(" ")[1].strip("\n")
    filtered = [line for line in unfiltered if line.startswith("cid__")]
    # also add embedding without the cid__:
    non_prefixed = [line[5:] for line in filtered]
    with open(embeddings_path, "w") as emb_f:
        emb_f.write(f"{len(filtered)+ len(non_prefixed)} {dimension}\n")
        emb_f.writelines(filtered)
        emb_f.writelines(non_prefixed)


def read_variables_file(var_file):
    variables = {}
    with open(var_file, 'r') as fp:
        for i, line in enumerate(fp):
            parameter, values = line.strip().split(':', maxsplit=1)
            variables[parameter] = values.split(',')
    for default_var in default_values:
        if default_var not in variables or variables[default_var][0] == '':
            variables[default_var] = [default_values[default_var]]
    return variables

def update_params(scenario_path, params):
    try:
        config = read_variables_file(f"/configs/config-{scenario_path.split('/')[-1]}-sm")
        for k,v in config.items():
            params[k] = v
        return params
    except:
        return params


def import_database(database_folder):
    dfs = []
    for file in os.listdir(database_folder):
        if file.endswith(".csv"):
            dfs.append(read_csv(os.path.join(database_folder, file)))
    return pd.concat(dfs, axis=1)
def import_scenario(scenario_path):
    source_df = import_database(os.path.join(scenario_path, "source"))
    target_df = import_database(os.path.join(scenario_path, "target"))
    return source_df, target_df

def match(scenario_name, scenario_path, similarity_matrix_generation_method="dot_product_similarity"):

    execution_specific_params = update_params(scenario_path, DEFAULT_PARAMS.copy())

    df_1 ,df_2 = import_scenario(scenario_path)
    input_1 = scenario_name + "_source"
    input_2 = scenario_name + "_target"

    execution_specific_params["expand_columns"] = ','.join(list(set(list(df_1.columns) + list(df_2.columns))))
    preprocessed = prepare_csv(df_1, df_2, execution_specific_params)
    Path(INFO_FILE_FP).parent.mkdir(parents=True, exist_ok=True)
    write_info_file([df_1, df_2], INFO_FILE_FP, [input_1, input_2])
    if not os.path.exists(EMBEDDINGS_FP(input_1, input_2)):
        edgelist = generate_edgelist(preprocessed, INFO_FILE_FP)
        walks = generate_random_walks(execution_specific_params)
        embeddings_generation(walks, None, EMBEDDINGS_FP(input_1, input_2), execution_specific_params)
        filter_embeddings(EMBEDDINGS_FP(input_1, input_2))

    wv = models.KeyedVectors.load_word2vec_format(EMBEDDINGS_FP(input_1, input_2), unicode_errors='ignore')

    if similarity_matrix_generation_method == "dot_product_similarity":
        similarity_matrix = dot_product_similarity_matrix(wv, preprocessed, list(df_1.columns), list(df_2.columns))
    elif similarity_matrix_generation_method == "binary_from_embdi":
        similarity_matrix = binary_similarity_matrix_from_embdi(wv, preprocessed, list(df_1.columns), list(df_2.columns))

    return "\n".join([
                        " ".join(
                            [str(x) for x in column]
                        )
                        for column in similarity_matrix
                    ]
    )


if __name__ == "__main__":
    args = parse_args()
    print(match(args.input_1, args.input_2))

