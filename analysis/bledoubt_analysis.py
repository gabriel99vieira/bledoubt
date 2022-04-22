import argparse
import math
from datetime import datetime
import json
import os

from typing import Dict, List, Set, Any, Callable
import matplotlib

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.colors
import seaborn as sn
import pandas as pd

from bledoubt.classifiers import *
from bledoubt.trajectory import Trajectory

DATETIME_FORMAT = "%a %b %d %X %Z %Y"

# def load_log(filename: str):
#     with open(filename, 'rt') as f:
#         result = json.load(f)
#         devs = result["devices"]
#         dets = result["detections"]
#         trajectories = {}
#         metadata = {}
#         for device in devs:
#             mac = device['address']
#             trajectories[mac] = []
#             metadata[mac] = device

#         for detection in dets:
#             trajectories[detection['mac']].append(detection)

#         return (metadata, trajectories)

def showTrajectoryLengthHistogram(trajectories):
    plt.hist([len(trajectories[mac]) for mac in  trajectories.keys()])
    plt.title('Histogram of Per-Device Detection Count')
    plt.xlabel('Number of detections')
    plt.ylabel('NUmber of devices')
    plt.savefig("length_histogram.png")

def showTrajectoryDiameterHistogram(trajectories):
    plt.hist([getDiameter(trajectories[mac]) for mac in  trajectories.keys()])
    plt.title('Histogram of Per-Device Spatial Extent')
    plt.xlabel('Distance (meters)')
    plt.ylabel('Number of devices')
    plt.savefig("diameter_histogram.png")

def showConfusionMatrices(diam, dur, hyb, eps, out_dir):
    def _forward(x): 
        return np.log10(x+0.5)

    def _inverse(x): 
        return (10**x)-0.5

    labels = [0,1]
    df_diam = pd.DataFrame(diam, columns=labels)
    df_dur = pd.DataFrame(dur, columns=labels)
    df_hyb = pd.DataFrame(hyb, columns=labels)
    df_eps = pd.DataFrame(eps, columns=labels)
    plt.figure(figsize = (10,10))

    plt.subplot(2,2,1)
    sn.heatmap(df_diam, annot=True, cmap="PuBu",cbar=False, norm=matplotlib.colors.FuncNorm((_forward, _inverse), vmin=0.0, vmax=np.max(diam)),fmt="d")
    plt.title('Diameter Classifier (Baseline)',fontsize="large")
    plt.xlabel('Actual label', fontsize="large")
    plt.ylabel('Predicted label',fontsize="large")

    ax = plt.subplot(2,2,2)
    sn.heatmap(df_dur, annot=True, cmap="PuBu",cbar=False, norm=matplotlib.colors.FuncNorm((_forward, _inverse), vmin=0.0, vmax=np.max(dur)), fmt="d")
    plt.title('Duration Classifier (Baseline)', fontsize="large")
    plt.xlabel('Actual label', fontsize="large")
    plt.ylabel('Predicted label', fontsize="large")

    plt.subplot(2,2,3)
    ax = sn.heatmap(df_hyb, annot=True, cmap="PuBu",cbar=False, norm=matplotlib.colors.FuncNorm((_forward, _inverse), vmin=0.0, vmax=np.max(hyb)), fmt="d")
    plt.title('Hybrid Classifier (Baseline)', fontsize="large")
    plt.xlabel('Actual label', fontsize="large")
    plt.ylabel('Predicted label', fontsize="large")

    plt.subplot(2,2,4)
    ax = sn.heatmap(df_eps, annot=True, cmap="PuBu",cbar=False, norm=matplotlib.colors.FuncNorm((_forward, _inverse), vmin=0.0, vmax=np.max(eps)), fmt="d")
    plt.title('Topological Classifier (Ours)', fontsize="large")
    plt.xlabel('Actual label', fontsize="large")
    plt.ylabel('Predicted label', fontsize="large")
    plt.subplots_adjust(hspace=0.5)
    plt.savefig(os.path.join(out_dir, "AggregateConfusionMatrix.png"))

def generateConfusionMatrix(trajectories: Dict[str, List[Dict[str, Any]]], known_macs: Set[str], algorithm: Callable[[List[Dict[str, Any]]], bool]) -> np.ndarray:
    confusion = np.zeros([2,2], dtype=np.int32)
    for mac in trajectories.keys():
        predicted_suspicion = algorithm(trajectories[mac])
        actual_suspicion = (mac in known_macs)
        confusion[int(predicted_suspicion), int(actual_suspicion)] += 1
    return confusion

def plot_in_roc_space(confusion_matrices, labels):
    tprs = [m[1,1] / (m[0,1]+m[1,1]) for m in confusion_matrices]
    fprs = [1- m[0,0] / (m[0,0]+m[1,0]) for m in confusion_matrices]
    plt.plot(fprs, tprs, 'b.', label=labels)
    plt.plot([0,1], [0,1], 'r--')
    plt.xlim([0,1])
    plt.ylim([0,1])
    plt.xlabel("False Positive Rate (1-Specificity)")
    plt.ylabel("True Positive Rate (Sensitivity)")
    plt.title("Receiver Operator Characteristic Plot")
    plt.savefig("roc_plot")

def process_logs(log_dir):
    trajectory_diameter_classifier = DiameterClassifier()
    trajectory_duration_classifier = DurationClassifier()
    trajectory_hybrid_classifier = HybridClassifier()
    trajectory_epsilon_component_classifier = EpsilonComponentClassifier()

    aggregate_diameter = np.zeros([2,2], dtype=np.int32)
    aggregate_duration = np.zeros([2,2], dtype=np.int32)
    aggregate_hybrid = np.zeros([2,2], dtype=np.int32)
    aggregate_epsilon = np.zeros([2,2], dtype=np.int32)
    
    known_macs_per_log = {}
    mac_ground_truth_file = os.path.join(log_dir, 'gt_macs.json')
    with open(mac_ground_truth_file, 'rt') as f:
        known_macs_per_log = json.load(f)

    for log_file in os.listdir(log_dir):
        filename = os.path.join(log_dir, log_file)
        if log_file == 'gt_macs.json':
            continue
        
        metadata, trajectories = Trajectory.get_trajectories_from_log(filename)
        print("\n")
        print("=" * len(filename))
        print(filename)
        print("=" * len(filename))
        print("")

        diam_mat = generateConfusionMatrix(trajectories, known_macs_per_log[log_file], trajectory_diameter_classifier)
        dur_mat = generateConfusionMatrix(trajectories, known_macs_per_log[log_file], trajectory_duration_classifier)
        hybrid_mat = generateConfusionMatrix(trajectories, known_macs_per_log[log_file], trajectory_hybrid_classifier)
        eps_mat = generateConfusionMatrix(trajectories, known_macs_per_log[log_file], trajectory_epsilon_component_classifier)

        aggregate_diameter += diam_mat
        aggregate_duration += dur_mat
        aggregate_hybrid += hybrid_mat
        aggregate_epsilon += eps_mat
        print("Diameter Classifier")
        print(diam_mat)
        print("Duration Classifier")
        print(dur_mat)
        print("Hybrid Classifier")
        print(hybrid_mat)
        print("Epsilon Classifier")
        print(eps_mat)

        print("kilobytes per minute: " +  str((60* os.stat(filename).st_size) / (1024 * max([trajectories[mac].get_duration() for mac in trajectories.keys()]))))

    print('\nAggregate states')
    print('----------------')
    print("Diameter Classifier")
    print(aggregate_diameter)
    print("Duration Classifier")
    print(aggregate_duration)
    print("Hybrid Classifier")
    print(aggregate_hybrid)
    print("Epsilon Classifier")
    print(aggregate_epsilon)

    return aggregate_diameter, aggregate_duration, aggregate_hybrid, aggregate_epsilon

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Generate plots for bledoubt data set.')
    parser.add_argument('-o', metavar='o', type=str, nargs='?', default='figures', help='the output directory')
    parser.add_argument('log_dir', metavar='D', type=str, nargs=1, help='the directory of the log files')
    args = parser.parse_args()
    print(args.o)

    log_dir = args.log_dir[0]
    out_dir = args.o

    if (not os.path.isdir(log_dir)):
        raise ValueError(log_dir + " is not a directory.")
    else:
        aggregate_diameter, aggregate_duration, aggregate_hybrid, aggregate_epsilon = process_logs(log_dir)
        if not os.path.isdir(out_dir):
            os.makedirs(out_dir)
        showConfusionMatrices(
            aggregate_diameter,
            aggregate_duration,
            aggregate_hybrid,
            aggregate_epsilon,
            out_dir
        )

# showConfusionMatrices(
#     np.array([[7322,   12], [ 151,   49]], dtype=np.int32),
#     np.array([[7321,    19], [ 152,   59]], dtype=np.int32),
#     np.array([[7414,   9], [  59,   49]], dtype=np.int32),
#     np.array([[7471, 19], [2, 46]], dtype=np.int32),
# )

#aggregate_length, aggregate_diameter, aggregate_duration, aggregate_hybrid, aggregate_epsilon = process_logs()
#showConfusionMatrices(
#    aggregate_diameter,
#    aggregate_duration,
#    aggregate_hybrid,
#    aggregate_epsilon,
#)

# plot_in_roc_space(
#     [np.array([[7340,    1],
#               [ 152,   48]]),
#     np.array([[7330,    0],
#               [ 162,   49]]),
#     np.array([[7432,    1],
#               [  60,   48]]),
#     np.array([[7489,    4],
#               [   3,   45]])],
#     ["Diameter","Duration","Hybrid","Topological"]
# )