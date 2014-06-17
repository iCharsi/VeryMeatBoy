package ru.ifmo.enf.finyutina.t05;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/11/14.
 */
class Blood {
    private final int height;
    private final int width;
    private final boolean top[][];
    private final boolean bottom[][];
    private final boolean left[][];
    private final boolean right[][];

    public Blood(int levelHeight, int levelWidth) {
        height = levelHeight;
        width = levelWidth;
        top = new boolean[height][width];
        bottom = new boolean[height][width];
        left = new boolean[height][width];
        right = new boolean[height][width];
    }

    public boolean getTop(int y, int x) {
        return top[y][x];
    }

    public boolean getBottom(int y, int x) {
        return bottom[y][x];
    }

    public boolean getLeft(int y, int x) {
        return left[y][x];
    }

    public boolean getRight(int y, int x) {
        return right[y][x];
    }

    public void makeTopBloody(int y, int x) {
        top[y][x] = true;
    }

    public void makeBottomBloody(int y, int x) {
        bottom[y][x] = true;
    }

    public void makeLeftBloody(int y, int x) {
        left[y][x] = true;
    }

    public void makeRightBloody(int y, int x) {
        right[y][x] = true;
    }
}
