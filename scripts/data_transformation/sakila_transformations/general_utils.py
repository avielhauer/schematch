import os
import pandas as pd
import numpy as np
from pathlib import Path
from typing import List, Tuple


def get_scenario_tables(data_dir: str):
    files = [os.path.join(data_dir, f) for f in os.listdir(data_dir) if
             os.path.isfile(os.path.join(data_dir, f)) and f.endswith('.csv')]
    tables = []
    for file in files:
        file_name = file.split(os.sep)[-1].replace('.csv', '')
        data = pd.read_csv(file)
        tables.append((file_name, data))
    return tables


def save_tables(save_dir: str, tables: List[Tuple]):
    if not os.path.exists(save_dir):
        Path(save_dir).mkdir(parents=True, exist_ok=True)

    for table_name, table in tables:
        table.to_csv(os.path.join(save_dir, f'{table_name}.csv'), index=False)


def get_scenario_gt(data_dir: str):
    gt_path = os.path.join(data_dir, 'gt.csv')
    if os.path.exists(gt_path):
        return pd.read_csv(gt_path)
    return None


def create_mappings(source_tables: List[Tuple], target_tables: List[Tuple]):
    assert len(source_tables) == len(target_tables)

    mappings = []
    for source_table_item, target_table_item in zip(source_tables, target_tables):
        source_table_name = source_table_item[0]
        source_attribute_names = source_table_item[1].columns
        target_table_name = target_table_item[0]
        target_attribute_names = target_table_item[1].columns
        mappings += [(f'{source_table_name}.{x}', f'{target_table_name}.{y}') for x, y in
                     zip(source_attribute_names, target_attribute_names)]

    mappings = pd.DataFrame(data=mappings, columns=['schema1', 'schema2'])

    return mappings


def translate_db_schema(source_scenario_dir: str, language_map: dict, target_scenario_dir: str = None,
                        out_gt_path: str = None):
    # Get the tables from the source scenario
    tables = get_scenario_tables(os.path.join(source_scenario_dir, 'tables'))

    # Get the ground truth from the source scenario (if any)
    gt = get_scenario_gt(source_scenario_dir)

    # Translate table and attributes names with the language map
    out_tables = []
    for table_name, table in tables:
        out_table_name = language_map['tables'][table_name]
        out_attributes = [language_map['attributes'][x] for x in table.columns]
        out_table = table.copy()
        out_table.columns = out_attributes
        out_tables.append((out_table_name, out_table))

    # Translate the ground truth
    if gt is None:
        # Create the ground truth by pairing old and new table.attribute values
        out_gt = create_mappings(tables, out_tables)
    else:
        # Translate the 'schema2' column from the ground truth
        new_mappings = []
        for mapping in gt['schema2']:
            assert len(mapping.split('.')) == 2
            table_name = mapping.split('.')[0]
            attribute_name = mapping.split('.')[1]
            new_table_name = language_map['tables'][table_name]
            new_attribute_name = language_map['attributes'][attribute_name]
            new_mappings.append(f'{new_table_name}.{new_attribute_name}')
        gt['schema2'] = new_mappings
        out_gt = gt.copy()

    if target_scenario_dir:
        save_tables(save_dir=os.path.join(target_scenario_dir, 'tables'), tables=out_tables)

        # out_gt.to_csv(os.path.join(target_scenario_dir, 'gt.csv'), index=False)
        out_gt_dir = os.sep.join(out_gt_path.split(os.sep)[:-1])
        if not os.path.exists(out_gt_dir):
            Path(out_gt_dir).mkdir(parents=True, exist_ok=True)
        np.savetxt(
            out_gt_path, out_gt.to_numpy(), fmt='%s', delimiter=' = ', encoding='utf-8'
        )

    return out_tables, out_gt
