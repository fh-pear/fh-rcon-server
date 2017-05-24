
import java.util.*;
import java.net.*;
import java.io.*;

public class codServer extends Server implements Runnable {

    private String serverName;
    private int serverPort;

    public codServer(String svrN, String h, int portN, String rcon, int serverP) {
        super(h, portN, rcon);
        serverName = svrN;
        System.out.println("server name: " + serverName);
        serverPort = serverP;
        System.out.println("server port: " + serverPort);

    }

    public void run() {
        int clientNumber = 0;
        ServerSocket listener = null;
        try {
            System.out.println("creating socket");
            listener = new ServerSocket(serverPort);
            System.out.println(serverName + " RCon server initiated, listening...");

            while (true) {
                new UserThread(this, listener.accept(), clientNumber++).start();
            }

        } catch (IOException e) {
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
            } catch (NullPointerException e) {
                System.out.println("This isn't good: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public String getServerName() {
        return serverName;
    }

}
