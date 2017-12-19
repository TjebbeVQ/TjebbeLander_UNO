package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.List;

/**
 * Created by Tjebb on 01/11/2017.
 */
public interface Database extends Remote {
    boolean login(String user, String pass) throws RemoteException;
    boolean register(String user, String pass, String salt)throws RemoteException;
    void updateHighScore(String user, int score)throws RemoteException;
    List<Object[]> getGames() throws RemoteException;
    //String getServer(int GameID)throws RemoteException;
    //int CreateGame(String GameName, int maxPlayers)throws RemoteException;
    boolean hasRoom(String gameID) throws RemoteException;
    int getUserId(String username)throws RemoteException;
    void joinedGame(String gameID) throws RemoteException;
    List<Object[]> getLeaderboard() throws RemoteException;
    void deleteGame(String gameID) throws RemoteException;
    int getNumberOfActiveGamesOnRMIPort(int RMIPort) throws RemoteException;
    int CreateGame(String GameName, int maxPlayers, int RMIPort)throws RemoteException;
    void addDatabase(Connection c) throws RemoteException;
    Connection getConn() throws RemoteException;
    long login(String user, String pass, boolean test)throws RemoteException;
    boolean tokenExpired(long token, int userID) throws RemoteException;
}
