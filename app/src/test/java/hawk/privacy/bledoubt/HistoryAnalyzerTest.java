package hawk.privacy.bledoubt;

import org.junit.Assert;
import org.junit.Test;

public class HistoryAnalyzerTest {
    @Test
    public void haversine_isCorrect() {
        double bostonLat = 42.3601;
        double bostonLong = 71.0589;
        double newYorkLat = 40.7128;
        double newYorkLong = 74.0060;

        double distance = HistoryAnalyzer.latLongToMeters(bostonLat, bostonLong, newYorkLat, newYorkLong);
        Assert.assertEquals(distance, 306449.6, .1);
    }

}
