package client;


import Interfaces.UnoGame;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Tjebb on 23/11/2017.
 */
public class WaitRefreshThread extends Thread{

    private JButton join;
    private JPanel waitscreen;
    private UnoGame game;
    private JFrame frame;
    private JLabel wait;

    public WaitRefreshThread(JButton join, JPanel waitscreen, UnoGame game, JFrame frame, JLabel wait) {
        this.join = join;
        this.waitscreen = waitscreen;
        this.game = game;
        this.frame = frame;
        this.wait = wait;
    }

    public void run() {
        try {
            while (!game.isStarted()){
                sleep(500); //check every 500ms if game is started
            }
            waitscreen.add(join, BorderLayout.PAGE_END);
            wait.setText("Game is ready!");
            wait.setFont(new Font("Arial",Font.BOLD,20));
            frame.validate();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
