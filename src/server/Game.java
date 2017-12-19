package server;

import java.util.*;

public class Game {
    private Integer id;
    private Map<Integer, Player> players;
    private Stack<Card> cards;
    private List<Player> scoreBoard;
    private Card legstapel; // is eiglijk maar 1 kaart

    public Game(int id, Stack<Card> cards, Map<Integer, Player> players) {
        this.id = id;
        this.players = players;
        this.cards = cards;
    }

    public Game(int id, Stack<Card> cards){
        this.id = id;
        this.cards = cards;
        this.players = new HashMap<>();
        this.scoreBoard = new ArrayList<>();
        this.legstapel = null;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public void setPlayers(Map<Integer, Player> players) {
        this.players = players;
    }

    public Stack<Card> getCards() {
        return cards;
    }

    public void setCards(Stack<Card> cards) {
        this.cards = cards;
    }

    public void addPlayer(Player player){
        if(players.size()<4){
            players.put(player.getId(), player);
            scoreBoard.add(player);
        }else{
            //foutmelding
        }
    }

    public Card getLegstapel() {
        return legstapel;
    }

    public void setLegstapel(Card legstapel) {
        this.legstapel = legstapel;
    }

    public List<Player> getScoreBoard() {
        return scoreBoard;
    }

    public void setScoreBoard(List<Player> scoreBoard) {
        this.scoreBoard = scoreBoard;
    }

    @Override
    public String toString() {
        StringBuilder print = new StringBuilder();
        print.append("This game has " + players.size() + "\n");
        Iterator<Map.Entry<Integer, Player>> iterator = players.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Integer, Player> entry = iterator.next();
            print.append(entry.getValue()+"\n");
        }

        return print.toString();

    }
}
