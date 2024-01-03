import argparse
import json
import tqdm

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('glove_directory')
    args = parser.parse_args()

    word_index = {}
    for i in tqdm.tqdm(range(10000, 11917)):
        with open(f"{args.glove_directory}/glove.42B.300d{i}", 'r') as glove_part:
            for j, line in enumerate(glove_part):
                word_index[line.split()[0]] = (i, j)
    with open(f"{args.glove_directory}/glove_index.json", 'w+') as indexfile:
        indexfile.write(json.dumps(word_index))
