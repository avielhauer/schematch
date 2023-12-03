'''
A simple script to execute ADnEV on similarity matrices.
Code mainly taken and modified from: https://github.com/shraga89/DSMA
'''

import math
import sys
from os import listdir
from os.path import isfile, join
import DataHandler as DH
import numpy as np
import tensorflow as tf
graph = tf.get_default_graph()


Y_HAT_SINGLE_FALLBACK = -1
i = 5
crnn_model_adapt = DH.data_loader("./models/11_11_2018_08_42/crnn_adapt_no_attention_model_fold_" + str(i))
crnn_model_eval = DH.data_loader("./models/11_11_2018_08_42/crnn_eval_model_fold_" + str(i))

def deep_adapt_and_evaluate(X_seq, adaptor=crnn_model_adapt, evaluator=crnn_model_eval):
    with graph.as_default():
        yhat_full = adaptor.predict_classes(X_seq, verbose=2)
    yhat_full = np.array(yhat_full.reshape(yhat_full.shape[1:-1] + (1,)))
    try:
        # 4 x 4 matrix or something like that is minimum input for model, else dimensions too small
        # at that point fall back to some default val, so ev
        with graph.as_default():
            yhat_single = evaluator.predict(X_seq, verbose=2)
    except:
        yhat_single = Y_HAT_SINGLE_FALLBACK
    k_adapt = 0
    k_adapt += 1
    yhat_full = np.array(yhat_full.reshape((1,) + yhat_full.shape))
    try:
        with graph.as_default():
            yhat_new = evaluator.predict(yhat_full, verbose=2)
    except:
        yhat_new = Y_HAT_SINGLE_FALLBACK
    while yhat_new > yhat_single:
        X_seq = yhat_full
        with graph.as_default():
            yhat_full = adaptor.predict_classes(X_seq, verbose=2)
        yhat_full = np.array(yhat_full.reshape(yhat_full.shape[1:-1] + (1,)))
        yhat_single = yhat_new
        k_adapt += 1
        with graph.as_default():
            yhat_new = evaluator.predict(X_seq, verbose=2)
    return X_seq, yhat_single

def match(sm_dir):
    sm_dir = "/adnev/DSMA/"+sm_dir
    csvs = [f for f in listdir( sm_dir) if isfile(join(sm_dir, f))]
    max_ev = float("-inf")
    max_sm = None
    for csv_file in csvs:
        with open(sm_dir+"/"+csv_file, "r") as f:
            lines = f.readlines()
            sm = np.array([map(lambda x: [float(x.strip())], ",".join(lines).split(","))])
            sm, ev = deep_adapt_and_evaluate(sm)
            if ev > max_ev:
                max_sm = sm

    dim = int(math.sqrt(max_sm.shape[1]))

    return "\n".join([
        " ".join(
            [str(x) for x in column]
        )
        for column in max_sm.reshape([dim,dim])]
    )


if __name__ == "__main__":
    matrix =np.array([map(lambda x: [x],[
        1,0,0,0,0,0,0,
        0,1,0,0,0,0,0,
        0,0,1,0,0,0,0,
        0,0,0,1,0,0,0,
        0,0,0,0,1,0,0,
        0,0,0,0,0,1,0,
        0,0,0,0,0,0,1,
    ])])
    deep_adapt_and_evaluate(crnn_model_adapt, crnn_model_eval,matrix)

