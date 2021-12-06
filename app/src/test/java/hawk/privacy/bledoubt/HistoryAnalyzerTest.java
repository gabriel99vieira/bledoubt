package hawk.privacy.bledoubt;

import org.junit.Assert;
import org.junit.Test;

public class HistoryAnalyzerTest {
    @Test
    public void haversine_predicts_distance_from_boston_to_new_york() {
        double bostonLat = 42.3601;
        double bostonLong = 71.0589;
        double newYorkLat = 40.7128;
        double newYorkLong = 74.0060;

        double distance = HistoryAnalyzer.latLongToMeters(bostonLat, bostonLong, newYorkLat, newYorkLong);
        Assert.assertEquals(distance, 306449.6, .1);
    }

//    @Test
//    public void topological_classifier_rejects_unconnected_trajectories() {
//        HistoryAnalyzer.
//    }

}
