package hawk.privacy.bledoubt;

import android.location.Location;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

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

    public BeaconDetection(Date timestamp, Location location, double distance) {
        this(timestamp, location.getLatitude(), location.getLongitude(), distance);
    }

    public BeaconDetection(BeaconDetection other) {
        this(other.timestamp, other.latitude, other.longitude, other.distanceEstimate);
    }

    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();
        try {
            result.put("t", timestamp);
            result.put("lat", latitude);
            result.put("long", longitude);
            result.put("d", distanceEstimate);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
