package ru.ifmo.enf.finyutina.t05;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/11/14.
 */
public interface Player {
    double MAX_HORIZONTAL_SPEED = 10.0 / Visuals.FPS;
    double MAX_TURBO_SPEED = 15.0 / Visuals.FPS;
    double MAX_VERTICAL_SPEED = 16.25 / Visuals.FPS;
    double MAX_BOUNCE_VERTICAL_SPEED = 37.5 / Visuals.FPS;
    double X_ACCELERATION = 18.75 / (Visuals.FPS * Visuals.FPS);
    double X_TURBO_ACCELERATION = 31.25 / (Visuals.FPS * Visuals.FPS);
    double DECELERATION = 81.25 / (Visuals.FPS * Visuals.FPS);
    double AIR_X_ACCELERATION = 9.375 / (Visuals.FPS * Visuals.FPS);
    double AIR_X_TURBO_ACCELERATION = 15.625 / (Visuals.FPS * Visuals.FPS);
    double Y_ACCELERATION = 31.25 / (Visuals.FPS * Visuals.FPS);
    double Y_ACCELERATION_JUMP_UNPRESSED = 93.75 / (Visuals.FPS * Visuals.FPS);
    double JUMP_STRENGTH = 16.25 / Visuals.FPS;
    double BOUNCE_STRENGTH = 15.0 / Visuals.FPS;
    double OVER_BOUNCE_COEFFICIENT = 0.5;
    double BOUNCE_VERTICAL = 25.0 / Visuals.FPS;
    double SIZE = 0.3;
}
