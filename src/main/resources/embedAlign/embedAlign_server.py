from flask import Flask, request
import argparse
from embedAlign_wrapper import match


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--embedAlign_host", default="0.0.0.0")
    parser.add_argument("-p", "--embedAlign_port", default=5004)
    args = parser.parse_args()

    app = Flask(__name__)

    @app.route("/match")
    def _match():
        if "source_graph_path" not in request.args or "target_graph_path" not in request.args \
                or "source_table" not in request.args or "target_table" not in request.args:
            return "", 400

        return match(
            "../../../../" + request.args.get("source_graph_path"),
            request.args.get("source_table"),
            "../../../../" + request.args.get("target_graph_path"),
            request.args.get("target_table")
        )

    app.run(host=args.embedAlign_host, port=args.embedAlign_port)
