from flask import Flask, request
import argparse
from prisma_wrapper import match


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--embedAlign_host", default="0.0.0.0")
    parser.add_argument("-p", "--embedAlign_port", default=5004)
    args = parser.parse_args()

    app = Flask(__name__)

    @app.route("/match")
    def _match():
        ARGS = [
            "source_graph_path",
            "target_graph_path",
            "source_table",
            "target_table",
            "features_dir",
        ]

        CONFIG_KEYS = [
            "dropColumns",
            "dropConstraints",
            "xNetMFGammaStrucAttr",
            "top_k_row",
            "top_k_col",
            "top_k_by_union"
        ]
        for arg in ARGS + CONFIG_KEYS:
            if arg not in request.args:
                return "", 400

        config = {k: request.args.get(k) for k in CONFIG_KEYS}
        return match(
            "../../../../" + request.args.get("source_graph_path"),
            request.args.get("source_table"),
            "../../../../" + request.args.get("target_graph_path"),
            request.args.get("target_table"),
            request.args.get("features_dir"),
            config,
        )

    app.run(host=args.embedAlign_host, port=args.embedAlign_port)
