package ru.ifmo.enf.finyutina.t05;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 6/9/14.
 */
interface Visuals {
    int WIDTH = 1200;
    int HEIGHT = 700;
    int CELL_SIZE = 24;

    int FPS = 100; //NOT TO BE CHANGED, at least for now, speed of saws and animation will change too,
                   //also affects physics precision a bit
                   //there's a possibility to switch from 100 to 50 fps with Settings.frameSkip (press F in game)

    long sleepTimePerFrame = (long)1e9 / FPS; //in nanoseconds
}
