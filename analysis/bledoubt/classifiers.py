from bledoubt.trajectory import Trajectory

class TrajectoryClassifier():
    """Abstract base class for classifiers which determine whether or not 
    a given trajectory corresponds to a suspicious device. Sub-classes should
    overrde the `__call__` operator. 
    """

    def __call__(self, trajectory: Trajectory) -> bool:
        """Returns true if and only if he trajectory is found to be suspcious

        Args:
            trajectory (Trajectory): A collection of beacon detections from one
                device, orderred by increasing timestamp
        """
        raise NotImplementedError()


class DurationClassifier(TrajectoryClassifier):
    """Classifies devices as suspicious when they are near the user over
    a long span of time (though not necessarily continuously).
    """
    def __init__(self, threshold_seconds=300):
        self.threshold_seconds = threshold_seconds
    
    def __call__(self, trajectory: Trajectory) -> bool:
        return trajectory.get_duration() > self.threshold_seconds
    

class DiameterClassifier(TrajectoryClassifier):
    """Classifies devices as suspicious when they are near the user at various
    locations which span a wide distance (though not necessarily following the user).
    """
    def __init__(self, threshold_meters=300):
        self.threshold_meters = threshold_meters
     
    def __call__(self, trajectory: Trajectory) -> bool:
        return trajectory.get_diameter() > self.threshold_meters


class HybridClassifier(TrajectoryClassifier):
    """Classifies devices as suspicious when they are near the user over a long
    span of time as well as a long distance (though neither necessarily continuously).
    """
    def __init__(self, threshold_seconds=300, threshold_meters=300): 
        self.duration_classifier = DurationClassifier(threshold_seconds)
        self.diameter_classifier = DiameterClassifier(threshold_meters)
        
    def __call__(self, trajectory: Trajectory) -> bool:
        return self.duration_classifier(trajectory) and self.diameter_classifier(trajectory)


class EpsilonComponentClassifier(TrajectoryClassifier):
    """Classifies devices as suspicious when they are *continuously* close to the user
    during a long time and over a long distance. Devices are considered continously
    nearby for any sub-trajectory which is epsilon-connected for the given epsilon.
    """
    def __init__(self, threshold_seconds=300, threshold_meters=300, epsilon_seconds=60): 
        self.hybrid_classifier = HybridClassifier(threshold_seconds, threshold_meters)
        self.epsilon_seconds = epsilon_seconds
    
    def __call__(self, trajectory: Trajectory) -> bool:
        components = trajectory.get_epsilon_components(self.epsilon_seconds)
        return any([self.hybrid_classifier(c) for c in components])

