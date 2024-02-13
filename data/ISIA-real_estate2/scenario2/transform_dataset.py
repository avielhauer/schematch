import csv
import dataclasses
import os
import re


@dataclasses.dataclass
class Column:
    table: str
    column: str

def get_columns(table):
    with open(table, 'r') as table_file:
        reader = csv.reader(table_file)
        return next(reader)

def write_ground_truth():
    ground_truth_files = [file for file in os.listdir("ground_truth") if file.endswith(".txt")]
    for file in ground_truth_files:
        with open(f"ground_truth/{file}", 'r') as gt_file:
            gt_matches = []
            for match in gt_file.readlines():
                match_source, match_target = match.strip().split(" = ") if "=" in match else match.strip().split(",")
                gt_matches.append((Column(*match_source.split(".")), Column(*match_target.split("."))))

            source_table = re.match(r"(\w+)_(\w+)\.txt", file).group(1)
            target_table = re.match(r"(\w+)_(\w+)\.txt", file).group(2)
            source_columns = get_columns(f"source/{source_table}.csv")
            target_columns = get_columns(f"target/{target_table}.csv")
            gt = [[0 for x in target_columns] for y in source_columns]
            source_lookup = {col: i for i, col in list(enumerate(source_columns))}
            target_lookup = {col: i for i, col in list(enumerate(target_columns))}
            for match in gt_matches:
                if match[0].table == source_table and match[1].table == target_table:
                    gt[source_lookup[match[0].column]][target_lookup[match[1].column]] = 1
                else:
                    raise ValueError()
            with open(f"ground_truth/{source_table}___{target_table}.csv", "w") as file:
                file.write("\n".join([
                                ",".join(
                                    [str(x) for x in column]
                                )
                                for column in gt
                            ]
            ))

if __name__ == '__main__':
    write_ground_truth()
