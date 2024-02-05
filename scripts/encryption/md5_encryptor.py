from dataset_encryptor import DatasetEncryptor
import hashlib

class MD5Encryptor(DatasetEncryptor):

    def encrypt_dataset_name(self, dataset_name):
        return dataset_name + "_md5_encrypted"

    def encrypt_data(self, data):
        return hashlib.md5(data.encode()).hexdigest()

    def encrypt_table(self, table):
        return hashlib.md5(table.encode()).hexdigest()

    def encrypt_column(self, column):
        return hashlib.md5(column.encode()).hexdigest()


if __name__ == "__main__":
    for dataset in ["EmbDI"]:
        encoder = MD5Encryptor(dataset)
        encoder.encrypt()