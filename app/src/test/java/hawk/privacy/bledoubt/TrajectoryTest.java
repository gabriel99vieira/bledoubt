package hawk.privacy.bledoubt;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        Assert.assertEquals(convexHull.size(),3);
        Assert.assertTrue(pointInArrayList(new Point2d(-50, 10), convexHull));
        Assert.assertTrue(pointInArrayList(new Point2d(-70, 10), convexHull));
        Assert.assertTrue(pointInArrayList(new Point2d(-60, 30), convexHull));
        Assert.assertFalse(pointInArrayList(new Point2d(-60, 20), convexHull));
    }

    @Test
    public void getEpsilonComponentsDecomposesThreePartTrajectory() {
        ArrayList<BeaconDetection> dets = new ArrayList<>();
        dets.add(new BeaconDetection("00:11:22:33:44:55", Date.from(Instant.ofEpochSecond(1)), 10, -50, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", Date.from(Instant.ofEpochSecond(62)), 10, -70, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", Date.from(Instant.ofEpochSecond(63)), 20, -60, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", Date.from(Instant.ofEpochSecond(124)), 30, -60, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", Date.from(Instant.ofEpochSecond(184)), 10, -50, -35));
        dets.add(new BeaconDetection("00:11:22:33:44:55", Date.from(Instant.ofEpochSecond(244)), 10, -70, -35));
        Trajectory t = new Trajectory(dets);

        List<Trajectory> epsilonComponents = t.getEpsilonComponents(60);
        System.out.println(epsilonComponents);
        Assert.assertEquals(epsilonComponents.size(), 3);
        Assert.assertEquals(epsilonComponents.get(0).size(), 1);
        Assert.assertEquals(epsilonComponents.get(1).size(), 2);
        Assert.assertEquals(epsilonComponents.get(2).size(), 3);

        for (Trajectory component : epsilonComponents) {
            Assert.assertTrue(component.isEpsilonConnected(60));
        }
    }

}
