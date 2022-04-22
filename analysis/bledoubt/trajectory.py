from datetime import datetime, time, timedelta
from collections import namedtuple
from typing import List, Dict, Any, Optional
import json
import math

DATETIME_FORMAT = '%a %b %d %X %Z %Y'


class Detection:
    """Represents a single BLE advertisement detected and recorded by a smart phone, 
    containing the associated timestamp, rssi, mac address, and gps coordinates.
    """

    def __init__(self, lat: float, long: float, mac: str, rssi: int, timestamp: datetime):
        self.lat = lat
        self.long = long
        self.mac = mac
        self.rssi = rssi
        self.timestamp = timestamp

    def diff_seconds(self, other):
        """Get the number of seconds from the other detection to this detection. This number
        will be positive if the other detection is in the past.

        Args:
            other (Detection)
        Returns:
            float: number of seconds
        """
        return (self.timestamp - other.timestamp).total_seconds()

    def diff_seconds_from_time(self, time: datetime):
        """Get the number of seconds from the other detection to this detection. This number
        will be positive if the other detection is in the past.

        Args:
            other (Detection)
        Returns:
            float: number of seconds
        """
        return (self.timestamp - time).total_seconds()

    @staticmethod
    def from_json(json: Dict[str, Any]):
        return Detection(
            json["lat"], 
            json["long"], 
            json["mac"], 
            json["rssi"], 
            datetime.strptime(json["t"], DATETIME_FORMAT)
        )

    
    def to_json(self):
        return {
            'lat': self.lat,
            'long': self.long,
            'mac': self.mac,
            'rssi': self.rssi,
            't': datetime.strftime(self.timestamp.astimezone(), DATETIME_FORMAT)
        }

    def __eq__(self, o: object) -> bool:
        return self.lat == o.lat and\
            self.long == o.long and\
            self.mac == o.mac and\
            self.rssi == o.rssi and\
            self.timestamp == o.timestamp 

class Device:
    pass

class Trajectory():
    """A collection of BLE Detections associated with a single device, 
    ordered by timestamp from earliest to latest.
    """
    TimeRange = namedtuple('TimeRange', 'start end')

    def __init__(self, detections: List[Detection]):
        self._detections = detections

    def __getitem__(self, index):
        return self._detections[index]

    def __setitem__(self, index, value):
        self._detections[index] = value

    def __len__(self):
        return len(self._detections)

    def __eq__(self, o: object) -> bool:
        return len(self) == len(o) and all([self[i] == o[i] for i in range(len(self))])

    def __repr__(self) -> str:
        return '<Trajectory: ' + str([d.rssi for d in self._detections]) + '>'

    def append(self, detection: Detection):
        self._detections.append(detection)

    def get_duration(self) -> float:
        """Get the duration between the first and last timestamps in this trajectory

        Returns:
            float: duration of the trajectory
        """
        if self._detections == []:
            return 0
        return (self[-1].timestamp - self[0].timestamp).total_seconds()

    def get_diameter(self) -> float:
        """Get the furthest distance in meters from any two points at which a detection
        in this trajectory was measured.

        Returns:
            float: The diameter of the set of positions in the trajectory
        """
        max_distance = 0
        for det1 in self:
            for det2 in self:
                max_distance = max(max_distance, self.lat_long_to_meters(det1.lat, det1.long, det2.lat, det2.long))
        return max_distance

    def is_near(self, other, epsilon_seconds: float):
        """Determine if trajectories which overlap or whose start and end times are within
         `epsilon_seconds` of each other. 

        Args:
            other (Trajectory): another trajectory
            epsilon_seconds (float): duration after which trajectories are "far apart"

        Returns:
            bool: true if the trajectories are near in time
        """
        return math.fabs(self[0].diff_seconds(other[-1])) < epsilon_seconds or \
               math.fabs(other[0].diff_seconds(self[-1])) < epsilon_seconds

    def is_near_time_range(self, time_range: TimeRange, epsilon_seconds: float):
        """Determine if trajectories which overlap or whose start and end times are within
         `epsilon_seconds` of each other. 

        Args:
            other (Trajectory): another trajectory
            epsilon_seconds (float): duration after which trajectories are "far apart"

        Returns:
            bool: true if the trajectories are near in time
        """
        return math.fabs(self[0].diff_seconds_from_time(time_range.end)) < epsilon_seconds or \
               math.fabs((time_range.start - self[-1].timestamp).total_seconds()) < epsilon_seconds

    def get_time_range(self):
        """
        Return the Timerange between the first and last Detection in this Trajectory.
        """
        return Trajectory.TimeRange(start=self._detections[0].timestamp, end=self._detections[-1].timestamp)

    def get_average_rssi(self):
        """
        Calculate the average RSSI from all Detections in this Trajectory.
        """
        return sum([det.rssi for det in self]) / len([det.rssi for det in self])

    def overlaps_trajectory(self, other):
        """Determine if the two trajectories overlap in time

        Args:
            other ([Trajectory]): another trajectory

        Returns:
            bool: true if trajectories overlap in time
        """
        return self.overlaps_time_range(other.get_time_range())

    def overlaps_time_range(self, time_range: TimeRange) -> bool:
        """Determine if this trajectory overlaps the provded time range.

        Args:
            time_range ([TimeRange]): a period of time

        Returns:
            bool: true if trajectories overlap in time
        """
        return (self[0].diff_seconds_from_time(time_range.start) >= 0 and self[0].diff_seconds_from_time(time_range.end) <= 0) \
                or (self[0].diff_seconds_from_time(time_range.start) <= 0 and self[-1].diff_seconds_from_time(time_range.start) >= 0)

    def concat(self, other):
        """Extend this trajectory by appending the detections from other to this trajectory's detections.
        Detections may end up out of order if `other` overlaps or precedes this trajectory.

        Args:
            other (Trajectory): another trajectory

        Returns:
            Trajectory: the lengthened trajectory
        """
        result = Trajectory(self._detections + other._detections)
        return result

    def has_consistent_mac(self):
        """Returns True if every detection in the trajectory has the same mac address.
        Used for validation of trajectory fusion algorithm.

        Returns:
            bool: True if all one mac.
        """

        return len(self) == 0 or all([det.mac == self._detections[0].mac for det in self._detections])

    @staticmethod
    def lat_long_to_meters(lat1: float, long1: float, lat2: float, long2: float):
        """
        Calculate the geodesic distance between two points on Earth using the Haversine formula.

        Returns: 
            float: distance
        """
        radius_of_earth = 6378100

        dlat = math.radians(lat2 - lat1)
        dlong = math.radians(long2 - long1)
        haversine = math.sin(dlat/2) * math.sin(dlat/2) + \
                    math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlong/2) *math.sin(dlong/2)
        central_angle = 2 * math.atan2(math.sqrt(haversine), math.sqrt(1-haversine))
        distance = radius_of_earth * central_angle
        return distance

    def get_epsilon_components(self, epsilon_seconds: float): # -> List[Trajectory]
        """Breaks this trajectory into subtrajectories which are epsilon-connected.
        
        Epsilon-connected components are a disjoint union of subtrajectories which are
        separated in time by more than `epsilon_seconds`, and for which any timestamp
        within a component is no more than `epsilon_seconds` different from the preceeding
        and following timestamps within that component.

        Args:
            epsilon_seconds ([type]): the longest time possible between adjecent detections
            in the same contiguous component.

        Returns:
            components (List[Trajectory]): the epsilon-components of this trajectory
        """
        components = [[self[0]]]
        current_component = 0
        for i in range(1, len(self)):
            if (self[i].diff_seconds(self[i-1]) < epsilon_seconds):
                components[current_component].append(self[i])
            else:
                components.append([self[i]])
                current_component = current_component+1

        return [Trajectory(component) for component in components]

    def split_at_time_interval(self, interval_in_seconds: float, start_time: Optional[datetime] = None):
        """Splits up the trajectory into subtrajectories, none of which 
        may be longer than `interval_in_seconds`. 

        This simulates the data available from a device in privacy mode.

        Args:
            interval_in_seconds (float): the max subtrajectory duration
            start_time (Optional datetime): the begining of the first interval. 
                If None (default), the first interval begins at the start time
                of the trajectory.

        Returns:
            List[Trajectory]: subtrajectories with shorter durations.
        """
        if len(self) == 0:
            return []

        component_start = start_time if start_time else self[0].timestamp
        
        components = [[self[0]]]
        current_component = 0
        for detection in self[1:]:
            if detection.diff_seconds_from_time(component_start) < interval_in_seconds:
                components[current_component].append(detection)
            else:
                components.append([detection])
                current_component += 1
                first_timestamp = components[current_component][0].timestamp
                component_start = first_timestamp - timedelta(seconds=(first_timestamp - component_start).total_seconds() % interval_in_seconds)
        
        return [Trajectory(component) for component in components]


    @staticmethod
    def get_trajectories_from_log(filename: str):
        """Read all the trajectories from a single BLEDoubt log file.

        Args:
            filename (str)

        Returns:
            metadata (List[Dict])
            trajectories (List[Trajectory])
        """
        with open(filename, 'rt') as f:
            result = json.load(f)
            devices = result["devices"]
            detections = result["detections"]

            trajectories: Dict[str, Trajectory] = {}
            metadata: Dict[str, Dict[str, Any]] = {}

            for device in devices:
                mac = device['address']
                trajectories[mac] = Trajectory([])
                metadata[mac] = device

            for detection in detections:
                trajectories[detection['mac']].append(Detection.from_json(detection))

            return (metadata, trajectories)

    @staticmethod 
    def export_to_json(metadata: Dict[str, dict], trajectories: Dict[str, Any], filename: str):
        md_list = [dct for dct in metadata.values()]
        detections = [det.to_json() for traj in trajectories.values() for det in traj]
        log_output = {
            'devices': md_list,
            'detections': detections
        }
        with open(filename, 'wt') as f:
            json.dump(log_output, f)
