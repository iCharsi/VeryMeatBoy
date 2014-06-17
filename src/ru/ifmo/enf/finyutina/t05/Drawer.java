package ru.ifmo.enf.finyutina.t05;

import ru.ifmo.enf.finyutina.t05.game_objects.Portal;
import ru.ifmo.enf.finyutina.t05.game_objects.Saw;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/11/14.
 */
class Drawer {
    static final int ANIMATION_FRAME_COUNT = 800;
    private static final int PLAYER_RADIUS = (int) Math.round(Player.SIZE * Visuals.CELL_SIZE);

    private final int animationFrame;
    private static final Point center = new Point(Visuals.WIDTH / 2, Visuals.HEIGHT / 2);
    private Point position;
    private Point transpose;
    private Point cameraPosition;
    private Point cameraTranspose;
    private Point2D.Double camera;
    private static final Point visibleCells = new Point(Visuals.WIDTH / (Visuals.CELL_SIZE * 2) + 1, Visuals.HEIGHT / (Visuals.CELL_SIZE * 2) + 1);
    private final Level level;
    private final Graphics g;
    private final GameResources resources;
    private final Blood blood;
    private final ImageObserver obs;
    Polygon arrowHead = new Polygon();

    public Drawer(Graphics g, ImageObserver obs, GameResources resources, Level level, Blood blood, int animationFrame) {
        this.level = level;
        this.animationFrame = animationFrame;
        this.g = g;
        this.obs = obs;
        this.resources = resources;
        this.blood = blood;
        arrowHead.addPoint(0, 5);
        arrowHead.addPoint(-5, -5);
        arrowHead.addPoint(5, -5);
    }

    public void initializeCamera(Point position, Point transpose) {
        this.position = position;
        this.transpose = transpose;
        cameraTranspose = new Point(position.x * Visuals.CELL_SIZE + transpose.x, position.y * Visuals.CELL_SIZE + transpose.y);
        if (level.getWidth() * Visuals.CELL_SIZE >= Visuals.WIDTH) {
            if (cameraTranspose.x < center.x) {
                cameraTranspose.x = center.x;
            }
            if (level.getWidth() * Visuals.CELL_SIZE - center.x < cameraTranspose.x) {
                cameraTranspose.x = level.getWidth() * Visuals.CELL_SIZE - center.x;
            }
        } else {
            cameraTranspose.x = level.getWidth() * Visuals.CELL_SIZE / 2;
        }
        if (level.getHeight() * Visuals.CELL_SIZE >= Visuals.HEIGHT) {
            if (cameraTranspose.y < center.y) {
                cameraTranspose.y = center.y;
            }
            if (level.getHeight() * Visuals.CELL_SIZE - center.y < cameraTranspose.y) {
                cameraTranspose.y = level.getHeight() * Visuals.CELL_SIZE - center.y;
            }
        } else {
            cameraTranspose.y = level.getHeight() * Visuals.CELL_SIZE / 2;
        }
        cameraPosition = new Point(cameraTranspose.x / Visuals.CELL_SIZE, cameraTranspose.y / Visuals.CELL_SIZE);
        cameraTranspose.x %= Visuals.CELL_SIZE;
        cameraTranspose.y %= Visuals.CELL_SIZE;
        camera = new Point2D.Double(cameraPosition.getX() + cameraTranspose.getX() / Visuals.CELL_SIZE,
                cameraPosition.getY() + cameraTranspose.getY() / Visuals.CELL_SIZE);
    }

    public void initializeEditorCamera(Point position, Point transpose, Point startingCell) {
        this.position = startingCell;
        this.transpose = new Point(Visuals.CELL_SIZE / 2, Visuals.CELL_SIZE / 2);
        this.cameraPosition = (Point)position.clone();
        this.cameraTranspose = (Point)transpose.clone();
        camera = new Point2D.Double(cameraPosition.getX() + cameraTranspose.getX() / Visuals.CELL_SIZE,
                cameraPosition.getY() + cameraTranspose.getY() / Visuals.CELL_SIZE);
    }

    public void drawPlayer() {
        g.setColor(new Color(255, 0, 0));
        g.fillOval(center.x - PLAYER_RADIUS + (position.x - cameraPosition.x) * Visuals.CELL_SIZE + transpose.x - cameraTranspose.x,
                center.y - PLAYER_RADIUS + (position.y - cameraPosition.y) * Visuals.CELL_SIZE + transpose.y - cameraTranspose.y,
                PLAYER_RADIUS * 2, PLAYER_RADIUS * 2);
        g.setColor(new Color(50, 0, 0));
        g.drawOval(center.x - PLAYER_RADIUS + (position.x - cameraPosition.x) * Visuals.CELL_SIZE + transpose.x - cameraTranspose.x,
                center.y - PLAYER_RADIUS + (position.y - cameraPosition.y) * Visuals.CELL_SIZE + transpose.y - cameraTranspose.y,
                PLAYER_RADIUS * 2, PLAYER_RADIUS * 2);
    }

    public void drawGoal() {
        Point finish = level.getFinishCell();
        g.setColor(new Color(255, 0, 255));
        g.fillOval(center.x - PLAYER_RADIUS + (finish.x - cameraPosition.x) * Visuals.CELL_SIZE + Visuals.CELL_SIZE / 2 - cameraTranspose.x,
                center.y - PLAYER_RADIUS + (finish.y - cameraPosition.y) * Visuals.CELL_SIZE + Visuals.CELL_SIZE - PLAYER_RADIUS - cameraTranspose.y,
                PLAYER_RADIUS * 2, PLAYER_RADIUS * 2);
        g.setColor(new Color(50, 0, 50));
        g.drawOval(center.x - PLAYER_RADIUS + (finish.x - cameraPosition.x) * Visuals.CELL_SIZE + Visuals.CELL_SIZE / 2 - cameraTranspose.x,
                center.y - PLAYER_RADIUS + (finish.y - cameraPosition.y) * Visuals.CELL_SIZE + Visuals.CELL_SIZE - PLAYER_RADIUS - cameraTranspose.y,
                PLAYER_RADIUS * 2, PLAYER_RADIUS * 2);
    }

    public void drawDeadPlayer() {
        if (animationFrame % 200 < 100) {
            g.setColor((new Color(150, 150, 150, 20 + 3 * (animationFrame % 200) / 4)));
        } else {
            g.setColor((new Color(150, 150, 150, 20 + 150 - 3 * (animationFrame % 200) / 4)));
        }
        g.fillOval(center.x - PLAYER_RADIUS + (position.x - cameraPosition.x) * Visuals.CELL_SIZE + transpose.x - cameraTranspose.x,
                center.y - PLAYER_RADIUS + (position.y - cameraPosition.y) * Visuals.CELL_SIZE + transpose.y - cameraTranspose.y,
                PLAYER_RADIUS * 2, PLAYER_RADIUS * 2);
        if (animationFrame % 200 < 100) {
            g.setColor((new Color(0, 0, 0, 20 + 3 * (animationFrame % 200) / 4)));
        } else {
            g.setColor((new Color(0, 0, 0, 20 + 150 - 3 * (animationFrame % 200) / 4)));
        }
        g.drawOval(center.x - PLAYER_RADIUS + (position.x - cameraPosition.x) * Visuals.CELL_SIZE + transpose.x - cameraTranspose.x,
                center.y - PLAYER_RADIUS + (position.y - cameraPosition.y) * Visuals.CELL_SIZE + transpose.y - cameraTranspose.y,
                PLAYER_RADIUS * 2, PLAYER_RADIUS * 2);
    }

    public void drawArrow(Point from, Point to, boolean emphasizeOrigin) {
        g.drawLine(from.x, from.y, to.x, to.y);
        if (emphasizeOrigin) {
            g.fillOval(from.x - 4, from.y - 4, 8, 8);
        }

        AffineTransform at = new AffineTransform();
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        at.translate(to.x, to.y);
        at.rotate(angle - Math.PI / 2.0);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setTransform(at);
        g2d.fill(arrowHead);
        g2d.dispose();
    }

    public void drawSaw(Point2D.Double position, double radius, boolean bloody, boolean animated, boolean transparent) {
        if ((position.x - radius - camera.x < visibleCells.x) &&
                (position.x + radius - camera.x > -visibleCells.x) &&
                (position.y - radius - camera.y < visibleCells.y) &&
                (position.y + radius - camera.y > -visibleCells.y)) { //only draw if visible
            int sawX = center.x + (int)((position.x - camera.x) * Visuals.CELL_SIZE);
            int sawY = center.y + (int)((position.y - camera.y) * Visuals.CELL_SIZE);

            BufferedImage sawTexture;
            if (transparent) {
                sawTexture = resources.getTransparentSawTexture();
            } else if (bloody) {
                sawTexture = resources.getBloodySawTexture();
            } else {
                sawTexture = resources.getSawTexture();
            }

            AffineTransform at = new AffineTransform();
            // 4. translate it's center to the required position
            at.translate(sawX, sawY);
            // 3. rotate
            if (animated) {
                at.rotate(animationFrame * Math.PI * 8.0 / Drawer.ANIMATION_FRAME_COUNT);
            }
            // 2. scale
            at.scale(2 * Visuals.CELL_SIZE * radius / sawTexture.getWidth(),
                    2 * Visuals.CELL_SIZE * radius / sawTexture.getHeight());
            // 1. translate the object so that you rotate it around the center
            at.translate(-sawTexture.getWidth() / 2.0, -sawTexture.getHeight() / 2.0);
            // draw the image
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(sawTexture, at, null);
        }
    }

    public void drawSaws(boolean editor) {
        for (int i = 0; i < level.getSawCount(); ++i) {
            Saw saw = level.getSaw(i);
            drawSaw(saw.getPosition(), saw.getRadius(), saw.isBloody(), true, false);
            if (editor && Settings.getEditorExtras() &&
                    Geometry.distance(saw.getFirstFramePosition(), saw.getLastFramePosition()) > Geometry.EPS) {
                drawSaw(saw.getFirstFramePosition(), saw.getRadius(), saw.isBloody(), false, true);
                drawSaw(saw.getLastFramePosition(), saw.getRadius(), saw.isBloody(), false, true);
                g.setColor(new Color(150, 150, 150, 150));
                ((Graphics2D)g).setStroke(new BasicStroke(2)); //bolder line
                Point begin = new Point(center.x + (int) (Visuals.CELL_SIZE * (saw.getFirstFramePosition().x - camera.x)),
                        center.y + (int) (Visuals.CELL_SIZE * (saw.getFirstFramePosition().y - camera.y)));
                Point end = new Point(center.x + (int) (Visuals.CELL_SIZE * (saw.getLastFramePosition().x - camera.x)),
                        center.y + (int) (Visuals.CELL_SIZE * (saw.getLastFramePosition().y - camera.y)));
                if (saw.getStartingDirection() == Saw.Direction.FORWARDS) {
                    drawArrow(begin, end, true);
                } else {
                    drawArrow(end, begin, true);
                }
                ((Graphics2D)g).setStroke(new BasicStroke(1)); //return stroke width back to normal
            }
        }
    }

    public void drawGrid() {
        g.setColor(new Color(255, 255, 255, 50));
        for (int y = (center.y - cameraTranspose.y) % Visuals.CELL_SIZE; y <= Visuals.HEIGHT; y += Visuals.CELL_SIZE) {
            g.drawLine(0, y, Visuals.WIDTH, y);
        }
        for (int x = (center.x - cameraTranspose.x) % Visuals.CELL_SIZE; x <= Visuals.WIDTH; x += Visuals.CELL_SIZE) {
            g.drawLine(x, 0, x, Visuals.HEIGHT);
        }
    }

    public void drawEmptyCells() {
        for (int y = -visibleCells.y; y <= visibleCells.y; ++y) {
            for (int x = -visibleCells.x; x <= visibleCells.x; ++x) {
                if (level.getFieldAt(cameraPosition.y + y, cameraPosition.x + x) == Level.Cell.EMPTY_NOBG) {
                    g.setColor(level.getBgColor());
                    int leftX = center.x + x * Visuals.CELL_SIZE - cameraTranspose.x;
                    int topY = center.y + y * Visuals.CELL_SIZE - cameraTranspose.y;
                    g.fillRect(leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE);
                }
            }
        }
    }

    public void drawWalls(boolean editor) {
        for (int y = -visibleCells.y; y <= visibleCells.y; ++y) {
            for (int x = -visibleCells.x; x <= visibleCells.x; ++x) {
                Level.Cell currentCell = level.getFieldAt(cameraPosition.y + y, cameraPosition.x + x);
                if (currentCell != Level.Cell.EMPTY && currentCell != Level.Cell.EMPTY_NOBG) {
                    int leftX = center.x + x * Visuals.CELL_SIZE - cameraTranspose.x;
                    int topY = center.y + y * Visuals.CELL_SIZE - cameraTranspose.y;
                    g.drawImage(resources.getTexture(level.getTextureAt(cameraPosition.y + y, cameraPosition.x + x)),
                            leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE, obs);
                    if (editor && level.getFieldAt(cameraPosition.y + y, cameraPosition.x + x) == Level.Cell.SPIKES) {
                        g.setColor(new Color(255, 0, 0, 100)); //make square with spikes red
                        g.fillRect(leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE);
                    }
                    if (!editor) {
                        if (blood.getTop(cameraPosition.y + y, cameraPosition.x + x)) {
                            g.drawImage(resources.getBloodTopTexture(),
                                    leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE, obs);
                        }
                        if (blood.getBottom(cameraPosition.y + y, cameraPosition.x + x)) {
                            g.drawImage(resources.getBloodBottomTexture(),
                                    leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE, obs);
                        }
                        if (blood.getLeft(cameraPosition.y + y, cameraPosition.x + x)) {
                            g.drawImage(resources.getBloodLeftTexture(),
                                    leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE, obs);
                        }
                        if (blood.getRight(cameraPosition.y + y, cameraPosition.x + x)) {
                            g.drawImage(resources.getBloodRightTexture(),
                                    leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE, obs);
                        }
                    }
                } else if (editor && level.cellOutOfBounds(cameraPosition.y + y, cameraPosition.x + x)) {
                    int leftX = center.x + x * Visuals.CELL_SIZE - cameraTranspose.x;
                    int topY = center.y + y * Visuals.CELL_SIZE - cameraTranspose.y;
                    g.setColor(new Color(0, 0, 25));
                    g.fillRect(leftX, topY, Visuals.CELL_SIZE, Visuals.CELL_SIZE);
                }
            }
        }
        if (editor && Settings.getEditorGrid()) {
            drawGrid();
        }
    }

    public void drawPortals(boolean editor) {
        for (int i = 0; i < level.getPortalCount(); ++i) {
            Portal portal = level.getPortal(i);
            Point2D.Double position = portal.getPosition();
            if ((position.x - Portal.RADIUS - camera.x < visibleCells.x) &&
                    (position.x + Portal.RADIUS - camera.x > -visibleCells.x) &&
                    (position.y - Portal.RADIUS - camera.y < visibleCells.y) &&
                    (position.y + Portal.RADIUS - camera.y > -visibleCells.y)) { //if visible
                int portalX = center.x + (int)((position.x - camera.x) * Visuals.CELL_SIZE);
                int portalY = center.y + (int)((position.y - camera.y) * Visuals.CELL_SIZE);

                BufferedImage portalTexture = resources.getPortalTexture();

                AffineTransform at = new AffineTransform();
                // 4. translate it's center to the required position
                at.translate(portalX, portalY);
                // 3. scale
                double yScaleFactor;
                if (editor) {
                    yScaleFactor = 1.0; //no animation
                } else if (animationFrame % 200 < 100) {
                    yScaleFactor = (animationFrame % 200) / 248.0 + 0.8;
                } else {
                    yScaleFactor = (200 - animationFrame % 200) / 248.0 + 0.8;
                }
                at.scale(2 * Visuals.CELL_SIZE * Portal.RADIUS / portalTexture.getWidth(),
                        yScaleFactor * 2 * Visuals.CELL_SIZE * Portal.RADIUS / portalTexture.getHeight());
                // 2. rotate
                if (!editor) {
                    at.rotate(animationFrame * Math.PI * 50.0 / Drawer.ANIMATION_FRAME_COUNT);
                }
                // 1. translate the object so that you rotate it around the center
                at.translate(-portalTexture.getWidth() / 2.0 - 2, -portalTexture.getHeight() / 2.0 - 2);
                // draw the image
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(portalTexture, at, null);
            }
        }
        for (int i = 0; i < level.getPortalCount(); ++i) {
            if (editor && Settings.getEditorExtras() && level.portalExists(level.getPortal(i).getDestinationID())) {
                Portal portal = level.getPortal(i);
                Portal portal2 = level.getPortal(portal.getDestinationID());
                g.setColor(new Color(255, 0, 0, 150));
                ((Graphics2D)g).setStroke(new BasicStroke(2)); //bolder line
                drawArrow(new Point(center.x + (int) (Visuals.CELL_SIZE * (portal.getPosition().x - camera.x)),
                        center.y + (int) (Visuals.CELL_SIZE * (portal.getPosition().y - camera.y))),
                        new Point(center.x + (int) (Visuals.CELL_SIZE * (portal2.getPosition().x - camera.x)),
                                center.y + (int) (Visuals.CELL_SIZE * (portal2.getPosition().y - camera.y))), false);
                ((Graphics2D)g).setStroke(new BasicStroke(1)); //return stroke width back to normal
            }
        }
    }

    public void drawFramedCaption(String caption, Point position, Color color, float fontSize) {
        g.setFont(g.getFont().deriveFont(fontSize));
        g.setColor(new Color(0, 0, 0, 100));
        Rectangle2D stringSize = g.getFontMetrics().getStringBounds(caption, g);
        g.fillRect(position.x - 5, position.y - 5, (int) stringSize.getWidth() + 10, (int) stringSize.getHeight() + 10);
        g.setColor(color);
        g.drawString(caption, position.x, position.y + (int) (stringSize.getHeight() * 0.75));
    }

    public void drawEditorHelp() {
        Image helpTexture = resources.getEditorHelpTexture();
        g.drawImage(helpTexture, 0, 0, helpTexture.getWidth(obs), helpTexture.getHeight(obs), obs);
    }

    public void drawGameHelp() {
        Image helpTexture = resources.getGameHelpTexture();
        g.drawImage(helpTexture, 0, 0, helpTexture.getWidth(obs), helpTexture.getHeight(obs), obs);
    }

    public void drawTexturePanel(int selectedTexture) {
        g.setColor(new Color(50, 50, 50, 230));
        g.fillRect(0, 0, Visuals.CELL_SIZE * 2, Visuals.HEIGHT);
        int capacity = (int)(Visuals.HEIGHT / (Visuals.CELL_SIZE * 1.5)) + 1;
        g.setColor(Color.GRAY);
        for (int i = Math.max(0, selectedTexture - capacity); i <= Math.min(resources.getTextureCount() - 1, selectedTexture + capacity); ++i) {
            int x = Visuals.CELL_SIZE / 2;
            int y = center.y + 3 * (i - selectedTexture - 1) * Visuals.CELL_SIZE / 2;
            g.drawImage(resources.getTexture(i), x, y, Visuals.CELL_SIZE, Visuals.CELL_SIZE, obs);
            if (i == selectedTexture) { //emphasize selected texture
                ((Graphics2D)g).setStroke(new BasicStroke(3));
                g.setColor(Color.YELLOW);
                g.drawRect(x - 2, y - 2, Visuals.CELL_SIZE + 4, Visuals.CELL_SIZE + 4);
                g.setColor(Color.GRAY);
                ((Graphics2D)g).setStroke(new BasicStroke(1));
            } else {
                g.drawRect(x - 1, y - 1, Visuals.CELL_SIZE + 2, Visuals.CELL_SIZE + 2);
            }
        }
    }

    public void drawMenu(Menu menu) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Visuals.WIDTH, Visuals.HEIGHT);
        g.setFont(g.getFont().deriveFont(24.0f));
        for (int i = 0; i < menu.getItemCount(); ++i) {
            if (i == menu.getSelectedItem()) {
                g.setColor(Color.RED);
            } else if (i == menu.getSecondaryItem()) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.LIGHT_GRAY);
            }
            int stringLength = (int) g.getFontMetrics().getStringBounds(menu.getCaption(i), g).getWidth();
            int indent = Math.min(60, Visuals.HEIGHT / (menu.getItemCount() + 1));
            g.drawString(menu.getCaption(i), Visuals.WIDTH / 2 - stringLength / 2, Visuals.HEIGHT / 2 - (menu.getItemCount() - 1) * indent / 2 + i * indent);
        }
    }
}
