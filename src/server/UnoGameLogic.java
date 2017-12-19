package server;

import Interfaces.Database;
import Interfaces.UnoGame;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class UnoGameLogic extends UnicastRemoteObject implements UnoGame {

    Interfaces.Database database = null; //for updating highscore at end of the game
    public String gameId;
    public Queue<Card> deck;
    public Card openCard = null;
    public Queue<Player> players;
    public List<Player> playerlist;
    public int maxPlayers;
    public boolean isStarted;
    public boolean gameEnded;
    public String winner;

    /**
     * constructor for the game
     * @param DB
     * @param MaxPlayers
     * @param gameId
     * @throws RemoteException
     */
    public UnoGameLogic(Database DB, int MaxPlayers, String gameId) throws RemoteException {
        super();
        this.database = DB;
        //create stack of card
        this.deck = createDeck();
        this.maxPlayers = MaxPlayers;
        this.players = new LinkedList<>();
        this.playerlist = new LinkedList<>();
        this.gameId = gameId;
        System.out.println("new game created!");
    }

    /**
     * creates a clean shuffled deck with skip and reverse cards
     *
     * @return Queue<Card>
     */
    private Queue<Card> createDeck() {
        List<Card> kaartenLijst = new ArrayList<>();
        String[] kleuren = new String[]{"yellow", "red", "blue", "green"};
        String[] acties = new String[]{"reverse", "skip"};
        String thema = "large";
        int id = 0;
        for (int i = 0; i < kleuren.length; i++) {
            //voeg de cijfers toe (2 kaarten voor elk cijfer)
            for (int j = 0; j < 10; j++) {
                kaartenLijst.add(new Card(id, kleuren[i], j, j, kleuren[i] + "_" + j + "_" + thema + ".png"));
                id++;
                kaartenLijst.add(new Card(id, kleuren[i], j, j, kleuren[i] + "_" + j + "_" + thema + ".png"));
                id++;
            }

            //voeg speciale kaarten toe (2 kaarten voor elke soort actie)
            for (int j = 0; j < acties.length; j++) {
                kaartenLijst.add(new Card(id, kleuren[i], null, 20, kleuren[i] + "_" + acties[j] + "_" + thema + ".png"));
                id++;
                kaartenLijst.add(new Card(id, kleuren[i], null, 20, kleuren[i] + "_" + acties[j] + "_" + thema + ".png"));
                id++;
            }
        }
        System.out.println(kaartenLijst.size());
        Queue<Card> kaartStapel = listToQueue(kaartenLijst); //shuffles the deck and creates a stack

        return kaartStapel;

    }

    /**
     * Creates a queue of cards
     *
     * @param kaartenLijst
     * @return Queue<Card>
     */
    private Queue<Card> listToQueue(List<Card> kaartenLijst) {
        List<Card> randomLijst = shuffleList(kaartenLijst);
        Queue<Card> stapel = new LinkedList<Card>();
        //System.out.println("dit is de stapel: ");
        for (int i = 0; i < randomLijst.size(); i++) {
            //System.out.println(randomLijst.get(i).toString());
            stapel.add(randomLijst.get(i));
        }

        return stapel;
    }

    /**
     * when you join a game you are added to the list of players.
     * when the last person joins, cards get distributed and one placed open
     *
     * @param userID
     * @param userName
     */
    @Override
    public synchronized void joinGame(int userID, String userName) throws RemoteException {
        Player p = new Player(userID, userName);
        players.add(p);
        playerlist.add(p);
        database.joinedGame(gameId);
        if (players.size() == maxPlayers) {
            //distribute the cards, start the game.

            Card card;
            for (int i = 0; i < 7; i++) {
                for (Player player : players) {
                    card = deck.remove();
                    player.giveCard(card);
                }
            }
            //place 1 card open
            openCard = deck.remove();
            isStarted = true;
            notifyAll();
        }

    }

    /**
     * method to try to play a card.
     * When it was the players last card, points get calculated, winner is named and game gets deleted from the database
     *
     * @param playerId
     * @param cardId
     * @return boolean
     */
    @Override
    public boolean placeCard(int playerId, int cardId) throws RemoteException {
        if (playerId == players.peek().getId()) { //is your turn?
            Player currentPlayer = players.peek();
            Card currentCard = currentPlayer.getCards().get(cardId);
            if (currentCard.kanOp(openCard)) {
                deck.add(openCard);//place previous open card at back of the deck
                openCard = currentCard; //place played card open
                currentPlayer.getCards().remove(cardId); //remove the played card from the players hand
                players.remove(); //removes from the front of the queue
                players.add(currentPlayer); //moves to back of the queue

                if (currentCard.getAction().split("_")[1].equals("skip")) {
                    Player p = players.remove();
                    players.add(p);
                    //this skips the next player
                }

                if (currentCard.getAction().split("_")[1].equals("reverse")) {
                    //reverse the queue sequence
                    reversePlayerQueue();
                }

                if (currentPlayer.getCards().size() == 0) {//you won!!!
                    calculatePoints(currentPlayer.getName());
                    gameEnded = true;


                    winner = "Game has been won by: " + currentPlayer.getName() + "\nCongratulation";
                    //remove game from database!!!
                    database.deleteGame(gameId);
                    //delete unogamelogic
                }

                return true; //placed correctly
            }
        }
        return false; //not your turn or not able to place selected card
    }

    /**
     * When a reverse card is placed, the sequence of the players gets reversed
     */
    private void reversePlayerQueue() {

        Object[] pArray = players.toArray();
        Queue<Player> temp = new LinkedList<>();
        switch (maxPlayers) {
            case 2:
                temp.add((Player) pArray[0]);
                temp.add((Player) pArray[1]);
                break;
            case 3:
                temp.add((Player) pArray[1]);
                temp.add((Player) pArray[0]);
                temp.add((Player) pArray[2]);
                break;
            case 4:
                temp.add((Player) pArray[2]);
                temp.add((Player) pArray[1]);
                temp.add((Player) pArray[0]);
                temp.add((Player) pArray[3]);
                break;
        }
        players = null;
        players = temp;

        System.out.println(players.toString());


    }

    /**
     * score is calculated for the winner and updated in the database
     * @param username
     */
    private void calculatePoints(String username) {
        try {
            int points = 0;
            for (Player p : players) {
                for (Card c : p.getCards().values()) {
                    points = points + c.getCost();
                }
            }

            database.updateHighScore(username, points);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * shuffles the newly created deck
     *
     * @param kaartenLijst
     * @return List<Card>
     */
    private List<Card> shuffleList(List<Card> kaartenLijst) {
        int n = kaartenLijst.size();
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(kaartenLijst, i, change);
        }
        return kaartenLijst;
    }

    /**
     * Swaps two cards (used for shuffling)
     *
     * @param kaartenLijst
     * @param i
     * @param change
     */
    private static void swap(List<Card> kaartenLijst, int i, int change) {
        Card helper = kaartenLijst.get(i);
        kaartenLijst.set(i, kaartenLijst.get(change));
        kaartenLijst.set(change, helper);
    }

    /**
     * returns true when game is full and ready to play
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public synchronized boolean isStarted() throws RemoteException {
        try {
            if (!isStarted) wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isStarted;
    }

    /**
     * returns the maximum number of players
     * @return Integer
     * @throws RemoteException
     */
    @Override
    public int getNumberOfPlayers() throws RemoteException {
        return maxPlayers;
    }

    /**
     * returns all the usernames of the different players (everyone except yourself)
     * @param userId
     * @return
     * @throws RemoteException
     */
    @Override
    public String[] getPlayers(int userId) throws RemoteException {
        String[] playerstring = new String[maxPlayers - 1];
        int i = 0;
        for (Player p : playerlist) {
            if (p.getId() != userId) {
                playerstring[i] = p.getName() + ": " + p.getNumberOfCardsInHand();
                i++;
            }
        }
        return playerstring;
    }

    /**
     * returns the cardlist of the given playerID
     * @param playerId
     * @return List<String[]>
     * @throws RemoteException
     */
    @Override
    public List<String[]> getCards(int playerId) throws RemoteException {
        Player player = null;
        List<String[]> kaarten = new ArrayList<>();
        for (Player p : playerlist) {
            if (p.getId() == playerId) {
                player = p;
                break;
            }
        }
        Map<Integer, Card> cards = player.getCards();
        Iterator<Map.Entry<Integer, Card>> iterator = cards.entrySet().iterator();
        String action = "";
        String id = "";

        while (iterator.hasNext()) {
            Map.Entry<Integer, Card> entry = iterator.next();
            id = entry.getValue().getId().toString();
            action = entry.getValue().getAction();

            kaarten.add(new String[]{id, action});
        }
        return kaarten;

    }

    /**
     * gets the open card (returns the image path)
     * @return String
     * @throws RemoteException
     */
    @Override
    public String getLegStapel() throws RemoteException {
        return openCard.toString();
    }

    /**
     * returns the uno value of the given playerID
     * @param playerId
     * @return Boolean
     * @throws RemoteException
     */
    @Override
    public Boolean checkUno(int playerId) throws RemoteException {
        Player player = null;
        for (Player p : playerlist) {
            if (p.getId() == playerId) {
                player = p;
                break;
            }
        }

        if (player.getCards().size() == 1 && !player.isUno()) return false;
        else return true;
    }

    /**
     * sets the uno boolean for the given playerID
     * @param playerId
     * @param uno
     * @throws RemoteException
     */
    @Override
    public void setUno(int playerId, boolean uno) throws RemoteException {
        Player player = null;
        for (Player p : playerlist) {
            if (p.getId() == playerId) {
                player = p;
                break;
            }
        }
        player.setUno(uno);
    }

    /**
     * give player a number of cards, is used when uno is not called and you have uno
     * uno also get revoked
     * @param playerId
     * @param aantal
     * @throws RemoteException
     */
    @Override
    public void giveCards(int playerId, int aantal) throws RemoteException {
        Player player = null;
        for (Player p : playerlist) {
            if (p.getId() == playerId) {
                player = p;
                player.setUno(false);
                break;
            }
        }

        for (int i = 0; i < aantal; i++) {
            player.giveCard(deck.remove());
        }
    }

    /**
     * returns true when it's the player turn
     * @param playerId
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean isMyTurn(int playerId) throws RemoteException {
        if (players.peek().getId() == playerId) return true;
        else return false;
    }

    /**
     * method for taking a number of cards
     * if you had uno, uno gets revoked
     * when only need to take one: you are allowed to place again if you are able to place the card
     * when you need to take more than one card, your turn gets skipped (for +2 and +4 cards)
     * @param playerId
     * @param aantal
     * @throws RemoteException
     */
    @Override
    public void pickCard(int playerId, int aantal) throws RemoteException {
        Player p = players.peek();

        //if that player has uno reset uno cuz you now have 2 or more cards
        if (p.getNumberOfCardsInHand() == 1) {
            p.setUno(false);
        }

        Card c = null;
        for (int i = 0; i < aantal; i++) {
            c = deck.remove();
            p.giveCard(c);
        }

        if (aantal == 1) {
            //if takes one card and that card can be places stays your turn
            if (!c.kanOp(openCard)) {
                //takes one card and cant place it turn ends
                players.remove();
                players.add(p);
            }
        } else {
            //has to take more than 1 card -> turn ends
            players.remove();
            players.add(p);
        }
    }

    /**
     * returns true when game has ended
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean isEnded() throws RemoteException {
        return gameEnded;
    }

    /**
     * returns the winner name or error message when player left mid game
     * @return String
     * @throws RemoteException
     */
    @Override
    public String getWinner() throws RemoteException {
        return winner;
    }

    /**
     * When a player leaves mid game the game ends and gets deleted
     * @param user
     * @throws RemoteException
     */
    @Override
    public void endGame(String user) throws RemoteException {
        gameEnded = true;
        winner = "User " + user + " left the game";
        database.deleteGame(gameId);
    }


}
