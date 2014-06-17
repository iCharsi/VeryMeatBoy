package ru.ifmo.enf.finyutina.t05;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Created by Angelika Finyutina (charsi.npc@gmail.com) on 4/7/14.
 */

class GameWindow extends JFrame {
    GameWindow() {
        super("Very Meat Boy ver. -9000 pre pre alpha");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        GameResources resources = new GameResources();
        try {
            resources.readDefaultResources();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load game resources. The game will shut down.",
                    "Oops", JOptionPane.ERROR_MESSAGE);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }

        super.add(new GamePanel(resources));

        setSize(Visuals.WIDTH, Visuals.HEIGHT);
        this.setResizable(false);
        this.setUndecorated(true);
        setLocationRelativeTo(null); //makes our window centered on the screen
    }
}