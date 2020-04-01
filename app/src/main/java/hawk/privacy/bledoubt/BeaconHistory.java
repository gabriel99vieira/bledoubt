package hawk.privacy.bledoubt;

import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A thread-safe wrapper around a HashTable of beaconsIds -> beaconDetection lists.
 */
public class BeaconHistory {
    protected static final String TAG = "[BeaconHistory]";
    private HashMap<String, Vector<BeaconDetection>> detections;
    private HashMap<String, DeviceMetadata> metadata;
    public BeaconHistory() {
        detections = new HashMap<>();
        metadata = new HashMap<>();
    }

    /**
     * TODO: Make a copy constructor.
     * @param other
     */
    public BeaconHistory (BeaconHistory other) {

    }

    public synchronized ArrayList<DeviceMetadata> getMainMenuViewModels() {
        ArrayList<DeviceMetadata> result = new ArrayList<>();
        for (String mac : getKnownMacs()) {
            result.add(metadata.get(mac));
        }
        return result;
    }

    public ArrayList<String> getKnownMacs() {
        ArrayList<String> macs = new ArrayList();
        for (String mac : detections.keySet()) {
            macs.add(mac);
        }
        return macs;
    }

    public synchronized void remove_all(Collection<String> macs) {
        for (String mac : macs) {
            this.detections.remove(mac);
            this.metadata.remove(mac);
        }
    }

    /**
     * Append a new detection event onto the history of the specified beacon.
     * @param beaconId
     * @param detectionEvent
     */
    public synchronized void add(String beaconId, BeaconDetection detectionEvent) {
        if (!detections.containsKey(beaconId)) {
            detections.put(beaconId, new Vector<BeaconDetection>());
            metadata.put(beaconId, new DeviceMetadata("New Device",beaconId,0));
        }
        detections.get(beaconId).add(detectionEvent);
        metadata.get(beaconId).incrementDetections();
    }

    /**
     * Get a copy of a the list of all detections of the given beacon.
     * @param beaconId
     * @return
     */
    public synchronized Vector<BeaconDetection> getSnapshot(String mac) {
        if (detections.containsKey(mac)) {
            return new Vector<>(detections.get(mac));
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

    /**
     * TODO: This is hard to read and should be refactored, assuming it even works.l[p
     * @return A json object describing the state of this history.
     * @throws JSONException
     */
    public synchronized JSONObject toJSONObject() throws JSONException {

        JSONArray devices = new JSONArray();
        for (Map.Entry device : detections.entrySet()) {
            JSONArray json_detections = new JSONArray();
            for (BeaconDetection detection : (Vector<BeaconDetection>) device.getValue()) {
                json_detections.put(detection.toJSONObject());
            }
            JSONObject json_device_entry = new JSONObject();
            json_device_entry.put("id", (String) device.getKey());
            json_device_entry.put("history", json_detections);
            devices.put(json_device_entry);
        }
        JSONObject root_object = new JSONObject();
        root_object.put("devices", devices);
        return root_object;
    }

    public synchronized void save() {
        String result = "";
        try {
            result = this.toJSONObject().toString(2);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter file = new FileWriter("device_history.json")) {
            file.write(result);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, result);
    }


}

