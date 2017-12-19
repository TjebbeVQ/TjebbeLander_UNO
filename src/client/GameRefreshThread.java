
package client;


import Interfaces.UnoGame;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

/**
 * Created by Tjebb on 23/11/2017.
 */
public class GameRefreshThread extends Thread{
    private JLabel legStapel, player1, player2, player3, yourTurn;
    private int userId;
    private UnoGame game;
    private JFrame frame;
    private Container container;

    public GameRefreshThread(JLabel legStapel, JLabel player1, JLabel player2, JLabel player3, JLabel yourTurn, int userId, UnoGame game, JFrame frame, Container container) {
        this.legStapel = legStapel;
        this.player1 = player1;
        this.player2 = player2;
        this.player3 = player3;
        this.yourTurn = yourTurn;
        this.userId = userId;
        this.game = game;
        this.frame = frame;
        this.container = container;
    }

    public void run() {
        try {
            String[] players;
            while (!game.isEnded()) {
                legStapel.setIcon(setIcon(game));
                if(game.isMyTurn(userId)){
                    yourTurn.setText("Your Turn!");
                }else{
                    yourTurn.setText("");
                }
                switch (game.getNumberOfPlayers()) {
                    case 2:
                        players = game.getPlayers(userId);
                        player1.setText(players[0]);
                        break;
                    case 3:
                        players = game.getPlayers(userId);
                        player1.setText(players[0]);
                        player2.setText(players[1]);
                        break;
                    case 4:
                        players = game.getPlayers(userId);
                        player1.setText(players[0]);
                        player2.setText(players[1]);
                        player3.setText(players[2]);
                        break;
                }
            }

            JOptionPane.showMessageDialog(null, game.getWinner());
            frame.setContentPane(container);
            frame.validate();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public Icon setIcon(UnoGame game) {
        try{
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate localDate = LocalDate.now();
//            System.out.println(dtf.format(localDate));

            if(localDate.getMonth()== Month.DECEMBER){
                Icon image = new ImageIcon(getClass().getResource("christmas/" + game.getLegStapel()));
                return image;
            }else if(localDate.getMonth()==Month.OCTOBER){
                Icon image = new ImageIcon(getClass().getResource("halloween/" + game.getLegStapel()));
                return image;
            }else{
                Icon image = new ImageIcon(getClass().getResource("large/" + game.getLegStapel()));
                return image;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        //System.out.println("return null");
        return null;
    }


}