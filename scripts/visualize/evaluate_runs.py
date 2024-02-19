# call python evaluate_runs.py /path/to/schemata/results/result_xy
import os
import sys
import csv
import matplotlib.pyplot as plt
from matplotlib.colors import LinearSegmentedColormap
import seaborn as sns
import numpy as np
import re

from output_helper import *

PERFORMANCES = {
    "Accuracy": "Accuracy",
    "F1": "F1",
    "Precision": "Precision",
    "Recall": "Recall",
    "NonBinaryPrecisionAtGroundTruth": "NonBinaryPrecisionAtGroundTruth"
}

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
                del runs[1]  # remove double ratio (1.0) # TODO: make this robust and smarter
                perfomances = [
                    x["perf"] for x in runs
                ]
                ratios = [r["ratio"] for r in runs]

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

    plt.tight_layout()  # Adjust layout to prevent overlap
    plt.show()


def plot_dataset_evaluation(matcher_to_performances, title=None, path_to_save=None, performances_to_be_plotted=PERFORMANCES):
    num_matchers = len(matcher_to_performances)
    num_columns=2
    fig, axs = plt.subplots(
        len(performances_to_be_plotted) // num_columns+1, num_columns, figsize=(12, 8), sharey=False
    )

    if title:
        fig.suptitle(title)
    for idx, ((performance_name, performance_id), ax) in enumerate(zip(performances_to_be_plotted.items(), axs.flatten()), start=1):
        x_labels = []
        y = []
        for matcher, performances in matcher_to_performances.items():
            if matcher.endswith("Matcher"):
                matcher = matcher[:-7]
            x_labels.append(matcher)
            y.append(max([performance[performance_id] for performance in performances]))
        ax.set_title(performance_name)
        ax.bar(x_labels, y)
        ax.tick_params(axis='x', labelrotation=90)
    plt.subplots_adjust(hspace=2.0)
    plt.savefig(os.path.join(path_to_save, "performance.png"), dpi=200)
    #plt.show()

if __name__ == "__main__":
    root_path = sys.argv[1]
    benchmarks = import_benchmark(root_path, PERFORMANCES)
    print(benchmarks)
    generalized_benchmarks = generalize_benchmark_matchers(benchmarks)
    print(generalized_benchmarks)

    plot_dataset_evaluation(generalized_benchmarks["_performances"], "Overall Performance", root_path)
    for dataset, scenario_dict in generalized_benchmarks.items():
        if dataset == "_performances":
            continue
        plot_dataset_evaluation(scenario_dict["_performances"], dataset, os.path.join(root_path, dataset))
        for scenario, performance_dict in scenario_dict.items():
            if scenario == "_performances":
                continue
            plot_dataset_evaluation(performance_dict["_performances"], scenario, os.path.join(root_path, dataset, scenario))

    #for dataset, scenario_dict in generalized_benchmarks.items():
    #    plot_dataset_evaluation(scenario_dict)