package dbServer;

import Interfaces.Database;
import org.apache.commons.codec.binary.Base64;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

/**
 * Created by Tjebb on 01/11/2017.
 */
public class DatabaseLogic extends UnicastRemoteObject implements Database {
    public Connection conn = null;
    public List<Connection> OtherDatabases;

    /**
     * Constructor
     * @param conn
     * @throws RemoteException
     */
    public DatabaseLogic(Connection conn) throws RemoteException{
        super();
        this.conn = conn;
        this.OtherDatabases = new ArrayList<>();
    }

    /**
     * Deze methode vraagt aan de DB achter het passwoord en username
     * als passwoord matched return true anders false
     * @param user
     * @param pass
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean login(String user, String pass)throws RemoteException{
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT Username, Password, Salt FROM users u WHERE Username='"+user+"'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            String password = rs.getString("Password");
            String saltString = rs.getString("Salt");

            byte[] salt = stringToByte(saltString);
            byte[] hashedPass = getHashWithSalt(pass, salt);
            String passString = bytetoString(hashedPass);

            System.out.println("DB pass: " + password);
            System.out.println("hashed pass from user pass: " + passString);

            if(passString.equals(password)){
                rs.close();
                stmt.close();
                if(stmt != null) stmt.close();

                return true;
            }else{
                rs.close();
                stmt.close();
                if(stmt != null) stmt.close();

                return false;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method for converting byte array to a base64 string
     * @param input
     * @return String
     */
    public String bytetoString(byte[] input) {
        return encodeBase64String(input);
    }

    /**
     * hash the string input with the salt
     * @param input
     * @param salt
     * @return byte[]
     * @throws NoSuchAlgorithmException
     */
    public byte[] getHashWithSalt(String input, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(salt);
        byte[] hashedBytes = digest.digest(stringToByte(input));
        return hashedBytes;
    }

    /**
     * converts string to a byte array
     * @param input
     * @return byte[]
     */
    public byte[] stringToByte(String input) {
        if (Base64.isBase64(input)) {
            return Base64.decodeBase64(input);

        } else {
            return Base64.encodeBase64(input.getBytes());
        }
    }

    /**
     * Deze methode zal proberen een user toevoegen aan de database.
     * Returned false als username niet uniek is, true als insert statement
     * completed is
     * @param user
     * @param pass
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean register(String user, String pass, String salt) throws RemoteException{
        String sql = null;
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            sql="INSERT INTO users (Username, Password, Salt) VALUES ('"+ user +"', '"+ pass+"', '"+ salt+"')";

            stmt.execute(sql);
            System.out.println("executed");
            stmt.close();
            if(stmt != null) stmt.close();

            return true;

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            updateAll(sql);
        }
        return false;

    }

    /**
     * Deze methode update de highscore als de gegeven score groter is dan de huidige highscore
     * als niet groter dan doet de methode nikss
     * @param user
     * @param score
     * @throws RemoteException
     */
    @Override
    public void updateHighScore(String user, int score)throws RemoteException{
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT HighScore FROM users u WHERE Username='"+user+"'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int currentHighScore = rs.getInt("HighScore");
            System.out.println(user + " " + currentHighScore + " " + score);
            if(currentHighScore>score){
                rs.close();
                stmt.close();
                if(stmt != null) stmt.close();
            }else {
                sql = "UPDATE users SET HighScore='" + score + "' WHERE Username='" + user + "'";
                stmt.execute(sql);
                stmt.close();
                if(stmt != null) stmt.close();
                //updating the other databases
                updateAll(sql);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method asks the database for all games
     * @return List of arrays: array contains 0: Gamename (STRING) 1: joined/max (STRING) 2: RMI portnumber (INT)
     * @throws RemoteException
     */
    @Override
    public List<Object[]> getGames() throws RemoteException{
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT GameName, MaxPlayers, NumberOfPlayers, RMIPort, GameID FROM games g";

            ResultSet rs = stmt.executeQuery(sql);
            List<Object[]> games = new ArrayList<>();
            int maxPlayers, numberOfPlayers, RMIPort, gameID;
            String gameName;

            while (rs.next()){
                Object [] game = new Object[4];
                gameName = rs.getString("GameName");
                maxPlayers = rs.getInt("MaxPlayers");
                numberOfPlayers = rs.getInt("NumberOfPlayers");
                RMIPort = rs.getInt("RMIPort");
                gameID = rs.getInt("GameID");
                System.out.println(gameName + " " + maxPlayers + " " + numberOfPlayers + " " + RMIPort);
                game[0] = gameName;
                game[1] = numberOfPlayers + "/" + maxPlayers;
                game[2] = RMIPort;
                game[3] = gameID;

                games.add(game);
            }

            return games;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method asks the database for all the highscores and usernames
     * @return List of arrays: array contains 0: Username (STRING) 1: Highscore (INT)
     * @throws RemoteException
     */
    @Override
    public List<Object[]> getLeaderboard() throws RemoteException{
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT UserName, HighScore FROM users u ORDER BY HighScore DESC";

            ResultSet rs = stmt.executeQuery(sql);
            List<Object[]> leaderboard = new ArrayList<>();
            int highScore;
            String username;

            while (rs.next()){
                Object [] score = new Object[2];
                username = rs.getString("UserName");
                highScore = rs.getInt("HighScore");
                System.out.println(username + ": " + highScore);
                score[0] = username;
                score[1] = highScore;
                leaderboard.add(score);
            }
            return leaderboard;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * method for cheching if the game has room for at least one more player
     * @param gameID
     * @return boolean
     * @throws RemoteException
     */
    @Override
    public boolean hasRoom(String gameID) throws RemoteException {
        try{
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT NumberOfPlayers, MaxPlayers FROM games g WHERE GameID='" + gameID+ "'";

            ResultSet rs = stmt.executeQuery(sql);
            if(rs == null) {
                System.out.println("no game found");
                return false;
            }else{
                rs.next();
                int numberOfPlayers = rs.getInt("NumberOfPlayers");
                int maxPlayers = rs.getInt("MaxPlayers");

                System.out.println(numberOfPlayers + "/" + maxPlayers);
                if(numberOfPlayers<maxPlayers){
                    return true;
                }else {
                    return false;
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get the userID from the given username
     * @param username
     * @return Integer
     * @throws RemoteException
     */
    @Override
    public int getUserId(String username) throws RemoteException {
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT UserId FROM users u WHERE Username='"+username+"'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int userId = rs.getInt("UserId");
            return userId;

        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Updates the current number of games in a game given the gameID
     * @param gameID
     * @throws RemoteException
     */
    @Override
    public void joinedGame(String gameID) throws RemoteException {
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT NumberOfPlayers FROM games g WHERE GameID='" + gameID+ "'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int currentNumberOfPlayers = rs.getInt("NumberOfPlayers");
            currentNumberOfPlayers++;
            sql = "UPDATE games SET NumberOfPlayers='" + currentNumberOfPlayers + "' WHERE GameID='" + gameID + "'";
            stmt.execute(sql);
            stmt.close();
            if(stmt != null) stmt.close();
            //updating otherr database
            updateAll(sql);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Deletes the game after the game has finished
     * @param gameID
     * @throws RemoteException
     */
    @Override
    public void deleteGame(String gameID) throws RemoteException{
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "DELETE FROM games WHERE GameID ='" + gameID+ "'";
            stmt.execute(sql);
            if(stmt != null) stmt.close();
            //updating other databases
            updateAll(sql);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Returns the number of active games on a given RMI port
     * @param RMIPort
     * @return Integer
     * @throws RemoteException
     */
    @Override
    public int getNumberOfActiveGamesOnRMIPort(int RMIPort) throws RemoteException {
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT COUNT(*) as count FROM games WHERE RMIPort ='" + RMIPort+ "'";
            ResultSet rs = stmt.executeQuery(sql);
            //if(stmt != null) stmt.close();
            if(rs == null){
                return Integer.MAX_VALUE;
            }else{
                rs.next();
                return rs.getInt("count");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }

    /**
     * creates game in databas on specified rmiport
     * @param GameName
     * @param maxPlayers
     * @param RMIPort
     * @return Integer
     * @throws RemoteException
     */
    @Override
    public int CreateGame(String GameName, int maxPlayers, int RMIPort) throws RemoteException {
        String sql=null;
        try {

            sql = "INSERT INTO games (GameName, MaxPlayers, RMIPort) VALUES ('"+ GameName +"', '"+ maxPlayers+ "', '"+ RMIPort +"')";

            PreparedStatement prest;
            prest = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            prest.executeUpdate();
            ResultSet rs = prest.getGeneratedKeys();
            if(rs.next())
            {
                int last_inserted_id = rs.getInt(1);
                return last_inserted_id;
            }

            return 0;

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            //updating other databases
            updateAll(sql);
        }
        return 0;
    }

    /**
     * add other database to the list for consitency
     * @param c
     * @throws RemoteException
     */
    @Override
    public void addDatabase(Connection c) throws RemoteException{
        OtherDatabases.add(c);
    }

    /**
     * returns the connection of the database
     * @return Connection
     */
    @Override
    public Connection getConn() throws RemoteException {
        return conn;
    }

    /**
     * Method for propagating the update to all other databases
     * Used for consistency
     * @param sql
     * @throws RemoteException
     */
    private void updateAll(String sql)throws RemoteException{
        for (Connection c:OtherDatabases) {
            try{
                Statement stmt = c.createStatement();
                stmt.execute(sql);
                System.out.println("executed update");
                stmt.close();
                if(stmt != null) stmt.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    protected static SecureRandom random = new SecureRandom();

    /**
     * generates a token
     * @return long
     */
    public synchronized long generateToken() {
        return Math.abs( random.nextLong() );
    }

    /**
     * Deze methode vraagt aan de DB achter het passwoord en username
     * als passwoord matched return session token
     * @param user
     * @param pass
     * @return long
     * @throws RemoteException
     */
    @Override
    public long login(String user, String pass, boolean test)throws RemoteException{
        long token = 0;
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT Username, Password, Salt FROM users u WHERE Username='"+user+"'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            String password = rs.getString("Password");
            String saltString = rs.getString("Salt");

            byte[] salt = stringToByte(saltString);
            byte[] hashedPass = getHashWithSalt(pass, salt);
            String passString = bytetoString(hashedPass);

            System.out.println("DB pass: " + password);
            System.out.println("hashed pass from user pass: " + passString);

            if(passString.equals(password)){
                rs.close();
                //generate token
                token = generateToken();

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                //put token in database
                String sql2 = "UPDATE users SET Token='" + token + "', Expire='"+ timestamp +"' WHERE Username='" + user + "'";
                stmt.execute(sql2);

                stmt.close();
                if(stmt != null) stmt.close();

                return token;

            }else{
                rs.close();
                stmt.close();
                if(stmt != null) stmt.close();
                return 0;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            //propagate update
            String sql2 = "UPDATE users SET Token='" + token + "' WHERE Username='" + user + "'";
            updateAll(sql2);
        }
        return 0;

    }

    /**
     * Returns if the session token has expired
     * @param token
     * @param userID
     * @return boolean
     */
    @Override
    public boolean tokenExpired(long token, int userID) throws RemoteException{
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT Token, Expire FROM users u WHERE Userid='"+userID+"'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();

            long sessiontoken = rs.getLong("Token");
            Timestamp expire = rs.getTimestamp("Expire");
            LocalDateTime exp = expire.toLocalDateTime();
            exp = exp.plusDays(1);


            if(sessiontoken==token){
                LocalDateTime now = LocalDateTime.now();
                if(now.isAfter(exp)){
                    return true;
                }
                return false;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
