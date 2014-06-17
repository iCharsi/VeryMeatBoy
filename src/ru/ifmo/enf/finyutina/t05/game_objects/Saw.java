package ru.ifmo.enf.finyutina.t05.game_objects;

import ru.ifmo.enf.finyutina.t05.Geometry;
import ru.ifmo.enf.finyutina.t05.Player;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/5/14.
 */

public class Saw {
    private double y0;
    private double x0;
    private double y1;
    private double x1;
    private final double radius;

    private final int frames;
    private final int startingFrame;
    private int currentFrame;
    public enum Direction {FORWARDS, BACKWARDS}
    private final Direction startingDirection;
    private Direction direction;
    public enum IntersectionState {INSIDE, NEAR, OUTSIDE}

    private boolean bloody = false;

    public void move(Point2D.Double by) {
        y0 += by.y;
        x0 += by.x;
        y1 += by.y;
        x1 += by.x;
    }

    public void initialize() {
        currentFrame = startingFrame;
        direction = startingDirection;
        bloody = false;
    }

    public void makeBloody() {
        bloody = true;
    }

    public boolean isBloody() {
        return bloody;
    }

    public Direction getStartingDirection() {
        return startingDirection;
    }

    public int getFrames() {
        return frames;
    }

    public int getStartingFrame() {
        return startingFrame;
    }

    public void nextFrame() {
        if (direction == Direction.FORWARDS && currentFrame == frames) {
            direction = Direction.BACKWARDS;
        }
        if (direction == Direction.BACKWARDS && currentFrame == 0) {
            direction = Direction.FORWARDS;
        }
        if (direction == Direction.FORWARDS) {
            ++currentFrame;
        } else {
            --currentFrame;
        }
    }

    public Point2D.Double getPosition() {
        return new Point2D.Double(x0 + ((x1 - x0) * currentFrame) / frames,
                                  y0 + ((y1 - y0) * currentFrame) / frames);
    }

    public Point2D.Double getFirstFramePosition() {
        return new Point2D.Double(x0, y0);
    }

    public Point2D.Double getLastFramePosition() {
        return new Point2D.Double(x1, y1);
    }

    public IntersectionState checkForIntersection(Point2D.Double from, Point2D.Double to) {
        final double distance = Geometry.distancePointToSegment(this.getPosition(), from, to);
        if (distance < this.radius) {
            return IntersectionState.INSIDE;
        } else if (distance < this.radius + Player.SIZE) {
            return IntersectionState.NEAR;
        }
        return IntersectionState.OUTSIDE;
    }

    public double getRadius() {
        return radius;
    }

    public Saw(double y0, double x0, double y1, double x1, double radius, int frames, int startingFrame, Direction startingDirection) {
        this.y0 = y0;
        this.x0 = x0;
        this.y1 = y1;
        this.x1 = x1;
        this.radius = radius;
        this.frames = frames;
        this.startingFrame = startingFrame;
        this.startingDirection = startingDirection;
        initialize();
    }

    public Saw(InputStream in) throws IOException {
        byte b[] = new byte[49];
        if (in.read(b) != 49) {
            throw new IOException();
        }
        ByteBuffer buf = ByteBuffer.wrap(b);
        this.y0 = buf.getDouble(0);
        this.x0 = buf.getDouble(8);
        this.y1 = buf.getDouble(16);
        this.x1 = buf.getDouble(24);
        this.radius = buf.getDouble(32);
        this.frames = buf.getInt(40);
        this.currentFrame = this.startingFrame = buf.getInt(44);
        if (b[48] == 0) {
            this.startingDirection = this.direction = Direction.FORWARDS;
        } else {
            this.startingDirection = this.direction = Direction.BACKWARDS;
        }
        initialize();
    }

    public void writeToStream(OutputStream out) throws IOException {
        double[] doubles = new double[] {y0, x0, y1, x1, radius};
        ByteBuffer buf = ByteBuffer.allocate(doubles.length * 8);
        buf.asDoubleBuffer().put(doubles);
        out.write(buf.array());

        int[] ints = new int[] {frames, startingFrame};
        buf = ByteBuffer.allocate(ints.length * 4);
        buf.asIntBuffer().put(ints);
        out.write(buf.array());

        byte direction = this.startingDirection == Direction.FORWARDS ? (byte)0 : 1;
        out.write(new byte[] {direction});
    }
}
