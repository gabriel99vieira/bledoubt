package hawk.privacy.bledoubt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;

public class Trajectory implements  Iterable<BeaconDetection> {
    List<BeaconDetection> detections;

    public Trajectory(List<BeaconDetection> detections) {
       this.detections = detections;
    }

    public Trajectory(Trajectory other) {
        for (BeaconDetection det : other.detections) {
            this.detections.add(new BeaconDetection(det));
        }
    }

    public Iterator<BeaconDetection> iterator() {
        return detections.iterator();
    }

    @NonNull
    @Override
    public String toString() {
        if (detections.isEmpty()) {
            return "Empty Trajectory";
        }
        else {
            return "Trajectory<" + detections.get(0).bluetoothAddress + ", Length " + detections.size();
        }
    }
}
