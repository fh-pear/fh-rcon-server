import java.util.*;
import java.io.IOException;

public class ClientServer
{

    private String host, rcon, status = "", map = "", info = "";
    private final String say = "say \"^0{^7FH^0}^7RCon: ", end = "\"";
    private int port, timeout = 200;
    boolean updating = false;

    private NetRcon server;
    private ArrayList<Client> playerList = new ArrayList<Client>();

    public ClientServer(String h, int portN, String r)
    {
        host = h;
        port = portN;
        rcon = r;

        init();
    }

    private void init()
    {
        //create an rcon instance
        server = new NetRcon(host, port, rcon, true, timeout, 50);
        getRconStatus();
        getInfo();
        //mapList();
    }

    public String getInfo()
    {
        server.setReturnData(true);
        do
        {
            info = server.sendCommand("serverinfo");
            info = info.replaceAll("\uFFFD\uFFFD\uFFFD\uFFFDprint\n", "");
        }
        while (info.isEmpty());
        server.setReturnData(false);

        return info;
    }

    public String sendRcon(String rconCommand)
    {

        if (rconCommand.equals("map_rotate") || rconCommand.contains("map mp_"))
        {
            server.setTimeout(1000);
        }

        String str = "";
        server.setReturnData(true);
        do
        {
            str = server.sendCommand(rconCommand);
        }
        while (info.isEmpty());
        server.setReturnData(false);
        server.setTimeout(timeout);

        return str;
    }

    public String getServerName()
    {
        String str = "";

        server.setReturnData(true);
        do
        {
            str = server.sendCommand("sv_hostname");
        }
        while (str.isEmpty());
        server.setReturnData(false);

        return str;
    }

    private void getRconStatus()
    {
        //System.out.println("ClientServer class, getting rcon status");
        boolean success = true;
        server.setReturnData(true);

        do
        {
            status = server.sendCommand("status");
            status = status.replaceAll("\uFFFD\uFFFD\uFFFD\uFFFDprint\n", "");
        }
        while (status.isEmpty());

        //System.out.println(info);
        String[] str = status.split("\\n");

        updating = true;
        playerList.clear();
        for (int i = 0; i < str.length; i++)
        {
            if (i == 0)
            {
                map = str[i];
                map = map.replace("map: ", "");
            }
            else if (i == 1)
            {
                if (!str[i].equals("num score ping guid                             name            lastmsg address               qport rate"))
                {
                    //System.out.println("Mismatch on header line");
                    success = false;
                    break;
                }
            }
            else if (i == 2)
            {
                if (!str[i].equals("--- ----- ---- -------------------------------- --------------- ------- --------------------- ----- -----"))
                {
                    //System.out.println("Mismatch on spacer line");
                    success = false;
                    break;
                }
            }
            else
            {
                try
                {
                    //System.out.println("adding this: " + str[i]);
                    Client c = new Client(str[i]);
                    if (c.guidIsValid())
                    {
                        playerList.add(c);
                    }
                    else
                    {
                        sendKick(c.getClientId(), c.getExactName(), c.getGuid()); // send the clientid to the kick command, invalid guid
                    }
                }
                catch (IllegalArgumentException e)
                {
                    //client string argument is empty
                }
            }
        }

        updating = false;
        server.setReturnData(false);

        if (!success)
        {
            getRconStatus();
        }
    }

    public void refreshStatus()
    {
        getRconStatus();
    }

    public ArrayList<Client> getPlayerList()
    {
        refreshStatus();
        return playerList;
    }

    public String getMap()
    {
        String str = "";

        server.setReturnData(true);
        do
        {
            str = server.sendCommand("mapname");
        }
        while (str.isEmpty());
        server.setReturnData(false);

        map = str;
        return map;
    }

    public String getStatus()
    {
        return status;
    }

    public String changeMap(String newMap)
    {
        String str = "";

        server.setReturnData(true);
        server.setTimeout(1000);
        if (newMap.equals("map_rotate"))
        {
            do
            {
                str = server.sendCommand(newMap);
            }
            while (str.isEmpty());
        }
        else
        {
            do
            {
                str = server.sendCommand("map " + newMap);
            }
            while (str.isEmpty());
        }

        System.out.println(str);
        server.setReturnData(false);
        server.setTimeout(timeout);

        return str;
    }

    public String sendPM(String clientid, String admin, String guid, String message)
    {
        String str = "";

        //System.out.println("client id: " + clientid);
        //System.out.println("guid: " + guid);
        server.setReturnData(true);
        do
        {
            str = server.sendCommand("tell " + clientid + " \"^3(^2" + admin + "^3)[pm]>^7 " + message + end);
        }
        while (str.isEmpty());
        server.setReturnData(false);

        //System.out.println(str);
        return "Personal message sent!";
    }

    public String sendMessage(String message)
    {
        String str = "";

        server.setReturnData(true);
        do
        {
            str = server.sendCommand(say + message + end);
        }
        while (str.isEmpty());
        server.setReturnData(false);

        return "Message sent!";
    }

    private void mapList()
    {
        String str = "";

        server.setReturnData(true);
        do
        {
            str = server.sendCommand("fdir *map_mp* .iwi");
        }
        while (str.isEmpty());
        server.setReturnData(false);

        //System.out.println(str);
    }

    private void sendKick(String clientid, String name, String guid)
    {
        System.out.println("Kicking client: " + clientid + "\t" + name + "\t" + guid);
        server.sendCommand("clientkick " + clientid);
    }

    /* TODO implement database here? */
    public String sendClientKick(String clientid, String guid, String name, String reason)
    {
        String str = "";

        //System.out.println("client id: " + clientid);
        //System.out.println("guid: " + guid);
        sendMessage(name + " was kicked for ^2" + reason);

        server.setReturnData(true);
        do
        {
            str = server.sendCommand("clientkick " + clientid);
        }
        while (str.isEmpty());

        server.setReturnData(false);

        return str;
    }

    public String sendBan(String clientid, String guid, String name, String reason)
    {
        String str = "";

        sendMessage(name + " was permanently banned for ^2" + reason);

        server.setReturnData(true);
        do
        {
            str = server.sendCommand("banclient " + clientid);
        }
        while (str.isEmpty());
        server.setReturnData(false);

        return str;
    }

    public String sendTempBan(String clientid, String guid, String name, String reason)
    {
        String str = "";

        //System.out.println("client id: " + clientid);
        //System.out.println("guid: " + guid);
        sendMessage(name + " was temporarily banned for ^2" + reason);

        server.setReturnData(true);
        do
        {
            str = server.sendCommand("clientkick " + clientid);
        }
        while (str.isEmpty());

        server.setReturnData(false);

        return str;
    }

    public boolean searchPlayerList(String clientid, String guid)
    {
        //getRconStatus();

        boolean ret = false;

        for (int i = 0; i < playerList.size(); i++)
        {

            if (playerList.get(i).getShortGuid().equals(guid) && playerList.get(i).getClientId().equals(clientid))
            {
                return true;
            }
        }

        return ret;
    }
}
