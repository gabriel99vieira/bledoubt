import argparse
from typing import List, NamedTuple, Tuple

from collections import namedtuple
from functools import reduce

import numpy as np
from scipy.stats import ks_2samp

from .trajectory import Trajectory

# 95% confidence ?
ks_test_threshold = 0.01
machine_eps = 1e-7




def rssi_ks_test(traj1: Trajectory, traj2: Trajectory):
    """
    Perform a two-sample KS test to compare the RSSI fingerprints of two trajectories.
    This was developed as an attempt to fuse the trajectories of devices in privacy
    mode, although experiments have not proved out.

    """
    rssis1 = np.array([detection.rssi for detection in traj1])
    rssis2 = np.array([detection.rssi for detection in traj2])
    
    result = ks_2samp(rssis1, rssis2, alternative='two-sided')
    return result

def is_fusion_valid(trajectory1: Trajectory, time_range: Trajectory.TimeRange):
    """Return true if the trajectory is within a minute of the time range but does not overlap it.

    Args:
        trajectory1 (Trajectory)
        time_range (Trajectory.TimeRange)

    Returns:
        bool
    """
    return trajectory1.is_near_time_range(time_range, epsilon_seconds=60)  \
            and not trajectory1.overlaps_time_range(time_range)

def merge_subtrajectories(subtrajectories: List[Trajectory]):
    """Merge the subtrajectories after orderring them by timestamp

    Args:
        sub_trajectories (List[Trajectory]): component subtrajectories (not necessarily in order)

    Returns:
        merged trajectory
    """
    ordered = sorted(subtrajectories, key=lambda traj: traj.get_time_range().start)
    return reduce(lambda l, r: l.concat(r), ordered)


def extend_trajectory(trajectory: Trajectory, candidates: List[Trajectory]):
    """Maximally extend a copy of `trajectory` with any candidate trajectories which match
    its RSSI statistics. Multiple candidates may be combined if they fit well together.

    Args:
        trajectory (Trajectory): [description]
        candidates (List[Trajectory]): [description]

    Returns:
        [type]: [description]
    """
    augmented = [trajectory] + candidates
    indices = get_maximal_trajectory_indices(augmented, 0)
    return merge_subtrajectories([augmented[idx] for idx in indices])
    
def unify_time_ranges(tr1: Trajectory.TimeRange, tr2: Trajectory.TimeRange):
    """Get the smallest timerange which includes the two input timeranges

    Args:
        tr1 (Trajectory.TimeRange)
        tr2 (Trajectory.TimeRange)

    Returns:
        Trajectory.TimeRange
    """
    return Trajectory.TimeRange(start=min(tr1.start, tr2.start), end=max(tr1.end, tr2.end))

def get_maximal_trajectory_indices(trajectories: List[Trajectory], start_index: int) -> List[int]:
    """Starting with the trajectory at `start_index`, work through the list of trajectories 
    attempting to concetenate them into the longest possible valid supertrajectory with a greedy
    strategy.

    Trajectories are only added to the supertrajectory if they do not overlap the current 
    supertrajectory, they appear within a minute of the current supertrajectory, and they are
    similar to the most recent addition to the supertrajectory in RSSI distribution as 
    demonstrated by a 2-sampled Kolmogorov-Smirnov test.

    Args:
        trajectories (List[Trajectory]): a list of trajectories, possibly to be fused
        start_index (int): ending trajectory 

    Returns:
        List[int]: the indices of trajecotries fused into the supertrajectory, possibly singleton
    """


    # No extension possible; return.
    if len(trajectories) == 1:
        return [0]

    last_traj = trajectories[start_index]
    time_range = trajectories[start_index].get_time_range()
    fused_indices = [start_index]
    valid_indices = [i for i in range(len(trajectories)) if is_fusion_valid(trajectories[i], time_range)]

    while valid_indices:
        scores: Tuple(int, float) = [(-1, ks_test_threshold - machine_eps)] + [(i, rssi_ks_test(last_traj, trajectories[i]).pvalue) for i in valid_indices]
        
        max_idx, _ = max(scores, key=lambda pair: pair[1])
    
        # No viable candidates; return
        if max_idx == -1:
            return fused_indices
    
        else:    
            last_traj = trajectories[max_idx]
            fused_indices.append(max_idx)
            time_range = unify_time_ranges(time_range, trajectories[max_idx].get_time_range())
            valid_indices = [i for i in range(len(trajectories)) if is_fusion_valid(trajectories[i], time_range)]

    return fused_indices        


def fuse_trajectories_greedy_backwards(trajectories: List[Trajectory]) -> List[Trajectory]:
    """Work backwards greedily through the list of trajectories to
    produce the largest possible collection of fused trajectories, where each 
    trajectory can appear in exactly one fused trajectory. Some or all trajectories
    may appear unaltered after fusion.

    Args:
        trajectories (List[Trajectory]): _description_

    Returns:
        List[Trajectory]: The fused trajectories
    """
    reverse_order = sorted(trajectories, key=lambda t: t[0].timestamp, reverse=True)
    fused_trajectories: List[Trajectory] = []

    while reverse_order != []:
        indices = sorted(get_maximal_trajectory_indices(reverse_order, 0))
        sub_trajectories = [reverse_order[i] for i in indices]
        fused_trajectories.append(merge_subtrajectories(sub_trajectories))

        index_set = set(indices)
        reverse_order = [traj for (i, traj) in enumerate(reverse_order) if i not in index_set]

    return fused_trajectories
    


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("log", metavar="L", type=str, nargs=1, help='The log to analyse')
    parser.add_argument("macs", metavar="M", type=str, nargs='+', help='The mac address of the trajectory')
    args = parser.parse_args()

    metadata, trajectories = Trajectory.get_trajectories_from_log(args.log[0])
    interval = 60*5

    print("======================")
    print("Stats for Trajectories")
    print("======================\n")
    for mac in args.macs:
        print(mac)
        print('-'*(5+6*2))
        traj = trajectories[mac]
        components = traj.split_at_time_interval(interval)
        if len(components) < 2:
            print(f"Trajectory too brief. Should be more than {interval} seconds long. Aborting.")
            exit()
        for i in range(1, len(components)):
            print(rssi_ks_test(components[i-1], components[i]))
    
    print("\n====================")
    print("Stats for Crossovers")
    print("====================\n")
    if len(args.macs) > 1:

        # For each pair of macs
        for mac1_idx, mac1 in enumerate(args.macs):
            for mac2 in args.macs[mac1_idx:]:
                if mac1 != mac2:
                    print(f"{mac1} and {mac2}")
                    print('-'*(2*(5+6*2) + 5))
                    traj1 = trajectories[mac1] 
                    traj2 = trajectories[mac2] 
                    components1 = traj1.split_at_time_interval(interval)
                    components2 = traj2.split_at_time_interval(interval)

                    # For each pair of components
                    for comp1 in components1:
                        for comp2 in components2:
                            print(rssi_ks_test(comp1, comp2))