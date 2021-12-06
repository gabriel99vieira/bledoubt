package hawk.privacy.bledoubt;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class TrajectoryTest {
    static boolean pointInArrayList(Point2d point, ArrayList<Point2d> list) {
        for (Point2d p : list) {
            if (p.equals(point)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void convexHullRemovesCentralPointAndPreservesHullInFourPointTrajectory() {
        ArrayList<BeaconDetection> dets = new ArrayList<>();
        dets.add(new BeaconDetection("00:11:22:33:44:55", new Date(), 10, -50, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", new Date(), 10, -70, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", new Date(), 20, -60, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", new Date(), 30, -60, -35));
        Trajectory t = new Trajectory(dets);

        ArrayList<Point2d> convexHull = t.getConvexHull();
        System.out.println(convexHull);
        Assert.assertEquals(convexHull.size(),3);
        Assert.assertTrue(pointInArrayList(new Point2d(-50, 10), convexHull));
        Assert.assertTrue(pointInArrayList(new Point2d(-70, 10), convexHull));
        Assert.assertTrue(pointInArrayList(new Point2d(-60, 30), convexHull));
        Assert.assertFalse(pointInArrayList(new Point2d(-60, 20), convexHull));
    }

}
