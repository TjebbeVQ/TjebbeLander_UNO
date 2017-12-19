package client;

import Interfaces.UnoGame;
import Interfaces.Lobby;
import org.apache.commons.codec.binary.Base64;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

/**
 * Created by Tjebb on 28/10/2017.
 */
public class Main {

    public static long token;
    public static int userIdentifier;

    public static void main(String[] args) {
        Main main = new Main();
        main.init();
    }

    /**
     * Initializes: connect to dispacher, and open the start window
     */
    public void init() {
        //connect to lobbyservice
        Interfaces.Lobby lobby = connectToLobbyService();
        if (lobby == null) System.out.println("lobby is null!");
        final JFrame frame = new JFrame("UNO GAME by Tjebbe & Lander");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(Startscreen(frame, lobby));
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Method that connects over RMI to the lobby
     *
     * @return
     */
    public Lobby connectToLobbyService() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 2000);
            System.out.println("registry aangemaakt");
            Lobby lobby = (Lobby) registry.lookup("LobbyService");
            return lobby;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Jpanel where it is possible to register and login to tht game
     *
     * @param frame
     * @param lobby
     * @return
     */
    public JPanel Startscreen(JFrame frame, Lobby lobby) {
        //public JPanel Startscreen(JFrame frame){
        final JButton btnLogin = new JButton("Click to login"); //here the login screen shows
        final JButton btnRegister = new JButton("Click here to register as new User"); //here the register screen shows

        BufferedImage img = null;
        try {
            LocalDate localDate = LocalDate.now();

            if(localDate.getMonth()== Month.DECEMBER){
                img = ImageIO.read(Main.class.getResource("christmas/logo2.jpg"));
            }else if(localDate.getMonth()==Month.OCTOBER){
                img = ImageIO.read(Main.class.getResource("halloween/logo2.jpg"));
            }else{
                img = ImageIO.read(Main.class.getResource("logo2.jpg"));
            }
            //img = ImageIO.read(Main.class.getResource("logo2.jpg")); //relative path (better!!!)
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel logo = new JLabel();
        logo.setIcon(new ImageIcon(img));


        btnLogin.setFont(new Font("Arial", Font.BOLD, 20));
        btnRegister.setFont(new Font("Arial", Font.ITALIC, 20));

        JPanel startscreen = new JPanel(new BorderLayout());
        startscreen.add(logo, BorderLayout.CENTER);
        startscreen.add(btnLogin, BorderLayout.PAGE_START);
        startscreen.add(btnRegister, BorderLayout.PAGE_END);

        btnLogin.addActionListener( //loading login window
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LoginDialog loginDlg = new LoginDialog(frame, lobby);
                        //LoginDialog loginDlg = new LoginDialog(frame);
                        loginDlg.setVisible(true);


                        //zonder controle naar lobby
                        //System.out.println("nieuw view");
                        //startscreen.setVisible(false);
                        //String name = "testnaam2";
                        //frame.setContentPane(ServerSelector(frame, name, lobby));


                        //if loginDlg.loginSucceeded -> change content pane to lobby pane
                        if (loginDlg.LoginSucceeded()) {
                            //JPanel lobby = new JPanel(new BorderLayout());
                            //JLabel test = new JLabel("dit is een test");
                            //lobby.add(test);
                            //frame.setContentPane(lobby);

                            //String name2 = "testnaam";
                            //frame.setContentPane(ServerSelector(frame, name2, lobby));
                            //frame.validate();

                            frame.setContentPane(ServerSelector(frame, loginDlg.getUsername(), lobby));
                            frame.validate();
                        }

                    }
                }
        );

        btnRegister.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RegisterDialog registerDlg = new RegisterDialog(frame, lobby);
                        registerDlg.setVisible(true);
                    }
                }
        );

        return startscreen;

    }

    /**
     * Jpanel that shows list of games, can join a game, create a game
     * refresh game list
     *
     * @param frame
     * @param name
     * @param l
     * @return
     */
    public JPanel ServerSelector(JFrame frame, String name, Lobby l) {
        System.out.println("server selector");

        JPanel menu = new JPanel(new GridLayout(1, 2, 10, 10));
        JPanel bottom = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton btnLogout = new JButton("Logout");
        JButton btnCreateGame = new JButton("Create new game");
        JButton btnLeaderboard = new JButton("Leaderboard");

        //JButton btnPlay = new JButton("Testen om te spelen");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnJoin = new JButton("Join game");
        JTextField tfJoin = new JTextField("Enter GameID");
        //menu.add(btnPlay);

        menu.add(btnCreateGame);
        menu.add(btnLeaderboard);
        menu.add(btnRefresh);
        menu.add(btnLogout);

        menu.setVisible(true);

        //RMI connection get list of created games
        String[] columns = {"Lobby Name", "Number of players", "GameID"};
        //get via RMI from server
        Object[][] data = getGames(l);
        //if data is null get dialog to say create own game

        JTable table = new JTable(data, columns);
        //JPanel tablePanel = new JPanel();
        //tablePanel.add(table);
        JScrollPane serverlist = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //serverlist.setWheelScrollingEnabled(true);

        DefaultListModel<String> model = new DefaultListModel<>();

        JList<String> playerList = new JList<>(model);
        model.addElement(name);
        /**
         * hier moet nog een stuk om te updaten
         */

        JPanel lobby = new JPanel(new BorderLayout());
        lobby.add(menu, BorderLayout.PAGE_START);
        lobby.add(serverlist, BorderLayout.CENTER);
        bottom.add(playerList);
        bottom.add(tfJoin);
        bottom.add(btnJoin);
        lobby.add(bottom, BorderLayout.PAGE_END);

        btnLogout.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int option = JOptionPane.showConfirmDialog(null, "Do you really want to leave?", "Bye", JOptionPane.YES_NO_OPTION);
                        if (option == 0) {
                            menu.setVisible(false);
                            frame.setContentPane(Startscreen(frame, l));
                        }
                    }
                }
        );
//        btnPlay.addActionListener(
//                new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        menu.setVisible(false);
//                        frame.setContentPane(Gamescreen(frame));
//                    }
//                }
//        );
        btnRefresh.addActionListener( //refreshes the contents of the server list
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object[][] data = getGames(l);
                        JTable table = new JTable(data, columns);
                        //JPanel tablePanel = new JPanel();
                        //tablePanel.add(table);
                        JScrollPane serverlist = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        serverlist.setWheelScrollingEnabled(true);
                        lobby.add(serverlist, BorderLayout.CENTER);
                        frame.revalidate();
                    }
                }
        );

        tfJoin.addFocusListener(new FocusListener() { //for placeholder
            @Override
            public void focusGained(FocusEvent e) {
                if (tfJoin.getText().equals("Enter GameID")) {
                    tfJoin.setText("");
                    tfJoin.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tfJoin.getText().isEmpty()) {
                    tfJoin.setForeground(Color.GRAY);
                    tfJoin.setText("Enter GameID");
                }
            }
        });

        //here get server+port url for that game from DB connect over rmi to the game en switch to game view
        btnJoin.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Trying to join game: " + tfJoin.getText());
                        try {
                            if(!l.tokenExpired(l.getUserId(name), token)){
                                //Check if that game has room to play
                                if (l.hasRoom(tfJoin.getText())) {

                                    //Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                                    //UnoGame game = (UnoGame) registry.lookup(tfJoin.getText());
                                    //game.joinGame(l.getUserId(name),name);

//                                do{
//                                    //frame.setContentPane(Gamescreen(frame, tfJoin.getText(), name, l.getUserId(name), game));
//                                    frame.removeAll();
//                                    frame.setContentPane(JoiningGameScreen(frame, tfJoin.getText(), name, l.getUserId(name)));
//                                    frame.validate();
//                                }while (!game.isStarted());
                                    //frame.setContentPane(Gamescreen(frame, tfJoin.getText(), name, l.getUserId(name), game));
                                    //join game + show lobby or game screen
                                    frame.setContentPane(JoiningGameScreen(frame, tfJoin.getText(), name, l.getUserId(name), l, frame.getContentPane()));
                                    //JoiningGameDialog join = new JoiningGameDialog(frame, l, tfJoin.getText(), name, l.getUserId(name));
                                    //frame.setContentPane(Gamescreen(frame, tfJoin.getText(), name, l.getUserId(name)));
                                    frame.validate();
                                } else {
                                    JOptionPane.showMessageDialog(frame, "Game is Full. \nTry another game.", "Join error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }else {
                                JOptionPane.showMessageDialog(frame, "Session expired, please login again.", "Session error",
                                        JOptionPane.ERROR_MESSAGE);
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                }
        );

        btnCreateGame.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CreateGameDialog create = new CreateGameDialog(frame, l);
                        create.setVisible(true);

                        Object[][] data = getGames(l);
                        JTable table = new JTable(data, columns);
                        //JPanel tablePanel = new JPanel();
                        //tablePanel.add(table);
                        JScrollPane serverlist = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        serverlist.setWheelScrollingEnabled(true);
                        lobby.add(serverlist, BorderLayout.CENTER);
                        frame.revalidate();
                    }
                }
        );

        btnLeaderboard.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame leaderboard = new JFrame("Leaderboard");
                        leaderboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        leaderboard.setContentPane(LeaderboardPanel(leaderboard, l));
                        leaderboard.pack();
                        leaderboard.setVisible(true);
                    }
                });


        return lobby;
    }

    /**
     * Panel that shows waiting for other players, and updates when game is full to a button
     * that allows you to move to the gamescreen panel
     *
     * @param frame
     * @param gameID
     * @param username
     * @param userID
     * @param l
     * @param container
     * @return
     */
//  TODO Button for leaving the game to keep the game and the database consistent
    public JPanel JoiningGameScreen(JFrame frame, String gameID, String username, int userID, Lobby l, Container container) {
        frame.setTitle("Joining game");
        JPanel waitScreen = new JPanel(new BorderLayout());
        JLabel wait = new JLabel("Waiting for other players to join...");
        JButton join = new JButton("Join Game!!");
        join.setBackground(Color.GREEN);
        waitScreen.add(wait, BorderLayout.CENTER);

        //waitScreen.add(refresh, BorderLayout.PAGE_END);

        frame.validate();


        UnoGame game;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            game = (UnoGame) registry.lookup(gameID);
            game.joinGame(userID, username);
            new WaitRefreshThread(join, waitScreen, game, frame, wait).start();

            join.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (!game.isStarted()) {
                            frame.revalidate();
                        } else {
                            frame.setContentPane(Gamescreen(frame, gameID, username, userID, game, l, container));
                            frame.validate();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


        return waitScreen;
    }

    /**
     * The actual game screen for playing the UNO game
     *
     * @param frame
     * @param gameID
     * @param username
     * @param userId
     * @param game
     * @param l
     * @param container
     * @return
     */
    public JPanel Gamescreen(JFrame frame, String gameID, String username, int userId, UnoGame game, Lobby l, Container container) {
        frame.setTitle("let's play UNO");

        JPanel gamescreen = new JPanel(new BorderLayout());
        Box box = new Box(BoxLayout.PAGE_AXIS);

        JPanel playOptions = new JPanel(new GridLayout(1, 3, 10, 10)); //for pick card and uno button
        JPanel playField = new JPanel(new GridLayout(3, 3)); //for players

        //code om eigen kaarten in hand te zien
        DefaultListModel model = new DefaultListModel();
        JList cardList = new JList(model);
        cardList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        cardList.setVisibleRowCount(3);
        ListCellRenderer renderer = new ComplexCellRenderer();
        cardList.setCellRenderer(renderer);
        JScrollPane pane = new JScrollPane(cardList);

        pane.setMaximumSize(new Dimension(frame.getWidth(), 10));
        pane.setMinimumSize(new Dimension(frame.getWidth() / 2, 10));

        JLabel legStapel = new JLabel();
        String[] players;
        final JLabel player1 = new JLabel();
        final JLabel player2 = new JLabel();
        final JLabel player3 = new JLabel();
        final JLabel yourTurn = new JLabel();
        player1.setFont(new Font("Arial", Font.BOLD, 20));
        player2.setFont(new Font("Arial", Font.BOLD, 20));
        player3.setFont(new Font("Arial", Font.BOLD, 20));
        yourTurn.setFont(new Font("Arial", Font.BOLD, 20));
        yourTurn.setForeground(Color.BLUE);

        JButton btnPickACard = new JButton("Pick a Card");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate localDate = LocalDate.now();
//        System.out.println(dtf.format(localDate));


        if(localDate.getMonth()== Month.DECEMBER){
            Icon image = new ImageIcon(getClass().getResource("christmas/card_back_large3.png"));
            btnPickACard.setIcon(image);
        }else if(localDate.getMonth()==Month.OCTOBER){
            Icon image = new ImageIcon(getClass().getResource("halloween/card_back_large3.png"));
            btnPickACard.setIcon(image);
        }else{
            Icon image = new ImageIcon(getClass().getResource("large/card_back_large3.png"));
            btnPickACard.setIcon(image);
        }
        //Icon image = new ImageIcon(getClass().getResource("large/card_back_large3.png"));
        //btnPickACard.setIcon(image);
        btnPickACard.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnPickACard.setHorizontalTextPosition(SwingConstants.CENTER);

        JButton btnUNO = new JButton("Press for UNO");
        if(localDate.getMonth()== Month.DECEMBER){
            Icon unoImage = new ImageIcon(getClass().getResource("christmas/Uno-Game.png"));
            btnUNO.setIcon(unoImage);
        }else if(localDate.getMonth()==Month.OCTOBER){
            Icon unoImage = new ImageIcon(getClass().getResource("halloween/Uno-Game.png"));
            btnUNO.setIcon(unoImage);
        }else{
            Icon unoImage = new ImageIcon(getClass().getResource("large/Uno-Game.png"));
            btnUNO.setIcon(unoImage);
        }

        btnUNO.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnUNO.setHorizontalTextPosition(SwingConstants.CENTER);
        legStapel.setHorizontalAlignment(JLabel.CENTER);
        player1.setHorizontalAlignment(JLabel.CENTER);
        player2.setHorizontalAlignment(JLabel.CENTER);
        player3.setHorizontalAlignment(JLabel.CENTER);



        try {
            switch (game.getNumberOfPlayers()) {
                case 2:

                    players = game.getPlayers(userId);
                    player1.setText(players[0]);

                    playField.add(new JPanel());//linksboven
                    playField.add(player1);//centerboven//player1
                    playField.add(new JPanel());//rechtsboven
                    playField.add(btnPickACard);//linkscenter
                    playField.add(legStapel);//centercenter
                    playField.add(new JPanel());//rechtscenter
                    playField.add(yourTurn);//linksonder
                    playField.add(pane);//centeronder
                    playField.add(btnUNO);//rechtsonder
                    break;
                case 3:
                    players = game.getPlayers(userId);
                    player1.setText(players[0]);
                    player2.setText(players[1]);

                    playField.add(player1);//linksboven //player1
                    playField.add(new JPanel());//centerboven//player2
                    playField.add(player2);//rechtsboven//player3
                    playField.add(btnPickACard);//linkscenter
                    playField.add(legStapel);//centercenter
                    playField.add(new JPanel());//rechtscenter
                    playField.add(yourTurn);//linksonder
                    playField.add(pane);//centeronder
                    playField.add(btnUNO);//rechtsonder
                    break;
                case 4:
                    players = game.getPlayers(userId);
                    player1.setText(players[0]);
                    player2.setText(players[1]);
                    player3.setText(players[2]);

                    playField.add(player1);//linksboven //player1
                    playField.add(player2);//centerboven//player2
                    playField.add(player3);//rechtsboven//player3
                    playField.add(btnPickACard);//linkscenter
                    playField.add(legStapel);//centercenter
                    playField.add(new JPanel());//rechtscenter
                    playField.add(yourTurn);//linksonder
                    playField.add(pane);//centeronder
                    playField.add(btnUNO);//rechtsonder
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JButton exitGame = new JButton("Leave Game");

        //JButton btnPickACard = new JButton("Pick a card");
        //playOptions.add(btnUNO);
        //playOptions.add(btnPickACard);
        playOptions.add(exitGame);
        playOptions.setMaximumSize(new Dimension(frame.getWidth(), 10));
        playOptions.setVisible(true);

        legStapel.setVisible(true);

        box.add(playField);
        box.add(playOptions);

        exitGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    game.endGame(username);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    game.endGame(username);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        try {

            legStapel.setIcon(setIcon(game));
            updateCardsInHandList(game, userId, model);

            /**
             * indien je klikt op 1 van je kaarten
             */
            cardList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList list = (JList) evt.getSource();
                    if (evt.getClickCount() == 1) {
                        String[] nameOfCard = (String[]) cardList.getSelectedValue();
                    } else if (evt.getClickCount() == 2) {
                        String[] nameOfCard = (String[]) cardList.getSelectedValue();

                        int cardId = Integer.parseInt(nameOfCard[0]);
                        try {
                            if (game.placeCard(userId, cardId)) {
                                //legStapel.setText(game.getLegStapel(gameId));
                                legStapel.setIcon(setIcon(game));
                                if (!game.checkUno(userId)) {
                                    game.giveCards(userId, 7);
                                    game.setUno(userId, false);
                                }
                                model.clear();
                                updateCardsInHandList(game, userId, model);

//                                if(game.isEnded()){
//                                    JOptionPane.showMessageDialog(null, "Game has been won");
//                                    frame.setContentPane(ServerSelector(frame,username,l));
//                                    frame.validate();
//                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Dit is geen geldige move");
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            btnUNO.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                if (game.isMyTurn(userId)) {
                                    game.setUno(userId, true);
                                }
                            } catch (RemoteException re) {
                                re.printStackTrace();
                            }
                        }
                    }
            );
            btnPickACard.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                if (game.isMyTurn(userId)) {
                                    game.pickCard(userId, 1);
                                    model.clear();
                                    updateCardsInHandList(game, userId, model);
                                }
                            } catch (RemoteException re) {
                                re.printStackTrace();
                            }
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        new GameRefreshThread(legStapel, player1, player2, player3, yourTurn, userId, game, frame, container).start();

        gamescreen.add(box);
        return gamescreen;


    }

    /**
     * This method created the leaderboard panel that displays the leaderboard/highscores
     *
     * @param frame
     * @param l
     * @return
     */
    public JPanel LeaderboardPanel(JFrame frame, Lobby l) {

        String[] columns = {"Username", "Highscore"};
        Object[][] data = getHighscores(l);

        JTable table = new JTable(data, columns);
        JScrollPane scoreList = new JScrollPane(table);

        JPanel leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardPanel.add(scoreList, BorderLayout.CENTER);

        return leaderboardPanel;
    }

    /**
     * Returns the game list from database in a format for JTable
     *
     * @param l
     * @return
     */
    public Object[][] getGames(Lobby l) {
        Object[][] data = null;
        try {
            List<Object[]> games = l.getGames();
            if (games == null) System.out.println("games is null");
            data = new Object[games.size()][3];
            int i = 0;
            for (Object[] o : games) {
                data[i][0] = games.get(i)[0]; //gamename
                data[i][1] = games.get(i)[1]; //number of players / max players
                data[i][2] = games.get(i)[3]; //Game ID
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * method that returns a datastructure containing username + highscore
     *
     * @param l
     * @return
     */
    public Object[][] getHighscores(Lobby l) {
        Object[][] data = null;
        try {
            List<Object[]> leaderboard = l.getHighscores();
            if (leaderboard == null) System.out.println("games is null");
            data = new Object[leaderboard.size()][2];
            int i = 0;
            for (Object[] o : leaderboard) {
                data[i][0] = leaderboard.get(i)[0]; //username
                data[i][1] = leaderboard.get(i)[1]; //highscore
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Sets the image of the card as an icon
     *
     * @param game
     * @return
     */
    public Icon setIcon(UnoGame game) {
        try {

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


            //System.out.println("seticon:" + game.getLegStapel(gameId));
            //Icon image = new ImageIcon(getClass().getResource("christmas/" + game.getLegStapel()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("return null");
        return null;
    }

    /**
     * Updates the list of the cards in hand of the player
     *
     * @param game
     * @param playerId
     * @param model
     * @throws RemoteException
     */
    public void updateCardsInHandList(UnoGame game, int playerId, DefaultListModel model) throws RemoteException {
        List<String[]> kaarten = game.getCards(playerId);
        int index = 0;
        for (String[] s : kaarten) {
            model.add(index, s);
            index++;
        }
    }

    /**
     * Class for the login dialog
     */
    private static class LoginDialog extends JDialog {
        //here connecting to the login service on the application server
        private JTextField tfUsername;
        private JPasswordField pfPassword;
        private JLabel lbUsername;
        private JLabel lbPassword;
        private JButton btnLogin;
        private JButton btnCancel;
        private boolean login;

        public LoginDialog(Frame parent, Lobby lobby) {
            //public LoginDialog(Frame parent) {

            super(parent, "Login", true);
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();

            cs.fill = GridBagConstraints.HORIZONTAL;

            lbUsername = new JLabel("Username: ");
            cs.gridx = 0;
            cs.gridy = 0;
            cs.gridwidth = 1;
            panel.add(lbUsername, cs);

            tfUsername = new JTextField("Username", 20);
            cs.gridx = 1;
            cs.gridy = 0;
            cs.gridwidth = 2;
            panel.add(tfUsername, cs);

            lbPassword = new JLabel("Password: ");
            cs.gridx = 0;
            cs.gridy = 1;
            cs.gridwidth = 1;
            panel.add(lbPassword, cs);

            pfPassword = new JPasswordField(20);
            cs.gridx = 1;
            cs.gridy = 1;
            cs.gridwidth = 2;
            panel.add(pfPassword, cs);
            panel.setBorder(new LineBorder(Color.GRAY));

            btnLogin = new JButton("Login");
            //action listener aan koppelen die login communiceerd over RMI met server
            btnLogin.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //using RMI to try to login
                            login = false;
                            long sessiontoken = 0;
                            try {
                                token = sessiontoken; //reset token to 0
                                sessiontoken = lobby.login(getUsername(), getPassword(), true);
                                token = sessiontoken; //set token to sessiontoken

                                if(sessiontoken!=0){
                                    login = true;
                                    userIdentifier = lobby.getUserId(getUsername());
                                }else login = false;

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }


                            if (login) {//when RMI answers login true and get session token!!
                                JOptionPane.showMessageDialog(parent, "Login correct!");
                                //get session token!!!


                                dispose();//sluit dialoogvenster
                                //nu naar nieuw view gaan


                            } else {//login not correct
                                JOptionPane.showMessageDialog(parent, "Username or password isn't correct. Please check again.", "Login error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
            );

            btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();

                }
            });

            tfUsername.addFocusListener(new FocusListener() { //for placeholder
                @Override
                public void focusGained(FocusEvent e) {
                    if (tfUsername.getText().equals("Username")) {
                        tfUsername.setText("");
                        tfUsername.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (tfUsername.getText().isEmpty()) {
                        tfUsername.setForeground(Color.GRAY);
                        tfUsername.setText("Username");
                    }
                }
            });


            JPanel bp = new JPanel();
            bp.add(btnLogin);
            bp.add(btnCancel);

            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(bp, BorderLayout.PAGE_END);

            pack();
            setResizable(false);
            setLocationRelativeTo(parent);
        }

        private String getUsername() {
            return tfUsername.getText().trim();
        }

        private String getPassword() {
            return new String(pfPassword.getPassword());
        }

        private boolean LoginSucceeded() {
            return login;
        }
    }

    /**
     * Class for the registration dialog
     */
    private static class RegisterDialog extends JDialog {

        private JTextField tfUsername;
        private JPasswordField pfPassword;
        private JPasswordField pfPassword2;
        private JLabel lbUsername;
        private JLabel lbPassword;
        private JLabel lbPassword2;
        private JButton btnRegister;

        private JButton btnCancel;

        public RegisterDialog(Frame parent, Lobby lobby) {
            super(parent, "Register new User", true);
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();

            cs.fill = GridBagConstraints.HORIZONTAL;

            lbUsername = new JLabel("Username: ");
            cs.gridx = 0;
            cs.gridy = 0;
            cs.gridwidth = 1;
            panel.add(lbUsername, cs);

            tfUsername = new JTextField(20);
            cs.gridx = 1;
            cs.gridy = 0;
            cs.gridwidth = 2;
            panel.add(tfUsername, cs);

            lbPassword = new JLabel("Password: ");
            cs.gridx = 0;
            cs.gridy = 1;
            cs.gridwidth = 1;
            panel.add(lbPassword, cs);

            pfPassword = new JPasswordField(20);
            cs.gridx = 1;
            cs.gridy = 1;
            cs.gridwidth = 2;
            panel.add(pfPassword, cs);

            lbPassword2 = new JLabel("Password: ");
            cs.gridx = 0;
            cs.gridy = 2;
            cs.gridwidth = 1;
            panel.add(lbPassword2, cs);

            pfPassword2 = new JPasswordField(20);
            cs.gridx = 1;
            cs.gridy = 2;
            cs.gridwidth = 2;
            panel.add(pfPassword2, cs);

            panel.setBorder(new LineBorder(Color.GRAY));

            btnRegister = new JButton("Register");
            //action listener aan koppelen die login communiceert over RMI met server
            //first check if the 2 passwords match or not error dialog if doesnt match
            btnRegister.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (getUsername().isEmpty() || getPassword1().isEmpty()) { //check if mandatory fields are empty
                                JOptionPane.showMessageDialog(parent, "Please fill in all fields.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            } else {
                                if (!equalPasswords()) { //if all fields are filled in, check if passwords match
                                    JOptionPane.showMessageDialog(parent, "Passwords do not match, Please check again.", "Password error",
                                            JOptionPane.ERROR_MESSAGE);
                                } else {
                                    //JOptionPane.showMessageDialog(parent, "Passwords match.");
                                    //try to register but server must check with DBServer if username is taken or not
                                    boolean register = false;
                                    try {
                                        byte[] salt = generateSalt();
                                        byte[] hashed = getHashWithSalt(getPassword2(), salt);
                                        String saltString = bytetoString(salt);
                                        String hashedPass = bytetoString(hashed);

                                        System.out.println(saltString);
                                        System.out.println(hashedPass);
                                        System.out.println(getPassword2());

                                        //register = lobby.register(getUsername(),getPassword2());
                                        register = lobby.register(getUsername(), hashedPass, saltString);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                    if (!register) {//if username is taken
                                        JOptionPane.showMessageDialog(parent, "Username is taken. Choose another one please.", "Username Taken",
                                                JOptionPane.ERROR_MESSAGE);
                                    } else { //registration finished
                                        JOptionPane.showMessageDialog(parent, "Registration complete. Enjoy the game! \nYou can now login.");
                                        dispose();
                                    }
                                }
                            }
                        }

                        public byte[] generateSalt() {
                            SecureRandom random = new SecureRandom();
                            byte bytes[] = new byte[20];
                            random.nextBytes(bytes);
                            return bytes;
                        }

                        public String bytetoString(byte[] input) {
                            return encodeBase64String(input);
                        }

                        public byte[] getHashWithSalt(String input, byte[] salt) throws NoSuchAlgorithmException {
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            digest.reset();
                            digest.update(salt);
                            byte[] hashedBytes = digest.digest(stringToByte(input));
                            return hashedBytes;
                        }

                        public byte[] stringToByte(String input) {
                            if (Base64.isBase64(input)) {
                                return Base64.decodeBase64(input);

                            } else {
                                return Base64.encodeBase64(input.getBytes());
                            }
                        }
                    }
            );

            btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            JPanel bp = new JPanel();
            bp.add(btnRegister);
            bp.add(btnCancel);


            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(bp, BorderLayout.PAGE_END);

            pack();
            setResizable(false);
            setLocationRelativeTo(parent);
        }

        private boolean equalPasswords() {
            return getPassword1().equals(getPassword2());
        }

        private String getUsername() {
            return tfUsername.getText().trim();
        }

        private String getPassword1() {
            return new String(pfPassword.getPassword());
        }

        private String getPassword2() {
            return new String(pfPassword2.getPassword());
        }
    }

    /**
     * Class for the create game dialog
     */
    private static class CreateGameDialog extends JDialog {
        private JLabel lblGameName;
        private JLabel lblMaxPlayers;
        private JTextField tfGameName;
        private JComboBox cbMaxPlayers;
        private JButton btnCreate;
        private JButton btnCancel;

        public CreateGameDialog(Frame parent, Lobby l) {
            super(parent, "Create new game", true);
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();

            cs.fill = GridBagConstraints.HORIZONTAL;

            lblGameName = new JLabel("Game name: ");
            cs.gridx = 0;
            cs.gridy = 0;
            cs.gridwidth = 1;
            panel.add(lblGameName, cs);

            tfGameName = new JTextField(20);
            cs.gridx = 1;
            cs.gridy = 0;
            cs.gridwidth = 2;
            panel.add(tfGameName, cs);

            lblMaxPlayers = new JLabel("Max players: ");
            cs.gridx = 0;
            cs.gridy = 1;
            cs.gridwidth = 1;
            panel.add(lblMaxPlayers, cs);

            String[] players = {"2", "3", "4"};
            cbMaxPlayers = new JComboBox(players);
            cs.gridx = 1;
            cs.gridy = 1;
            cs.gridwidth = 1;
            panel.add(cbMaxPlayers, cs);
            panel.setBorder(new LineBorder(Color.GRAY));

            btnCreate = new JButton("Create");
            btnCancel = new JButton("Cancel");

            btnCancel.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dispose();
                        }
                    }
            );

            btnCreate.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String name = getGameName();
                            int maxplayers = getMaxPlayers();
                            System.out.println("Creating game: " + name + " " + maxplayers);
                            boolean succes = false;
                            try {
                                if (name.length() < 1) {
                                    JOptionPane.showMessageDialog(parent, "Please enter a game name.", "Creation error",
                                            JOptionPane.ERROR_MESSAGE);
                                } else {
                                    succes = l.CreateGame(name, maxplayers, token, userIdentifier);
                                    if (succes) {
                                        JOptionPane.showMessageDialog(parent, "Game Created");
                                        dispose();
                                    } else {
                                        JOptionPane.showMessageDialog(parent, "Error creating game. \nTry again.", "Creation error",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
            );

            JPanel bp = new JPanel();
            bp.add(btnCreate);
            bp.add(btnCancel);

            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(bp, BorderLayout.PAGE_END);

            pack();
            setResizable(false);
            setLocationRelativeTo(parent);

        }

        private String getGameName() {
            return tfGameName.getText().trim();
        }

        private int getMaxPlayers() {
            return Integer.parseInt((String) cbMaxPlayers.getItemAt(cbMaxPlayers.getSelectedIndex()));
        }

    }
}
