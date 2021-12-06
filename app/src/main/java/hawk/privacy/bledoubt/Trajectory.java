package hawk.privacy.bledoubt;

import static java.time.temporal.ChronoUnit.SECONDS;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

import org.altbeacon.beacon.Beacon;

import kotlin.time.Duration;

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

    public double getDurationInSeconds() {
        /** Return the duration from the first to the last detection as a .
         */
        long startMillis = this.detections.get(0).timestamp.getTime();
        long endMillis = this.detections.get(detections.size() - 1).timestamp.getTime();
        long durationMillis = (endMillis - startMillis);
        return durationMillis / 1000.0;
    }



    public double getDiameterInMeters() {
        /** Return the duration from the first to the last detection as a .
         *
         * Algorithm Notes:
         *
         * 1. Calculate the convex hull using Andrew's Monotone Chain algorithm, in lat-long space.
         * 2. Find max distance Shamos's method of the Rotating Calipers.
         *
         *
         * Using the method of the Rotating Calipers from Toussaint, 1983
         * https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.155.5671&rep=rep1&type=pdf
         *
         * Based on PhD thesis of Shamos 1978
         * http://euro.ecom.cmu.edu/people/faculty/mshamos/1978ShamosThesis.pdf
         */
        if (detections.isEmpty())
            return 0;

        ArrayList<Point2d> convex_hull = getConvexHull();

        //ArrayList<Point3D> sphericalPoints = transformToEuclideanSpace(centroid_lat, centroid_long);
        //Point3D origin = sphericalPoints.get(0);
        //double diameterLowerBound = 0;
        //for (Point3D point : sphericalPoints) {
        //    diameterLowerBound = Math.max(diameterLowerBound, point.distance(origin));
        //}
        // TODO Finish
        return 0;
    }

    /**
     * Get the convex hull in lat-long space of the detections in this trajectory listed in
     * cannonical (counter-clockwise) order.
     *
     * Runtime should be O(n log n).
     *
     * Reference: Andrew's Monotone Chain algorithm
     * - A. M. Andrew, "Another Efficient Algorithm for Convex Hulls in Two Dimensions", Info. Proc.
     *   Letters 9, 216-219 (1979).
     */
    public ArrayList<Point2d> getConvexHull() {
        // Sort vertices in lexical order.
        ArrayList<Point2d> latLongs = new ArrayList<>();
        for (BeaconDetection det : this.detections) {
            latLongs.add(new Point2d(det.longitude, det.latitude));
        }
        Collections.sort(latLongs, new Point2d.LexicalComparator());

        // Get convex curve below
        ArrayList<Point2d> lowerHull = new ArrayList<>();
        for (Point2d p : latLongs) {
            while (lowerHull.size() > 1
                    && Point2d.orderedClockwise(
                        lowerHull.get(lowerHull.size()-2),
                        lowerHull.get(lowerHull.size()-1),
                        p
            )) {
                lowerHull.remove(lowerHull.size() - 1);
            }
            lowerHull.add(p);
        }

        // Get convex curve above
        ArrayList<Point2d> upperHull = new ArrayList<>();
        for (int i = latLongs.size()-1; i >= 0; i--) {
            Point2d p = latLongs.get(i);
            while (upperHull.size() > 1
                    && Point2d.orderedClockwise(
                    upperHull.get(upperHull.size()-2),
                    upperHull.get(upperHull.size()-1),
                    p
            )) {
                upperHull.remove(upperHull.size() - 1);
            }
            upperHull.add(p);
        }

        // Remove redundant points at end of each hull.
        lowerHull.remove(lowerHull.size()-1);
        upperHull.remove(upperHull.size()-1);

    upperHull.addAll(lowerHull);
    return upperHull;
    }

//    private ArrayList<Point3D> transformToEuclideanSpace() {
//        ArrayList<Point3D> sphericalPoints = new ArrayList<>();
//        for (BeaconDetection det : this.detections) {
//            sphericalPoints.add(Point3D.fromLatLong(det.latitude, det.longitude));
//        }
//        return sphericalPoints;
//    }

    public List<Trajectory> getEpsilonComponents(float epsilon) {
        List<Trajectory> components = new ArrayList<>();
        return components;
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
