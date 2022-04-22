import os

from datetime import datetime
from typing import List

from bledoubt.trajectory import Trajectory
from bledoubt.trajectory_fusion import fuse_trajectories_greedy_backwards

def get_num_good_links(trajs: List[Trajectory], interval_sec: float, start: datetime):
    good_links_count = 0
    #print("Start")
    for traj in trajs:
        last_period = traj[0].diff_seconds_from_time(start) // interval_sec
        last_mac = [traj[0].mac]
        debug_str = last_mac[0]
        for detection in traj[1:]:
            next_detection_period = detection.diff_seconds_from_time(start) // interval_sec
            if next_detection_period > last_period:
                debug_str += "||" + detection.mac
                if last_mac[0].lower() == detection.mac.lower():
                    debug_str+=":)"
                    good_links_count += 1
                #else:
                    #print(last_mac[0], detection.mac)
                last_period = next_detection_period
                last_mac[0] = detection.mac
        #print (debug_str)
    #print ("End")
    return good_links_count


log_dir = './logs'
total_traj = 0
total_fused = 0
total_frags = 0
total_consistent = 0
total_good_links = 0
total_opt_good_links = 0
for log in os.listdir(log_dir):
    if log == 'gt_macs.json':
        continue
    print('=' * len(log))
    print(log)
    print('=' * len(log))
    metadata, trajectories = Trajectory.get_trajectories_from_log(os.path.join(log_dir, log))
    nontrivial = [t[1] for t in filter(lambda t: len(t[1]) > 10 and t[1].get_duration() > 60, trajectories.items())]
    print('Num Trajectories: {}'.format(len([t for t in nontrivial])))
    total_traj += len([t for t in nontrivial])
    fused_original = fuse_trajectories_greedy_backwards(nontrivial)
    print('Num fused: {}'.format(len(nontrivial) - len(fused_original)))
    total_fused += len(nontrivial) - len(fused_original)
    trajectory_frags = []
    interval_sec = 5*60
    start_time = min([t[0].timestamp for t in nontrivial])
    for trajectory in nontrivial:
        new_frags = trajectory.split_at_time_interval(interval_sec, start_time)
        trajectory_frags += new_frags

    print('Num Frags: {}'.format(len(trajectory_frags)))
    total_frags += len(trajectory_frags)
    fused = fuse_trajectories_greedy_backwards(trajectory_frags)
    print('Num after fusion: {}'.format(len(fused)))
    total_fused += len(fused)
    consistent = [f for f in fused if f.has_consistent_mac()]
    print('Num consistent: {}'.format(len(consistent)))
    total_consistent += len(consistent)
    num_good_links_optimal = get_num_good_links(nontrivial, interval_sec, start_time)
    print('Optimal good links: {}'.format(num_good_links_optimal))
    total_opt_good_links += num_good_links_optimal
    num_good_links_empirical = get_num_good_links(fused, interval_sec, start_time)
    print('Actual good links: {}'.format(num_good_links_empirical))
    total_good_links += num_good_links_empirical

print("""Trajectories {}
Fused {}
Frags {}
Consistent {}
Optimal Good Links {}
Actual Good Links {}""".format(total_traj, total_fused, total_frags, total_consistent, total_opt_good_links, total_good_links))




