
import java.util.*;
import java.io.IOException;

public class Server {

    private String host, rcon, status = "", map = "", info = "";
    private String say = "say \"^0{^7FH^0}^7RCon: ", end = "\"";
    private int port;
    boolean updating = false;

    private NetRcon server;
    private ArrayList<Client> playerList = new ArrayList<Client>();

    public Server(String h, int portN, String r) {
        host = h;
        port = portN;
        rcon = r;

        init();
    }

    private void init() {
        //create an rcon instance
        server = new NetRcon(host, port, rcon, true, 200, 100);
        getRconStatus();
        getInfo();
        //mapList();

        Thread t1 = new Thread(
                new Runnable() {
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(2000);
                        getRconStatus();
                    } catch (InterruptedException e) {

                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("haha we done fucked up somewhere");
                        System.gc();
                    }
                }
            }
        });
        t1.start();
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return rcon;
    }

    public int getPort() {
        return port;
    }

    public void getInfo() {
        do {
            info = server.sendCommand("serverinfo");
            info = info.replaceAll("\uFFFD\uFFFD\uFFFD\uFFFDprint\n", "");
        } while (info.isEmpty());
    }

    private void getRconStatus() {
        boolean success = true;
        server.setReturnData(true);

        do {
            status = server.sendCommand("status");
            status = status.replaceAll("\uFFFD\uFFFD\uFFFD\uFFFDprint\n", "");
        } while (status.isEmpty());

        //System.out.println(status);
        //System.out.println(info);
        String[] str = status.split("\\n");

        updating = true;
        playerList.clear();
        for (int i = 0; i < str.length; i++) {
            if (i == 0) {
                map = str[i];
                map = map.replace("map: ", "");
            } else if (i == 1) {
                if (!str[i].equals("num score ping guid                             name            lastmsg address               qport rate")) {
                    //System.out.println("Mismatch on header line");
                    success = false;
                    break;
                }
            } else if (i == 2) {
                if (!str[i].equals("--- ----- ---- -------------------------------- --------------- ------- --------------------- ----- -----")) {
                    //System.out.println("Mismatch on spacer line");
                    success = false;
                    break;
                }
            } else {
                //System.out.println("adding this: " + str[i]);
                Client c = new Client(str[i]);
                if (c.guidIsValid()) {
                    playerList.add(c);
                } else {
                    sendKick(c.getClientId(), c.getExactName(), c.getGuid()); // send the clientid to the kick command, invalid guid
                }
            }
        }

        updating = false;
        server.setReturnData(false);

        if (!success) {
            getRconStatus();
        }
    }

    public void refreshStatus() {
        getRconStatus();
    }

    public ArrayList<Client> getPlayerList() {
        return playerList;
    }

    public String getMap() {
        return map;
    }

    public String getStatus() {
        return status;
    }

    public String changeMap(String newMap) {
        String str = "";

        server.setReturnData(true);
        server.setTimeout(300);
        if (newMap.equals("map_rotate")) {
            do {
                str = server.sendCommand(newMap);
            } while (str.isEmpty());
        } else {
            do {
                str = server.sendCommand("map " + newMap);
            } while (str.isEmpty());
        }

        System.out.println(str);
        server.setReturnData(false);
        server.setTimeout(200);

        return str;
    }

    public String sendPM(String clientid, String guid, String message) {
        String str = "";

        //System.out.println("client id: " + clientid);
        //System.out.println("guid: " + guid);
        server.setReturnData(true);
        do {
            str = server.sendCommand("tell " + clientid + " \"^3[pm]>^7 " + message + end);
        } while (str.isEmpty());
        server.setReturnData(false);

        //System.out.println(str);
        return "Personal message sent!";
    }

    public String sendMessage(String message) {
        String str = "";

        server.setReturnData(true);
        do {
            str = server.sendCommand(say + message + end);
        } while (str.isEmpty());
        server.setReturnData(false);

        return "Message sent!";
    }

    private void mapList() {
        String str = "";

        server.setReturnData(true);
        do {
            str = server.sendCommand("fdir *map_mp* .iwi");
        } while (str.isEmpty());
        server.setReturnData(false);

        //System.out.println(str);
    }

    private void sendKick(String clientid, String name, String guid) {
        System.out.println("Kicking client: " + clientid + "\t" + name + "\t" + guid);
        server.sendCommand("clientkick " + clientid);
    }

    /* TODO implement database here? */
    public String sendClientKick(String clientid, String guid, String name, String reason) {
        String str = "";

        //System.out.println("client id: " + clientid);
        //System.out.println("guid: " + guid);
        sendMessage(name + " was kicked for ^2" + reason);

        server.setReturnData(true);
        do {
            str = server.sendCommand("clientkick " + clientid);
        } while (str.isEmpty());

        server.setReturnData(false);

        return str;
    }

    public String sendBan(String clientid, String guid, String reason) {
        String str = "";

        server.setReturnData(true);
        str = server.sendCommand("banclient " + clientid);
        server.setReturnData(false);

        return str;
    }

    public String sendTempBan(String clientid, String guid, String timeLength, String reason) {
        String str = "";

        server.setReturnData(true);
        str = server.sendCommand("clientkick " + clientid);
        server.setReturnData(false);

        return str;
    }

    public boolean searchPlayerList(String clientid, String guid) {
        //getRconStatus();
        /*Thread t1 = new Thread(
         new Runnable() {
            public void run() {         
               getRconStatus();  
            }
         });  
      t1.start();
      
   	try {
   	Thread.sleep(10);
   	}
   	catch (InterruptedException e) {
   	   //sigh
   	}*/
        boolean ret = false;

        for (int i = 0; i < playerList.size(); i++) {

            if (playerList.get(i).getShortGuid().equals(guid) && playerList.get(i).getClientId().equals(clientid)) {
                return true;
            }
        }

        return ret;
    }
}
