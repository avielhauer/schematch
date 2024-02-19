import os
import seaborn as sns
import sys
import numpy as np
from matplotlib.colors import LinearSegmentedColormap
import matplotlib.pyplot as plt
import csv
import matplotlib.patches as patches

from output_helper import *

sns.set_style("whitegrid")
sns.set(rc={"axes.grid": False})


def create_custom_colormap():
    # Define custom colormap with red to green transition
    cmap_colors = [(0, 1, 0), (1, 0, 0)]  # Red to green
    return LinearSegmentedColormap.from_list("custom_cmap", cmap_colors, N=256)

def plot_heatmap(data, data2, dataset, scenario, matcher, show, output_path):
    custom_cmap = create_custom_colormap()
    plt.imshow(data, cmap=custom_cmap, interpolation='nearest', vmin=0, vmax=1)

    # Overlay circles on top of the heatmap
    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            # Check if the corresponding value in data2 is 1
            if data2[i, j] == 1:
                # Draw a circle with a bold black edge
                rect = patches.Rectangle((j - 0.5, i - 0.5), 1, 1, linewidth=2, edgecolor='black', facecolor='none')
                plt.gca().add_patch(rect)

    plt.xticks([])
    plt.yticks([])

    plt.title(f"{matcher} \n{scenario} - {dataset}", fontsize=12)
    plt.ylabel("source columns")
    plt.xlabel("target columns")
    cbar = plt.colorbar()
    cbar.set_label('abs(Ground_truth - Actual)', labelpad=10, fontsize=14)

    if show:
        plt.show()
    plt.savefig(output_path, dpi=200)
    plt.close()


def process_output_file(matcher_file_path, dataset, scenario, matcher, show=False):
    with open(matcher_file_path) as fd:
        csvreader = csv.reader(fd)

        data = []
        data2 = []
        for row in list(csvreader)[1:]:
            data.append([abs(float(cell.split(" / ")[0]) - float(cell.split(" / ")[1])) for cell in row[1:]])
            data2.append([int(cell.split(" / ")[0]) for cell in row[1:]])
    data = np.asarray(data)
    data2 = np.asarray(data2)
    output_path = matcher_file_path[:-3] + "png"
    plot_heatmap(data, data2, dataset, scenario, matcher, show, output_path)


if __name__ == "__main__":
    root_path = sys.argv[1]

    for dataset in os.listdir(root_path):
        dataset_path = os.path.join(root_path, dataset)
        if not os.path.isdir(dataset_path) or dataset == "_performances":
            continue
        # Iterate over scenarios
        for scenario in os.listdir(dataset_path):
            scenario_path = os.path.join(dataset_path, scenario)
            if not os.path.isdir(scenario_path) or scenario == "_performances":
                continue
            print("Dataset:", dataset)
            print("Scenario:", scenario)
            outputs_dir = os.path.join(scenario_path, "_outputs", "MatchingStepLine1")
            for matcher in os.listdir(outputs_dir):
                if matcher.endswith(".csv"):
                    process_output_file(os.path.join(outputs_dir, matcher), dataset, scenario, matcher, show=False)
