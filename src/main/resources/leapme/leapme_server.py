from flask import Flask, request
import argparse
from leapme_transfer_predict import GloveEmbeddingsCalculator, predict

PREDICTION_USED_INFO_TYPES = ["values", "names", "both"]
PREDICTION_FEATURES = ["embeddings", "no-embeddings", "both"]
DATA_DIRECTORY_MOUNT = '/leapme/data/'

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('glove_path')
    # When running in container, we need to listen on all interfaces.
    parser.add_argument('--leapme-host', default='0.0.0.0')
    parser.add_argument('-p', '--leapme-port', default=5000)
    args = parser.parse_args()

    GloveEmbeddingsCalculator.set_path(args.glove_path)

    app = Flask(__name__)

    @app.route("/match")
    def match():
        if "table1" not in request.args or "table2" not in request.args:
            return "", 400

        return predict(
            request.args.get("prediction_used_info_types", "both"),
            request.args.get("prediction_used_features", "both"),
            DATA_DIRECTORY_MOUNT + request.args.get("table1"),
            DATA_DIRECTORY_MOUNT + request.args.get("table2")
        )

    app.run(host=args.leapme_host, port=args.leapme_port)
