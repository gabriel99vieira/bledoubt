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
