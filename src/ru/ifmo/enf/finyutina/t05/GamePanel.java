package ru.ifmo.enf.finyutina.t05;

import ru.ifmo.enf.finyutina.t05.game_objects.Portal;
import ru.ifmo.enf.finyutina.t05.game_objects.Saw;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 4/7/14.
 */

public class GamePanel extends JPanel
        implements KeyListener, MouseListener, MouseMotionListener {
    private static final double EPS = 1e-9;

    private enum GameState {MENU, GAME, DEAD, VICTORY, EDITOR}
    private GameState gameState;
    private Level level;
    private int levelIndex;

    private Point position = new Point();
    private Point2D.Double offset = new Point2D.Double();
    private final Point transpose = new Point();
    private final Point2D.Double speed = new Point2D.Double();
    private boolean midAir = false;
    private boolean canBounceLeft = false;
    private boolean canBounceRight = false;
    private boolean turbo = false;
    private final GameResources resources;
    private final boolean keys[] = new boolean[256];
    private int jumpTrigger;
    private Menu menu;
    private int animationFrame = 0;
    private int totalFrame = 0;
    private int inactivePortal = -1; //-1 - all are active
    private long lastTime;
    boolean skipNextFrame = false;
    private byte selectedTexture;

    String message = "";
    int messageFrames = 0;

    private Blood blood;

    private final Timer timer = new Timer();
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            long currentTime = System.nanoTime();
            if (currentTime - lastTime < Visuals.sleepTimePerFrame) {
                return;
            }
            lastTime += Visuals.sleepTimePerFrame;

            if (keys[KeyEvent.VK_ESCAPE] && (gameState == GameState.GAME || gameState == GameState.DEAD || gameState == GameState.EDITOR)) {
                if (gameState == GameState.DEAD) {
                    startNewLevel(levelIndex);
                }
                if (gameState == GameState.EDITOR) {
                    startNewLevel(0);
                }
                gameState = GameState.MENU;
                menu = new Menu(resources.getLevelCount());
            }
            if ((gameState == GameState.GAME || gameState == GameState.DEAD || gameState == GameState.VICTORY) && keys[KeyEvent.VK_R]) {
                startNewLevel(levelIndex);
            }
            if (gameState == GameState.VICTORY && keys[KeyEvent.VK_ENTER]) {
                nextLevel();
            }
            if (gameState == GameState.GAME || gameState == GameState.DEAD || gameState == GameState.VICTORY) {
                for (int i = 0; i < level.getSawCount(); ++i) {
                    level.getSaw(i).nextFrame();
                }
                animationFrame = (animationFrame + 1) % Drawer.ANIMATION_FRAME_COUNT;
                if (gameState == GameState.GAME) {
                    ++totalFrame;
                }
            }

            if (gameState == GameState.EDITOR) { //everything editor related happens here
                if (keys[KeyEvent.VK_RIGHT]) {
                    offset.x += 0.5;
                }
                if (keys[KeyEvent.VK_LEFT]) {
                    offset.x -= 0.5;
                }
                if (keys[KeyEvent.VK_UP]) {
                    offset.y -= 0.5;
                }
                if (keys[KeyEvent.VK_DOWN]) {
                    offset.y += 0.5;
                }
                correctThePosition();
            }

            if (gameState == GameState.GAME) { //everything game related happens here
                turbo = keys[KeyEvent.VK_SHIFT];
                if (position.y < 0 || position.y >= level.getHeight() ||
                    position.x < 0 || position.x >= level.getWidth() || spikesTouched()) {
                    gameState = GameState.DEAD;
                }
                if (midAir && ((!keys[KeyEvent.VK_S] && !keys[KeyEvent.VK_X]) || jumpTrigger > 0) && speed.y < 0) {
                    speed.y += Player.Y_ACCELERATION_JUMP_UNPRESSED;
                    if (speed.y > Player.Y_ACCELERATION) {
                        speed.y = Player.Y_ACCELERATION;
                    }
                }
                if (jumpTrigger > 0) {
                    if (!midAir) {
                        jumpTrigger = 0;
                        speed.y = -Player.JUMP_STRENGTH;
                        midAir = true;
                    } else if (canBounceLeft) {
                        jumpTrigger = 0;
                        speed.x = -Player.BOUNCE_STRENGTH;
                        speed.y -= Player.BOUNCE_VERTICAL;
                        if (-speed.y > Player.MAX_VERTICAL_SPEED) {
                            speed.y = -Player.MAX_VERTICAL_SPEED - (-speed.y - Player.MAX_VERTICAL_SPEED) * Player.OVER_BOUNCE_COEFFICIENT;
                        }
                        if (-speed.y > Player.MAX_BOUNCE_VERTICAL_SPEED) {
                            speed.y = -Player.MAX_BOUNCE_VERTICAL_SPEED;
                        }
                        canBounceLeft = false;
                    } else if (canBounceRight) {
                        jumpTrigger = 0;
                        speed.x = Player.BOUNCE_STRENGTH;
                        speed.y -= Player.BOUNCE_VERTICAL;
                        if (-speed.y > Player.MAX_VERTICAL_SPEED) {
                            speed.y = -Player.MAX_VERTICAL_SPEED - (-speed.y - Player.MAX_VERTICAL_SPEED) * Player.OVER_BOUNCE_COEFFICIENT;
                        }
                        if (-speed.y > Player.MAX_BOUNCE_VERTICAL_SPEED) {
                            speed.y = -Player.MAX_BOUNCE_VERTICAL_SPEED;
                        }
                        canBounceRight = false;
                    }
                }
                if (jumpTrigger > 0) {
                    --jumpTrigger;
                }
                if (keys[KeyEvent.VK_LEFT]) {
                    canBounceLeft = canBounceRight = false;
                    if (speed.x > 0) {
                        if (speed.x > Player.DECELERATION) {
                            speed.x -= Player.DECELERATION;
                        } else {
                            speed.x = 0;
                        }
                    }
                    if (midAir) {
                        speed.x -= turbo ? Player.AIR_X_TURBO_ACCELERATION : Player.AIR_X_ACCELERATION;
                    } else {
                        speed.x -= turbo ? Player.X_TURBO_ACCELERATION : Player.X_ACCELERATION;
                    }
                    if (!turbo && -speed.x > Player.MAX_HORIZONTAL_SPEED) {
                        speed.x = -Player.MAX_HORIZONTAL_SPEED;
                    }
                    if (turbo && -speed.x > Player.MAX_TURBO_SPEED) {
                        speed.x = -Player.MAX_TURBO_SPEED;
                    }
                } else if (keys[KeyEvent.VK_RIGHT]) {
                    canBounceLeft = canBounceRight = false;
                    if (speed.x < 0) {
                        if (-speed.x > Player.DECELERATION) {
                            speed.x += Player.DECELERATION;
                        } else {
                            speed.x = 0;
                        }
                    }
                    if (midAir) {
                        speed.x += turbo ? Player.AIR_X_TURBO_ACCELERATION : Player.AIR_X_ACCELERATION;
                    } else {
                        speed.x += turbo ? Player.X_TURBO_ACCELERATION : Player.X_ACCELERATION;
                    }
                    if (!turbo && speed.x > Player.MAX_HORIZONTAL_SPEED) {
                        speed.x = Player.MAX_HORIZONTAL_SPEED;
                    }
                    if (turbo && speed.x > Player.MAX_TURBO_SPEED) {
                        speed.x = Player.MAX_TURBO_SPEED;
                    }
                } else if (!midAir) {
                    if (Math.abs(speed.x) < Player.DECELERATION) {
                        speed.x = 0;
                    } else if (speed.x < 0) {
                        speed.x += Player.DECELERATION;
                    } else {
                        speed.x -= Player.DECELERATION;
                    }
                }
                speed.y += Player.Y_ACCELERATION;
                if (speed.y > Player.MAX_VERTICAL_SPEED) {
                    speed.y = Player.MAX_VERTICAL_SPEED;
                }
                double deltaX = speed.x;
                double deltaY = speed.y;
                while (Math.abs(deltaX) > EPS || Math.abs(deltaY) > EPS) {
                    double multiplier = Math.min(1.0, 1.0 / Math.max(Math.abs(deltaX), Math.abs(deltaY)));
                    double xChange = deltaX * multiplier;
                    double yChange = deltaY * multiplier;
                    deltaX -= xChange;
                    deltaY -= yChange;
                    boolean xLeft = offset.x < Player.SIZE - EPS;
                    boolean xRight = offset.x > 1.0 - Player.SIZE + EPS;
                    boolean yTop = offset.y < Player.SIZE - EPS;
                    boolean yBottom = offset.y > 1.0 - Player.SIZE + EPS;
                    boolean leftWall = level.getFieldAt(position.y, position.x - 1) == Level.Cell.WALL ||
                            (yTop && level.getFieldAt(position.y - 1, position.x - 1) == Level.Cell.WALL) ||
                            (yBottom && level.getFieldAt(position.y + 1, position.x - 1) == Level.Cell.WALL);
                    boolean rightWall = level.getFieldAt(position.y, position.x + 1) == Level.Cell.WALL ||
                            (yTop && level.getFieldAt(position.y - 1, position.x + 1) == Level.Cell.WALL) ||
                            (yBottom && level.getFieldAt(position.y + 1, position.x + 1) == Level.Cell.WALL);
                    boolean topWall = (level.getFieldAt(position.y - 1, position.x) == Level.Cell.WALL ||
                            (xLeft && level.getFieldAt(position.y - 1, position.x - 1) == Level.Cell.WALL) ||
                            (xRight && level.getFieldAt(position.y - 1, position.x + 1) == Level.Cell.WALL));
                    boolean bottomWall = (level.getFieldAt(position.y + 1, position.x) == Level.Cell.WALL ||
                            (xLeft && level.getFieldAt(position.y + 1, position.x - 1) == Level.Cell.WALL) ||
                            (xRight && level.getFieldAt(position.y + 1, position.x + 1) == Level.Cell.WALL));
                    if (speed.x < EPS && leftWall) {
                        final double maxXChange = offset.x - Player.SIZE;
                        if (-speed.x > maxXChange) {
                            xChange = -maxXChange;
                            speed.x = deltaX = 0;
                        }
                    }
                    if (speed.x > -EPS && rightWall) {
                        final double maxXChange = 1.0 - Player.SIZE - offset.x;
                        if (speed.x > maxXChange) {
                            xChange = maxXChange;
                            speed.x = deltaX = 0;
                        }
                    }
                    if (speed.y < -EPS && topWall) {
                        final double maxYChange = offset.y - Player.SIZE;
                        if (-speed.y > maxYChange) {
                            yChange = -maxYChange;
                            speed.y = deltaY = 0;
                            if (level.getFieldAt(position.y - 1, position.x) == Level.Cell.WALL) {
                                blood.makeBottomBloody(position.y - 1, position.x);
                            }
                        }
                    }
                    if (speed.y > EPS && bottomWall) {
                        final double maxYChange = 1.0 - Player.SIZE - offset.y;
                        if (speed.y > maxYChange) {
                            yChange = maxYChange;
                            speed.y = deltaY = 0;
                            midAir = false;
                            if (level.getFieldAt(position.y + 1, position.x) == Level.Cell.WALL) {
                                blood.makeTopBloody(position.y + 1, position.x);
                            }
                        }
                    }
                    if (Math.abs(speed.y) > EPS) {
                        midAir = true;
                    }
                    for (int tries = 0; tries < 2; ++tries) {
                        Point oldPosition = (Point)position.clone();
                        double oldXOffset = offset.x;
                        double oldYOffset = offset.y;
                        offset.x += xChange;
                        offset.y += yChange;
                        correctThePosition();
                        if (level.getFieldAt(position.y, position.x) == Level.Cell.WALL) { //went into a wall diagonally
                            position = oldPosition;
                            offset.x = oldXOffset;
                            offset.y = oldYOffset;
                            double timeX; //relative time it takes to reach the cell adjacent by X
                            double timeY; //relative time it takes to reach the cell adjacent by Y
                            if (xChange > 0.0) {
                                timeX = (1.0 - Player.SIZE - offset.x) / xChange;
                            } else {
                                timeX = (offset.x - Player.SIZE - 0.0) / (-xChange);
                            }
                            if (yChange > 0.0) {
                                timeY = (1.0 - Player.SIZE - offset.y) / yChange;
                            } else {
                                timeY = (offset.y - Player.SIZE - 0.0) / (-yChange);
                            }
                            if (timeX > timeY) { //reaching the cell adjacent by Y first
                                if (xChange > 0.0) {
                                    xChange = 1.0 - Player.SIZE - offset.x;
                                } else {
                                    xChange = Player.SIZE - offset.x;
                                }
                                deltaX = 0; //continue moving in Y direction, but stop moving along X once reached the wall
                            } else {
                                if (yChange > 0.0) {
                                    yChange = 1.0 - Player.SIZE - offset.y;
                                } else {
                                    yChange = Player.SIZE - offset.y;
                                }
                                deltaY = 0;
                            }
                        } else {
                            break; //everything is OK
                        }
                    }
                    if (position.x == level.getFinishCell().x && position.y == level.getFinishCell().y) {
                        gameState = GameState.VICTORY;
                        return;
                    }
                    yTop = offset.y < Player.SIZE - EPS;
                    yBottom = offset.y > 1.0 - Player.SIZE + EPS;
                    leftWall = level.getFieldAt(position.y, position.x - 1) == Level.Cell.WALL ||
                            (yTop && level.getFieldAt(position.y - 1, position.x - 1) == Level.Cell.WALL) ||
                            (yBottom && level.getFieldAt(position.y + 1, position.x - 1) == Level.Cell.WALL);
                    rightWall = level.getFieldAt(position.y, position.x + 1) == Level.Cell.WALL ||
                            (yTop && level.getFieldAt(position.y - 1, position.x + 1) == Level.Cell.WALL) ||
                            (yBottom && level.getFieldAt(position.y + 1, position.x + 1) == Level.Cell.WALL);
                    canBounceRight = speed.x < EPS && leftWall && offset.x < Player.SIZE + EPS;
                    canBounceLeft = speed.x > -EPS && rightWall && offset.x > 1.0 - Player.SIZE - EPS;
                    if (speed.x < EPS && level.getFieldAt(position.y, position.x - 1) == Level.Cell.WALL && offset.x < Player.SIZE + EPS) {
                        blood.makeRightBloody(position.y, position.x - 1);
                    }
                    if (speed.x > -EPS && level.getFieldAt(position.y, position.x + 1) == Level.Cell.WALL && offset.x > 1.0 - Player.SIZE - EPS) {
                        blood.makeLeftBloody(position.y, position.x + 1);
                    }
                    if (spikesTouched()) {
                        gameState = GameState.DEAD;
                    }
                    Point2D.Double from = new Point2D.Double(position.x + offset.x - xChange, position.y + offset.y - yChange);
                    Point2D.Double to = new Point2D.Double(position.x + offset.x, position.y + offset.y);
                    for (int i = 0; i < level.getSawCount(); ++i) {
                        Saw.IntersectionState state = level.getSaw(i).checkForIntersection(from, to);
                        if (state == Saw.IntersectionState.INSIDE) {
                            gameState = GameState.DEAD;
                            level.getSaw(i).makeBloody();
                        } else if (state == Saw.IntersectionState.NEAR) {
                            level.getSaw(i).makeBloody();
                        }
                    }
                    for (int i = 0; i < level.getPortalCount(); ++i) {
                        Portal.IntersectionState state = level.getPortal(i).checkForIntersection(from, to);
                        if (state == Portal.IntersectionState.INSIDE) {
                            if (i != inactivePortal && level.portalExists(level.getPortal(i).getDestinationID())) {
                                Point2D.Double teleportFrom = level.getPortal(i).getPosition();
                                Point2D.Double teleportTo = level.getPortal(level.getPortal(i).getDestinationID()).getPosition();
                                double dx = teleportTo.x - teleportFrom.x;
                                double dy = teleportTo.y - teleportFrom.y;
                                position.x += Math.round(dx);
                                offset.x += dx - Math.round(dx);
                                position.y += Math.round(dy);
                                offset.y += dy - Math.round(dy);
                                correctThePosition();
                                inactivePortal = level.getPortal(i).getDestinationID();
                                break;
                            }
                        } else {
                            if (i == inactivePortal) {
                                inactivePortal = -1;
                            }
                        }
                    }
                }
            }
            transpose.x = (int)Math.round(offset.x * Visuals.CELL_SIZE);
            transpose.y = (int)Math.round(offset.y * Visuals.CELL_SIZE);

            if (!Settings.getFrameSkip() || !skipNextFrame) {
                repaint();
            }
            skipNextFrame = !skipNextFrame;

            if (messageFrames > 0) {
                --messageFrames;
            }
        }
    };

    private void correctThePosition() {
        if (offset.x < 0.0 - EPS) {
            offset.x += 1.0;
            --position.x;
        }
        if (offset.x > 1.0 + EPS) {
            offset.x -= 1.0;
            ++position.x;
        }
        if (offset.y < 0.0 - EPS) {
            offset.y += 1.0;
            --position.y;
        }
        if (offset.y > 1.0 + EPS) {
            offset.y -= 1.0;
            ++position.y;
        }
    }

    private boolean spikesTouched() {
        boolean xLeft = offset.x < Player.SIZE - EPS;
        boolean xRight = offset.x > 1.0 - Player.SIZE + EPS;
        boolean yTop = offset.y < Player.SIZE - EPS;
        boolean yBottom = offset.y > 1.0 - Player.SIZE + EPS;
        return level.getFieldAt(position.y, position.x) == Level.Cell.SPIKES ||
                (xLeft && level.getFieldAt(position.y, position.x - 1) == Level.Cell.SPIKES) ||
                (xRight && level.getFieldAt(position.y, position.x + 1) == Level.Cell.SPIKES) ||
                (yTop && level.getFieldAt(position.y - 1, position.x) == Level.Cell.SPIKES) ||
                (yBottom && level.getFieldAt(position.y + 1, position.x) == Level.Cell.SPIKES) ||
                (xLeft && yTop && level.getFieldAt(position.y - 1, position.x - 1) == Level.Cell.SPIKES) ||
                (xLeft && yBottom && level.getFieldAt(position.y + 1, position.x - 1) == Level.Cell.SPIKES) ||
                (xRight && yTop && level.getFieldAt(position.y - 1, position.x + 1) == Level.Cell.SPIKES) ||
                (xRight && yBottom && level.getFieldAt(position.y + 1, position.x + 1) == Level.Cell.SPIKES);
    }

    @Override
    public void paintComponent (Graphics g) {
        Drawer drawer = new Drawer(g, this, resources, level, blood, animationFrame);
        if (gameState != GameState.MENU) {
            if (level.getBackground() == GameResources.NO_BACKGROUND) {
                g.setColor(level.getBgColor());
                g.fillRect(0, 0, Visuals.WIDTH, Visuals.HEIGHT);
            } else {
                g.drawImage(resources.getBackground(level.getBackground()), 0, 0, Visuals.WIDTH, Visuals.HEIGHT, this);
            }
        }
        if (gameState == GameState.MENU) {
            drawer.drawMenu(menu);
        } else if (gameState == GameState.EDITOR) {
            drawer.initializeEditorCamera(position, transpose, level.getStartingCell());
            drawer.drawEmptyCells();
            drawer.drawWalls(true);
            drawer.drawSaws(true); //draw saws in front of walls in the editor
            drawer.drawPortals(true);
            drawer.drawPlayer();
            drawer.drawGoal();
            drawer.drawTexturePanel(selectedTexture);
            if (keys[KeyEvent.VK_F1]) {
                drawer.drawEditorHelp();
            }
        } else {
            drawer.initializeCamera(position, transpose);
            drawer.drawEmptyCells();
            if (gameState == GameState.DEAD) {
                drawer.drawDeadPlayer(); //draw dead player behind game objects
            }
            drawer.drawSaws(false); //draw saws behind walls in game
            drawer.drawWalls(false);
            drawer.drawPortals(false);
            if (gameState != GameState.DEAD) {
                drawer.drawPlayer(); //alive player in front of game objects
            }
            drawer.drawGoal();
            if (gameState == GameState.DEAD) {
                drawer.drawFramedCaption(Strings.DEATH_MESSAGE, new Point(30, 30), new Color(255, 0, 0), 12.0f);
            }
            if (gameState == GameState.VICTORY) {
                drawer.drawFramedCaption(Strings.VICTORY_MESSAGE, new Point(30, 30), new Color(0, 255, 0), 12.0f);
            }
            String time = new DecimalFormat("0.00").format(Visuals.sleepTimePerFrame * 1e-9 * totalFrame);
            drawer.drawFramedCaption(time, new Point(Visuals.WIDTH - 50, 20), new Color(0, 255, 0), 12.0f);
            if (keys[KeyEvent.VK_F1]) {
                drawer.drawGameHelp();
            }
        }
        if (messageFrames > 0) {
            drawer.drawFramedCaption(message, new Point(30, 50), new Color(150, 150, 255), 12.0f);
        }
    }

    public void showMessage(String message, double seconds) {
        this.message = message;
        this.messageFrames = Math.max(0, (int)(seconds * Visuals.FPS));
    }

    private void startNewLevel(int index) {
        level = resources.getLevel(index);
        levelIndex = index;
        for (int i = 0; i < level.getSawCount(); ++i) {
            level.getSaw(i).initialize();
        }
        inactivePortal = -1;
        blood = new Blood(level.getHeight(), level.getWidth());
        position = (Point)level.getStartingCell().clone();
        offset = new Point2D.Double(0.5, 0.5);
        speed.x = speed.y = 0.0;
        jumpTrigger = 0;
        animationFrame = 0;
        totalFrame = 0;
        gameState = GameState.GAME;
    }

    private void nextLevel() {
        if (levelIndex + 1 == resources.getLevelCount()) {
            startNewLevel(0);
            gameState = GameState.MENU;
        } else {
            startNewLevel(levelIndex + 1);
        }
    }

    public GamePanel(GameResources resources) {
        this.resources = resources;
        this.setDoubleBuffered(true);
        startNewLevel(0);
        menu = new Menu(resources.getLevelCount());
        gameState = GameState.MENU;
        lastTime = System.nanoTime();
        timer.scheduleAtFixedRate(timerTask, 100, 1);
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    private void selectNewBgColor() {
        JColorChooser colorChooser = new JColorChooser(level.getBgColor());
        AbstractColorChooserPanel colorPanel = null;
        for (AbstractColorChooserPanel panel : colorChooser.getChooserPanels()) {
            if (panel.getDisplayName().equals("RGB")) {
                colorPanel = panel;
            }
        }

        int result = JOptionPane.showConfirmDialog(null, colorPanel,
                "Choose new background color for this level", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Color color = colorChooser.getColor();
            level.setBgColor(new Color(color.getRed(), color.getGreen(), color.getBlue())); //removing alpha
        }
    }

    public void keyPressed(KeyEvent e) {
        boolean flag = true;

        if (gameState == GameState.GAME) {
            if (e.getKeyCode() == KeyEvent.VK_S && !keys[KeyEvent.VK_S]) {
                jumpTrigger = 8;
            }
            if (e.getKeyCode() == KeyEvent.VK_X && !keys[KeyEvent.VK_X]) {
                jumpTrigger = 8;
            }
        }
        if (gameState == GameState.EDITOR) {
            if (e.getKeyCode() == KeyEvent.VK_Y && !keys[KeyEvent.VK_Y]) {
                level.extendTop();
            }
            if (e.getKeyCode() == KeyEvent.VK_H && !keys[KeyEvent.VK_H]) {
                level.shrinkTop();
            }
            if (e.getKeyCode() == KeyEvent.VK_J && !keys[KeyEvent.VK_J]) {
                level.extendBottom();
            }
            if (e.getKeyCode() == KeyEvent.VK_U && !keys[KeyEvent.VK_U]) {
                level.shrinkBottom();
            }
            if (e.getKeyCode() == KeyEvent.VK_I && !keys[KeyEvent.VK_I]) {
                level.extendLeft();
            }
            if (e.getKeyCode() == KeyEvent.VK_O && !keys[KeyEvent.VK_O]) {
                level.shrinkLeft();
            }
            if (e.getKeyCode() == KeyEvent.VK_L && !keys[KeyEvent.VK_L]) {
                level.extendRight();
            }
            if (e.getKeyCode() == KeyEvent.VK_K && !keys[KeyEvent.VK_K]) {
                level.shrinkRight();
            }

            if (e.getKeyCode() == KeyEvent.VK_G && !keys[KeyEvent.VK_G]) {
                Settings.switchEditorGrid();
            }
            if (e.getKeyCode() == KeyEvent.VK_T && !keys[KeyEvent.VK_T]) {
                Settings.switchEditorExtras();
            }

            if (e.getKeyCode() == KeyEvent.VK_A && !keys[KeyEvent.VK_A]) {
                if (selectedTexture > 0) {
                    --selectedTexture;
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_Z && !keys[KeyEvent.VK_Z]) {
                if (selectedTexture < resources.getTextureCount() - 1) {
                    ++selectedTexture;
                }
            }

            if (e.getKeyCode() == KeyEvent.VK_COMMA && !keys[KeyEvent.VK_COMMA]) {
                if (level.getBackground() == 0) {
                    level.setBackground(GameResources.NO_BACKGROUND);
                } else if (level.getBackground() == GameResources.NO_BACKGROUND) {
                    level.setBackground(resources.getBackgroundsCount() - 1);
                } else {
                    level.setBackground(level.getBackground() - 1);
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_PERIOD && !keys[KeyEvent.VK_PERIOD]) {
                if (level.getBackground() == resources.getBackgroundsCount() - 1) {
                    level.setBackground(GameResources.NO_BACKGROUND);
                } else if (level.getBackground() == GameResources.NO_BACKGROUND) {
                    level.setBackground(0);
                } else {
                    level.setBackground(level.getBackground() + 1);
                }
            }

            if (e.getKeyCode() == KeyEvent.VK_B && !keys[KeyEvent.VK_B]) {
                selectNewBgColor();
                flag = false;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_F && !keys[KeyEvent.VK_F]) {
            Settings.switchFrameSkip();
            if (Settings.getFrameSkip()) {
                showMessage(Strings.FRAME_SKIP_ON, 2.0);
            } else {
                showMessage(Strings.FRAME_SKIP_OFF, 2.0);
            }
        }

        keys[e.getKeyCode()] = flag;

        if (gameState == GameState.MENU) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                menu.goUp();
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                menu.goDown();
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (menu.getParent() != null) {
                    menu = menu.getParent();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
                String action = menu.getAction(menu.getSelectedItem());
                if (action.length() > 6 && action.substring(0, 6).equals("editor")) {
                    menu.selectSecondaryItem();
                    showMessage(Strings.MENU_SWITCH_LEVELS, 2.0);
                }
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (menu.getSecondaryItem() != -1) {
                    int swapLevelIndex = menu.getSecondaryItem() - 2;
                    int putAfter = menu.getSelectedItem() - 2;
                    resources.swapLevel(swapLevelIndex, putAfter);
                    menu.deselectSecondaryItem();
                } else {
                    String action = menu.getAction(menu.getSelectedItem());
                    if (action != null) {
                        if (action.equals("new_game")) {
                            startNewLevel(0);
                            gameState = GameState.GAME;
                            showMessage(Strings.NEED_HELP, 3.0);
                        } else if (action.equals("continue")) {
                            gameState = GameState.GAME;
                        } else if (action.equals("exit")) {
                            timer.cancel();
                            SwingUtilities.getWindowAncestor(this).dispose();
                        } else if (action.equals("save_levels")) {
                            JFileChooser fc = new JFileChooser();
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("Very Meat Boy files", "vmb");
                            fc.setFileFilter(filter);
                            fc.setAcceptAllFileFilterUsed(false);
                            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                                try {
                                    String fileName = fc.getSelectedFile().getAbsolutePath();
                                    if (fileName.length() < 4 || !fileName.substring(fileName.length() - 4).equals(".vmb")) {
                                        fileName += ".vmb";
                                    }
                                    File file = new File(fileName);
                                    if (file.createNewFile()) {
                                        throw new IOException();
                                    }
                                    OutputStream out = new FileOutputStream(file);
                                    resources.writeCustomLevelSet(out);
                                    showMessage(Strings.LEVEL_SET_SAVE_SUCCESS, 2.0);
                                } catch (IOException ex) {
                                    showMessage(Strings.LEVEL_SET_SAVE_ERROR, 2.0);
                                }
                            }
                        } else if (action.equals("load_levels")) {
                            JFileChooser fc = new JFileChooser();
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("Very Meat Boy files", "vmb");
                            fc.setFileFilter(filter);
                            fc.setAcceptAllFileFilterUsed(false);
                            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                                try {
                                    InputStream in = new FileInputStream(fc.getSelectedFile());
                                    resources.readCustomLevelSet(in);
                                    showMessage(Strings.LEVEL_SET_LOAD_SUCCESS, 2.0);
                                } catch (IOException ex) {
                                    showMessage(Strings.LEVEL_SET_LOAD_ERROR, 2.0);
                                }
                                menu = new Menu(resources.getLevelCount());
                            }
                        } else if (action.length() > 6 && action.substring(0, 6).equals("editor")) {
                            int editorLevel = Integer.valueOf(action.substring(6));
                            if (editorLevel == resources.getLevelCount()) {
                                resources.addLevel(new Level(), editorLevel);
                            }
                            startEditor(editorLevel);
                        }
                    }
                    menu = menu.goInside(menu.getSelectedItem());
                }
            }
        }
    }

    public void startEditor(int editorLevel) {
        startNewLevel(editorLevel);
        selectedTexture = 0;
        gameState = GameState.EDITOR;
        showMessage(Strings.NEED_HELP, 3.0);
    }

    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    private int editorSpikesFlag = 0; //0 - do nothing
                                      //1 - change walls into spikes
                                      //2 - change spikes into walls
    private byte editorSelectionType = 0; //0 - portal
                                          //1 - saw
    private int editorSelectionIndex = -1; //-1 - nothing is selected;
    private int editorPortalIndex = -1;

    private void editSaw(int index) {
        Saw saw = level.getSaw(index);
        JTextField x0Field = new JTextField(new DecimalFormat("0.00").format(saw.getFirstFramePosition().x));
        JTextField y0Field = new JTextField(new DecimalFormat("0.00").format(saw.getFirstFramePosition().y));
        JTextField x1Field = new JTextField(new DecimalFormat("0.00").format(saw.getLastFramePosition().x));
        JTextField y1Field = new JTextField(new DecimalFormat("0.00").format(saw.getLastFramePosition().y));
        JTextField radiusField = new JTextField(new DecimalFormat("0.00").format(saw.getRadius()));
        JTextField framesField = new JTextField(saw.getFrames() + "");
        JTextField startingFrameField = new JTextField(saw.getStartingFrame() + "");

        JPanel sawPanel = new JPanel();
        sawPanel.setLayout(new GridLayout(7, 2, 5, 5));
        sawPanel.add(new JLabel("x0:"));
        sawPanel.add(x0Field);
        sawPanel.add(new JLabel("y0:"));
        sawPanel.add(y0Field);
        sawPanel.add(new JLabel("x1:"));
        sawPanel.add(x1Field);
        sawPanel.add(new JLabel("y1:"));
        sawPanel.add(y1Field);
        sawPanel.add(new JLabel("Radius (>= 0.2):"));
        sawPanel.add(radiusField);
        sawPanel.add(new JLabel("Frames (100 per second):"));
        sawPanel.add(framesField);
        sawPanel.add(new JLabel("Starting frame:"));
        sawPanel.add(startingFrameField);

        int result = JOptionPane.showConfirmDialog(null, sawPanel,
                "Enter new values for the saw", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double x0 = Double.parseDouble(x0Field.getText());
                double y0 = Double.parseDouble(y0Field.getText());
                double x1 = Double.parseDouble(x1Field.getText());
                double y1 = Double.parseDouble(y1Field.getText());
                double radius = Double.parseDouble(radiusField.getText());
                int frames = Integer.parseInt(framesField.getText());
                int startingFrame = Integer.parseInt(startingFrameField.getText());
                if (frames <= 0 || startingFrame < 0 || startingFrame > frames || radius < 0.2 - Geometry.EPS) {
                    throw new NumberFormatException();
                }
                level.setSaw(new Saw(y0, x0, y1, x1, radius, frames, startingFrame, Saw.Direction.FORWARDS), index);
            } catch (NumberFormatException ex) {
                showMessage(Strings.EDITOR_INCORRECT_VALUES, 2.0);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (gameState == GameState.EDITOR) {
            Point p = new Point(position.x * Visuals.CELL_SIZE + transpose.x - Visuals.WIDTH / 2 + e.getX(),
                    position.y * Visuals.CELL_SIZE + transpose.y - Visuals.HEIGHT / 2 + e.getY());
            Point2D.Double doubleP = new Point2D.Double((double)p.x / Visuals.CELL_SIZE, (double)p.y / Visuals.CELL_SIZE);
            p.x /= Visuals.CELL_SIZE;
            p.y /= Visuals.CELL_SIZE;
            if (keys[KeyEvent.VK_S] && e.getButton() == MouseEvent.BUTTON1) {
                //S + left mouse = change cell from wall to spikes and back
                if (level.getFieldAt(p.y, p.x) == Level.Cell.WALL) {
                    editorSpikesFlag = 1;
                    level.setFieldAt(p.y, p.x, Level.Cell.SPIKES);
                } else if (level.getFieldAt(p.y, p.x) == Level.Cell.SPIKES) {
                    editorSpikesFlag = 2;
                    level.setFieldAt(p.y, p.x, Level.Cell.WALL);
                } else {
                    editorSpikesFlag = 0;
                }
            } else if (e.getButton() == MouseEvent.BUTTON1 && keys[KeyEvent.VK_CONTROL]) {
                //Ctrl + left mouse button = add wall with selected texture
                level.setFieldAt(p.y, p.x, Level.Cell.WALL);
                level.setTextureAt(p.y, p.x, selectedTexture);
            } else if (e.getButton() == MouseEvent.BUTTON3 && keys[KeyEvent.VK_CONTROL]) {
                //Ctrl + right mouse button = remove walls/spikes
                level.setFieldAt(p.y, p.x, Level.Cell.EMPTY);
                level.setTextureAt(p.y, p.x, GameResources.NO_TEXTURE);
            } else if (e.getButton() == MouseEvent.BUTTON1 && keys[KeyEvent.VK_E]) {
                //E + left mouse button = set no background for one cell
                level.setFieldAt(p.y, p.x, Level.Cell.EMPTY_NOBG);
                level.setTextureAt(p.y, p.x, GameResources.NO_TEXTURE);
            } else if (e.getButton() == MouseEvent.BUTTON1 && keys[KeyEvent.VK_ALT]) {
                //Alt + left mouse button = game object's settings
                editorPortalIndex = -1;
                if (message.equals(Strings.EDITOR_PORTAL_DESTINATION)) {
                    showMessage("", 0.0);
                }
                for (int i = level.getPortalCount() - 1; i >= 0; --i) {
                    if (level.getPortal(i).checkForIntersection(doubleP, doubleP) == Portal.IntersectionState.INSIDE) {
                        editorPortalIndex = i;
                        showMessage(Strings.EDITOR_PORTAL_DESTINATION, 36000.0);
                        break;
                    }
                }
                if (editorPortalIndex == -1) {
                    for (int i = level.getSawCount() - 1; i >= 0; --i) {
                        if (level.getSaw(i).checkForIntersection(doubleP, doubleP) == Saw.IntersectionState.INSIDE) {
                            keys[KeyEvent.VK_ALT] = false; //depress Alt before calling a saw dialog, so the button doesn't stuck
                            editSaw(i);
                            break;
                        }
                    }
                }
            } else if (e.getButton() == MouseEvent.BUTTON3 && keys[KeyEvent.VK_ALT]) {
                //Alt + right mouse button = remove game object
                boolean removed = false;
                for (int i = level.getPortalCount() - 1; i >= 0; --i) {
                    if (level.getPortal(i).checkForIntersection(doubleP, doubleP) == Portal.IntersectionState.INSIDE) {
                        level.removePortal(i);
                        removed = true;
                        break;
                    }
                }
                if (!removed) {
                    for (int i = level.getSawCount() - 1; i >= 0; --i) {
                        if (level.getSaw(i).checkForIntersection(doubleP, doubleP) == Saw.IntersectionState.INSIDE) {
                            level.removeSaw(i);
                            break;
                        }
                    }
                }
            } else if (keys[KeyEvent.VK_1] && e.getButton() == MouseEvent.BUTTON1) {
                //1 + left mouse button = add Saw
                level.addSaw(new Saw(doubleP.y, doubleP.x, doubleP.y, doubleP.x, 1.5, 100, 0, Saw.Direction.FORWARDS));
            } else if (keys[KeyEvent.VK_2] && e.getButton() == MouseEvent.BUTTON1) {
                //2 + left mouse button = add Portal
                level.addPortal(new Portal(doubleP.y, doubleP.x, -1));
            } else if (keys[KeyEvent.VK_P] && e.getButton() == MouseEvent.BUTTON1) {
                level.setStartingCell((Point) p.clone());
            } else if (keys[KeyEvent.VK_SEMICOLON] && e.getButton() == MouseEvent.BUTTON1) {
                level.setFinishCell((Point) p.clone());
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                //handling portal destination setup before dragging
                if (editorPortalIndex != -1) {
                    for (int i = level.getPortalCount() - 1; i >= 0; --i) {
                        if (level.getPortal(i).checkForIntersection(doubleP, doubleP) == Portal.IntersectionState.INSIDE) {
                            if (editorPortalIndex == i) {
                                showMessage(Strings.EDITOR_PORTAL_TO_ITSELF, 2.0);
                            } else {
                                level.getPortal(editorPortalIndex).setDestinationID(i);
                            }
                            break;
                        }
                    }
                    editorPortalIndex = -1;
                    if (message.equals(Strings.EDITOR_PORTAL_DESTINATION)) {
                        showMessage("", 0.0);
                    }
                    return; //nothing else should be done on this click
                }
                //just left mouse button + drag = drag game object; dragging itself is implemented in mouseDragged
                editorSelectionIndex = -1;
                for (int i = level.getPortalCount() - 1; i >= 0; --i) { //last portal is drawn on top, so drag it with higher priority
                    if (level.getPortal(i).checkForIntersection(doubleP, doubleP) == Portal.IntersectionState.INSIDE) {
                        editorSelectionType = 1;
                        editorSelectionIndex = i;
                        break;
                    }
                }
                if (editorSelectionIndex == -1) {
                    for (int i = level.getSawCount() - 1; i >= 0; --i) { //same as with portals
                        if (level.getSaw(i).checkForIntersection(doubleP, doubleP) == Saw.IntersectionState.INSIDE) {
                            editorSelectionType = 0;
                            editorSelectionIndex = i;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (gameState == GameState.EDITOR) {
            int b1 = MouseEvent.BUTTON1_DOWN_MASK;
            int b3 = MouseEvent.BUTTON3_DOWN_MASK;
            Point p = new Point(position.x * Visuals.CELL_SIZE + transpose.x - Visuals.WIDTH / 2 + e.getX(),
                    position.y * Visuals.CELL_SIZE + transpose.y - Visuals.HEIGHT / 2 + e.getY());
            Point2D.Double doubleP = new Point2D.Double((double)p.x / Visuals.CELL_SIZE, (double)p.y / Visuals.CELL_SIZE);
            p.x /= Visuals.CELL_SIZE;
            p.y /= Visuals.CELL_SIZE;
            if (keys[KeyEvent.VK_S] && (e.getModifiersEx() & (b1 | b3)) == b1) {
                //S + left mouse = change cell from wall to spikes and back
                if (level.getFieldAt(p.y, p.x) == Level.Cell.WALL || level.getFieldAt(p.y, p.x) == Level.Cell.SPIKES) {
                    if (editorSpikesFlag == 1) {
                        level.setFieldAt(p.y, p.x, Level.Cell.SPIKES);
                    } else if (editorSpikesFlag == 2) {
                        level.setFieldAt(p.y, p.x, Level.Cell.WALL);
                    }
                }
            } else if ((e.getModifiersEx() & (b1 | b3)) == b1 && keys[KeyEvent.VK_CONTROL]) {
                //Ctrl + left mouse button = add wall with selected texture
                level.setFieldAt(p.y, p.x, Level.Cell.WALL);
                level.setTextureAt(p.y, p.x, selectedTexture);
            } else if ((e.getModifiersEx() & (b1 | b3)) == b3 && keys[KeyEvent.VK_CONTROL]) {
                //Ctrl + right mouse button = remove walls/spikes
                level.setFieldAt(p.y, p.x, Level.Cell.EMPTY);
                level.setTextureAt(p.y, p.x, GameResources.NO_TEXTURE);
            } else if ((e.getModifiersEx() & (b1 | b3)) == b1 && keys[KeyEvent.VK_E]) {
                //E + left mouse button = set no background for one cell
                level.setFieldAt(p.y, p.x, Level.Cell.EMPTY_NOBG);
                level.setTextureAt(p.y, p.x, GameResources.NO_TEXTURE);
            } else if ((e.getModifiersEx() & (b1 | b3)) == b1 && editorSelectionIndex != -1) {
                //just left mouse button + drag = drag game object
                if (keys[KeyEvent.VK_SHIFT]) {
                    doubleP = new Point2D.Double(Math.round(doubleP.x), Math.round(doubleP.y));
                }
                if (editorSelectionType == 0) {
                    Point2D.Double position = level.getSaw(editorSelectionIndex).getPosition();
                    level.getSaw(editorSelectionIndex).move(new Point2D.Double(doubleP.x - position.x, doubleP.y - position.y));
                } else {
                    Point2D.Double position = level.getPortal(editorSelectionIndex).getPosition();
                    level.getPortal(editorSelectionIndex).move(new Point2D.Double(doubleP.x - position.x, doubleP.y - position.y));
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        editorSelectionIndex = -1; //stop dragging a game object
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }
}
