package hawk.privacy.bledoubt;

import org.junit.Assert;
import org.junit.Test;

public class Point2dTest {
    @Test
    public void orderedClockwiseCorrectForCardinalDirections() {
        Assert.assertTrue(Point2d.orderedClockwise(
                new Point2d(1,0),
                new Point2d(0,0),
                new Point2d(0,1)
        ));
        Assert.assertFalse(Point2d.orderedClockwise(
                new Point2d(0,1),
                new Point2d(0,0),
                new Point2d(1,0)
        ));
        Assert.assertFalse(Point2d.orderedClockwise(
                new Point2d(-1,0),
                new Point2d(0,0),
                new Point2d(0,1)
        ));
        Assert.assertTrue(Point2d.orderedClockwise(
                new Point2d(0,1),
                new Point2d(0,0),
                new Point2d(-1,0)
        ));
    }

    @Test
    public void straightLineOrderedClockwise() {
        Assert.assertTrue(Point2d.orderedClockwise(
                new Point2d(0,1),
                new Point2d(0,0),
                new Point2d(0,-1)
        ));
    }

    @Test
    public void lexicalOrderComparatorGetsAllFiveCasesRight() {
        Point2d.LexicalComparator comparator = new Point2d.LexicalComparator();
        Assert.assertTrue(comparator.compare(new Point2d(1,2), new Point2d(2,1)) < 0);
        Assert.assertTrue(comparator.compare(new Point2d(2,1), new Point2d(2,2)) < 0);
        Assert.assertTrue(comparator.compare(new Point2d(2,1), new Point2d(1,2)) > 0);
        Assert.assertTrue(comparator.compare(new Point2d(2,2), new Point2d(2,1)) > 0);
        Assert.assertEquals(comparator.compare(new Point2d(2,2), new Point2d(2,2)), 0);
    }
}
