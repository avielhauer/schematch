import os
import argparse
import json

from general_utils import translate_db_schema


def get_language_map(path: str):
    return json.load(open(path, encoding='utf-8'))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Translate scenario.',
        usage='translate_schema.py [<args>] [-h | --help]'
    )
    parser.add_argument("--schema_dir", type=str, required=True)
    parser.add_argument("--language", type=str, required=True)
    parser.add_argument("--language_map_path", type=str, required=True)
    parser.add_argument("--out_dir", type=str, required=True)
    parser.add_argument("--out_gt_dir", type=str, required=True)
    args = parser.parse_args()

    schema_dir = args.schema_dir
    schema_name = schema_dir.split(os.sep)[-1]
    language = args.language
    language_map_path = args.language_map_path
    out_dir = args.out_dir

    translate_db_schema(
        source_scenario_dir=schema_dir,
        language_map=get_language_map(language_map_path),
        target_scenario_dir=os.path.join(out_dir, f'{schema_name}_{language}'),
        out_gt_path=os.path.join(out_dir, 'gt', f'{schema_name}_{schema_name}-{language}.txt')
    )




