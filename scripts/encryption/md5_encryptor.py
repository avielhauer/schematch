from dataset_encryptor import DatasetEncryptor
import hashlib

class MD5Encryptor(DatasetEncryptor):

    def encrypt_dataset_name(self, dataset_name):
        return dataset_name + "_md5_encrypted"

    def encrypt_data(self, data):
        if self.encrypting_target:
            return hashlib.md5((data+"_target").encode()).hexdigest()
        else:
            return hashlib.md5((data+"_source").encode()).hexdigest()

    def encrypt_table(self, table):
        if self.encrypting_target:
            return hashlib.md5((table+"_target").encode()).hexdigest()
        else:
            return hashlib.md5((table+"_source").encode()).hexdigest()

    def encrypt_column(self, column):
        if self.encrypting_target:
            return hashlib.md5((column+"_target").encode()).hexdigest()
        else:
            return hashlib.md5((column+"_source").encode()).hexdigest()


if __name__ == "__main__":
    for dataset in ["EmbDI", "Sakila", "Pubs"]:
        encoder = MD5Encryptor(dataset)
        encoder.encrypt()