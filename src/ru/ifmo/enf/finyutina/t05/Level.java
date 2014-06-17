package ru.ifmo.enf.finyutina.t05;

import ru.ifmo.enf.finyutina.t05.game_objects.Portal;
import ru.ifmo.enf.finyutina.t05.game_objects.Saw;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/5/14.
 */

public class Level {
    private static final int bufSize = 4096;
    private int height;
    private int width;
    public enum Cell {EMPTY, WALL, SPIKES, EMPTY_NOBG}
    private Cell field[][];
    private byte texture[][];
    private Point startingCell;
    private Point finishCell;
    private Color bgColor;
    private int background = GameResources.NO_BACKGROUND;
    private final Vector<Saw> saws = new Vector<Saw>();
    private final Vector<Portal> portals = new Vector<Portal>();

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Point getStartingCell() {
        return startingCell;
    }

    public void setStartingCell(Point startingCell) {
        this.startingCell = startingCell;
    }

    public Point getFinishCell() {
        return finishCell;
    }

    public void setFinishCell(Point finishCell) {
        this.finishCell = finishCell;
    }

    public void addSaw(Saw saw) {
        saws.add(saw);
    }

    public void addPortal(Portal portal) {
        portals.add(portal);
    }

    public void removeSaw(int index) {
        if (index >= 0 && index < saws.size()) {
            saws.removeElementAt(index);
        }
    }

    public void removePortal(int index) {
        if (index >= 0 && index < portals.size()) {
            portals.removeElementAt(index);
        }
    }

    public int getSawCount() {
        return saws.size();
    }

    public Saw getSaw(int index) {
        return saws.get(index);
    }

    public void setSaw(Saw saw, int index) {
        if (index >= 0 && index < saws.size()) {
            saws.setElementAt(saw, index);
        }
    }

    public int getPortalCount() {
        return portals.size();
    }

    public boolean portalExists(int index) {
        return index >= 0 && index < portals.size();
    }

    public Portal getPortal(int index) {
        return portals.get(index);
    }

    public boolean cellOutOfBounds(int y, int x) {
        return y >= height || y < 0 || x >= width || x < 0;
    }

    public Cell getFieldAt(int y, int x) {
        if (cellOutOfBounds(y, x)) {
            return Cell.EMPTY;
        }
        return field[y][x];
    }

    public void setFieldAt(int y, int x, Cell value) {
        if (!cellOutOfBounds(y, x)) {
            field[y][x] = value;
        }
    }

    public byte getTextureAt(int y, int x) {
        if (cellOutOfBounds(y, x)) {
            return GameResources.NO_TEXTURE;
        }
        return texture[y][x];
    }

    public void setTextureAt(int y, int x, byte value) {
        if (!cellOutOfBounds(y, x)) {
            texture[y][x] = value;
        }
    }

    public void extendTop() {
        Cell[][] newField = new Cell[height + 1][width];
        byte[][] newTexture = new byte[height + 1][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                newField[i + 1][j] = field[i][j];
                newTexture[i + 1][j] = texture[i][j];
            }
        }
        for (int j = 0; j < width; ++j) {
            newField[0][j] = Cell.EMPTY;
            newTexture[0][j] = GameResources.NO_TEXTURE;
        }
        for (Saw saw : saws) {
            saw.move(new Point2D.Double(0.0, 1.0));
        }
        for (Portal portal : portals) {
            portal.move(new Point2D.Double(0.0, 1.0));
        }
        ++startingCell.y;
        ++finishCell.y;
        ++height;
        field = newField;
        texture = newTexture;
    }

    public void shrinkTop() {
        if (height == 0) {
            return;
        }
        Cell[][] newField = new Cell[height - 1][width];
        byte[][] newTexture = new byte[height - 1][width];
        for (int i = 0; i < height - 1; ++i) {
            for (int j = 0; j < width; ++j) {
                newField[i][j] = field[i + 1][j];
                newTexture[i][j] = texture[i + 1][j];
            }
        }
        for (Saw saw : saws) {
            saw.move(new Point2D.Double(0.0, -1.0));
        }
        for (Portal portal : portals) {
            portal.move(new Point2D.Double(0.0, -1.0));
        }
        --startingCell.y;
        --finishCell.y;
        --height;
        field = newField;
        texture = newTexture;
    }

    public void extendBottom() {
        Cell[][] newField = new Cell[height + 1][width];
        byte[][] newTexture = new byte[height + 1][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                newField[i][j] = field[i][j];
                newTexture[i][j] = texture[i][j];
            }
        }
        for (int j = 0; j < width; ++j) {
            newField[height][j] = Cell.EMPTY;
            newTexture[height][j] = GameResources.NO_TEXTURE;
        }
        ++height;
        field = newField;
        texture = newTexture;
    }

    public void shrinkBottom() {
        if (height == 0) {
            return;
        }
        Cell[][] newField = new Cell[height - 1][width];
        byte[][] newTexture = new byte[height - 1][width];
        for (int i = 0; i < height - 1; ++i) {
            for (int j = 0; j < width; ++j) {
                newField[i][j] = field[i][j];
                newTexture[i][j] = texture[i][j];
            }
        }
        --height;
        field = newField;
        texture = newTexture;
    }

    public void extendLeft() {
        Cell[][] newField = new Cell[height][width + 1];
        byte[][] newTexture = new byte[height][width + 1];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                newField[i][j + 1] = field[i][j];
                newTexture[i][j + 1] = texture[i][j];
            }
        }
        for (int i = 0; i < height; ++i) {
            newField[i][0] = Cell.EMPTY;
            newTexture[i][0] = GameResources.NO_TEXTURE;
        }
        for (Saw saw : saws) {
            saw.move(new Point2D.Double(1.0, 0.0));
        }
        for (Portal portal : portals) {
            portal.move(new Point2D.Double(1.0, 0.0));
        }
        ++startingCell.x;
        ++finishCell.x;
        ++width;
        field = newField;
        texture = newTexture;
    }

    public void shrinkLeft() {
        if (width == 0) {
            return;
        }
        Cell[][] newField = new Cell[height][width - 1];
        byte[][] newTexture = new byte[height][width - 1];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width - 1; ++j) {
                newField[i][j] = field[i][j + 1];
                newTexture[i][j] = texture[i][j + 1];
            }
        }
        for (Saw saw : saws) {
            saw.move(new Point2D.Double(-1.0, 0.0));
        }
        for (Portal portal : portals) {
            portal.move(new Point2D.Double(-1.0, 0.0));
        }
        --startingCell.x;
        --finishCell.x;
        --width;
        field = newField;
        texture = newTexture;
    }

    public void extendRight() {
        Cell[][] newField = new Cell[height][width + 1];
        byte[][] newTexture = new byte[height][width + 1];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                newField[i][j] = field[i][j];
                newTexture[i][j] = texture[i][j];
            }
        }
        for (int i = 0; i < height; ++i) {
            newField[i][width] = Cell.EMPTY;
            newTexture[i][width] = GameResources.NO_TEXTURE;
        }
        ++width;
        field = newField;
        texture = newTexture;
    }

    public void shrinkRight() {
        if (height == 0) {
            return;
        }
        Cell[][] newField = new Cell[height][width - 1];
        byte[][] newTexture = new byte[height][width - 1];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width - 1; ++j) {
                newField[i][j] = field[i][j];
                newTexture[i][j] = texture[i][j];
            }
        }
        --width;
        field = newField;
        texture = newTexture;
    }

    public Level() {
        height = 50;
        width = 50;
        field = new Cell[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                field[i][j] = Cell.EMPTY;
            }
        }
        texture = new byte[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                texture[i][j] = GameResources.NO_TEXTURE;
            }
        }
        bgColor = Color.BLACK;
        background = GameResources.NO_BACKGROUND;
        startingCell = new Point(20, 20);
        finishCell = new Point(24, 20);
    }

    public Level(InputStream in) throws IOException {
        final byte[] b = new byte[bufSize];
        if (in.read(b, 0, 48) != 48) {
            throw new IOException("LevelSet file is corrupt");
        }
        final ByteBuffer buf = ByteBuffer.wrap(b, 0, 48);
        height = buf.getInt(0);
        width = buf.getInt(4);
        background = buf.getInt(8);
        final int red = buf.getInt(12);
        final int green = buf.getInt(16);
        final int blue = buf.getInt(20);
        int x = buf.getInt(24);
        int y = buf.getInt(28);
        startingCell = new Point(x, y);
        x = buf.getInt(32);
        y = buf.getInt(36);
        finishCell = new Point(x, y);
        bgColor = new Color(red, green, blue);
        final int sawCount = buf.getInt(40);
        final int portalCount = buf.getInt(44);
        for (int i = 0; i < sawCount; ++i) {
            saws.add(new Saw(in));
        }
        for (int i = 0; i < portalCount; ++i) {
            portals.add(new Portal(in));
        }
        if (in.read(b, 0, Math.min(bufSize, height * width)) != Math.min(bufSize, height * width)) {
            throw new IOException("LevelSet file is corrupt");
        }
        int bufPos = 0;
        field = new Cell[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (bufPos == bufSize) {
                    if (in.read(b, 0, Math.min(bufSize, (height - i) * width - j)) != Math.min(bufSize, (height - i) * width - j)) {
                        throw new IOException("LevelSet file is corrupt");
                    }
                    bufPos = 0;
                }
                if (b[bufPos] == 0) {
                    field[i][j] = Cell.EMPTY;
                } else if (b[bufPos] == 1) {
                    field[i][j] = Cell.WALL;
                } else if (b[bufPos] == 2) {
                    field[i][j] = Cell.SPIKES;
                } else {
                    field[i][j] = Cell.EMPTY_NOBG;
                }
                ++bufPos;
            }
        }
        if (in.read(b, 0, Math.min(bufSize, height * width)) != Math.min(bufSize, height * width)) {
            throw new IOException("LevelSet file is corrupt");
        }
        bufPos = 0;
        texture = new byte[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (bufPos == bufSize) {
                    if (in.read(b, 0, Math.min(bufSize, (height - i) * width - j)) != Math.min(bufSize, (height - i) * width - j)) {
                        throw new IOException("LevelSet file is corrupt");
                    }
                    bufPos = 0;
                }
                texture[i][j] = b[bufPos++];
            }
        }
    }

    public void writeToStream(OutputStream out) throws IOException {
        int[] ints = new int[] {height, width, background, bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(),
                startingCell.x, startingCell.y, finishCell.x, finishCell.y, saws.size(), portals.size()};
        ByteBuffer buf = ByteBuffer.allocate(ints.length * 4);
        buf.asIntBuffer().put(ints);
        out.write(buf.array());
        for (Saw saw : saws) {
            saw.writeToStream(out);
        }
        for (Portal portal : portals) {
            portal.writeToStream(out);
        }
        for (int i = 0; i < height; ++i) {
            byte[] array = new byte[width];
            for (int j = 0; j < width; ++j) {
                if (field[i][j] == Cell.EMPTY) {
                    array[j] = 0;
                } else if (field[i][j] == Cell.WALL) {
                    array[j] = 1;
                } else if (field[i][j] == Cell.SPIKES) {
                    array[j] = 2;
                } else {
                    array[j] = 3;
                }
            }
            out.write(array);
        }
        for (int i = 0; i < height; ++i) {
            out.write(texture[i]);
        }
    }
}
