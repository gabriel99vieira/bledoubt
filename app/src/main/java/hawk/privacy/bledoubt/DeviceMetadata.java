package hawk.privacy.bledoubt;

import androidx.annotation.NonNull;

public class DeviceMetadata {
    private String identifier;
    private String mac;
    private int numDetections;

    public DeviceMetadata(@NonNull final String identifier, @NonNull final String mac, int num_detections) {
        this.identifier = identifier;
        this.mac = mac;
        this.numDetections = num_detections;
    }

    public String identifierFromMac(String mac) {
        return "";
    }

    public String getIdentifier() {
        return identifier;
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
