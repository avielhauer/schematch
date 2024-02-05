import glob
import os
from abc import abstractmethod
import csv
from pathlib import Path

CUR_DIR = os.path.dirname(os.path.realpath(__file__))
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.realpath(__file__))))
DATA_DIR = os.path.join(BASE_DIR, "data")

class DatasetEncryptor:
    def __init__(self, dataset: str):
        self.dataset = dataset
        self.dataset_dir = os.path.join(DATA_DIR, self.dataset)
        self.scenario_dirs = glob.glob(os.path.join(self.dataset_dir, "*"))
        self.encrypted_dataset_dir = os.path.join(DATA_DIR, self.encrypt_dataset_name(self.dataset))

    @abstractmethod
    def encrypt_dataset_name(self, dataset_name):
        return dataset_name + "_encrypted"
    @abstractmethod
    def encrypt_data(self, data):
        return data

    @abstractmethod
    def encrypt_table(self, table):
        return table

    @abstractmethod
    def encrypt_column(self, column):
        return column

    def encrypt(self):
        for scenario in self.scenario_dirs:
            self.encrypt_source(scenario)
            self.encrypt_target(scenario)
            self.encrypt_ground_truth(scenario)
            self.copy_empty_metadata(scenario)

    def encrypt_source(self, scenario_dir):
        self.encrypt_data_folder(scenario_dir, os.path.join(scenario_dir, "source"))

    def encrypt_target(self, scenario_dir):
        self.encrypt_data_folder(scenario_dir, os.path.join(scenario_dir, "target"))

    def encrypt_data_folder(self, scenario, folder_dir):
        csv_files = glob.glob(os.path.join(folder_dir, "*.csv"))
        os.makedirs(os.path.join(self.encrypted_dataset_dir, os.path.basename(scenario), os.path.basename(folder_dir)), exist_ok=True)

        for csv_file in csv_files:
            table = os.path.basename(csv_file)[:-4]
            encrypted_table = self.encrypt_table(table) + ".csv"
            self.encrypt_data_csv_file(csv_file, os.path.join(self.encrypted_dataset_dir, os.path.basename(scenario), os.path.basename(folder_dir), encrypted_table))

    def encrypt_ground_truth(self, scenario):
        os.makedirs(os.path.join(self.encrypted_dataset_dir, os.path.basename(scenario), "ground_truth"), exist_ok=True)
        csv_files = glob.glob(os.path.join(scenario, "ground_truth", "*.csv"))
        for csv_file in csv_files:
            source_table, target_table = os.path.basename(csv_file).split("___")
            target_table = self.encrypt_table(target_table[:-4])
            source_table = self.encrypt_table(source_table)

            with open(csv_file, "r") as fp:
                lines = fp.readlines()

            to_file = source_table + "___" + target_table + ".csv"
            with open(os.path.join(self.encrypted_dataset_dir, os.path.basename(scenario), "ground_truth", to_file), "w") as fp:
                fp.writelines(lines)

    def copy_empty_metadata(self, scenario):
        self.copy_database_inds(os.path.join(scenario, "metadata"))
        self.copy_schema_metadata(scenario, "source")
        self.copy_schema_metadata(scenario, "target")

    def copy_database_inds(self, metadata_folder):
        target_folder = os.path.join(self.encrypted_dataset_dir, os.path.basename(os.path.dirname(metadata_folder)), "metadata")
        os.makedirs(target_folder, exist_ok=True)
        (Path(target_folder) / "source-to-target-inds.txt").touch()
        (Path(target_folder) / "target-to-source-inds.txt").touch()

    def copy_schema_metadata(self, scenario_path, subfolder_name):
        target_folder = os.path.join(self.encrypted_dataset_dir, os.path.basename(scenario_path), "metadata", subfolder_name)
        os.makedirs(target_folder, exist_ok=True)
        (Path(target_folder) / "inds.txt").touch()
        for folder in glob.glob(os.path.join(scenario_path, "metadata", subfolder_name, "*/")):
            target_table_folder = os.path.join(target_folder, self.encrypt_table(os.path.basename(os.path.dirname(folder))))
            os.makedirs(target_table_folder, exist_ok=True)
            (Path(target_table_folder) / "FD_results.txt").touch()
            (Path(target_table_folder) / "UCC_results.txt").touch()

    # Before we had a Metanome wrapper to update metadata, we simply encrypted the existing metadata files - this
    # is error-prone (metadata might change), but perhaps useful for other use-cases. So code is currently "dead".
    def encrypt_metadata(self, scenario):
        self.encrypt_database_inds(os.path.join(scenario, "metadata"))
        self.encrypt_schema_metadata(scenario, "source")
        self.encrypt_schema_metadata(scenario, "target")

    def encrypt_database_inds(self, metadata_folder):
        target_folder = os.path.join(self.encrypted_dataset_dir, os.path.basename(os.path.dirname(metadata_folder)), "metadata")
        os.makedirs(target_folder, exist_ok=True)
        self.encrypt_inds(os.path.join(metadata_folder, "source-to-target-inds.txt"), os.path.join(target_folder, "source-to-target-inds.txt"))
        self.encrypt_inds(os.path.join(metadata_folder, "target-to-source-inds.txt"), os.path.join(target_folder, "target-to-source-inds.txt"))

    def encrypt_schema_metadata(self, scenario_path, subfolder_name):
        target_folder = os.path.join(self.encrypted_dataset_dir, os.path.basename(scenario_path),"metadata", subfolder_name)
        os.makedirs(target_folder, exist_ok=True)
        self.encrypt_inds(os.path.join(scenario_path, "metadata", subfolder_name, "inds.txt"), os.path.join(target_folder, "inds.txt"))
        for folder in glob.glob(os.path.join(scenario_path, "metadata", subfolder_name, "*/")):
            target_table_folder = os.path.join(target_folder, self.encrypt_table(os.path.basename(os.path.dirname(folder))))
            os.makedirs(target_table_folder, exist_ok=True)
            self.encrypt_fd_file(os.path.join(folder, "FD_results.txt"), os.path.join(target_table_folder, "FD_results.txt"))
            self.encrypt_ucc_file(os.path.join(folder, "UCC_results.txt"), os.path.join(target_table_folder, "UCC_results.txt"))


    def encrypt_fd_file(self, fd_file_path, target_file_path):
        with open(fd_file_path, "r") as fp:
            fds = fp.readlines()

        with open(target_file_path, "w") as fp:
            fp.writelines([self.encrypt_fd_line(line)+"\n" for line in fds])

    def encrypt_ucc_file(self, ucc_file_path, target_file_path):
        with open(ucc_file_path, "r") as fp:
            uccs = fp.readlines()
        with open(target_file_path, "w") as fp:
            fp.writelines([self.encrypt_ucc_line(ucc)+"\n" for ucc in uccs])

    def encrypt_ucc_line(self, ucc_line):
        return "[" + ", ".join([self.encrypt_metadata_token(token) for token in ucc_line.strip().strip("[]").split(", ")]) + "]"

    def encrypt_fd_line(self, fd_line):
        left, right = fd_line.split(" --> ")
        left = left.strip("[]").split(", ")
        newline = "[" + ", ".join([self.encrypt_metadata_token(token) for token in left]) + "] --> "
        newline += ", ".join([self.encrypt_metadata_token(token) for token in right.split(", ")])
        return newline


    def encrypt_inds(self, ind_file_path, target_file_path):
        with open(ind_file_path, "r") as fp:
            lines = fp.readlines()

        with open(target_file_path, "w") as fp:
            fp.writelines([self.encrypt_ind_line(line)+"\n" for line in lines])

    def encrypt_ind_line(self, line):
        left, right = line.split(" --> ")
        left = left.strip("[]").split(", ")
        newline = "[" + ", ".join([self.encrypt_metadata_token(token) for token in left]) + "] --> "
        right = right.strip().split("],")
        for tokens in right:
            tokens = tokens.strip("[] ").split(", ")
            newline += (", " if newline[-1] == "]" else "") + "[" + ", ".join([self.encrypt_metadata_token(token) for token in tokens]) + "]"
        return newline

    def encrypt_metadata_token(self, token):
        if not token:
            return token
        delimeter = ".csv."
        return self.encrypt_table(token.split(delimeter)[0].strip()) + delimeter + self.encrypt_column(token.split(delimeter)[1].strip())

    def encrypt_data_csv_file(self, csv_path, target_path):
        encrypted_rows = []
        with open(csv_path, "r") as fp:
            csv_reader = csv.reader(fp)
            for row in csv_reader:
                if not encrypted_rows:
                    encrypted_rows.append([self.encrypt_column(col) for col in row])
                else:
                    encrypted_rows.append([self.encrypt_data(col) for col in row])
        with open(target_path, "w", newline='') as fp:
            csv_writer = csv.writer(fp)
            csv_writer.writerows(encrypted_rows)

if __name__ == "__main__":
    dataset_encryptor = DatasetEncryptor("Efes-bib")
    dataset_encryptor.encrypt()