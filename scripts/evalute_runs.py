# call python evaluate_runs.py /path/to/schemata/results/result_xy
import os
import sys
import csv
import matplotlib.pyplot as plt
from matplotlib.colors import LinearSegmentedColormap
import matplotlib.patches as patches
import seaborn as sns
import numpy as np
import re

sns.set_style("whitegrid")
sns.set(rc={"axes.grid": False})


benchmark_root = sys.argv[1]


def extract_properties(matcher_desc):
    pattern = r'(\w+)\s*=\s*([^\s:]+)'
    matches = re.findall(pattern, matcher_desc)
    return dict(matches)

def create_custom_colormap():
    # Define custom colormap with red to green transition
    cmap_colors = [(0, 1, 0), (1, 0, 0)]  # Red to green
    return LinearSegmentedColormap.from_list("custom_cmap", cmap_colors, N=256)

def plot_heatmap(data, data2, dataset, scenario, matcher):
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
    matcher_clean = matcher[:-4]
    if matcher_clean.startswith("Node2Vec"):
        pattern = r'(\w+)\s*=\s*([^\s:]+)'
        matches = re.findall(pattern, matcher_clean)
        properties = dict(matches)
        matcher_clean = f"EmbedAlign Struc: {properties['xNetMFGammaStruc']} Attr: {properties['xNetMFGammaAttr']} Top3: {properties['filterKNearest'][:-1]}"

    plt.title(f"{matcher_clean} \n{scenario} - {dataset}", fontsize=16)
    plt.ylabel("source columns")
    plt.xlabel("target columns")
    cbar = plt.colorbar()
    cbar.set_label('abs(Ground_truth - Actual)', labelpad=10, fontsize=14)

    plt.show()

def process_output_file(matcher_file_path, dataset, scenario, matcher):
    with open(matcher_file_path) as fd:
        csvreader = csv.reader(fd)

        data = []
        data2 = []
        for row in list(csvreader)[1:]:
            data.append([abs(float(cell.split(" / ")[0]) - float(cell.split(" / ")[1])) for cell in row[1:]])
            data2.append([int(cell.split(" / ")[0]) for cell in row[1:]])
    data = np.asarray(data)
    data2 = np.asarray(data2)
    plot_heatmap(data, data2, dataset, scenario, matcher)

def extract_performance(csv_path):
    performances = []
    with open(csv_path) as fd:
        csvreader = csv.reader(fd)

        row1, row2 = list(csvreader)[:2]
        for i, matcher in enumerate(row1):
            if i == 0:
                continue
            if not matcher.startswith("Node2Vec"):
                continue
            properties = extract_properties(matcher)
            properties["perf"] = float(row2[i])
            properties["ratio"] = float(properties["xNetMFGammaStruc"]) / float(properties["xNetMFGammaAttr"])
            properties["filterKNearest"] = properties["filterKNearest"][:-1]
            performances.append(properties)
    return performances


performance = {}
for dataset in os.listdir(benchmark_root):
    dataset_path = os.path.join(benchmark_root, dataset)
    if os.path.isdir(dataset_path) and dataset != "_performances" and dataset.startswith("EmbDI"):
        performance_dataset = {}
        # Iterate over scenarios
        for scenario in os.listdir(dataset_path):
            scenario_path = os.path.join(dataset_path, scenario)
            if os.path.isdir(scenario_path) and scenario != "_performances":
                # Now you can perform operations within each scenario
                print("Dataset:", dataset)
                print("Scenario:", scenario)
                # Add your processing logic here
                outputs_dir = os.path.join(scenario_path, "_outputs", "MatchingStepLine1")
                for matcher in os.listdir(outputs_dir):
                    if not matcher.startswith("Node"):
                        continue
                    print("matcher: ", matcher)
                    process_output_file(os.path.join(outputs_dir, matcher), dataset, scenario, matcher)
                performance_csv = os.path.join(scenario_path, "_performances", "NonBinaryPrecisionAtGroundTruth", "performance_overview_line1.csv")
                performance_dataset[scenario] = extract_performance(performance_csv)
        performance[dataset] = performance_dataset



COLORS = {"True": "blue", "False": "red"}
def visualize_performance(benchmarks):
    plt.figure(figsize=(10, 8))

    datasets = sorted(list(benchmarks.keys()))
    num_datasets = len(datasets)
    num_columns = 1

    for idx, dataset in enumerate(datasets, start=1):
        plt.subplot(num_datasets // num_columns + 1, num_columns, idx)
        plt.title(f"{dataset} - All Scenario Performance Scores", fontsize=18)
        plt.ylabel("Performance Score", fontdict={"fontsize": 16})
        plt.xlabel("GammaStruc / GammaAttr", fontdict={"fontsize": 16})

        seen = set()
        for scenario, benchmark in benchmarks[dataset].items():
            for filterKNearest in ["True", "False"]:
                runs = sorted([x for x in benchmark if x["filterKNearest"] == filterKNearest], key=lambda x: x["ratio"])
                del runs[1] # remove double ratio (1.0) # TODO: make this robust and smarter
                perfomances = [
                    x["perf"] for x in runs
                ]
                ratios = [r["ratio"] for r in runs]
                # plt.text(
                #     ratios[-1],
                #     perfomances[-1],
                #     scenario.replace("_", " "),
                #     fontsize=12,
                #     ha="right",
                #     va="bottom",
                #     color="black",
                # )

                label = f"filter {filterKNearest}"
                if label in seen:
                    plt.plot(
                        ratios,
                        perfomances,
                        color=COLORS[filterKNearest],
                    )
                else:
                    plt.plot(
                        ratios,
                        perfomances,
                        color=COLORS[filterKNearest],
                        label=label
                    )
                    seen.add(label)

        plt.legend(loc='center left', bbox_to_anchor=(1, 0.5), fontsize=16)
        #plt.ylim([0.0, max(throughputs) * 1.5])
        #plt.xticks(BS)

    plt.tight_layout()  # Adjust layout to prevent overlap
    #plt.savefig("figures/pagesize_read_write.png", dpi=400)
    plt.show()

visualize_performance({dataset : v for dataset, v in performance.items() if dataset.startswith("EmbDI")})
visualize_performance({dataset : v for dataset, v in performance.items() if dataset.startswith("Sakila")})
visualize_performance({dataset : v for dataset, v in performance.items() if dataset.startswith("Pubs")})
