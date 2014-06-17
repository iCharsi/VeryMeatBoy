package ru.ifmo.enf.finyutina.t05;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/14/14.
 */
class Settings {
    private static boolean editorGrid = false;
    private static boolean editorExtras = true;
    private static boolean frameSkip = false;

    public static boolean getEditorGrid() {
        return editorGrid;
    }

    public static void switchEditorGrid() {
        editorGrid = !editorGrid;
    }

    public static boolean getEditorExtras() {
        return editorExtras;
    }

    public static void switchEditorExtras() {
        editorExtras = !editorExtras;
    }

    public static boolean getFrameSkip() {
        return frameSkip;
    }

    public static void switchFrameSkip() {
        frameSkip = !frameSkip;
    }
}
