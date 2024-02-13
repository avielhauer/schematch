from flask import Flask, request
import argparse
import networkx as nx
from pyvis.network import Network


def visualize_graph(graph, title='test.html'):
    net = Network(height="1500px", notebook=True, directed=True)
    net.from_nx(graph)
    for node in net.nodes:

        if "SIBLING_CLUSTER" in node['label']:
            node['color'] = "red"
    net.show(title)

def visualize(
    source_graph_file,
    target_graph_file,
):
    graphA = nx.read_graphml(source_graph_file, node_type=str)
    graphB = nx.read_graphml(target_graph_file, node_type=str)
    combinedGraph = nx.compose(graphA, graphB)
    visualize_graph(combinedGraph, source_graph_file.split("/")[-2] + '.html')


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
        ]

        CONFIG_KEYS = []
        for arg in ARGS + CONFIG_KEYS:
            if arg not in request.args:
                return "", 400

        config = {k: request.args.get(k) for k in CONFIG_KEYS}
        visualize(
            "../../../../" + request.args.get("source_graph_path"),
            "../../../../" + request.args.get("target_graph_path"),
        )
        return "", 204

    app.run(host=args.embedAlign_host, port=args.embedAlign_port)
