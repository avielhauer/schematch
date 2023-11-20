import warnings
from itertools import combinations

import pandas as pd
from fastai.data.transforms import Normalize
from fastai.metrics import accuracy, Precision, Recall
from fastai.tabular.core import FillMissing, Categorify
from fastai.tabular.data import TabularDataLoaders
from fastai.tabular.learner import tabular_learner

warnings.filterwarnings('ignore', category=UserWarning, message="'has_mps' is deprecated")

USED_INFO_TYPES = ["values", "names", "both"]
FEATURES_SUBSETS = ["embeddings", "no-embeddings", "both"]
DATASETS = ["cameras", "headphones", "phones", "tvs"]
DATA_DIRECTORY = "final_features"
BS = 32
LR = 1e-3
procs = [FillMissing, Categorify, Normalize]
metrics = [accuracy, Precision(), Recall()]
comb1 = list(combinations(DATASETS, 1))
comb2 = list(combinations(DATASETS, 2))
comb3 = list(combinations(DATASETS, 3))
training_combinations = comb1+comb2+comb3
dataframes = dict()
for dataset in DATASETS:
    dataframes[dataset] = pd.read_csv(f'{DATA_DIRECTORY}/{dataset}.csv', on_bad_lines="warn")
    dataframes[dataset]["dataset"] = dataset

if __name__ == '__main__':
    table = pd.concat([dataframes[dataset] for dataset in DATASETS])
    table["related"] = ["POS" if x == 1.0 else "NEG" for x in table["related"]]
    table = table.fillna(0)
    iteration = 0
    for training_datasets in [DATASETS[:3]]:
        for used_info in USED_INFO_TYPES:
            for features_subset in FEATURES_SUBSETS:
                iteration += 1
                dep_var = "related"
                contin = []

                for feature in table.columns.values:
                    if(not feature==dep_var and not feature.startswith("label") and not feature.startswith("target") and not feature.startswith("source") and not feature.startswith("dataset")):
                        if(used_info == "names"):
                            if((features_subset == "no-embeddings" or features_subset == "both") and feature.startswith("name-")):
                                contin.append(feature)
                            if((features_subset == "embeddings" or features_subset == "both") and feature.startswith("NameG")):
                                contin.append(feature)
                        if(used_info == "values"):
                            if((features_subset == "no-embeddings" or features_subset == "both") and not feature.startswith("name-") and not feature.startswith("NameG") and not feature.startswith("Glove")):
                                contin.append(feature)
                            if((features_subset == "embeddings" or features_subset == "both") and not feature.startswith("name-") and not feature.startswith("NameG") and feature.startswith("Glove")):
                                contin.append(feature)
                        if(used_info == "both"):
                            if((features_subset == "no-embeddings" or features_subset == "both") and not feature.startswith("NameG") and not feature.startswith("Glove")):
                                contin.append(feature)
                            if((features_subset == "embeddings" or features_subset == "both") and (feature.startswith("NameG") or feature.startswith("Glove"))):
                                contin.append(feature)

                for v in contin:
                    table[v] = table[v].astype("float")

                train_idx = []
                val_idx = []
                for i, j in list(table.iterrows()):
                    is_training = j["dataset"] in training_datasets
                    if(is_training):
                        train_idx.append(i)
                    is_validation = (not is_training)
                    if(is_validation):
                        val_idx.append(i)
                if(len(train_idx) > BS):
                    bs = BS
                else:
                    bs = len(train_idx)
                data = TabularDataLoaders.from_df(
                            table, 'models', y_names=[dep_var],
                            valid_idx=val_idx, procs=procs, cat_names=[],
                            cont_names=contin, bs=bs)
                print(f"Iteration {iteration}/9: Training model for info {used_info} and features {features_subset}")
                model = tabular_learner(data, layers=[128, 64])
                model.metrics = metrics
                model.fit(10, lr=LR)
                model.fit(5, lr=LR*0.1)
                model.fit(5, lr=LR*0.01)

                model.export(f"transfer_learning___info_{used_info}_features_{features_subset}")
