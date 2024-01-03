from flask import Flask, request
import argparse
from ic_wrapper import match



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--ic_host", default="0.0.0.0")
    parser.add_argument("-p", "--ic_port", default=5004)
    args = parser.parse_args()


    app = Flask(__name__)

    @app.route("/match")
    def _match():
        if not set(["source_graph_path", "target_graph_path", "alignment_seeds"]).issubset(request.args):
            return "", 400

        return match(
            request.args.get("source_graph_path"),
            request.args.get("target_graph_path"),
            request.args.get("alignment_seeds")
        )

    app.run(host=args.ic_host, port=args.ic_port)
