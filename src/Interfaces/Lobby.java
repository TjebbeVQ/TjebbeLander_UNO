package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Tjebb on 04/11/2017.
 */
public interface Lobby extends Remote {

    boolean login(String user, String pass) throws RemoteException;
    boolean register(String user, String pass, String salt)throws RemoteException;
    List<Object[]> getGames()throws RemoteException;
    List<Object[]> getHighscores()throws RemoteException;
    boolean CreateGame(String gameName, int maxPlayers)throws RemoteException;
    boolean CreateGame(String gameName, int maxPlayers, long token, int userID)throws RemoteException;
    boolean hasRoom(String gameID) throws RemoteException;
    int getUserId(String username) throws RemoteException;
    long login(String user, String pass, boolean test) throws RemoteException;
    boolean tokenExpired(int userID, long token)throws RemoteException;
}
