import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.LinkedHashMap;

public class UserProtocol
{

    public static final String UNIT_SEPARATOR = "\t";

    private final int BANPOWER = 60;
    private final int TEMPBANPOWER = 40;
    private final int KICKPOWER = 20;
    private final int MAPPOWER = 40;
    private final int NEXTMAPPOWER = 20;
    private final int RCON = 90;

    private final int LOGIN = 0;
    private final int VERIFY_VERSION = 1;
    private final int WAITING = 2;

    private ClientServer cod;
    private int level, clientLevel;
    private boolean fullDetails, clientMask = false;
    private String adminid, clientid = "", adminName = "";
    private Map<String, B3Level> levels;

    private int state = LOGIN;

    private Database d;
    private Client clientCache;

    public UserProtocol(ClientServer c)
    {
        cod = c;
    }

    public String processInput(String theInput)
    {
        String theOutput = null;

        if (state == LOGIN)
        {
            if (theInput.equalsIgnoreCase("exit"))
            {
                theOutput = "exit";
            }
            else
            {
                theOutput = processLogin(theInput);
            }
        }
        else if (state == VERIFY_VERSION)
        {
            theOutput = processVersion(theInput);
        }
        else if (state == WAITING)
        {
            if (theInput.equalsIgnoreCase("exit"))
            {
                theOutput = "exit";
            }
            else
            {
                theOutput = processCommand(theInput);
            }
        }
        else
        {
            theOutput = "exit";
        }

        if (theOutput.equals("exit"))
        {
            d.close();
        }

        return theOutput.trim().concat("\n...");
    }

    private String processLogin(String in)
    {
        d = new Database();
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(UserProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] user = in.split(UNIT_SEPARATOR);
        ArrayList<String> details;

        //System.out.println("arg1: " + user[0]);
        d.login(user[0]);
        details = new ArrayList<String>(d.getResults());

        if (details.isEmpty())
        {
            return "exit";
        }
        if (details.get(1) == null)
        {
            return "exit";
        }

        populateLevels();

        //System.out.println("input: " + user[1]);
        //System.out.println("datab: " + details.get(1));
        if (details.get(1).equals(user[1]))
        {
            //System.out.println("correct");
            level = parseLevel(details.get(2));

            if (level > 80)
            {
                fullDetails = true;
            }
            else
            {
                fullDetails = false;
            }

            if (level < 20) // these are levels that are below moderators (mods are groupbits = 32). below mod should not be able to login
            {
                return "exit";
            }

            adminid = user[0];
            //System.out.println("adminid: " + adminid);
            state = VERIFY_VERSION;

            return "Verify version";
        }
        else
        {
            return "exit";
        }
    }

    private String processVersion(String in)
    {
        if (in.equals(Version.VERSION))
        {
            state = WAITING;
            //System.out.println("logged in, waiting");
            return "Logged in. Waiting for command(s).";
        }
        else
        {
            return "You're using version: " + in
                    + "\n You need to update to version: " + Version.VERSION;
        }
    }

    private String processCommand(String in)
    {
        /* check the command. if it's changepassword, only output what the command is
         * this will ensure that a user's password doesn't get logged
         */
        if (in.contains("changepassword"))
        {
            System.out.println("RECEIVED COMMAND: changepassword");
        }
        else
        {
            System.out.println("RECEIVED COMMAND: " + in);
        }

        String str = "";
        String[] s = in.split(UNIT_SEPARATOR);

        if (s[0].equals("status"))
        {
            str = cmdStatus();
        }
        else if (s[0].equals("kick"))
        {
            str = cmdKick(s);
        }
        else if (s[0].equals("getmap"))
        {
            str = cmdGetMap();
        }
        else if (s[0].equals("map"))
        {
            str = cmdMap(s);
        }
        else if (s[0].equals("pm"))
        {
            str = cmdPM(s);
        }
        else if (s[0].equals("say"))
        {
            str = cmdSay(s);
        }
        else if (s[0].equals("aliases"))
        {
            str = cmdAliases(s);
        }
        else if (s[0].equals("getdataid"))
        {
            str = cmdGetDataId(s);
        }
        else if (s[0].equals("penalties"))
        {
            str = cmdGetPenalties(s);
        }
        else if (s[0].equals("getclient"))
        {
            str = cmdGetClient(s);
        }
        else if (s[0].equals("ban"))
        {
            str = cmdBan(s);
        }
        else if (s[0].equals("tempban"))
        {
            str = cmdTempBan(s);
        }
        else if (s[0].equals("getprofile"))
        {
            str = cmdGetProfile(s);
        }
        else if (s[0].equals("changepassword"))
        {
            str = cmdChangePassword(s);
        }
        else if (s[0].equals("search"))
        {
            str = cmdSearch(s);
        }
        else if (s[0].equals("servername"))
        {
            str = cmdGetServerName(s);
        }
        else if (s[0].equals("serverinfo"))
        {
            str = cmdGetServerInfo(s);
        }
        else if (s[0].equals("rcon"))
        {
            str = cmdRcon(s);
        }
        else if (s[0].equals("getb3groups"))
        {
            str = cmdb3Groups();
        }
        else
        {
            str = "Unknown command '" + s[0] + "'";
        }

        //System.out.println("command results: " + str);
        return str;
    }

    private String cmdb3Groups()
    {
        StringBuilder str = new StringBuilder(200);

        for (B3Level value : levels.values())
        {
            str.append(value);
            str.append("\n");
        }

        //System.out.println("b3groups: " + str.toString());
        return str.toString();
    }

    private String cmdRcon(String[] opts)
    {
        if (opts.length != 2)
        {
            return "Invalid parameters for command 'rcon'";
        }

        if (level < RCON)
        {
            return "You are not a high enough level";
        }

        return cod.sendRcon(opts[1]);
    }

    private String cmdGetServerName(String[] opts)
    {
        return cod.getServerName();
    }

    private String cmdGetServerInfo(String[] opts)
    {
        return cod.getInfo();
    }

    private String cmdSay(String[] opts)
    {
        StringBuilder str = new StringBuilder(50);

        if (opts.length < 2)
        {
            str.append("Invalid parameters for global message.");
        }
        else if (opts.length == 2)
        {
            str.append(cod.sendMessage(opts[1]));
        }
        else
        {
            String m = "";
            for (int i = 1; i < opts.length; i++)
            {
                m += opts[i];
            }

            str.append(cod.sendMessage(m));
        }

        return str.toString();
    }

    private String cmdGetMap()
    {
        return cod.getMap();
    }

    private String cmdMap(String[] opts)
    {
        if (opts.length != 2)
        {
            return "Invalid parameters for map change!";
        }

        if (level < NEXTMAPPOWER)
        {
            return "You are not a high enough level to rotate the map";
        }
        if (level < MAPPOWER && !opts[1].equals("map_rotate"))
        {
            return "You are not a high enough level to change the map";
        }

        return cod.changeMap(opts[1]);
    }

    private String cmdStatus()
    {
        StringBuilder str = new StringBuilder(6000);
        ArrayList<Client> c = cod.getPlayerList();

        for (int i = 0; i < c.size(); i++)
        {
            str.append(c.get(i).toString(fullDetails));
        }

        return str.toString();
    }

    // opts: pm:<cid>:<short_guid>:<message>
    private String cmdPM(String[] opts)
    {
        if (opts.length != 4)
        {
            return "Invalid PM parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty())
        {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + "'";
        }

        ArrayList<Client> c = cod.getPlayerList();

        for (int i = 0; i < c.size(); i++)
        {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2]))
            {
                return cod.sendPM(client.getClientId(), adminName, client.getGuid(), opts[3]);
            }
        }

        return "ERROR: Client not found in current player list.";
    }

    // opts: kick:<cid>:<short_guid>:<reason>
    private String cmdKick(String[] opts)
    {
        if (opts.length != 4)
        {
            return "Invalid kick parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty())
        {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + "'";
        }

        if (level < KICKPOWER)
        {
            return "You are not a high enough level to kick";
        }

        ArrayList<Client> c = cod.getPlayerList();
        String str = "";

        for (int i = 0; i < c.size(); i++)
        {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2]))
            {

                if (levelVsClient(client.getGuid()))
                {
                    //System.out.println("this will kick: " + client.getName());
                    try
                    {
                        String array[] =
                        {
                            "", client.getGuid()
                        };
                        String reason = "(RCon) " + opts[3];

                        d.kickClient(cmdGetDataId(array), adminid, reason);
                        str = "Kick added to database. \n";
                    }
                    catch (SQLException e)
                    {
                        str = "Adding kick to the database failed...";
                    }

                    str += cod.sendClientKick(client.getClientId(), client.getGuid(), client.getName(), opts[3]);
                    return str;
                }
                else
                {
                    return client.getName() + " is a higher/equal level admin.";
                }
            }
        }

        return "ERROR: Client not found in current player list.";
    }

    // opts: temp:<cid>:<short_guid>:<duration>:<reason>
    private String cmdTempBan(String[] opts)
    {
        if (opts.length != 6)
        {
            return "Invalid tempban parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty() || opts[4].isEmpty())
        {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + UNIT_SEPARATOR + opts[4]
                    + UNIT_SEPARATOR + opts[5] + "'";
        }

        if (level < TEMPBANPOWER)
        {
            return "You are not a high enough level to tempban";
        }

        ArrayList<Client> c = cod.getPlayerList();
        String str = "";

        for (int i = 0; i < c.size(); i++)
        {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2]))
            {

                if (levelVsClient(client.getGuid()))
                {
                    try
                    {
                        String array[] =
                        {
                            "", client.getGuid()
                        };
                        String reason = "(RCon) " + opts[4];
                        long sec = Long.parseLong(opts[3]);
                        long dur = Long.parseLong(opts[5]);
                        if (dur < 0)
                        {
                            throw new NumberFormatException(dur + " is negative");
                        }

                        d.tempbanClient(cmdGetDataId(array), adminid, reason, sec, dur);
                        str = "Tempban added to database.\n ";
                    }
                    catch (SQLException e)
                    {
                        System.out.println(e.getMessage());
                        str = "Adding ban to database failed...";
                    }
                    catch (NumberFormatException e)
                    {
                        return "You must supply a positive number for tempban duration: " + e;
                    }

                    //str += cod.sendTempBan(client.getClientId(), client.getGuid(), client.getName(), opts[4]);
                    str += client.getName() + " with guid " + client.getGuid() + " would be tempbanned";
                    return str;
                }
                else
                {
                    return client.getName() + " is a higher/equal level admin.";
                }
            }
        }

        return "ERROR: Client not found in current player list.";
    }

    // opts: ban:<cid>:<short_guid>:<reason>
    private String cmdBan(String[] opts)
    {
        if (opts.length != 4)
        {
            return "Invalid ban parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty())
        {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + "'";
        }

        if (level < BANPOWER)
        {
            return "You are not a high enough level to ban";
        }

        ArrayList<Client> c = cod.getPlayerList();
        String str = "";

        for (int i = 0; i < c.size(); i++)
        {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2]))
            {

                if (levelVsClient(client.getGuid()))
                {
                    try
                    {
                        String array[] =
                        {
                            "", client.getGuid()
                        };
                        String reason = "(RCon) " + opts[3];

                        d.banClient(cmdGetDataId(array), adminid, reason);
                        str = "Ban added to database.\n ";
                    }
                    catch (SQLException e)
                    {
                        System.out.println(e.getMessage());
                        str = "Adding ban to database failed...";
                    }

                    str += cod.sendBan(client.getClientId(), client.getGuid(), client.getName(), opts[3]);
                    return str;
                }
                else
                {
                    return client.getName() + " is a higher/equal level admin.";
                }
            }
        }

        return "ERROR: Client not found in current player list.";
    }

    /* 
     * @opts[] getprofile:self     OR     getprofile:<@id>
     * @return str
     *      <id>:<name>:<guid>:<connections>:<level (String title)>:<level (int value)>:<first seen>:<last seen>
     */
    private String cmdGetProfile(String opts[])
    {
        StringBuilder profile = new StringBuilder(150);
        ResultSet results = null;

        if (opts.length != 2)
        {
            return "Invalid paramenters for command 'getprofile'";
        }

        if (opts[1].equals("self"))
        {
            try
            {
                results = d.getClientById(adminid);

                if (results.next())
                {
                    profile.append(results.getString("id"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("name"));
                    adminName = results.getString("name");
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("guid"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("connections"));
                    profile.append(UNIT_SEPARATOR);

                    String level = results.getString("group_bits");
                    int intLevel = parseLevel(level);
                    level = getLevelTitle(intLevel);
                    profile.append(level);
                    profile.append(UNIT_SEPARATOR);
                    profile.append(intLevel);
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("time_add"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("time_edit"));
                }
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
            }
        }
        else if (fullDetails)
        {
            try
            {
                results = d.getClientById(opts[1]);

                if (results.next())
                {
                    profile.append(results.getString("id"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("name"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("guid"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("connections"));
                    profile.append(UNIT_SEPARATOR);

                    String level = results.getString("group_bits");
                    int intLevel = parseLevel(level);
                    level = getLevelTitle(intLevel);
                    profile.append(level);
                    profile.append(UNIT_SEPARATOR);
                    profile.append(intLevel);
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("time_add"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("time_edit"));
                }
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
            }
        }
        else
        {
            try
            {
                results = d.getClientById(opts[1]);

                if (results.next())
                {
                    profile.append(results.getString("id"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("name"));
                    profile.append(UNIT_SEPARATOR);

                    String guid = results.getString("guid");
                    if (guid.length() > 8)
                    {
                        guid = guid.substring(guid.length() - 8);
                    }
                    profile.append(guid);
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("connections"));
                    profile.append(UNIT_SEPARATOR);

                    String level = results.getString("group_bits");
                    int intLevel = parseLevel(level);
                    level = getLevelTitle(intLevel);
                    profile.append(level);
                    profile.append(UNIT_SEPARATOR);
                    profile.append(intLevel);
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("time_add"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("time_edit"));
                }
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
            }
        }

        //System.out.println("results inside getprofile method: " + profile.toString());
        return profile.toString();
    }

    // opts: getdataid:<cid>:<short_guid>
    // OR
    // opts: getdataid:<guid>
    private String cmdGetDataId(String[] opts)
    {
        ResultSet results = null;
        String dataid = "";
        ArrayList<Client> c = cod.getPlayerList();

        try
        {
            if (opts.length == 2) //full guid search
            {
                results = d.getClient(opts[1]);
            }
            else if (opts.length == 3) //short guid search with a client id to match
            {
                for (int i = 0; i < c.size(); i++)
                {
                    Client client = c.get(i);

                    if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2]))
                    {
                        results = d.getClient(client.getGuid());
                    }
                }
                
                //System.out.println("results: " + results);
                if (results == null)
                    return "Client disconnected.";

            }
            else
            {
                return "Invalid getdataid parameters";
            }

            if (results.next());
            else
            {
                return "none";
            }

            dataid = results.getString("id");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return "Error: " + e.getMessage() + "\n" + e.getStackTrace();
        }
        catch (NullPointerException e)
        {
            return "none";
        }

        return dataid;
    }

    // opts: search:<type>:<data>
    // <type>: name, guid, @id
    public String cmdSearch(String[] opts)
    {
        ResultSet results = null;
        String str = "";

        try
        {
            if (opts.length != 3)
            {
                return "Invalid search parameters";
            }

            if (opts[2].length() < 2 && !opts[1].toLowerCase().equals("@id"))
            {
                return "ERROR: Enter more than one character to search for";
            }

            if (opts[1].equals("name"))
            {
                // the search will support partial name searches
                results = d.searchByName(opts[2]);
                StringBuilder string = new StringBuilder(150);

                while (results.next())
                {
                    string.append(results.getString("id"));
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("name"));
                    string.append(UNIT_SEPARATOR);

                    if (fullDetails)
                    {
                        string.append(results.getString("guid"));
                    }
                    else
                    {
                        String guid = results.getString("guid");
                        if (guid.length() > 8)
                        {
                            guid = guid.substring(guid.length() - 8);
                        }
                        string.append(guid);
                    }
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("connections"));
                    string.append(UNIT_SEPARATOR);

                    String level = results.getString("group_bits");
                    int intLevel = parseLevel(level);
                    level = getLevelTitle(intLevel);
                    string.append(level);
                    string.append(UNIT_SEPARATOR);
                    string.append(intLevel);
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("time_add"));
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("time_edit"));

                    if (!results.isLast())
                    {
                        string.append("\n");
                    }
                }

                str = string.toString();

            }
            else if (opts[1].equals("guid"))
            {
                /* the search will support partial guid searches
                 * this means that a valid search parameter for a guid COULD be "abc"
                 */
                results = d.searchByGuid(opts[2]);
                StringBuilder string = new StringBuilder(150);

                while (results.next())
                {
                    string.append(results.getString("id"));
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("name"));
                    string.append(UNIT_SEPARATOR);

                    if (fullDetails)
                    {
                        string.append(results.getString("guid"));
                    }
                    else
                    {
                        String guid = results.getString("guid");
                        if (guid.length() > 8)
                        {
                            guid = guid.substring(guid.length() - 8);
                        }
                        string.append(guid);
                    }
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("connections"));
                    string.append(UNIT_SEPARATOR);

                    String level = results.getString("group_bits");
                    int intLevel = parseLevel(level);
                    level = getLevelTitle(intLevel);
                    string.append(level);
                    string.append(UNIT_SEPARATOR);
                    string.append(intLevel);
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("time_add"));
                    string.append(UNIT_SEPARATOR);

                    string.append(results.getString("time_edit"));

                    if (!results.isLast())
                    {
                        string.append("\n");
                    }
                }

                str = string.toString();

            }
            else if (opts[1].equals("@id"))
            {
                String[] profile = new String[2];
                profile[0] = "getprofile";
                profile[1] = opts[2];
                str = cmdGetProfile(profile);

            }
            else
            {
                return "ERROR: Unknown search type " + opts[0] + " \n"
                        + "Available types are: name, guid, clientid";
            }
        }
        catch (SQLException e)
        {
            str = "Database ERROR: " + e.getMessage();
        }

        if (str.equals(""))
        {
            str = "none";
        }

        return str;
    }

    // opts: getclient:<@id>
    public String cmdGetClient(String[] opts)
    {
        clientMask = false;
        ResultSet clientResults = null;
        StringBuilder str = new StringBuilder();

        if (opts.length != 2)
        {
            return "Invalid getclient parameters";
        }

        try
        {
            Integer.parseInt(opts[1]);
            clientResults = d.getClientById(opts[1]);

            if (clientResults.next());
            else
            {
                return "none";
            }

            clientLevel = parseLevel(clientResults.getString("group_bits"));
            if (!clientResults.getString("mask_level").equals("0"))
            {
                clientMask = true;
            }

            if (clientMask && level < 90) // mask info
            {
                str.append(clientResults.getString("id"));
                str.append("\t");

                String guid = clientResults.getString("guid");
                str.append(guid.substring(guid.length() - 8));
                str.append("\t");

                str.append(clientResults.getString("name"));
                str.append("\t");
                str.append(clientResults.getString("connections"));
                str.append("\t");
                str.append(clientResults.getString("time_add"));
                str.append("\t");
                str.append(clientResults.getString("time_edit"));
                str.append("\t");
            }
            else if (!clientMask && level < 90) // no mask
            {

            }
            else if (level >= 90) // ignore mask
            {
                str.append(clientResults.getString("id"));
                str.append("\t");

                if (fullDetails)
                {
                    str.append(clientResults.getString("guid"));
                    str.append("\t");
                }
                else
                {
                    String guid = clientResults.getString("guid");
                    str.append(guid.substring(guid.length() - 8));
                    str.append("\t");
                }

                str.append(clientResults.getString("name"));
                str.append("\t");
                str.append(clientResults.getString("connections"));
                str.append("\t");
                str.append(clientResults.getString("time_add"));
                str.append("\t");
                str.append(clientResults.getString("time_edit"));
                str.append("\t");

            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
        catch (NullPointerException e)
        {
            return "Error: Database error";
        }
        catch (NumberFormatException e)
        {
            return "Your program supplied '" + opts[1] + "' as a number. Could not parse as a number.";
        }

        if (str.toString().isEmpty())
        {
            return "none";
        }

        return str.toString();
    }

    // opts: aliases:<@id>
    private String cmdAliases(String[] opts)
    {
        ResultSet results = null, clientResults = null;
        StringBuilder str = new StringBuilder(6000);

        if (opts.length != 2)
        {
            return "Invalid aliases parameters";
        }

        if (!clientid.equals(opts[1]))
        {
            cmdGetClient(opts);
        }

        try
        {
            Integer.parseInt(opts[1]);

            clientResults = d.getClientById(opts[1]);
            if (clientResults.next());
            else
            {
                return "none";
            }

            if (!clientResults.getString("mask_level").equals("0") && level < 90)
            {
                return "none";
            }

            results = d.getAliases(opts[1]);

            while (results.next())
            {
                str.append(results.getString("alias"));
                str.append("\t");
                str.append(results.getString("time_add"));
                str.append("\t");
                str.append(results.getString("time_edit"));
                str.append("\n");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
        catch (NullPointerException e)
        {
            return "Error: Database error";
        }
        catch (NumberFormatException e)
        {
            return "Your program supplied '" + opts[1] + "' as a number. Could not parse as a number.";
        }

        if (str.toString().isEmpty())
        {
            return "none";
        }

        return str.toString();
    }

    // changepassword:<old password hash>:<new plaintext password>
    private String cmdChangePassword(String[] opts)
    {
        if (opts.length != 3)
        {
            return "Invalid parameters for command 'changepassword'";
        }
        String str = "";

        //save a database call by checking to see if the new password meets reqs first
        String newPass = opts[2];
        int min = 10;
        int digit = 0; //need 1
        int needDigit = 1;
        int special = 0; //need 1
        int needSpecial = 1;
        int upCount = 0; //need 1
        int needUpper = 1;
        int lowCount = 0; //need 1
        int needLower = 1;

        if (newPass.length() >= min)
        {
            for (int i = 0; i < newPass.length(); i++)
            {
                char c = newPass.charAt(i);
                if (Character.isUpperCase(c))
                {
                    upCount++;
                }
                if (Character.isLowerCase(c))
                {
                    lowCount++;
                }
                if (Character.isDigit(c))
                {
                    digit++;
                }
                if (c >= 33 && c <= 46 || c == 64)
                {
                    special++;
                }
            }
            if (special >= needSpecial && lowCount >= needLower && upCount >= needUpper && digit >= needDigit)
            {
                //System.out.println("New Password is good");
                //check old password for verification
                try
                {
                    ResultSet user = d.getClientById(adminid);
                    if (user.next())
                    {
                        String oldHash = opts[1];
                        if (oldHash.equals(user.getString("password")))
                        {
                            //update password here since the current password matches
                            d.updatePassword(adminid, Function.getMD5(newPass));
                            str = "success";
                        }
                        else
                        {
                            str = "You entered your current password incorrectly";
                        }
                    }
                }
                catch (SQLException ex)
                {
                    str = "Failed to change password.";
                    Logger.getLogger(UserProtocol.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                str = "Your password must contain at least " + min + " characters: "
                        + needDigit + " number, " + needSpecial + " special character, "
                        + needUpper + " one uppercase, and " + needLower + " lower case letter.";
            }
        }
        else
        {
            str = "Your password must contain at least " + min + " characters: "
                    + needDigit + " number, " + needSpecial + " special character, "
                    + needUpper + " one uppercase, and " + needLower + " lower case letter.";
        }

        return str;
    }

    // penalties:<@id>
    private String cmdGetPenalties(String[] opts)
    {
        ResultSet results = null, clientResults = null;
        StringBuilder str = new StringBuilder(5000);

        if (opts.length != 2)
        {
            return "Invalid penalties parameters";
        }

        if (!clientid.equals(opts[1]))
        {
            cmdGetClient(opts);
        }

        try
        {
            Integer.parseInt(opts[1]);

            results = d.getPenalties(opts[1]);

            while (results.next())
            {
                str.append(results.getString("id"));
                str.append("\t");
                str.append(results.getString("type"));
                str.append("\t");
                str.append(results.getString("duration"));
                str.append("\t");
                str.append(results.getString("inactive"));
                str.append("\t");
                str.append(results.getString("reason"));
                str.append("\t");
                str.append(results.getString("data"));
                str.append("\t");
                str.append(results.getString("time_add"));
                str.append("\t");
                str.append(results.getString("time_expire"));
                str.append("\n");
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
        catch (NumberFormatException e)
        {
            return "Your program supplied '" + opts[1] + "' as a number. Could not parse as a number.";
        }

        if (str.toString().isEmpty())
        {
            return "none";
        }

        return str.toString();
    }

    private boolean levelVsClient(String guid)
    {
        d.getDetails(guid);
        ArrayList<String> results = new ArrayList<String>(d.getResults());

        try
        {
            if (level > parseLevel(results.get(1)))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (IndexOutOfBoundsException e)
        { // cause: guid not in database. assume they are level 0
            System.out.println("Not in database");
            return true;
        }
    }

    private String getLevelTitle(int l)
    {
        String str = "";

        switch (l)
        {
            case 100:
                str = "Leader";
                break;
            case 90:
                str = "Executive Admin";
                break;
            case 80:
                str = "Senior Admin";
                break;
            case 69:
                str = "Assistant Senior Staff";
                break;
            case 60:
                str = "Full Admin";
                break;
            case 40:
                str = "Admin";
                break;
            case 20:
                str = "Moderator";
                break;
            case 15:
                str = "VIP";
                break;
            case 10:
                str = "Recruit";
                break;
            case 2:
                str = "Regular";
                break;
            case 1:
                str = "User";
                break;
            default:
                str = "Guest";
                break;
        }

        return str;
    }

    private int parseLevel(String groupBits)
    {
        int l = 0;

        try
        {

            switch (Integer.parseInt(groupBits))
            {
                case 2048: // leaders
                    l = 100;
                    break;
                case 1024: // executive admin
                    l = 90;
                    break;
                case 512: // senior admin
                    l = 80;
                    break;
                case 256: // asst senior staff
                    l = 69;
                    break;
                case 128: // full admin
                    l = 60;
                    break;
                case 64: // admin
                    l = 40;
                    break;
                case 32: // moderator
                    l = 20;
                    break;
                case 16: // these are levels that are below moderators (mods are groupbits = 32). below mod should not be able to login
                    l = 15;
                    break;
                case 8:
                    l = 10;
                    break;
                case 2:
                    l = 2;
                    break;
                case 1:
                    l = 1;
                    break;
                default:
                    l = 0;
                    break;
            }

        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }

        //System.out.println("level: " + l);
        return l;
    }

    public void populateLevels()
    {
        ResultSet results;
        levels = new LinkedHashMap<>(12);

        try
        {
            results = d.getGroups();

            while (results.next())
            {
                B3Level b3l = new B3Level(results.getString("id"),
                        results.getString("name"),
                        results.getString("keyword"),
                        results.getString("level"));

                levels.put(b3l.getGroupbits(), b3l);
            }
        }
        catch (SQLException e)
        {
            //default b3 groups
            levels.put("0", new B3Level("0", "Guest", "guest", "0"));
            levels.put("1", new B3Level("1", "User", "user", "1"));
            levels.put("2", new B3Level("2", "Regular", "reg", "2"));
            levels.put("8", new B3Level("8", "Moderator", "mod", "20"));
            levels.put("16", new B3Level("16", "Admin", "admin", "40"));
            levels.put("32", new B3Level("32", "Full Admin", "fulladmin", "60"));
            levels.put("64", new B3Level("64", "Senior Admin", "senioradmin", "80"));
            levels.put("128", new B3Level("128", "Super Admin", "superadmin", "100"));
        }
    }
}
