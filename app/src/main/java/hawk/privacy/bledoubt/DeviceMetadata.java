package hawk.privacy.bledoubt;

import org.altbeacon.beacon.Beacon;

import androidx.annotation.NonNull;

public class DeviceMetadata {
    private String identifier;
    private Beacon beacon;
    private int numDetections;
    private BeaconType type;

    public DeviceMetadata (Beacon beacon, BeaconType type) {
        this.identifier = generateIdentifier(beacon, type);
        this.beacon = beacon;
        this.type = type;
        this.numDetections = 0;
    }

    public DeviceMetadata(DeviceMetadata other) {
        this(other.beacon, other.type);
        this.numDetections = other.numDetections;
    }

    private static String generateIdentifier(Beacon beacon, BeaconType type) {
        String id = "";
        String name = beacon.getBluetoothName();
        if (name != null && name != "") {
            id += name;
        } else {
            id += beacon.getBluetoothAddress();
        }
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Beacon getBeacon() {
        return this.beacon;
    }

    public void setIdentifier(@NonNull final String identifier) {
        this.identifier = identifier;
    }

    public int getNumDetections() {
        return numDetections;
    }

    public void incrementDetections() {
        numDetections += 1;
    }
}
