import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by zack on 18/10/2016.
 */
public class PointSET {
    // use the system red-black tree set to store all the points
    private TreeSet<Point2D> trSet = new TreeSet<>();

    // constructor
    public PointSET() {
    }

    // methods
    public boolean isEmpty() {
        return trSet.isEmpty();
    }

    public int size() {
        return trSet.size();
    }

    public void insert(Point2D p) {
        if (p == null) throw new NullPointerException("insert");
        trSet.add(p);
    }

    public boolean contains(Point2D p) {
        if (p == null) throw new NullPointerException("contains");
        return trSet.contains(p);
    }

    public void draw() {
        if (!isEmpty())
            for (Point2D p : trSet)
                p.draw();
    }

    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new NullPointerException("range");
        // iterate through the tree set, find each point in range
        // and add them to the arraylist which is iterable
        ArrayList<Point2D> arrList = new ArrayList<>();
        for (Point2D p : trSet) {
            if (insideRect(p, rect)) arrList.add(p);
        }
        return arrList;
    }

    /**
     * The brute-force for nearest method is straightforward, iterate through
     * the tree set and find the closest point to the query one.
     */
    public Point2D nearest(Point2D p) {
        if (p == null) throw new NullPointerException("nearest");
        if (isEmpty()) return null;
        double minDist = p.distanceSquaredTo(trSet.first());
        Point2D np = trSet.first();
        for (Point2D tp : trSet) {
            double tmpMinDist = p.distanceSquaredTo(tp);
            if (tmpMinDist < minDist) {
                minDist = tmpMinDist;
                np = tp;
            }
        }
        return np;
    }

    /**
     * To check if the point is inside a rect, we can also use the contains
     * method of the RectHV, which returns a boolean.
     */
    private boolean insideRect(Point2D p, RectHV rect) {
        if (p.x() < rect.xmin() || p.x() > rect.xmax()) return false;
        if (p.y() < rect.ymin() || p.y() > rect.ymax()) return false;
        return true;
    }

    /**
     * The main method is used for unit testing.
     */
    public static void main(String[] args) {
        RectHV rectHV_1 = new RectHV(0.0, 0.0, 1.0, 1.0);
        RectHV rectHV_2 = new RectHV(0.5, 0.5, 0.7, 0.7);
        boolean intersects = rectHV_1.intersects(rectHV_2);
        Point2D p1 = new Point2D(0.5, 0.5);
        Point2D p2 = new Point2D(0.2, 0.2);
        Point2D p3 = new Point2D(0.5, 0.9);
        Point2D p4 = new Point2D(0.6, 0.6);
        Point2D p5 = new Point2D(0.6, 0.9);
        RectHV rect = new RectHV(0.45, 0.45, 0.7, 0.7);
        PointSET ptSet = new PointSET();
        ptSet.insert(p1);
        ptSet.insert(p2);
        ptSet.insert(p3);
        ptSet.insert(p4);
        ptSet.insert(p5);
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.setPenRadius(0.01);
        for (Point2D p : ptSet.range(rect)) {
            p.draw();
        }
        rect.draw();
        ptSet.draw();
        Point2D p6 = new Point2D(0.5, 0.6);
        Point2D np = ptSet.nearest(p6);
        StdDraw.setPenColor(StdDraw.BLACK);
        np.draw();
    }
}
