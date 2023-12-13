from flask import Flask, request
import argparse
from ic_wrapper import match



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--ic_host", default="0.0.0.0")
    parser.add_argument("-p", "--ic_port", default=5003)
    args = parser.parse_args()


    app = Flask(__name__)

    @app.route("/match")
    def _match():
        if "source_graph_path" not in request.args or "target_graph_path" not in request.args:
            return "", 400

        return match(
            request.args.get("source_graph_path"),
            request.args.get("target_graph_path")
        )

    app.run(host=args.ic_host, port=args.ic_port)
