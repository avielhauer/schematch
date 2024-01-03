from flask import Flask, request
import argparse
from node2vec_matcher import match


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--node2vec_host", default="0.0.0.0")
    parser.add_argument("-p", "--node2vec_port", default=5004)
    args = parser.parse_args()

    app = Flask(__name__)

    @app.route("/match")
    def _match():
        if "source_graph_path" not in request.args or "target_graph_path" not in request.args \
                or "source_graph_id" not in request.args or "target_graph_id" not in request.args:
            return "", 400

        return match(
            "../../../../" + request.args.get("source_graph_path"),
            request.args.get("source_graph_id"),
            "../../../../" + request.args.get("target_graph_path"),
            request.args.get("target_graph_id")
        )

    app.run(host=args.node2vec_host, port=args.node2vec_port)
