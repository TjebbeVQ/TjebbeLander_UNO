package Interfaces;

import server.Card;
import server.Game;
import server.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public interface UnoGame extends Remote{

    public void joinGame(int userID, String userName)throws RemoteException;
    public boolean placeCard(int playerId, int cardId)throws RemoteException;
    //public boolean takeCardFromDeck(int playerId)throws RemoteException;
    public boolean isStarted()throws RemoteException;
    //public int getNumberOfCards(int playerId)throws RemoteException;
    public int getNumberOfPlayers()throws RemoteException;
    public String[] getPlayers(int userID)throws RemoteException;
    public String getLegStapel() throws  RemoteException;
    public List<String[]> getCards(int playerId) throws RemoteException;
    public Boolean checkUno(int playerId) throws RemoteException;
    public void giveCards(int playerId, int aantal) throws RemoteException;
    public void setUno(int playerId, boolean uno) throws RemoteException;
    public boolean isMyTurn(int playerId) throws RemoteException;
    public void pickCard(int playerId, int aantal) throws RemoteException;
    public boolean isEnded() throws RemoteException;
    public String getWinner() throws RemoteException;
    public void endGame(String user) throws RemoteException;
}
