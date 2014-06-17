package ru.ifmo.enf.finyutina.t05.game_objects;

import ru.ifmo.enf.finyutina.t05.Geometry;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/8/14.
 */
public class Portal {
    private double y;
    private double x;
    public static final double RADIUS = 1.2;
    private int destinationID;
    public enum IntersectionState {INSIDE, OUTSIDE}

    public int getDestinationID() {
        return destinationID;
    }

    public void setDestinationID(int id) {
        this.destinationID = id;
    }

    public void move(Point2D.Double by) {
        y += by.y;
        x += by.x;
    }

    public Point2D.Double getPosition() {
        return new Point2D.Double(x, y);
    }

    public IntersectionState checkForIntersection(Point2D.Double from, Point2D.Double to) {
        final double distance = Geometry.distancePointToSegment(this.getPosition(), from, to);
        if (distance < RADIUS) {
            return IntersectionState.INSIDE;
        }
        return IntersectionState.OUTSIDE;
    }

    public Portal(double y, double x, int destinationID) {
        this.y = y;
        this.x = x;
        this.destinationID = destinationID;
    }

    public Portal(InputStream in) throws IOException {
        byte b[] = new byte[20];
        if (in.read(b) != 20) {
            throw new IOException();
        }
        ByteBuffer buf = ByteBuffer.wrap(b);
        this.y = buf.getDouble(0);
        this.x = buf.getDouble(8);
        this.destinationID = buf.getInt(16);
    }

    public void writeToStream(OutputStream out) throws IOException {
        double[] doubles = new double[] {y, x};
        ByteBuffer buf = ByteBuffer.allocate(doubles.length * 8);
        buf.asDoubleBuffer().put(doubles);
        out.write(buf.array());

        int[] ints = new int[] {destinationID};
        buf = ByteBuffer.allocate(ints.length * 4);
        buf.asIntBuffer().put(ints);
        out.write(buf.array());
    }
}
