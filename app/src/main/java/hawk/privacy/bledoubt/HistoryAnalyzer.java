package hawk.privacy.bledoubt;

import org.altbeacon.beacon.Beacon;

import java.util.Vector;

public class HistoryAnalyzer {
     //public static Vector<Beacon> findTrackingBeacons( BeaconHistory _history) {
      //  BeaconHistory history = new BeaconHistory(_history);
      //  removeGeostationaryDevices(history);
   // }

    private static void removeGeostationaryDevices(BeaconHistory history) {
        double latMin, latMax, longMin, longMax;
        for (String mac : history.geKnownMacs()) {
            latMin = latMax = longMin = longMax = 0;
            Vector<BeaconDetection> traiectory = history.getSnapshot(mac);
            for (BeaconDetection detection : traiectory) {
                latMin = Math.min(detection.latitude, latMin);
                latMax = Math.max(detection.latitude, latMax);
                longMin = Math.min(detection.longitude, longMin);
                longMax = Math.max(detection.longitude, longMax);
            }

        }
    }

    /**
     * Calculates distance in meters from distance in latitude and longitude, given in degrees.
     * Planet assumed to be Earth.
     *
     * Using Haversine formula as seen in this stack overflow post:
     * https://stackoverflow.com/questions/18861728/calculating-distance-between-two-points-represented-by-lat-long-upto-15-feet-acc
     */
    public static double latLongToMeters(double latDegrees1, double longDegrees1, double latDegrees2, double longDegrees2 ) {
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
}
