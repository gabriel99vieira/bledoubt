package hawk.privacy.bledoubt;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class BeaconDetectionTest {

    @Test
    public void haversine_predicts_distance_from_boston_to_new_york() {
        double bostonLat = 42.3601;
        double bostonLong = 71.0589;
        double newYorkLat = 40.7128;
        double newYorkLong = 74.0060;

        double distance = BeaconDetection.latLongToMeters(bostonLat, bostonLong, newYorkLat, newYorkLong);
        Assert.assertEquals(distance, 306449.6, .1);
    }

    @Test
    public void get_distance_predicts_distance_from_boston_to_new_york0() {
        BeaconDetection det1 = new BeaconDetection("00:11:22:33:44:55", new Date(), 42.3601, 71.0589, -35);
        BeaconDetection det2 = new BeaconDetection("00:11:22:33:44:55", new Date(), 40.7128, 74.0060, -36);

        double distance = det1.distanceInMeters(det2);
        Assert.assertEquals(distance, 306449.6, .1);
    }
}