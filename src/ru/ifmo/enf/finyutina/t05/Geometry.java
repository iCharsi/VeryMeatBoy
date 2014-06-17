package ru.ifmo.enf.finyutina.t05;

import java.awt.geom.Point2D;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/8/14.
 */
public class Geometry {
    static final double EPS = 1e-9;

    public static double distance(Point2D.Double p1, Point2D.Double p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static double distancePointToSegment(Point2D.Double p, Point2D.Double a, Point2D.Double b) {
        final Point2D.Double intersection;
        final boolean between;
        if (Math.abs(a.x - b.x) < EPS && Math.abs(a.y - b.y) < EPS) { //segment is a point
            intersection = a;
            between = true;
        } else {
            //a0 * x + b0 * y + c0 = 0 - segment
            //a1 * x + b1 * y + c1 = 0 - line through the given point, normal to the given segment
            final double a0 = b.y - a.y;
            final double b0 = a.x - b.x;
            final double c0 = -(a0 * a.x + b0 * a.y);
            final double a1 = b0;
            final double b1 = -a0;
            final double c1 = -(a1 * p.x + b1 * p.y);
            final double denominator = a0 * b1 - a1 * b0;
            intersection = new Point2D.Double(-(c0 * b1 - b0 * c1) / denominator, -(a0 * c1 - c0 * a1) / denominator);
            if (Math.abs(a.x - b.x) < EPS) {
                between = (intersection.y >= a.y && b.y >= intersection.y) || (intersection.y <= a.y && b.y <= intersection.y);
            } else {
                between = (intersection.x >= a.x && b.x >= intersection.x) || (intersection.x <= a.x && b.x <= intersection.x);
            }
        }
        if (between) {
            return distance(intersection, p);
        } else {
            return Math.min(distance(a, p), distance(b, p));
        }
    }
}
