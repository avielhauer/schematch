# call python evaluate_runs.py /path/to/schemata/results/result_xy
import os
import sys
import matplotlib.pyplot as plt
from copy import deepcopy

from output_helper import *

RENAME_MATCHER = {
 "LongestCommonSubsequence" : "LCSM",
 "SetCosineInstance" : "SCI"
}
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


def plot_dataset_evaluation(matcher_to_performances, title=None, path_to_save=None, performances_to_be_plotted=PERFORMANCES, ax_plotters=None):
    num_graphs = len(performances_to_be_plotted) + (0 if ax_plotters is None else len(ax_plotters))
    num_columns=2
    fig, axs = plt.subplots(
        max(1, num_graphs // num_columns), num_columns, figsize=(20, 12), sharey=False
    )

    if title:
        fig.suptitle(title)
    for idx, ((performance_name, performance_id), ax) in enumerate(zip(performances_to_be_plotted.items(), axs.flatten()[:len(performances_to_be_plotted)]), start=1):
        x_labels = []
        y = []
        for matcher, performances in matcher_to_performances.items():
            if matcher.endswith("Matcher"):
                matcher = matcher[:-7]
            x_labels.append(RENAME_MATCHER[matcher] if matcher in RENAME_MATCHER else matcher)
            y.append(max([performance[performance_id] for performance in performances]))
        ax.set_title(performance_name)
        ax.bar(x_labels, y)
        ax.tick_params(axis='x', labelrotation=90)
    if ax_plotters:
        for ax, ax_plotter in zip(axs.flatten()[len(performances_to_be_plotted):], ax_plotters):
            ax_plotter(matcher_to_performances, ax, fig)
    plt.subplots_adjust(hspace=2.0)
    plt.savefig(os.path.join(path_to_save, "performance.png"), dpi=200)
    #plt.show()
    plt.close()


def group_performances(performances, group_by_props):
    grouped_performances = {}
    for perf in performances:
        key = tuple([perf["props"][prop] for prop in group_by_props])
        grouped_performances.setdefault(key, []).append(perf)
    return grouped_performances
def generate_perf_plots_gamma_struc(performances=PERFORMANCES):
    plotters = []
    for performance_name, performance_id in performances.items():
        def plot_embed_align_struc(performance_name=performance_name, performance_id=performance_id):
            def plot(matcher_to_performances, ax, fig):
                n2v_perf = deepcopy(matcher_to_performances["Node2VecMatcher"])
                grouped_perfs = group_performances(n2v_perf, ["topKRow", "topKCol", "dropColumns", "dropConstraints"])
                sorted_filtered_perfs = {k:sorted(v, key=lambda x: x["props"]["xNetMFGammaStrucAttr"]) for k,v in grouped_perfs.items()}
                ax.set_title(f"{performance_name} for different xNetMFGammaStrucAttr")
                ax.set_xlabel("xNetMFGammaStrucAttr")
                for config, perfs in sorted_filtered_perfs.items():
                    ax.plot([perf["props"]["xNetMFGammaStrucAttr"] for perf in perfs], [perf[performance_id] for perf in perfs], label="test")
            return plot
        plotters.append(plot_embed_align_struc())
    return plotters

if __name__ == "__main__":
    root_path = sys.argv[1]
    benchmarks = import_benchmark(root_path, PERFORMANCES)
    generalized_benchmarks = generalize_benchmark_matchers(benchmarks)
    print(generalized_benchmarks)

    plot_dataset_evaluation(generalized_benchmarks["_performances"], "Overall Performance", root_path, ax_plotters=generate_perf_plots_gamma_struc())
    for dataset, scenario_dict in generalized_benchmarks.items():
        if dataset == "_performances":
            continue
        plot_dataset_evaluation(scenario_dict["_performances"], dataset, os.path.join(root_path, dataset), ax_plotters=generate_perf_plots_gamma_struc())
        for scenario, performance_dict in scenario_dict.items():
            if scenario == "_performances":
                continue
            plot_dataset_evaluation(performance_dict["_performances"], scenario, os.path.join(root_path, dataset, scenario), ax_plotters=generate_perf_plots_gamma_struc())

    #for dataset, scenario_dict in generalized_benchmarks.items():
    #    plot_dataset_evaluation(scenario_dict)