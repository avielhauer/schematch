from flask import Flask, request
import argparse
from adnev_wrapper import match



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--adnev_host", default="0.0.0.0")
    parser.add_argument("-p", "--adnev_port", default=5002)
    args = parser.parse_args()


    app = Flask(__name__)

    @app.route("/match")
    def _match():
        if "sm_dir" not in request.args:
            return "", 400

        return match(
            request.args.get("sm_dir")
        )

    app.run(host=args.adnev_host, port=args.adnev_port)
