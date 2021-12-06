package hawk.privacy.bledoubt;

import android.content.Context;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class HistoryAnalyzer extends Worker {
    public class TopologicalClassifier {
        protected final float epsilonSeconds;
        protected final float minDiameterSeconds;
        protected final float minDurationSeconds;

        public TopologicalClassifier(float epsilonSeconds, float minDiameterMeters, float minDurationMeters) {
            this.epsilonSeconds = epsilonSeconds;
            this.minDiameterSeconds = minDiameterMeters;
            this.minDurationSeconds = minDurationMeters;
        }

        public boolean isSuspicious(Trajectory trajectory) {
            List<Trajectory> epsilon_components = trajectory.getEpsilonComponents(epsilonSeconds);
            //epsilon_components.stream().anyMatch(t
            //        -> t.getDurationInSeconds() > minDurationSeconds && t.getDiameter() > minDiameterSeconds
            //);
            return false;
        }
    }


    static final double MAX_BLE_RANGE_M = 10;
     //public static Vector<Beacon> findTrackingBeacons( BeaconHistory _history) {
      //  BeaconHistory history = new BeaconHistory(_history);
      //  removeGeostationaryDevices(history);
   // }

    private static void removeGeostationaryDevices(BeaconHistory history) {
//        ArrayList<String> macs_to_remove = new ArrayList<>();
//        double latMin, latMax, longMin, longMax;
//        for (String mac : history.getKnownMacs()) {
//            latMin = latMax = longMin = longMax = 0;
//            Vector<BeaconDetection> traiectory = history.getSnapshot(mac);
//            for (BeaconDetection detection : traiectory) {
//                latMin = Math.min(detection.latitude, latMin);
//                latMax = Math.max(detection.latitude, latMax);
//                longMin = Math.min(detection.longitude, longMin);
//                longMax = Math.max(detection.longitude, longMax);
//            }
//            if (latLongToMeters(latMin, longMin, latMax, longMax) < 2 * MAX_BLE_RANGE_M) {
//                macs_to_remove.add(mac);
//            }
//        }
//        history.remove_all(macs_to_remove);
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




    public static final String TAG = "[HistoryAnalyzer]";

    @NonNull
    Context context;
    public HistoryAnalyzer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @Override
    public Result doWork() {
        Log.i(TAG, "Inside");
        BeaconHistory history = BeaconHistory.getAppBeaconHistory(context);
        List<DeviceMetadata> devices =  history.getDeviceList();
        if (!devices.isEmpty())
            Notifications.getInstance().createSuspiciousDeviceNotification(context, devices.get(0));
        return Result.success();
    }

}
