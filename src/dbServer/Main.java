package dbServer;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Tjebb on 28/10/2017.
 */
public class Main {
    //static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    //static final String DB_URL = "jdbc:mysql://localhost/UNO";

    //static final String USER = "root";
    //static final String PASS = "root";

    public static final int PORT = 4444;
    public static final int NUMBEROFDATABASES = 4;

    public static void main(String[] args){
        Main main = new Main();
        //File f = new File("uno.db");
        //if(!f.exists()){

            main.createDatabase(NUMBEROFDATABASES);
        //}
        main.startDBServer(NUMBEROFDATABASES);

    }

    private void createDatabase(int aantal) {
        for(int i = 0; i<aantal; i++){

            File f = new File("uno" + i + ".db");
            if(f.exists()){
                break;
            }
            System.out.println("initializing database");
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:uno" + i +".db", "root", "root");
                //creating the database!
                stmt = c.createStatement();
                String sql = "CREATE TABLE USERS " +
                        "(USERID INTEGER PRIMARY KEY        AUTOINCREMENT," +
                        " USERNAME           TEXT       NOT NULL, " +
                        " PASSWORD           TEXT       NOT NULL, " +
                        " SALT               TEXT       NOT NULL, " +
                        " EXPIRE          TIMESTAMP  NOT NULL    DEFAULT '0000-00-00 00:00:00', " +
                        " TOKEN              LONG       NOT NULL    DEFAULT '0', " +
                        " HIGHSCORE          INTEGER    NULL        DEFAULT '0')";

                stmt.executeUpdate(sql);
                sql = "CREATE TABLE GAMES " +
                        "(GAMEID INTEGER PRIMARY KEY     AUTOINCREMENT," +
                        " GAMENAME           TEXT        NOT NULL, " +
                        " MAXPLAYERS         INTEGER     NOT NULL DEFAULT '4', " +
                        " NUMBEROFPLAYERS    INTEGER     NOT NULL DEFAULT '0',"+
                        " RMIPORT            INTEGER     NOT NULL DEFAULT '1099')";
                stmt.executeUpdate(sql);
                stmt.close();
                System.out.println("Opened database successfully");

            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }

    }

    public void startDBServer(int aantal){
        Connection c = null;
//        try {
//            Class.forName("org.sqlite.JDBC");
//            c = DriverManager.getConnection("jdbc:sqlite:uno.db", "root", "root");
//            c.setAutoCommit(true); //to automatically commit a sql statement
//            System.out.println("Opened database successfully");
//            //created database connection
//
//            Registry registry = LocateRegistry.createRegistry(PORT);
//            //create service named databaseService
//            registry.rebind("DatabaseService", new DatabaseLogic(c));
//            System.out.println(registry.toString());
//            System.out.println("system is ready");
//
//        } catch ( Exception e ) {
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//            System.exit(0);
//
//      starting aantal databases and adding them to the database registry
        try {
            Registry registry = LocateRegistry.createRegistry(PORT);
            List<Connection> databases = new ArrayList<>();
            for (int i=0; i<aantal; i++){
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:uno" + i + ".db", "root", "root");
                c.setAutoCommit(true);
                System.out.println("Opened database " + i + " successfully");
                databases.add(c);
                //registry.rebind("DatabaseService" + i, new DatabaseLogic(c));
            }

            int teller = 0;
            for (Connection conn:databases) {
                DatabaseLogic temp = new DatabaseLogic(conn);
                for(int i=0; i<NUMBEROFDATABASES; i++){
                    if(teller!=i){
                        temp.addDatabase(databases.get(i));
                        System.out.println("Adding database " + i + "to logic " + teller);
                    }
                }
                registry.rebind("DatabaseService" + teller, temp);
                teller++;
            }



            System.out.println("system is ready");
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }


    }
}
