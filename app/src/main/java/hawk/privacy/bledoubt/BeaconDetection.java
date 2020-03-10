package hawk.privacy.bledoubt;

import org.altbeacon.beacon.Beacon;

import java.util.Date;

/**
 * A struct representing a single time & place in which a beacon has been observed.
 */
public class BeaconDetection {
    public Date timestamp;
    public double latitude;
    public double longitude;
    public double distanceEstimate;

    public BeaconDetection(Date timestamp, double latitude, double longitude, double distance) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceEstimate = distance;
    }

    public BeaconDetection(BeaconDetection other) {
        this(other.timestamp, other.latitude, other.longitude, other.distanceEstimate);
    }



}
