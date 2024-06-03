import os
import shutil
import csv


CUR_DIR = os.path.dirname(os.path.realpath(__file__))
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.realpath(__file__))))
DATA_DIR = os.path.join(BASE_DIR, "data")
NEW_DATASET_NAME = "CROSS_SCENARIOS"


def transpose_csv(input_file, output_file):
    with open(input_file, 'r') as infile:
        reader = csv.reader(infile)
        data = list(reader)

    transposed_data = list(zip(*data))

    with open(output_file, 'w') as outfile:
        writer = csv.writer(outfile)
        writer.writerows(transposed_data)

def generate_scenario(source_table_info, target_table_info):
    scenario_path = f"{DATA_DIR}/{NEW_DATASET_NAME}/{source_table_info['scenario_name']}_{source_table_info['target_or_source']}_{target_table_info['scenario_name']}_{target_table_info['target_or_source']}"

    os.makedirs(scenario_path, exist_ok=True)
    os.makedirs(f"{scenario_path}/metadata")

    shutil.copytree(source_table_info["tables_folder"], os.path.join(scenario_path, "source"))
    shutil.copytree(target_table_info["tables_folder"], os.path.join(scenario_path, "target"))

    shutil.copytree(source_table_info["metadata_folder"], os.path.join(scenario_path, "metadata", "source"))
    shutil.copytree(source_table_info["metadata_folder"], os.path.join(scenario_path, "metadata", "target"))
    fd = os.open(os.path.join(scenario_path, "metadata", "source-to-target-inds.txt"), os.O_CREAT | os.O_WRONLY | os.O_APPEND)
    os.close(fd)
    fd = os.open(os.path.join(scenario_path,"metadata","target-to-source-inds.txt"), os.O_CREAT | os.O_WRONLY | os.O_APPEND)
    os.close(fd)

    if source_table_info["scenario_path"] == target_table_info["scenario_path"]:
        ## we know matches
        shutil.copytree(os.path.join(source_table_info["scenario_path"], "ground_truth"), os.path.join(scenario_path, "ground_truth"))
        if source_table_info["target_or_source"] == "target":
            ## we need to rotate our ground truth, as source and target are swapped
            csvs = os.listdir(os.path.join(scenario_path, "ground_truth"))
            for csv in csvs:
                csv_path = os.path.join(scenario_path, "ground_truth", csv)
                transpose_csv(csv_path, csv_path)
                new_csv_name = csv[:-len(".csv")].split("___")[1] + "___" + csv[:-len(".csv")].split("___")[0] + ".csv"
                os.rename(os.path.join(scenario_path, "ground_truth", csv), os.path.join(scenario_path, "ground_truth", new_csv_name))

    else:
        os.makedirs(f"{scenario_path}/ground_truth")

def extract_table_info(scenario_path, target_source_specifier):
    return {
        "tables_folder": os.path.join(scenario_path, target_source_specifier),
        "metadata_folder": os.path.join(scenario_path, "metadata", target_source_specifier),
        "scenario_path": scenario_path,
        "scenario_name" : os.path.basename(scenario_path),
        "target_or_source": target_source_specifier,
    }


def load_scenario_tables(dataset_names):
    all_table_infos = []
    for dataset in dataset_names:
        scenarios = os.listdir(os.path.join(DATA_DIR, dataset))
        for scenario in scenarios:
            all_table_infos.append(extract_table_info(os.path.join(DATA_DIR, dataset, scenario), "source"))
            all_table_infos.append(extract_table_info(os.path.join(DATA_DIR, dataset, scenario), "target"))
    return all_table_infos


if __name__ == "__main__":
    table_infos = load_scenario_tables(["EmbDI", "DeNorm"])
    print(table_infos)
    cross = [(table_info_a, table_info_b) for table_info_a in table_infos for table_info_b in table_infos if table_info_a != table_info_b]
    for source_info, target_info in cross:
        generate_scenario(source_info, target_info)
