from output_helper import *


PERFORMANCES = {
    "Accuracy": "Accuracy",
    "F1": "F1",
    "Precision": "Precision",
    "Recall": "Recall"
}

CONFIGS = [(-1,-1),(0,1), (1,0), (1,1), (1,2), (2,2), (3,3), (0,0)]

benchmark = import_benchmark("/home/fabian/Desktop/MP/repos/schematch/results/03-13-2024_16-46-15_topK_SM_Evaluation", PERFORMANCES)

gen_benchmark = generalize_benchmark_matchers(benchmark)

gen_benchmark = gen_benchmark["_performances"]["EmbedAlignMatcher"]

def calculate_f1_score(precision, recall):
    if precision == 0 or recall == 0:
        return 0  # Avoid division by zero
    else:
        return 2 * (precision * recall) / (precision + recall)


for config in CONFIGS:
    perf = [bench for bench in gen_benchmark if int(bench["props"]["topKRow"]) == config[0] and int(bench["props"]["topKCol"]) == config[1]][0]
    #print(perf)
    print(config, perf["Precision"], perf["Recall"], calculate_f1_score(perf["Precision"], perf["Recall"]))
