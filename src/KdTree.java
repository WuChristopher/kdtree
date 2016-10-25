import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.In;

import java.util.ArrayList;

/**
 * Created by zack on 21/10/2016.
 * This is the 2d tree implementation, which can be used to find all points
 * in a given rectangle, search the closest point to the query point.
 */
public class KdTree {
    // fields
    private Node root = null;
    // static inner class, cause' no need to adapt for the generic type
    // use static inner class to save 8 bytes.
    private static class Node {
        private Point2D p; // this node's point
        private RectHV rect; // the rect divided by this node's point, axis-aligned rect
        private Node lb; // sub-tree for left or bottom
        private Node rt; // sub-tree for right or top
        private int count; // all nodes in this tree
        private boolean vertOrient; // orientation of the node, divide vertically or horizontally
    }
    // constructor
    public KdTree() {}

    // methods
    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return size(root);
    }

    /**
     * The public API for inserting, calling the recursive private method
     */
    public void insert(Point2D p) {
        if (p == null) throw new NullPointerException("insert");
        root = insert(root, p, true, 0.0, 0.0, 1.0, 1.0);
    }

    /**
     * Traverse the tree per the point's coordinates and the Node's orientation
     */
    public boolean contains(Point2D p) {
        if (p == null) throw new NullPointerException("contains");
        Node x = root;
        while (x != null) {
            int cmp;
            if (x.vertOrient)
                cmp = Point2D.X_ORDER.compare(p, x.p);
            else cmp = Point2D.Y_ORDER.compare(p, x.p);
            if (cmp < 0) x = x.lb;
            else if (cmp > 0) x = x.rt;
            else if (x.p.equals(p)) return true;
            else x = x.rt;
        }
        return false;
    }

    /**
     * The public API for drawing, calling the recursive private method
     */
    public void draw() {
        draw(root);
    }

    /**
     * The public API for find all points contained in a given rectangle,
     * calling the recursive private method. All found points are added to
     * the iterable ArrayList.
     */
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new NullPointerException("range");
        ArrayList<Point2D> arrl = new ArrayList<>();
        insideRect(root, rect, arrl);
        return arrl;
    }

    /**
     * The public API for finding the closest point, calling the recursive
     * private method. Note that we use the positive infinity as the initial
     * distance between our points and the query point.
     */
    public Point2D nearest(Point2D p) {
        if (p == null) throw new NullPointerException("nearest");
        return nearest(root, p, Double.POSITIVE_INFINITY);
    }

    /**
     * The RectHV's distanceToPoint returns zero if the rect contains that point.
     * So if the Node's point's distance to the query point is zero, the Node is found.
     * Otherwise, search per the pruning rule: only try traversing the sub-tree whose
     * axis-aligned rect's distance the query point is less than the current min distance.
     * And always search the sub-tree with the shorter distance from its axis-aligned rect
     * to the query point first. Note that when the current Node's distance to the query
     * point is less than the argument minDist, update the minDist and record the current
     * Node(or its point) as the closest. After having traversed the sub trees, if any
     * closer Node is returned, then the returned Node is the closest, if no returned Node,
     * the current Node is the closest. Actually this pruning rule is a depth-first search
     * algorithm.
     */
    private Point2D nearest(Node x, Point2D p, double minDist) {
        if (x == null) return null;
        double dist = p.distanceSquaredTo(x.p);
        if (dist == 0) return x.p;
        boolean isClosest = false;
        if (dist < minDist) {
            minDist = dist;
            isClosest = true;
        }

        Point2D np = null;
        double distToLbRect = Double.POSITIVE_INFINITY;
        if (x.lb != null) distToLbRect = x.lb.rect.distanceSquaredTo(p);
        double distToRtRect = Double.POSITIVE_INFINITY;
        if (x.rt != null) distToRtRect = x.rt.rect.distanceSquaredTo(p);
        if (distToLbRect < distToRtRect) {
            Point2D tmpPt = null;
            if (distToLbRect < minDist) tmpPt = nearest(x.lb, p, minDist);
            if (tmpPt != null) {
                np = tmpPt;
                minDist = tmpPt.distanceSquaredTo(p);
            }
            if (distToRtRect < minDist) tmpPt = nearest(x.rt, p, minDist);
            if (tmpPt != null) np = tmpPt;
        } else {
            Point2D tmpPt = null;
            if (distToRtRect < minDist) tmpPt = nearest(x.rt, p, minDist);
            if (tmpPt != null) {
                np = tmpPt;
                minDist = tmpPt.distanceSquaredTo(p);
            }
            if (distToLbRect < minDist) tmpPt = nearest(x.lb, p, minDist);
            if (tmpPt != null) np = tmpPt;
        }

        if (np != null) return np;
        if (isClosest) return x.p;
        return null;
    }

    /**
     * The RectHV intersects method returns true if one rect lies in the other.
     * Thus the pruning rule is: only search sub trees whose axis-aligned rectangles
     * intersect with the given rect.
     */
    private void insideRect(Node x, RectHV rect, ArrayList<Point2D> arrl) {
        if (x == null) return;
        if (rect.contains(x.p)) arrl.add(x.p);
        if (x.lb != null && x.lb.rect.intersects(rect))
            insideRect(x.lb, rect, arrl);
        if (x.rt != null && x.rt.rect.intersects(rect))
            insideRect(x.rt, rect, arrl);
    }

    /**
     * If the point's x-coordinate is smaller than the current Node's point's,
     * insert the point to the left-bottom sub tree, otherwise the right-top.
     * Note that check if the points equals the current Node's point and discard
     * the point if it's true.
     */
    private Node insert(Node x, Point2D p, boolean vertOrient,
                        double xmin, double ymin, double xmax, double ymax) {
        if (x == null) {
            Node node = new Node();
            node.p = p;
            node.lb = null;
            node.rt = null;
            node.count = 1;
            node.vertOrient = vertOrient;
            node.rect = new RectHV(xmin, ymin, xmax, ymax);
            return node;
        }
        if (x.p.equals(p)) return x;
        int cmp;
        if (vertOrient) {
            cmp = Point2D.X_ORDER.compare(p, x.p);
            if (cmp < 0)
                x.lb = insert(x.lb, p,
                        !vertOrient, xmin, ymin, x.p.x(), ymax);
            else
                x.rt = insert(x.rt, p,
                        !vertOrient, x.p.x(), ymin, xmax, ymax);
        } else {
            cmp = Point2D.Y_ORDER.compare(p, x.p);
            if (cmp < 0)
                x.lb = insert(x.lb, p,
                        !vertOrient, xmin, ymin, xmax, x.p.y());
            else
                x.rt = insert(x.rt, p,
                        !vertOrient, xmin, x.p.y(), xmax, ymax);
        }

        x.count = 1 + size(x.lb) + size(x.rt);
        return x;
    }

    private int size(Node x) {
        if (x == null) return 0;
        return x.count;
    }

    /**
     * Use the axis-aligned rect, which is divided by the Node's point, and
     * the Node's point itself to construct the dividing line segment.
     */
    private void draw(Node x) {
        if (x == null) return;
        StdDraw.setPenRadius(0.01);
        StdDraw.setPenColor(StdDraw.BLACK);
        x.p.draw();
        StdDraw.setPenRadius(0.005);
        RectHV rect;
        if (x.vertOrient) {
            rect = new RectHV(x.p.x(), x.rect.ymin(), x.p.x(), x.rect.ymax());
            StdDraw.setPenColor(StdDraw.RED);
            rect.draw();
        }
        else {
            rect = new RectHV(x.rect.xmin(), x.p.y(), x.rect.xmax(), x.p.y());
            StdDraw.setPenColor(StdDraw.BLUE);
            rect.draw();
        }
        draw(x.lb);
        draw(x.rt);
    }

    public static void main(String[] args) {
        String filename = args[0];
        In in = new In(filename);

        StdDraw.enableDoubleBuffering();

        KdTree kdTree = new KdTree();
        while (!in.isEmpty()) {
            double x = in.readDouble();
            double y = in.readDouble();
            Point2D p = new Point2D(x, y);
            kdTree.insert(p);
        }
        kdTree.draw();
        StdDraw.show();
    }

}
