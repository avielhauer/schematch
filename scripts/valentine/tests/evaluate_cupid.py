import os
import time

import pandas as pd

from valentine.algorithms import Cupid
from valentine.data_sources import DataframeTable

class TestAlgorithms():

    def test_cupid(self):
        startzeit = time.time()
        #Pfad ändern
        path = 'schematch_data'
        big_true_positive_counter = 0
        big_false_positive_counter = 0
        all_groundtruths = 0
        for master_data_set in os.listdir(path):
            first_level_path = os.path.join(path, master_data_set)
            debugger = os.listdir(first_level_path)
            #debugger.pop(0)
            for slave_data_set in debugger:
                second_level_path = os.path.join(first_level_path, slave_data_set)
                list_target_dataframe_tables = []
                list_source_dataframe_tables = []
                for dic in os.listdir(second_level_path):


                    if dic == "ground_truth":

                        ground_truth_dict = dict()
                        ground_truths = os.path.join(second_level_path, dic)
                        for file in os.listdir(ground_truths):
                            ground_truth_dict[str(file)] = []
                            with open(os.path.join(ground_truths, file), 'r') as datei:
                                for zeile in datei:
                                    ground_truth_dict[str(file)].append(zeile.strip())

                    if dic == "target":

                        targets = os.path.join(second_level_path, dic)
                        for file in os.listdir(targets):
                            file_path = os.path.join(targets, file)
                            list_target_dataframe_tables.append(DataframeTable(pd.read_csv(file_path), name=str(file)+"t"))

                    if dic == "source":

                        sources = os.path.join(second_level_path, dic)
                        for file in os.listdir(sources):
                            file_path = os.path.join(sources, file)
                            list_source_dataframe_tables.append(DataframeTable(pd.read_csv(file_path), name=str(file)+"s"))

                print(second_level_path)
                true_positive_counter = 0
                false_positive_counter = 0
                ground_truth_counter = 0
                for ground_truth_file in ground_truth_dict:
                    truths = 0
                    for part in ground_truth_dict[ground_truth_file]:
                        truths += part.count('1')
                    ground_truth_counter += truths
                all_groundtruths += ground_truth_counter

                for source in list_source_dataframe_tables:
                    for target in list_target_dataframe_tables:
                        cu_matcher = Cupid()
                        matches_cu_matcher = cu_matcher.get_matches(source, target)
                        #print(source.name.split(".")[0]+'___'+target.name)
                        if matches_cu_matcher is not {}:
                            try:
                                ground_truths = ground_truth_dict[source.name[:-1].split(".")[0]+'___'+target.name[:-1]]
                            except:
                                ground_truths = "zero"
                            #Fall wenn or noch betrachten
                            if (ground_truths != "zero" and matches_cu_matcher != {}):
                                #print(ground_truths)
                                #print(matches_cu_matcher)
                                # source ist zeilen
                                # target ist spalten
                                for match in matches_cu_matcher:
                                    source_columns = list(source.get_df().columns.values)
                                    target_columns = list(target.get_df().columns.values)
                                    source_match_column = match[0][1]
                                    target_match_column = match[1][1]
                                    source_match_column_index = source_columns.index(source_match_column)
                                    #print(target_columns)
                                    target_match_column_index = target_columns.index(target_match_column)
                                    #print(source_match_column_index)
                                    #print(target_match_column_index)
                                    if ground_truths[source_match_column_index].split(",")[target_match_column_index] == "1":
                                        true_positive_counter += 1
                                    else:
                                        false_positive_counter += 1

                            if (ground_truths == "zero" and matches_cu_matcher != {}):
                                #print(matches_cu_matcher)
                                for match in matches_cu_matcher:
                                    false_positive_counter += 1

                big_false_positive_counter += false_positive_counter
                big_true_positive_counter += true_positive_counter
                print('true_positive_counter:',true_positive_counter)
                print('false_positive_counter:',false_positive_counter)
                print('ground_truth_counter:', ground_truth_counter)
                try:
                    precision = (true_positive_counter / (true_positive_counter + false_positive_counter))
                    recall = (true_positive_counter/ground_truth_counter)
                    print('precision:',precision)
                    print('recall:',recall)
                    print('f1-score:',((2*precision*recall) / (precision + recall)))
                except:
                    print('precision: 1')
                print("_______")
        endzeit = time.time()
        ausfuehrungszeit = endzeit - startzeit
        print('Ausführungszeit:', ausfuehrungszeit)
        print('true_positive_counter:', big_true_positive_counter)
        print('false_positive_counter:', big_false_positive_counter)
        print('ground_truth_counter:', all_groundtruths)
        precision = (big_true_positive_counter / (big_true_positive_counter + big_false_positive_counter))
        recall = (big_true_positive_counter / all_groundtruths)
        print('precision:', precision)
        print('recall:', recall)
        print('f1-score:', ((2 * precision * recall) / (precision + recall)))