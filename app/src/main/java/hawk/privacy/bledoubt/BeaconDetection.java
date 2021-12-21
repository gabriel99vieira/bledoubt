package hawk.privacy.bledoubt;

import android.location.Location;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

/**
 * A struct representing a single time & place in which a beacon has been observed.
 */
@Entity(primaryKeys = {"bluetoothAddress", "timestamp"})
public class BeaconDetection {

    @NonNull
    public String bluetoothAddress;

    @NonNull
    @TypeConverters(TimestampConverter.class)
    public Date timestamp;
    public double latitude;
    public double longitude;
    public double rssi;

    public BeaconDetection(String bluetoothAddress, Date timestamp, double latitude,
                           double longitude, double rssi) {
        this.bluetoothAddress = bluetoothAddress;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rssi = rssi;
    }

    public BeaconDetection(Beacon beacon, Date timestamp, Location location) {
        this(beacon.getBluetoothAddress(), timestamp, location, beacon.getRssi());
    }

    public BeaconDetection(String bluetoothAddress, Date timestamp, Location location, double rssi) {
        this(bluetoothAddress, timestamp, location.getLatitude(),
                location.getLongitude(), rssi);
    }

    public BeaconDetection(BeaconDetection other) {
        this(other.bluetoothAddress, other.timestamp, other.latitude,
                other.longitude, other.rssi);
    }

    /**
     * Calculates distance in meters from distance in latitude and longitude, given in degrees.
     * Planet assumed to be Earth.
     *
     * Using Haversine formula as seen in this stack overflow post:
     * https://stackoverflow.com/questions/18861728/calculating-distance-between-two-points-represented-by-lat-long-upto-15-feet-acc
     */
    public static double latLongToMeters(double latDegrees1, double longDegrees1, double latDegrees2, double longDegrees2) {
        final double radiusOfEarthMeters = 6378100;
        double dLat = Math.toRadians(latDegrees2 - latDegrees1);
        double dLong = Math.toRadians(longDegrees2 - longDegrees1);

        double haversine = Math.sin(dLat / 2) * Math.sin(dLat / 2) +

                Math.cos(Math.toRadians(latDegrees1)) * Math.cos(Math.toRadians(latDegrees2))
                        * Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1-haversine));
        double distance = radiusOfEarthMeters * centralAngle;
        return distance;
    }

    public double distanceInMeters(BeaconDetection other) {
        return latLongToMeters(this.latitude, this.longitude, other.latitude, other.longitude);
    }

    double timeDifferenceInSeconds(BeaconDetection other) {
        long otherMillis = other.timestamp.getTime();
        long thisMillis = this.timestamp.getTime();
        long durationMillis = (thisMillis - otherMillis);
        return durationMillis / 1000.0;
    }

    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();
        try {
            result.put("mac", bluetoothAddress);
            result.put("t", timestamp);
            result.put("lat", latitude);
            result.put("long", longitude);
            result.put("rssi", rssi);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static BeaconDetection fromJSONObject(JSONObject obj) {
        BeaconDetection result;
        Location loc = new Location("Json");
        try {
            loc.setLatitude(obj.getDouble("lat"));
            loc.setLongitude(obj.getDouble("long"));
            result = new BeaconDetection (
                obj.getString("mac"),
                TimestampConverter.fromTimestamp(obj.getString("t")),
                loc,
                obj.getDouble("rssi")
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}

class TimestampConverter {
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @TypeConverter
    public static Date fromTimestamp(@NonNull String value) {
        try {
            return df.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String toTimestamp(Date timestamp) {
        return df.format(timestamp);
    }
}
