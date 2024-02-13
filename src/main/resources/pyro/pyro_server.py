import json
import os
import time

from flask import Flask, request
import argparse


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    # When running in container, we need to listen on all interfaces.
    parser.add_argument("--pyro_host", default="0.0.0.0")
    parser.add_argument("-p", "--pyro_port", default=6001)
    args = parser.parse_args()

    app = Flask(__name__)

    @app.route("/match")
    def _match():
        ARGS = [
            "table"
        ]

        CONFIG_KEYS = []
        for arg in ARGS + CONFIG_KEYS:
            if arg not in request.args:
                return "", 400

        config = {k: request.args.get(k) for k in CONFIG_KEYS}
        return "\n".join(run_pyro(request.args.get("table"))), 200


    def run_pyro(table_file):
        classpath = "metanome-cli-1.1.0.jar:pyro-distro-1.0-SNAPSHOT-distro.jar"
        main_class = "de.metanome.cli.App"
        algorithm = "de.hpi.isg.pyro.algorithms.Pyro"
        input_key = "inputFile"
        input_file = table_file
        separator = ","
        max_ucc_error = "0.03"
        output = "file"

        # Assemble the command
        os.system("rm -rf ./results")
        command = f"java -cp {classpath} {main_class} --algorithm {algorithm} --input-key {input_key} --files {input_file} --output {output} --separator {separator} --header --algorithm-config maxUccError:{max_ucc_error}"
        os.system(command)

        fd_file = [file for file in os.listdir("./results") if "_fds" in file]
        if len(fd_file) == 0:
            return []  # No FDs present here?!
        fd_file = fd_file[0]

        with (open(f"./results/{fd_file}", "r") as f):
            fds = [json.loads(fd) for fd in f.readlines()]
            parsed_fds = []
            for fd in fds:
                if len(fd["determinant"]["columnIdentifiers"]) == 0:
                    continue

                parsed_fds.append("|".join([det['columnIdentifier'] for det in fd["determinant"]["columnIdentifiers"]]))
                parsed_fds.append(fd["dependant"]["columnIdentifier"])
            return parsed_fds

    app.run(host=args.pyro_host, port=args.pyro_port)
