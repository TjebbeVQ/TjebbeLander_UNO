package server;

import java.util.*;

public class Player implements Comparable<Player>{
    private Integer id;
    private String name;
    private Map<Integer, Card> cards = new HashMap<>();
    private Integer score;
    private boolean uno = false;
    //token

    public Player(int id, String name, Integer score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    public Player(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, Card> getCards() {
        return cards;
    }

    public void setCards(HashMap<Integer, Card> cards) {
        this.cards = cards;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setCards(Map<Integer, Card> cards) {
        this.cards = cards;
    }

    public boolean isUno() {
        return uno;
    }

    public void setUno(boolean uno) {
        this.uno = uno;
    }

    public void giveCard(Card card) {
        cards.put(card.getId(), card);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }

    @Override
    public int compareTo(Player player) {
        return player.getScore() - this.score ;
    }

    public int getNumberOfCardsInHand(){
        return cards.size();
    }
}
