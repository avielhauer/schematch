import os
import argparse
import pandas as pd
import numpy as np
import json


def get_schema_names(path: str):
    file_name = path.split(os.sep)[-1].replace('.txt', '')
    schema_names = file_name.split('_')
    internal_schema_names = [f'schema{i + 1}' for i in range(len(schema_names))]
    return schema_names, internal_schema_names


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Translate ground truth.',
        usage='translate_gt.py [<args>] [-h | --help]'
    )
    parser.add_argument("--gt_path", type=str, required=True)
    parser.add_argument("--out_path", type=str, required=True)
    parser.add_argument("--language_map_path", type=str, required=True)
    args = parser.parse_args()

    gt_path = args.gt_path
    out_path = args.out_path
    language_map_path = args.language_map_path

    schema_names, internal_schema_names = get_schema_names(gt_path)
    gt = pd.read_csv(gt_path, sep=' = ', names=['schema1', 'schema2'], engine='python')
    language_map = json.load(open(language_map_path, encoding='utf-8'))

    for schema_name, internal_schema_name in zip(schema_names, internal_schema_names):
        gt_copy = gt.copy()
        new_mappings = []
        for mapping in gt_copy[internal_schema_name]:
            assert len(mapping.split('.')) == 2
            table_name = mapping.split('.')[0]
            attribute_name = mapping.split('.')[1]
            new_table_name = language_map['tables'][table_name]
            new_attribute_name = language_map['attributes'][attribute_name]
            new_mappings.append(f'{new_table_name}.{new_attribute_name}')
        gt_copy[internal_schema_name] = new_mappings

        out_gt_name = gt_path.split(os.sep)[-1].replace(schema_name, f'{schema_name}-it')
        out_gt_path = os.path.join(out_path, out_gt_name)
        # gt.to_csv(out_gt_path, header=False, index=False)
        np.savetxt(
            out_gt_path, gt_copy.to_numpy(), fmt='%s', delimiter=' = ', encoding='utf-8'
        )
