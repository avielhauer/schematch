import csv
import os
import re
import dataclasses


@dataclasses.dataclass
class Column:
    table: str
    column: str

def get_columns(table):
    with open(table, 'r') as table_file:
        reader = csv.reader(table_file)
        return next(reader)

def write_ground_truth(folder, source, target):
    with open(f"gt/{source}_{target}.txt", 'r') as gt_file:
        gt_matches = []
        for match in gt_file.readlines():
            match_source, match_target = match.strip().split(" = ") if "=" in match else match.strip().split(",")
            gt_matches.append((Column(*match_source.split(".")), Column(*match_target.split("."))))

    table_matches = list(dict([(match[0].table, match[1].table) for match in gt_matches]).items())
    for table_match in table_matches:
        source_columns = get_columns(f"{source}/tables/{table_match[0]}.csv")
        target_columns = get_columns(f"{target}/tables/{table_match[1]}.csv")
        gt = [[0 for x in target_columns] for y in source_columns]
        source_lookup = {col: i for i, col in list(enumerate(source_columns))}
        target_lookup = {col: i for i, col in list(enumerate(target_columns))}
        for match in gt_matches:
            if match[0].table == table_match[0] and match[1].table == table_match[1]:
                gt[source_lookup[match[0].column]][target_lookup[match[1].column]] = 1
        with open(f"{folder}/ground_truth/{table_match[0]}___{table_match[1]}.csv", "w") as file:
            file.write("\n".join([
                            ",".join(
                                [str(x) for x in column]
                            )
                            for column in gt
                        ]
        ))


def write_metadata(folder, source, target):
    files = [
        f"{folder}/metadata/source-to-target-inds.txt",
        f"{folder}/metadata/target-to-source-inds.txt",
        f"{folder}/metadata/source/inds.txt",
        f"{folder}/metadata/target/inds.txt",
    ]
    for table in os.listdir(source + "/tables"):
        table_name = table[:-4]
        os.makedirs(f"{folder}/metadata/source/{table_name}")
        files.append(f"{folder}/metadata/source/{table_name}/FD_results.txt")
        files.append(f"{folder}/metadata/source/{table_name}/UCC_results.txt")
    for table in os.listdir(target + "/tables"):
        table_name = table[:-4]
        os.makedirs(f"{folder}/metadata/target/{table_name}")
        files.append(f"{folder}/metadata/target/{table_name}/FD_results.txt")
        files.append(f"{folder}/metadata/target/{table_name}/UCC_results.txt")
    for file in files:
        fp = open(file, "w+")
        fp.close()


def create_scenario(source, target):
    folder = f"../../Sakila/{source}_{target}"
    os.makedirs(f"{folder}", exist_ok=True)
    os.makedirs(f"{folder}/source", exist_ok=True)
    os.makedirs(f"{folder}/target", exist_ok=True)
    os.makedirs(f"{folder}/ground_truth", exist_ok=True)
    os.makedirs(f"{folder}/metadata", exist_ok=True)
    os.makedirs(f"{folder}/metadata/source", exist_ok=True)
    os.makedirs(f"{folder}/metadata/target", exist_ok=True)
    os.system(f"cp -r {source}/tables/. {folder}/source/")
    os.system(f"cp -r {target}/tables/. {folder}/target/")

    write_ground_truth(folder, source, target)
    write_metadata(folder, source, target)

if __name__ == '__main__':
    scenarios = os.listdir("gt/")
    for scenario in scenarios:
        schema_1, schema_2, _ = re.split(r"[._]", scenario)
        create_scenario(schema_1, schema_2)
