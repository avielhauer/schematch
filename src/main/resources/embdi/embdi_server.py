from flask import Flask, request
import argparse
from embdi_wrapper import match



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--embdi-host", default="0.0.0.0")
    parser.add_argument("-p", "--embdi-port", default=5001)
    args = parser.parse_args()


    app = Flask(__name__)

    @app.route("/match")
    def _match():
        if "table1" not in request.args or "table2" not in request.args or "sm_mode" not in request.args:
            return "", 400

        return match(
            request.args.get("table1"),
            request.args.get("table2"),
            request.args.get("sm_mode")
        )

    app.run(host=args.embdi_host, port=args.embdi_port)
