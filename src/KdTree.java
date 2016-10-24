import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.In;

import java.util.ArrayList;

/**
 * Created by zack on 21/10/2016.
 */
public class KdTree {
    // fields
    private Node root = null;
    // static inner class
    private static class Node {
        private Point2D p;
        private RectHV rect;
        private Node lb;
        private Node rt;
        private int count;
        private boolean vertOrient;
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

    public void insert(Point2D p) {
        if (p == null) throw new NullPointerException("insert");
        root = insert(root, p, true, 0.0, 0.0, 1.0, 1.0);
    }

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

    public void draw() {
        draw(root);
    }

    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new NullPointerException("range");
        ArrayList<Point2D> arrl = new ArrayList<>();
        insideRect(root, rect, arrl);
        return arrl;
    }

    public Point2D nearest(Point2D p) {
        if (p == null) throw new NullPointerException("nearest");
        return nearest(root, p, Double.POSITIVE_INFINITY);
    }

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

    private void insideRect(Node x, RectHV rect, ArrayList<Point2D> arrl) {
        if (x == null) return;
        if (rect.contains(x.p)) arrl.add(x.p);
        if (x.lb != null && x.lb.rect.intersects(rect))
            insideRect(x.lb, rect, arrl);
        if (x.rt != null && x.rt.rect.intersects(rect))
            insideRect(x.rt, rect, arrl);
    }

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
//        Point2D p1 = new Point2D(0.5, 0.2);
//        Point2D p2 = new Point2D(0.5, 0.4);
//        Point2D p3 = new Point2D(0.5, 0.6);
//        Point2D p4 = new Point2D(0.5, 0.8);
//        KdTree kdTree = new KdTree();
//        kdTree.insert(p1);
//        kdTree.insert(p2);
//        kdTree.insert(p3);
//        kdTree.insert(p4);
//        kdTree.draw();
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
