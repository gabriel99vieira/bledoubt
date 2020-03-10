package hawk.privacy.bledoubt;

import java.util.HashMap;
import java.util.Vector;
import java.util.Date;

/**
 * A thread-safe wrapper around a HashTable of beaconsIds -> beaconDetection lists.
 */
public class BeaconHistory {
    private HashMap<String, Vector<BeaconDetection>> detections;

    public BeaconHistory() {
        detections = new HashMap<>();
    }

    /**
     * Append a new detection event onto the history of the specified beacon.
     * @param beaconId
     * @param detectionEvent
     */
    public synchronized void add(String beaconId, BeaconDetection detectionEvent) {
        if (!detections.containsKey(beaconId)) {
            detections.put(beaconId, new Vector<BeaconDetection>());
        }
        detections.get(beaconId).add(detectionEvent);
    }

    /**
     * Get a copy of a the list of all detections of the given beacon.
     * @param beaconId
     * @return
     */
    public synchronized Vector<BeaconDetection> getSnapshot(String beaconId) {
        if (detections.containsKey(beaconId)) {
            return new Vector<>(detections.get(beaconId));
        }
        return new Vector<>();
    }

    /**
     * Remove all devices and detection events from the history.
     */
    public synchronized void clear() {
        detections.clear();
    }

    @Override
    public synchronized String toString() {
        String result = "Beacon History:\n";
        for (String beaconId : detections.keySet()) {
            result += "Beacon ID:" + beaconId +
                       ". Num detections: " + detections.get(beaconId).size() + ".\n";
        }
        return result;
    }

}

