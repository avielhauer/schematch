import os
import csv
import numpy as np
import matplotlib.pyplot as plt

CUR_DIR = os.path.dirname(os.path.realpath(__file__))
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.realpath(__file__))))
RESULTS_DIR = os.path.join(BASE_DIR, "results")
PERFORMANCE_FOLDER_NAME = "_performances"


def plot_histogram(matcher, matches, non_matches):
    bins = np.linspace(min(min(matches), min(non_matches)), max(max(matches), max(non_matches)), 10)

    # Compute histograms
    num_matches = len(matches)
    nun_non_matches = len(non_matches)
    matches_hist, bins_matches = np.histogram(matches, bins=bins)
    matches_hist = [matches / float(num_matches) for matches in matches_hist]
    non_matches_hist, bins_non_matches = np.histogram(non_matches, bins=bins)
    non_matches_hist = [matches / float(nun_non_matches) for matches in non_matches_hist]

    # Define width of bars
    width = (bins[1] - bins[0]) / 2
    print(matches_hist, bins_matches)
    # Plot bar charts side-by-side
    plt.bar(bins_matches[:-1] - width/2, matches_hist, width=width, alpha=0.5, label='Data1', edgecolor='black')
    plt.bar(bins_non_matches[:-1] + width/2, non_matches_hist, width=width, alpha=0.5, label='Data2', edgecolor='black')

    #plt.hist(matches, bins=bins, alpha=0.5, label='Matching Tables', edgecolor='black')
    #plt.hist(non_matches, bins=bins, alpha=0.5, label='Non Matching Tables', edgecolor='black')

    plt.title(matcher)
    plt.xlabel("Relative amount of matches")
    plt.ylabel("Frequency")

    plt.tight_layout()
    plt.show()


def plot_scatter(matcher, stats):

    aggs_match = {}
    matches = 0
    aggs_no_match = {}
    no_matches = 0
    for stat in stats:
        x = min(stat["source_attr"], stat["target_attr"])
        y = stat["POSITIVES"]

        if stat["ACTUAL_TRUES"] > 0:
            aggs_match.setdefault((x,y), 0)
            aggs_match[(x,y)] += 1
            matches += 1
        else:
            aggs_no_match.setdefault((x,y), 0)
            aggs_no_match[(x,y)] += 1
            no_matches += 1
    for x,y in set(aggs_match.keys()) | set(aggs_no_match.keys()):
        if (x,y) in aggs_match and (x,y) in aggs_no_match:
            if (aggs_match[(x,y)] / matches) > (aggs_no_match[(x,y)] / no_matches):
                plt.scatter([x], [y],color="green",s=[1200 * aggs_match[(x,y)] / matches])
                plt.scatter([x], [y],color="red",s=[1200 * aggs_no_match[(x,y)] / no_matches])
            else:
                plt.scatter([x], [y],color="red",s=[1200 * aggs_no_match[(x,y)] / no_matches])
                plt.scatter([x], [y],color="green",s=[1200 * aggs_match[(x,y)] / matches])
        elif (x,y) in aggs_match:
            plt.scatter([x], [y],color="green",s=[1200 * aggs_match[(x,y)] / matches])
        else:
            plt.scatter([x], [y],color="red",s=[1200 * aggs_no_match[(x,y)] / no_matches])


        #else:
        #    plt.scatter([min(stat["source_attr"], stat["target_attr"])], [stat["POSITIVES"]],color="red",s=[12])

    plt.title(matcher)
    plt.xlabel("Number of Attributes")
    plt.ylabel("Number of Matches")

    plt.tight_layout()
    plt.show()

def analyze_output(csv_path):
    ACTUAL_TRUES = 0
    POSITIVES = 0
    FP = 0
    TP = 0
    FN = 0
    TN = 0
    with open(csv_path, "r") as fp:
        csv_reader = csv.reader(fp)
        rows = list(csv_reader)[1:] # skip first row and first column
        source_attr = len(rows)
        target_attr = len(rows[0][1:])
        for row in rows:
            for cell in row[1:]:
                gt, prob = cell.split(" / ")
                gt = int(gt)
                prob = float(prob)
                if gt == 1:
                    ACTUAL_TRUES += 1
                    if prob >= 0.5:
                        TP += 1
                        POSITIVES += 1
                    else:
                        FN += 1
                else:
                    if prob >= 0.5:
                        POSITIVES += 1
                        FP += 1
                    else:
                        TN += 1
    return {
        "FP": FP,
        "TP": TP,
        "FN": FN,
        "TN": TN,
        "POSITIVES" : POSITIVES,
        "ACTUAL_TRUES": ACTUAL_TRUES,
        "source_attr":source_attr,
        "target_attr":target_attr,
    }
def visualize_cross_results(results_folder_name):
        matcher_histograms = {}
        relative_base_result_path = os.path.join(RESULTS_DIR, results_folder_name)
        for dataset in os.listdir(relative_base_result_path):
            if dataset == PERFORMANCE_FOLDER_NAME:
                continue
            dataset_path = os.path.join(relative_base_result_path, dataset)
            for scenario in os.listdir(dataset_path):
                if scenario == PERFORMANCE_FOLDER_NAME:
                    continue
                scenario_outputs_path = os.path.join(dataset_path, scenario, "_outputs", "MatchingStepLine1")
                for output_csv in os.listdir(scenario_outputs_path):
                    stats = analyze_output(os.path.join(scenario_outputs_path, output_csv))
                    matcher = output_csv[:-len(".csv")]
                    matcher_histograms.setdefault(matcher, {})
                    if stats["ACTUAL_TRUES"] > 0:
                        matcher_histograms[matcher].setdefault("matches" , []).append(stats["POSITIVES"] / float(min(stats["source_attr"], stats["target_attr"])))
                    else:
                        matcher_histograms[matcher].setdefault("non_matches" , []).append(stats["POSITIVES"] / float(min(stats["source_attr"], stats["target_attr"])))
                    matcher_histograms[matcher].setdefault("stats", []).append(stats)
        for matcher, data in matcher_histograms.items():
            plot_histogram(matcher, data["matches"], data["non_matches"])
            plot_scatter(matcher, data["stats"])
if __name__ == "__main__":
    results_folder = "05-27-2024_14-29-05_cloud_all_no_embdi_leapme_new"
    visualize_cross_results(results_folder)
