from flask import Flask, request
import argparse
from embdi_wrapper import match



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--leapme-host", default="0.0.0.0")
    parser.add_argument("-p", "--leapme-port", default=5001)
    args = parser.parse_args()


    app = Flask(__name__)

    @app.route("/match")
    def _match():
        if "table1" not in request.args or "table2" not in request.args:
            return "", 400

        return match(
            request.args.get("table1"),
            request.args.get("table2")
        )

    app.run(host=args.leapme_host, port=args.leapme_port)
