package hawk.privacy.bledoubt;

import java.util.Comparator;
import java.util.Date;

public class Point2d {
    double x, y;

    public Point2d() {}
    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Point2d other) {
        return Math.sqrt(
                (this.x - other.x) * (this.x - other.x) +
                (this.y - other.y) * (this.y - other.y)
        );
    }

    public static boolean orderedClockwise(Point2d p1, Point2d p2, Point2d p3){
        return -(p2.y - p1.y) * (p3.x - p2.x) + (p2.x - p1.x) * (p3.y - p2.y) <= 0;
    }

    public boolean equals(Object other) {
        if(other instanceof Point2d)
            return x == ((Point2d) other).x && y == ((Point2d) other).y;
        return false;
    }

    public String toString()
    {
        return "[" + x + ", " + y + "]";
    }

    public static class LexicalComparator implements Comparator<Point2d> {
        @Override
        public int compare(Point2d p1, Point2d p2) {
            if (p1.x < p2.x)
                return -1;
            else if (p1.x > p2.x)
                return 1;
            else if (p1.y < p2.y)
                return -1;
            else if (p1.y > p2.y)
                return 1;
            else
                return 0;
        }
    }
}
