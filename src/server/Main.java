package server;

import Interfaces.Database;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tjebb on 28/10/2017.
 */
public class Main {
    //server port
    public static final int PORT = 1099;
    public static final int DB_PORT = 4444;
    public static final int LOBBY_PORT = 2000;
    public static final int NUMBEROFDATABASES = 4;


    public static void main(String[] args){
        Main main = new Main();
        main.startServer();
    }

    private void startServer() {
        try{
            //connecting to DB server
            Registry database = LocateRegistry.getRegistry("localhost", DB_PORT);

            List<Database> DBList = new ArrayList<>();
            Interfaces.Database DB;
            for(int i = 0; i<NUMBEROFDATABASES; i++){
                DB = (Database) database.lookup("DatabaseService" + i);
                DBList.add(DB);
                System.out.println("Connecting to database: " + i);
            }
//            int teller = 0;
//            Database temp;
//            for (Database db:DBList) {
//                for(int i = 0; i<NUMBEROFDATABASES; i++){
//                    if(teller != i){
//                        temp = DBList.get(i);
//                        db.addDatabase(temp.getConn());
//                        System.out.println("adding database " + i + "to the database " + teller);
//                    }
//                }
//            }


//            Interfaces.Database DB = (Database) database.lookup("DatabaseService");
//            System.out.println("connecting over RMI to Database Server");

            //create on port 1099
            Registry registry = LocateRegistry.createRegistry(PORT);

            //create on port 2000
            Registry lobby = LocateRegistry.createRegistry(LOBBY_PORT);
            //create service named LobbyService
            lobby.rebind("LobbyService", new LobbyLogic(DBList, registry, PORT));
            System.out.println(lobby.toString());


            //create service named UnoGameService

            //registry.rebind("UnoGameService", new UnoGameLogic(DB));
            //System.out.println(registry.toString());

        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("system is ready");
    }

}
