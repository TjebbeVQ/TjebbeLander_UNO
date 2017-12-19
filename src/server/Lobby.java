package server;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    List<Player> playersInLobby;

    public Lobby() {
        this.playersInLobby = new ArrayList<>();
    }

    public List<Player> getPlayersInLobby() {
        return playersInLobby;
    }

    public void setPlayersInLobby(List<Player> playersInLobby) {
        this.playersInLobby = playersInLobby;
    }

    public void addPlayerToLobby(Player player){
        playersInLobby.add(player);
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "playersInLobby=" + playersInLobby +
                '}';
    }
}
