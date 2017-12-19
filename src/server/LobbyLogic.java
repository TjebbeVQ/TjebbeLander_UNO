package server;

import Interfaces.Database;
import Interfaces.Lobby;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Created by Tjebb on 04/11/2017.
 */
public class LobbyLogic extends UnicastRemoteObject implements Lobby {
    Interfaces.Database database = null;
    Registry registry = null;
    HashMap<Integer, Registry> serverlist = null;
    List<Database> DBList = null;

    public LobbyLogic(Database DB, Registry registry)throws RemoteException{
        super();
        this.database = DB;
        this.registry = registry;
    }

    /**
     * Creates the lobby logic or dispacher. Contains a hashmap containing all the gameservers,
     * a list of all the database servers
     * @param DBList
     * @param registry
     * @param rmiPort
     * @throws RemoteException
     */
    public LobbyLogic (List<Database> DBList, Registry registry, int rmiPort)throws RemoteException{
        super();
        this.DBList = DBList;
        database = DBList.get(0); //dispachers own database is the first in the list, used for login, register,...
        serverlist = new HashMap<>();
        serverlist.put(rmiPort, registry);
    }

    /**
     * Check if the login is valid against the database
     * @param user
     * @param pass
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean login(String user, String pass) throws RemoteException {
        return database.login(user, pass);
    }

    /**
     * Registers a new user in the database
     * @param user
     * @param pass
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean register(String user, String pass, String salt) throws RemoteException{
        return database.register(user,pass, salt);
    }

    /**
     * Gets list of all games from database
     * @return List<Object[]>
     * @throws RemoteException
     */
    @Override
    public List<Object[]> getGames() throws RemoteException{
        System.out.println("Getting games from DB");
        return database.getGames();
    }

    /**
     * Gets list of all highscores from the database
     * @return List<Object[]>
     * @throws RemoteException
     */
    @Override
    public List<Object[]> getHighscores() throws RemoteException{
        System.out.println("Getting scores from DB");
        return database.getLeaderboard();
    }


//    /**
//     * addes game info to the database, and rebinds the gameid to a new instance
//     * of the unogamelogic
//     * @param gameName
//     * @param maxPlayers
//     * @return
//     * @throws RemoteException
//     */
//    @Override
//    public boolean CreateGame(String gameName, int maxPlayers) throws RemoteException {
//        //create a new game thread!
//        System.out.println("adding to DB");
//        int gameID = database.CreateGame(gameName, maxPlayers);
//        if(gameID > 0){
//            System.out.println("rebinding: "+ gameID);
//
//            registry.rebind(Integer.toString(gameID), new UnoGameLogic(database, maxPlayers, Integer.toString(gameID)));
//            return true;
//        }
//        //new GameThread(gameName, maxPlayers, database, registry).start();
//        return false;
//    }

    /**
     * Creates a new game and adds that game information to the database.
     * Per every 20 active games on a server, a new gameserver is created.
     * Per 40 active games of per 2 game servers, the next gameserver gets another database server.
     * @param gameName
     * @param maxPlayers
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean CreateGame(String gameName, int maxPlayers)throws RemoteException{
//      Per 2 gameservers 1 database!
        Set set = serverlist.entrySet();
        Iterator iterator = set.iterator();
        int teller = 0;
        while (iterator.hasNext()){
            Map.Entry<Integer, Registry> mentry = (Map.Entry)iterator.next();
            if(database.getNumberOfActiveGamesOnRMIPort(mentry.getKey())<20){//als aantal actieve games kleiner is dan 20
                int gameID = database.CreateGame(gameName, maxPlayers, mentry.getKey()); //adds in database

                if(teller/2 > DBList.size()){
                    //more games than the databases can handle
                    return false;
                }else{
                    mentry.getValue().rebind(Integer.toString(gameID),new UnoGameLogic(DBList.get(teller/2), maxPlayers, Integer.toString(gameID)));
                    System.out.println("Created game on RMIPort: " + mentry.getKey() + " GameID: " + gameID + " Using database no. " + teller/2);
                    return true;
                }
            }
            teller++;
        }

        //teller++;
        if(teller/2 > DBList.size()){
            //more games than the databases can handle
            return false;
        }else{
            //if full while loop no return (so no servers with less than 20 games
            //create new server on new rmi port
            int newPort = 1099 + serverlist.size();
            System.out.println("new rmi port: " + newPort);
            Registry newRegistry = LocateRegistry.createRegistry(newPort);
            serverlist.put(newPort, newRegistry);
            System.out.println("Registry created");
            int gameID = database.CreateGame(gameName, maxPlayers, newPort); //adds in database
            newRegistry.rebind(Integer.toString(gameID), new UnoGameLogic(DBList.get(teller/2), maxPlayers, Integer.toString(gameID)));
            System.out.println("Created game on RMIPort: " + newPort + " GameID: " + gameID + " Using database no. " + teller/2);

            return true;
        }
    }

    /**
     * Returns true if the game is not yet full
     * @param gameID
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean hasRoom(String gameID) throws RemoteException {
        return database.hasRoom(gameID);
    }

    /**
     * Returns the userId of the given username
     * @param username
     * @return Integer
     * @throws RemoteException
     */
    @Override
    public int getUserId(String username) throws RemoteException {
        return database.getUserId(username);
    }

    /**
     * Check if the login is valid against the database
     * @param user
     * @param pass
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public long login(String user, String pass, boolean test) throws RemoteException {
        return database.login(user, pass, test);
    }

    @Override
    public boolean CreateGame(String gameName, int maxPlayers, long token, int userID) throws RemoteException {
        //Per 2 gameservers 1 database!

        //first check token!!
        if(database.tokenExpired(token, userID)){
            return false;
        }
        Set set = serverlist.entrySet();
        Iterator iterator = set.iterator();
        int teller = 0;
        while (iterator.hasNext()){
            Map.Entry<Integer, Registry> mentry = (Map.Entry)iterator.next();
            if(database.getNumberOfActiveGamesOnRMIPort(mentry.getKey())<20){//als aantal actieve games kleiner is dan 20
                int gameID = database.CreateGame(gameName, maxPlayers, mentry.getKey()); //adds in database

                if(teller/2 > DBList.size()){
                    //more games than the databases can handle
                    return false;
                }else{
                    mentry.getValue().rebind(Integer.toString(gameID),new UnoGameLogic(DBList.get(teller/2), maxPlayers, Integer.toString(gameID)));
                    System.out.println("Created game on RMIPort: " + mentry.getKey() + " GameID: " + gameID + " Using database no. " + teller/2);
                    return true;
                }
            }
            teller++;
        }

        //teller++;
        if(teller/2 > DBList.size()){
            //more games than the databases can handle
            return false;
        }else{
            //if full while loop no return (so no servers with less than 20 games
            //create new server on new rmi port
            int newPort = 1099 + serverlist.size();
            System.out.println("new rmi port: " + newPort);
            Registry newRegistry = LocateRegistry.createRegistry(newPort);
            serverlist.put(newPort, newRegistry);
            System.out.println("Registry created");
            int gameID = database.CreateGame(gameName, maxPlayers, newPort); //adds in database
            newRegistry.rebind(Integer.toString(gameID), new UnoGameLogic(DBList.get(teller/2), maxPlayers, Integer.toString(gameID)));
            System.out.println("Created game on RMIPort: " + newPort + " GameID: " + gameID + " Using database no. " + teller/2);

            return true;
        }
    }

    @Override
    public boolean tokenExpired(int userID, long token) throws RemoteException {
        if(database.tokenExpired(token,userID)){
            return true;
        }
        return false;
    }
}
