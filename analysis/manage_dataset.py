import argparse
from collections import defaultdict
from datetime import datetime
import os
from typing import List 
import itertools
import json


import pandas as pd
import matplotlib.pyplot as plt
from joypy import joyplot
import numpy as np

from bledoubt.trajectory import Detection, Trajectory
import bledoubt.classifiers

def int_to_mac(x: int) -> str:
    """Translate positive integer into a mac address.
    Implementation stolen shamelessly from 
    https://stackoverflow.com/questions/36857521/how-to-convert-mac-address-to-decimal-in-python

    Args:
        x (int): between 1 and 2^(48)

    Raises:
        ValueError: If outside range

    Returns:
        str: Equivalent mac address
    """
    if x > 2**(8*6) or x < 1:
        raise ValueError("Invalid Mac Address. Must be between 1 and 2^(48)") 
    mac_hex = "{:012x}".format(x)
    mac_str = ":".join(mac_hex[i:i+2] for i in range(0, len(mac_hex), 2))
    return mac_str

def save_mac_mapping(mapping: dict, out_dir: str):
    filename = os.path.join(out_dir, "mac_mapping.json")
    with open(filename, 'wt') as f:
        json.dump(mapping, f)

def anonymize_logs(log_dir: str, out_dir: str):
    """Removes potentially identifying information from the input logs, including
    device names and BLE addresses. Moves each log to a nondescript place on Earth.
    Saves logs to `out_dir` directory.

    Assumes every file in `log_dir` is either a BLEdoubt json log or
    a ground-truth json file (listing suspicious devices for each log)
    named "gt_macs.json".

    Args:
        log_dir (str): the original dataset directory.
        out_dir (str): the directory for the anonymized dataset.
    """
    # The get_next_unused_mac.next_mac_int is a static counter as described here:
    # https://stackoverflow.com/questions/279561/what-is-the-python-equivalent-of-static-variables-inside-a-function
    def get_next_unused_mac():
        next_mac = int_to_mac(get_next_unused_mac.next_mac_int)
        get_next_unused_mac.next_mac_int += 1
        return next_mac
    get_next_unused_mac.next_mac_int = 1

    if not os.path.exists(out_dir):
        os.makedirs(out_dir)

    gt_filename = os.path.join(log_dir, 'gt_macs.json')    

    mac_mapping = defaultdict(get_next_unused_mac)
    for log_basename in os.listdir(log_dir):
        log_name = os.path.join(log_dir, log_basename)
        if (log_name == gt_filename):
            continue
        
        metadata, trajs = Trajectory.get_trajectories_from_log(log_name)

        # Sanitize Metadata
        for mac, device in metadata.items():
            print(device)
            traj = trajs[mac]
            new_mac = mac_mapping[mac]

            device['address'] = new_mac
            device['name'] = ""
            device['type'] = "0x"
            device['id1'] = "0x"
            device['id2'] = "0x"
            device['id3'] = "0x"
            device['manufacturer'] = 0

            # Sanitize mac address from trajectory data
            for detection in traj:
                detection.mac = new_mac

        # Save current sanitized log
        output_log_name = os.path.join(out_dir, log_basename)
        Trajectory.export_to_json(metadata, trajs, output_log_name)
    
    # Save mac address mapping for validation
    save_mac_mapping(mac_mapping, out_dir)

    # Save ground-truth file
    if os.path.exists(gt_filename):
        output_ground_truth = {}
        with open(gt_filename, 'rt') as f:
            input_ground_truth = json.load(f)
            for log_name, mac_list in input_ground_truth.items():
                output_ground_truth[log_name] = [mac_mapping[mac] for mac in mac_list]
        
        if output_ground_truth:
            with open(os.path.join(out_dir, 'gt_macs.json'), 'wt') as f:
                json.dump(output_ground_truth,f)

        
def plot_all_rssis_over_time(trajectories: List[Trajectory], outfile: str):
    start_time = min(*[t[0].timestamp for t in trajectories])
    macs = [t[0].mac for t in trajectories]
    #rssis =  np.array([detection.rssi for detection in trajectory])
    #minutes = np.array([(detection.timestamp - start_time).total_seconds() // 60 for detection in trajectory])
    active_rssis = [[detection.rssi for detection in trajectory] for trajectory in trajectories]
    num_entries = sum([len(rssis) for rssis in active_rssis])
    rssis = [[None]*num_entries for device in active_rssis]
    start_index = 0
    for i, rssi_bloc in enumerate(active_rssis):
        rssis[i][start_index:start_index+len(rssi_bloc)] = rssi_bloc
        start_index += len(rssi_bloc)

    unflat_minutes = [[(detection.timestamp - start_time).total_seconds() // 60 for detection in trajectory] for trajectory in trajectories]
    minutes = list(itertools.chain.from_iterable(unflat_minutes))
    print("Minutes: " +  str(len(minutes)))
    print("Rssis: " +  str(len(rssis[0])) + " x " + str(len(rssis)))
    print([len(col) for col in rssis])
    data = {
        macs[i]: rssis[i] for i in range(len(macs))
    }
    data["minute"] = minutes
    df = pd.DataFrame(data)
    key_set = macs + ['minute']
    joyplot(
        data= df[key_set],
        by='minute',
        figsize=(12, 0.25*len(np.unique(np.array(minutes)))),
        alpha=0.7,
        overlap=2
    )
    plt.savefig(outfile)

def plot_all_timing_over_time(trajectories: List[Trajectory], outfile: str):
    start_time = min(*[t[0].timestamp for t in trajectories])
    macs = [t[0].mac for t in trajectories]
    #rssis =  np.array([detection.rssi for detection in trajectory])
    #minutes = np.array([(detection.timestamp - start_time).total_seconds() // 60 for detection in trajectory])
    active_durations = [[(trajectory[i].timestamp - trajectory[i-1].timestamp).total_seconds() for i in range(1, len(trajectory))] for trajectory in trajectories]
    num_entries = sum([len(durations) for durations in active_durations])
    durations = [[None]*num_entries for device in active_durations]
    start_index = 0
    for i, duration_bloc in enumerate(active_durations):
        durations[i][start_index:start_index+len(duration_bloc)] = duration_bloc
        start_index += len(duration_bloc)

    unflat_minutes = [[(detection.timestamp - start_time).total_seconds() // 60 for detection in trajectory[1:]] for trajectory in trajectories]
    minutes = list(itertools.chain.from_iterable(unflat_minutes))
    print("Minutes: " +  str(len(minutes)))
    print("Time Gaps: " +  str(len(durations[0])) + " x " + str(len(durations)))
    print([len(col) for col in durations])
    data = {
        macs[i]: durations[i] for i in range(len(macs))
    }
    data["minute"] = minutes
    df = pd.DataFrame(data)
    key_set = macs + ['minute']
    joyplot(
        data= df[key_set],
        by='minute',
        figsize=(12, 0.25*len(np.unique(np.array(minutes)))),
        alpha=0.7,
        overlap=2,
        xlim=[0,10], 
        ylim=[0,10],
    )
    plt.savefig(outfile)

def save_charts_for_log(log: str):
    metadata, trajectories = Trajectory.get_trajectories_from_log("logs/{log}".format(log=log))
    #nontrivial = [t[1] for t in filter(lambda t: len(t[1]) > 10 and t[1].get_duration() > 60, trajectories.items())]
    nontrivial = [t[1] for t in filter(lambda t: len(t[1]) > 1, trajectories.items())]
    plot_all_rssis_over_time(nontrivial, "temp/{log_basename}.png".format(log_basename=os.path.splitext(log)[0]))

def get_loud_device_macs(log: str, threshold_rssi: float):
    metadata, trajectories = Trajectory.get_trajectories_from_log("logs/{log}".format(log=log))
    #nontrivial_macs = [t[0] for t in filter(lambda t: len(t[1]) > 1, trajectories.items())]
    loud_devices = []
    for mac, device in metadata.items():
    #for mac in nontrivial_macs:
        average_rssi = trajectories[mac].get_average_rssi()
        if (average_rssi > threshold_rssi):
            loud_devices.append((mac, average_rssi))
    return loud_devices

def get_long_trajectories(log: str, min_num_detections: int):
    metadata, trajectories = Trajectory.get_trajectories_from_log("logs/{log}".format(log=log))
    frequent_devices = []
    for mac, device in metadata.items():
    #for mac in nontrivial_macs:

        if (len(trajectories[mac]) >= min_num_detections):
            frequent_devices.append((mac, len(trajectories[mac])))
    return frequent_devices

def get_suspicious_devices(log, algorithm):
    metadata, trajectories = Trajectory.get_trajectories_from_log("logs/{log}".format(log=log))
    result = []
    for mac in trajectories.keys():
        if algorithm(trajectories[mac]):
            result += [mac]
    return result

def get_suspicious_device_trajs(log, algorithm):
    metadata, trajectories = Trajectory.get_trajectories_from_log("logs/{log}".format(log=log))
    result = []
    for mac in trajectories.keys():
        if algorithm(trajectories[mac]):
            result += [mac]
    return [trajectories[mac] for mac in result]


def plot_trajectories(trajectories: List[Trajectory]):
    colors = np.random.rand(len(trajectories),3)
    macs = []
    
    plt.subplots(len(trajectories), 1)
    for i, trajectory in enumerate(trajectories):
        longs = [det.long for det in trajectory]
        lats = [det.lat for det in trajectory]
        
        plt.subplot(len(trajectories), 1, i+1)
        plt.plot(longs, lats, colors[i,:])
        macs.append(trajectory[0].mac)
        print("lats" )
        print(lats)
        print("longs" )
        print(longs)
    plt.legend(macs)
    plt.savefig("temp/trajs.png")
    

def main():
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers()

    anonymize_parser = subparsers.add_parser('anonymize', help="Remove PII from the logs in the source directory.")
    anonymize_parser.add_argument('source_directory', metavar='S', type=str, nargs=1, help='the directory of the log files')
    anonymize_parser.add_argument('dest_directory', metavar='T', type=str, nargs=1, help='the directory of the output anonymized files')

    args = parser.parse_args()    
    if 'source_directory' in args and 'dest_directory' in args:
        anonymize_logs(args.source_directory[0], args.dest_directory[0])

if __name__ == "__main__":
    main()
