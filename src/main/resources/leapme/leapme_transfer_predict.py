from __future__ import annotations

import sys
OLD_STDOUT = sys.stdout
sys.stdout = sys.stderr

import csv
import collections
import dataclasses as dc
import fastai.learner
import json
import nltk
import regex
from sklearn.exceptions import UndefinedMetricWarning
import strsimpy
import strsimpy.jaro_winkler
import numpy as np
import pandas as pd
import time
from typing import List, Dict
import warnings


warnings.filterwarnings('ignore', category=UserWarning, message="'has_mps' is deprecated")
warnings.filterwarnings('ignore', category=UndefinedMetricWarning, message='Recall is ill-defined')


def time_command(command, msg=""):
    start = time.time()
    result = command()
    end = time.time()
    print(f"{msg}: Took {end - start:.3f}s", file=sys.stderr)
    return result


class GloveEmbeddingsCalculator:
    INSTANCE: GloveEmbeddingsCalculator = None
    GLOVE_PATH: str = None

    def __init__(self):
        with open(f"{GloveEmbeddingsCalculator.GLOVE_PATH}/glove_index.json", 'r') as index_file:
            self.lookup_index = time_command(lambda: json.load(index_file), "Loading glove index")
        self.seen_words_index = {}

    @staticmethod
    def get_instance():
        assert GloveEmbeddingsCalculator.GLOVE_PATH is not None
        if GloveEmbeddingsCalculator.INSTANCE is None:
            GloveEmbeddingsCalculator.INSTANCE = GloveEmbeddingsCalculator()
        return GloveEmbeddingsCalculator.INSTANCE

    @staticmethod
    def set_path(glove_path: str):
        GloveEmbeddingsCalculator.GLOVE_PATH = glove_path
        GloveEmbeddingsCalculator.get_instance()

    def compute_embeddings(self, string: str):
        string = string.replace('_', ' ')
        num_words = 0
        tokenized_words = nltk.word_tokenize(string)
        aggregated_embedding = np.full((300,), 0.0)
        for word in tokenized_words:
            if (word_embedding := self.get_word_embedding(word.lower())) is not None:
                aggregated_embedding += word_embedding
                num_words += 1

        return aggregated_embedding / num_words if num_words > 0 else aggregated_embedding

    def get_word_embedding(self, word):
        if word not in self.lookup_index:
            return None
        if word not in self.seen_words_index:
            components = self.get_line_of_file(self.lookup_index[word][0], self.lookup_index[word][1]).split()[1:]
            self.seen_words_index[word] = np.array([float(component) for component in components])
        return self.seen_words_index[word]

    @staticmethod
    def get_line_of_file(chunk_index, line_in_chunk):
        with open(f"{GloveEmbeddingsCalculator.GLOVE_PATH}/glove.42B.300d{chunk_index}", 'r') as chunk_file:
            for i, line in enumerate(chunk_file):
                if i == line_in_chunk:
                    return line


@dc.dataclass
class Property:
    name: str
    name_glove: np.ndarray = None
    count: int = 0
    numeric_value: float = 0
    attribute_glove: np.ndarray = dc.field(default_factory=lambda: np.full((300,), 0.0))
    patterns_absolute: Dict = dc.field(default_factory=lambda: collections.defaultdict(float))
    character_patterns_frequency: Dict = dc.field(default_factory=lambda: collections.defaultdict(float))
    token_patterns_frequency: Dict = dc.field(default_factory=lambda: collections.defaultdict(float))

    IGNORE_MISSING_VALUES = True
    CHARACTER_PATTERNS = [
        regex.compile(r"\p{L}"),   # Letter
        regex.compile(r"\p{Lu}"),  # Uppercase
        regex.compile(r"\p{Ll}"),  # Lowercase
        regex.compile(r"\p{M}"),   # Combining
        regex.compile(r"\p{N}"),   # Numeric
        regex.compile(r"\p{C}"),   # Control
        regex.compile(r"\p{P}"),   # Punctuation
        regex.compile(r"\p{S}"),   # Math/Currency
        regex.compile(r"\p{Z}")    # Whitespace
    ]
    TOKEN_PATTERNS = [
        regex.compile(r"<[^<>]+>"),                                    # HTML
        regex.compile(r"\b\p{Lu}+\b"),                                 # uppercase words
        regex.compile(r"\b\p{Ll}+[^\p{Z}<>]*?\b"),                     # words starting with lowercase letter
        regex.compile(r"\b\p{Lu}+[^\p{Z}<>]*?\p{Ll}+[^\p{Z}<>]*?\b"),  # words starting with uppercase letter
        # Added useless capturing group so converted string in csv header matches the LEAPME one
        regex.compile(r"\d+([.,]?)\d*")                                # numeric strings
    ]

    def consume_row_value(self, row_value):
        if row_value != "" or not self.IGNORE_MISSING_VALUES:
            self.count += 1
            self.numeric_value += float(row_value) if row_value.isnumeric() else -1

            for compiled_pattern in self.CHARACTER_PATTERNS:
                num_occurrences = len(regex.findall(compiled_pattern, row_value))
                self.patterns_absolute[compiled_pattern.pattern] += num_occurrences
                self.character_patterns_frequency[compiled_pattern.pattern] += (
                    num_occurrences / len(row_value) if len(row_value) > 0 else 0.0
                )

            for compiled_pattern in self.TOKEN_PATTERNS:
                num_occurrences = len(regex.findall(compiled_pattern, row_value))
                self.patterns_absolute[compiled_pattern.pattern] += num_occurrences
                # Here, we are computing the frequency by dividing by the number of tokens instead of the number of
                # characters
                token_num = len(regex.split(r"\s+", row_value))
                # We can safely divide, token_num >= 1
                self.token_patterns_frequency[compiled_pattern.pattern] += num_occurrences / token_num

            value_embeddings = GloveEmbeddingsCalculator.get_instance().compute_embeddings(row_value)
            self.attribute_glove += value_embeddings

    @property
    def count_for_division(self):
        return self.count if self.count > 0 else 1


@dc.dataclass
class PropertyPair(Property):
    other_name: str = ""
    name_distance: Dict = dc.field(default_factory=dict)

    NAME_DISTANCE_MEASURES = [
        {"name": "name-osa", "compute": strsimpy.OptimalStringAlignment().distance},
        {"name": "name-lv", "compute": strsimpy.Levenshtein().distance},
        {"name": "name-dl", "compute": strsimpy.Damerau().distance},
        {"name": "name-lcs", "compute": strsimpy.LongestCommonSubsequence().distance},
        {"name": "name-qgram", "compute": strsimpy.QGram(3).distance},
        {"name": "name-cosine", "compute": strsimpy.Cosine(3).distance},
        {"name": "name-jaccard", "compute": strsimpy.Jaccard(3).distance},
        # TODO: jw produced different results for "authid", "autnm"
        {"name": "name-jw", "compute": strsimpy.jaro_winkler.JaroWinkler().distance}
    ]

    @staticmethod
    def construct_from(p1: Property, p2: Property) -> PropertyPair:
        return PropertyPair(
            name=p1.name,
            other_name=p2.name,
            name_glove=p1.name_glove - p2.name_glove,
            numeric_value=p1.numeric_value / p1.count_for_division - p2.numeric_value / p2.count_for_division,
            attribute_glove=p1.attribute_glove / p1.count_for_division - p2.attribute_glove / p2.count_for_division,
            patterns_absolute={
                pattern: p1.patterns_absolute[pattern] / p1.count_for_division
                         - p2.patterns_absolute[pattern] / p2.count_for_division
                for pattern in p1.patterns_absolute
            },
            character_patterns_frequency={
                pattern: p1.character_patterns_frequency[pattern] / p1.count_for_division
                         - p2.character_patterns_frequency[pattern] / p2.count_for_division
                for pattern in p1.character_patterns_frequency
            },
            token_patterns_frequency={
                pattern: p1.token_patterns_frequency[pattern] / p1.count_for_division
                         - p2.token_patterns_frequency[pattern] / p2.count_for_division
                for pattern in p1.token_patterns_frequency
            },
            name_distance={
                measure["name"]: measure["compute"](p1.name, p2.name)
                for measure in PropertyPair.NAME_DISTANCE_MEASURES
            }
        )

    @staticmethod
    def csv_header():
        def convert(name):
            return regex.sub(r'[^\w-]', '.', name)

        # IMPORTANT: Keep in sync with to_list regarding order!
        return ["label1", "label2"] + [f"NameGlove300.{i}-dif" for i in range(300)] + ["Numeric.value-dif"] + [
            f"GloveCrawl300.{i}-dif" for i in range(300)
        ] + [
            convert(f"Number of occurrences of pattern {compiled_pattern.pattern}-dif")
            for compiled_pattern in Property.CHARACTER_PATTERNS + Property.TOKEN_PATTERNS
        ] + [
            convert(f"Character density for pattern {compiled_pattern.pattern}-dif")
            for compiled_pattern in Property.CHARACTER_PATTERNS
        ] + [
            convert(f"Token density of pattern {compiled_pattern.pattern}-dif")
            for compiled_pattern in Property.TOKEN_PATTERNS
        ] + [
            measure["name"] for measure in PropertyPair.NAME_DISTANCE_MEASURES
        ] + [
            # Even though this field is not used for the prediction, the prediction will complain if fields that were
            # present in the dataframe during training aren't present during prediction as well
            "related"
        ]

    def to_list(self):
        # IMPORTANT: Keep in sync with csv_header regarding order!
        return [self.name, self.other_name] + list(self.name_glove) + [self.numeric_value] + list(self.attribute_glove) + [
            self.patterns_absolute[compiled_pattern.pattern]
            # Sometimes all cells of a property are empty, then we have no data here
            if compiled_pattern in self.patterns_absolute
            else 0.0
            for compiled_pattern in Property.CHARACTER_PATTERNS + Property.TOKEN_PATTERNS
        ] + [
            self.character_patterns_frequency[compiled_pattern.pattern]
            if compiled_pattern in self.character_patterns_frequency
            else 0.0
            for compiled_pattern in Property.CHARACTER_PATTERNS
        ] + [
            self.token_patterns_frequency[compiled_pattern.pattern]
            if compiled_pattern in self.token_patterns_frequency
            else 0.0
            for compiled_pattern in Property.TOKEN_PATTERNS
        ] + [
            self.name_distance[measure["name"]] for measure in PropertyPair.NAME_DISTANCE_MEASURES
        ] + ["NEG"]


class Table:
    parsed_tables = {}

    @staticmethod
    def load(path: str):
        if path not in Table.parsed_tables:
            Table.parsed_tables[path] = Table(path)
        return Table.parsed_tables[path]

    def __init__(self, path: str):
        self.path: str = path
        self.properties: List[Property] = []
        self.parse_data()

    def parse_data(self):
        with open(self.path, 'r') as table_file:
            reader = csv.reader(table_file)
            for title in next(reader):
                self.properties.append(Property(
                    title,
                    GloveEmbeddingsCalculator.get_instance().compute_embeddings(title)
                ))

            for row in reader:
                for property_index, property_value in enumerate(row):
                    self.properties[property_index].consume_row_value(property_value)

    def property_pairs(self, other_table: Table) -> List[PropertyPair]:
        pairs = []
        for p1 in self.properties:
            for p2 in other_table.properties:
                pairs.append(PropertyPair.construct_from(p1, p2))
        return pairs


def predictions_on_property_pairs(used_info_types: str, used_features: str, property_pairs: List[PropertyPair]):
    # Convert property pairs to data representation understood by the model
    dataframe = pd.DataFrame(data=np.array([pair.to_list() for pair in property_pairs]),
                             columns=PropertyPair.csv_header())
    dataframe = dataframe.astype({
        name: str if name in ["label1", "label2", "related"] else float
        for name in PropertyPair.csv_header()
    })

    # Perform the predictions
    model = fastai.learner.load_learner(
        f"models/transfer_learning___info_{used_info_types}_features_{used_features}"
    )
    test_dl = model.dls.test_dl(dataframe)
    return model.get_preds(dl=test_dl)


def write_predictions(table1_property_count: int, table2_property_count: int, predictions: tuple) -> str:
    result = ""
    for i in range(table1_property_count):
        line = ""
        for j in range(table2_property_count):
            line += str(float(predictions[0][i * table2_property_count + j][1]))
            line += " "
        result += line + "\n"
    return result


def predict(used_info_types: str, used_features: str, table1_path: str, table2_path: str) -> str:
    table1 = time_command(lambda: Table.load(table1_path), "Loading table 1")
    table2 = time_command(lambda: Table.load(table2_path), "Loading table 2")

    property_pairs = time_command(lambda: table1.property_pairs(table2), "Computing property pairs")
    predictions = time_command(lambda: predictions_on_property_pairs(used_info_types, used_features, property_pairs), "Making predictions")
    return write_predictions(len(table1.properties), len(table2.properties), predictions)
