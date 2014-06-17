package ru.ifmo.enf.finyutina.t05;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 5/12/14.
 */

class GameResources {
    public static final byte NO_TEXTURE = (byte)-1;
    public static final int NO_BACKGROUND = -1;

    private final Vector<Level> levels = new Vector<Level>();
    private final Vector<BufferedImage> textures = new Vector<BufferedImage>();
    private BufferedImage bloodTopTexture;
    private BufferedImage bloodBottomTexture;
    private BufferedImage bloodLeftTexture;
    private BufferedImage bloodRightTexture;
    private BufferedImage editorHelpTexture;
    private BufferedImage gameHelpTexture;
    private BufferedImage sawTexture;
    private BufferedImage bloodySawTexture;
    private BufferedImage transparentSawTexture;
    private BufferedImage portalTexture;
    private final Vector<BufferedImage> backgrounds = new Vector<BufferedImage>();

    public int getLevelCount() {
        return levels.size();
    }

    public Level getLevel(int level) {
        return levels.get(level);
    }

    public void addLevel(Level level, int index) {
        levels.insertElementAt(level, index);
    }

    public void swapLevel(int index, int putAfter) {
        if (putAfter < -1) {
            putAfter = -1;
        }
        if (putAfter >= index) {
            --putAfter;
        }
        Level tempLevel = levels.get(index);
        levels.removeElementAt(index);
        if (putAfter + 1 >= levels.size()) {
            levels.add(tempLevel);
        } else {
            levels.add(putAfter + 1, tempLevel);
        }
    }

    public int getTextureCount() {
        return textures.size();
    }

    public Image getTexture(int index) {
        return textures.get(index);
    }

    public Image getBloodTopTexture() {
        return bloodTopTexture;
    }

    public Image getBloodBottomTexture() {
        return bloodBottomTexture;
    }

    public Image getBloodLeftTexture() {
        return bloodLeftTexture;
    }

    public Image getBloodRightTexture() {
        return bloodRightTexture;
    }

    public Image getEditorHelpTexture() {
        return editorHelpTexture;
    }

    public Image getGameHelpTexture() {
        return gameHelpTexture;
    }

    public BufferedImage getSawTexture() {
        return sawTexture;
    }

    public BufferedImage getBloodySawTexture() {
        return bloodySawTexture;
    }

    public BufferedImage getTransparentSawTexture() {
        return transparentSawTexture;
    }

    public BufferedImage getPortalTexture() {
        return portalTexture;
    }

    public int getBackgroundsCount() {
        return backgrounds.size();
    }

    public Image getBackground(int index) {
        return backgrounds.get(index);
    }

    public void readDefaultResources() throws IOException {
        levels.clear();
        textures.clear();
        readDefaultTextureSet();
        readCustomLevelSet(this.getClass().getResourceAsStream("resources/levels.vmb"));
    }

    public void readCustomLevelSet(InputStream in) throws IOException {
        if (in == null) {
            throw new IOException();
        }
        byte[] b = new byte[4];
        if (in.read(b) != 4) {
            throw new IOException("LevelSet file is corrupt");
        }
        int levelCount = ByteBuffer.wrap(b).getInt();
        levels.clear();
        for (int i = 0; i < levelCount; ++i) {
            levels.add(new Level(in));
        }
    }

    public void writeCustomLevelSet(OutputStream out) throws IOException {
        if (out == null) {
            throw new IOException();
        }
        ByteBuffer b = ByteBuffer.allocate(4);
        b.asIntBuffer().put(levels.size());
        out.write(b.array());
        for (Level level : levels) {
            level.writeToStream(out);
        }
    }

    BufferedImage makeTransparent(BufferedImage image, double alpha) {
        int[] imagePixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

        for (int i = 0; i < imagePixels.length; i++) {
            imagePixels[i] = (imagePixels[i] & 0x00ffffff) | (((int)((imagePixels[i] >>> 24) * alpha)) << 24);
        }

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, image.getWidth(), image.getHeight(), imagePixels, 0, image.getWidth());
        return result;
    }

    void readDefaultTextureSet() throws IOException {
        int i = 0;
        while (true) {
            URL imageResource = getClass().getResource("resources/textures/" + i + ".png");
            if (imageResource == null) {
                break;
            }
            textures.add(ImageIO.read(imageResource));
            ++i;
        }
        i = 0;
        while (true) {
            URL imageResource = getClass().getResource("resources/textures/bg" + i + ".png");
            if (imageResource == null) {
                break;
            }
            backgrounds.add(ImageIO.read(imageResource));
            ++i;
        }
        bloodTopTexture = ImageIO.read(getClass().getResource("resources/textures/blood_top.png"));
        bloodBottomTexture = ImageIO.read(getClass().getResource("resources/textures/blood_bottom.png"));
        bloodLeftTexture = ImageIO.read(getClass().getResource("resources/textures/blood_left.png"));
        bloodRightTexture = ImageIO.read(getClass().getResource("resources/textures/blood_right.png"));
        editorHelpTexture = ImageIO.read(getClass().getResource("resources/textures/help_editor.png"));
        gameHelpTexture = ImageIO.read(getClass().getResource("resources/textures/help_game.png"));
        sawTexture = ImageIO.read(getClass().getResource("resources/textures/saw.png"));
        bloodySawTexture = ImageIO.read(getClass().getResource("resources/textures/bloody_saw.png"));
        transparentSawTexture = makeTransparent(sawTexture, 0.5);
        portalTexture = ImageIO.read(getClass().getResource("resources/textures/portal.png"));
    }

    public void readCustomTextureSet(File directory) throws IOException { //useless for now
        int i = 0;
        while (true) {
            File imageFile = new File(directory.getAbsolutePath() + "/" + i + ".png");
            if (!imageFile.exists()) {
                break;
            }
            textures.add(ImageIO.read(imageFile));
            ++i;
        }
    }
}
