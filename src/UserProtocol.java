
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserProtocol {

    public static final String UNIT_SEPARATOR = "\t";

    private final int LOGIN = 0;
    private final int VERIFY_VERSION = 1;
    private final int WAITING = 2;

    private ClientServer cod;
    private int level, clientLevel;
    private boolean fullDetails, clientMask = false;
    private String adminid, clientid = "";

    private int state = LOGIN;

    private Database d;
    private Client clientCache;

    public UserProtocol(ClientServer c) {
        cod = c;
    }

    public String processInput(String theInput) {
        String theOutput = null;

        if (state == LOGIN) {
            if (theInput.equalsIgnoreCase("exit")) {
                theOutput = "exit";
            } else {
                theOutput = processLogin(theInput);
            }
        } else if (state == VERIFY_VERSION) {
            theOutput = processVersion(theInput);
        } else if (state == WAITING) {
            if (theInput.equalsIgnoreCase("exit")) {
                theOutput = "exit";
            } else {
                theOutput = processCommand(theInput);
            }
        } else {
            theOutput = "exit";
        }

        if (theOutput.equals("exit")) {
            d.close();
        }

        return theOutput.concat("\n...");
    }

    private String processLogin(String in) {
        d = new Database();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(UserProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] user = in.split(UNIT_SEPARATOR);
        ArrayList<String> details;

        //System.out.println("arg1: " + user[0]);
        d.login(user[0]);
        details = new ArrayList<String>(d.getResults());

        if (details.isEmpty()) {
            return "exit";
        }
        if (details.get(1) == null) {
            return "exit";
        }

        //System.out.println("input: " + user[1]);
        //System.out.println("datab: " + details.get(1));
        if (details.get(1).equals(user[1])) {
            //System.out.println("correct");
            level = parseLevel(details.get(2));

            if (level > 80) {
                fullDetails = true;
            } else {
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
        } else {
            return "exit";
        }
    }

    private String processVersion(String in) {
        if (in.equals(Version.VERSION)) {
            state = WAITING;
            //System.out.println("logged in, waiting");
            return "Logged in. Waiting for command(s).";
        } else {
            return "You're using version: " + in
                    + "\n You need to update to version: " + Version.VERSION;
        }
    }

    private String processCommand(String in) {
        System.out.println("RECEIVED COMMAND: " + in);
        String str = "";
        String[] s = in.split(UNIT_SEPARATOR);

        if (s[0].equals("status")) {
            str = cmdStatus();
        } else if (s[0].equals("kick")) {
            str = cmdKick(s);
        } else if (s[0].equals("getmap")) {
            str = cmdGetMap();
        } else if (s[0].equals("map")) {
            str = cmdMap(s);
        } else if (s[0].equals("pm")) {
            str = cmdPM(s);
        } else if (s[0].equals("say")) {
            str = cmdSay(s);
        } else if (s[0].equals("aliases")) {
            str = cmdAliases(s);
        } else if (s[0].equals("getdataid")) {
            str = cmdGetDataId(s);
        } else if (s[0].equals("penalties")) {
            str = cmdGetPenalties(s);
        } else if (s[0].equals("getclient")) {
            str = cmdGetClient(s);
        } else if (s[0].equals("ban")) {
            str = cmdBan(s);
        } else if (s[0].equals("tempban")) {
            return "tempban command not implemented at this time";
        } else if (s[0].equals("getprofile")) {
            str = cmdGetProfile(s);
        } else if (s[0].equals("changepassword")) {
            str = cmdChangePassword(s);
        } else {
            str = "Unknown command '" + s[0] + "'";
        }

        //System.out.println("command results: " + str);
        return str;
    }

    private String cmdSay(String[] opts) {
        if (opts.length < 2) {
            return "Invalid parameters for global message.";
        } else if (opts.length == 2) {
            return cod.sendMessage(opts[1]);
        } else {
            String m = "";
            for (int i = 1; i < opts.length; i++) {
                m += opts[i];
            }

            return cod.sendMessage(m);
        }
    }

    private String cmdGetMap() {
        return cod.getMap();
    }

    private String cmdMap(String[] opts) {
        if (opts.length != 2) {
            return "Invalid parameters for map change!";
        }

        return cod.changeMap(opts[1]);
    }

    private String cmdStatus() {
        String str = "";
        ArrayList<Client> c = cod.getPlayerList();

        for (int i = 0; i < c.size(); i++) {
            str = str.concat(c.get(i).toString(fullDetails));
        }

        return str;
    }

    // opts: pm:<cid>:<short_guid>:<message>
    private String cmdPM(String[] opts) {
        if (opts.length != 4) {
            return "Invalid PM parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty()) {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + "'";
        }

        ArrayList<Client> c = cod.getPlayerList();

        for (int i = 0; i < c.size(); i++) {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2])) {
                return cod.sendPM(client.getClientId(), client.getGuid(), opts[3]);
            }
        }

        return "ERROR: Client not found in current player list.";
    }

    // opts: kick:<cid>:<short_guid>:<reason>
    private String cmdKick(String[] opts) {
        if (opts.length != 4) {
            return "Invalid kick parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty()) {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + "'";
        }

        ArrayList<Client> c = cod.getPlayerList();
        String str = "";

        for (int i = 0; i < c.size(); i++) {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2])) {

                if (levelVsClient(client.getGuid())) {
                    //System.out.println("this will kick: " + client.getName());
                    try {
                        String array[] = {"", client.getGuid()};
                        String reason = "(RCon) " + opts[3];

                        d.kickClient(cmdGetDataId(array), adminid, reason);
                        str = "Kick added to database. \n";
                    } catch (SQLException e) {
                        str = "Adding kick to the database failed...";
                    }

                    str += cod.sendClientKick(client.getClientId(), client.getGuid(), client.getName(), opts[3]);
                    return str;
                } else {
                    return client.getName() + " is a higher/equal level admin.";
                }
            }
        }

        return "ERROR: Client not found in current player list.";
    }

    // opts: tempban:<cid>:<short_guid>:<duration>:<reason>
    private String cmdTempBan(String[] opts) {
        if (opts.length != 5) {
            return "Invalid tempban parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty() || opts[4].isEmpty()) {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + UNIT_SEPARATOR + opts[4] + "'";
        }

        ArrayList<Client> c = cod.getPlayerList();

        for (int i = 0; i < c.size(); i++) {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2])) {

                if (levelVsClient(client.getGuid())) {
                    return cod.sendTempBan(client.getClientId(), client.getGuid(), opts[3], opts[4]);
                } else {
                    return client.getName() + " is a higher/equal level admin.";
                }
            }
        }

        return "ERROR: Client not found in current player list.";
    }

    // opts: ban:<cid>:<short_guid>:<reason>
    private String cmdBan(String[] opts) {
        if (opts.length != 4) {
            return "Invalid ban parameters.";
        }

        if (opts[1].isEmpty() || opts[2].isEmpty() || opts[3].isEmpty()) {
            return "Invalid parameters - '" + opts[0] + UNIT_SEPARATOR + opts[1] + UNIT_SEPARATOR
                    + opts[2] + UNIT_SEPARATOR + opts[3] + "'";
        }

        ArrayList<Client> c = cod.getPlayerList();
        String str = "";

        for (int i = 0; i < c.size(); i++) {
            Client client = c.get(i);

            if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2])) {

                if (levelVsClient(client.getGuid())) {
                    try {
                        String array[] = {"", client.getGuid()};
                        String reason = "(RCon) " + opts[3];

                        d.banClient(cmdGetDataId(array), adminid, reason);
                        str = "Ban added to database.\n ";
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        str = "Adding ban to database failed...";
                    }

                    str += cod.sendBan(client.getClientId(), client.getGuid(), opts[3]);
                    return str;
                } else {
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
    private String cmdGetProfile(String opts[]) {
        StringBuilder profile = new StringBuilder(150);
        ResultSet results = null;

        if (opts.length != 2) {
            return "Invalid paramenters for command 'getprofile'";
        }

        if (opts[1].equals("self")) {
            try {
                results = d.getClientById(adminid);

                if (results.next()) {
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
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else if (fullDetails) {
            try {
                results = d.getClientById(opts[1]);

                if (results.next()) {
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
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            try {
                results = d.getClientById(opts[1]);

                if (results.next()) {
                    profile.append(results.getString("id"));
                    profile.append(UNIT_SEPARATOR);

                    profile.append(results.getString("name"));
                    profile.append(UNIT_SEPARATOR);

                    String guid = results.getString("guid");
                    if (guid.length() > 8) {
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
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        //System.out.println("results inside getprofile method: " + profile.toString());
        return profile.toString();
    }

    // opts: getdataid:<cid>:<short_guid>
    // OR
    // opts: getdataid:<guid>
    private String cmdGetDataId(String[] opts) {
        ResultSet results = null;
        String dataid = "";
        ArrayList<Client> c = cod.getPlayerList();

        try {
            if (opts.length == 2) //full guid search
            {
                results = d.getClient(opts[1]);
            } else if (opts.length == 3) //short guid search with a client id to match
            {
                for (int i = 0; i < c.size(); i++) {
                    Client client = c.get(i);

                    if (client.getClientId().equals(opts[1]) && client.getShortGuid().equals(opts[2])) {
                        results = d.getClient(client.getGuid());
                    }
                }

            } else {
                return "Invalid getdataid parameters";
            }

            if (results.next()); else {
                return "none";
            }

            dataid = results.getString("id");
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage() + "\n" + e.getStackTrace();
        } catch (NullPointerException e) {
            return "none";
        }

        return dataid;
    }

    // opts: search:<type>:<data>
    // <type>: name, guid, clientid, aliases
    public String cmdSearch(String[] opts) {
        ResultSet results = null;
        String str = "";

        if (opts.length != 3) {
            return "Invalid search parameters";
        }

        if (opts[0].equals("name")) {

        } else if (opts[0].equals("guid")) {

        } else if (opts[0].equals("clientid")) {
            String[] profile = new String[2];
            profile[0] = "getprofile";
            profile[1] = opts[2];
            str = cmdGetProfile(profile);

        } else if (opts[0].equals("aliases")) {

        } else {
            return "ERROR: Unknown search type " + opts[0] + " \n"
                    + "Available types are: name, guid, clientid, and aliases";
        }

        return str;
    }

    // opts: getclient:<@id>
    public String cmdGetClient(String[] opts) {
        clientMask = false;
        ResultSet clientResults = null;
        String str = "";

        if (opts.length != 2) {
            return "Invalid getclient parameters";
        }

        try {
            Integer.parseInt(opts[1]);
            clientResults = d.getClientById(opts[1]);

            if (clientResults.next()); else {
                return "none";
            }

            clientLevel = parseLevel(clientResults.getString("group_bits"));
            if (!clientResults.getString("mask_level").equals("0")) {
                clientMask = true;
            }

            if (clientMask && level < 90) // mask info
            {
                str = str + clientResults.getString("id") + "\t";

                String guid = clientResults.getString("guid");
                str = str + guid.substring(guid.length() - 8) + "\t";

                str = str + clientResults.getString("name") + "\t";
                str = str + clientResults.getString("connections") + "\t";
                str = str + clientResults.getString("time_add") + "\t";
                str = str + clientResults.getString("time_edit") + "\t";
            } else if (!clientMask && level < 90) // no mask
            {

            } else if (level >= 90) // ignore mask
            {
                str = str + clientResults.getString("id") + "\t";

                if (fullDetails) {
                    str = str + clientResults.getString("guid") + "\t";
                } else {
                    String guid = clientResults.getString("guid");
                    str = str + guid.substring(guid.length() - 8) + "\t";
                }

                str = str + clientResults.getString("name") + "\t";
                str = str + clientResults.getString("connections") + "\t";
                str = str + clientResults.getString("time_add") + "\t";
                str = str + clientResults.getString("time_edit") + "\t";

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } catch (NullPointerException e) {
            return "Error: Database error";
        } catch (NumberFormatException e) {
            return "Your program supplied '" + opts[1] + "' as a number. Could not parse as a number.";
        }

        if (str.equals(""))
            str = "none";
        
        return str;
    }

    // opts: aliases:<@id>
    private String cmdAliases(String[] opts) {
        ResultSet results = null, clientResults = null;
        String str = "";

        if (opts.length != 2) {
            return "Invalid aliases parameters";
        }

        if (!clientid.equals(opts[1])) {
            cmdGetClient(opts);
        }

        try {
            Integer.parseInt(opts[1]);

            clientResults = d.getClientById(opts[1]);
            if (clientResults.next()); else {
                return "none";
            }

            if (!clientResults.getString("mask_level").equals("0") && level < 90) {
                return "none";
            }

            results = d.getAliases(opts[1]);

            while (results.next()) {
                str = str + results.getString("alias") + "\t";
                str = str + results.getString("time_add") + "\t";
                str = str + results.getString("time_edit") + "\n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } catch (NullPointerException e) {
            return "Error: Database error";
        } catch (NumberFormatException e) {
            return "Your program supplied '" + opts[1] + "' as a number. Could not parse as a number.";
        }

        if (str.equals("")) {
            str = "none";
        }

        return str;
    }

    // changepassword:<old password hash>:<new plaintext password>
    private String cmdChangePassword(String[] opts) {
        if (opts.length != 3) {
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

        if (newPass.length() >= min) {
            for (int i = 0; i < newPass.length(); i++) {
                char c = newPass.charAt(i);
                if (Character.isUpperCase(c)) {
                    upCount++;
                }
                if (Character.isLowerCase(c)) {
                    lowCount++;
                }
                if (Character.isDigit(c)) {
                    digit++;
                }
                if (c >= 33 && c <= 46 || c == 64) {
                    special++;
                }
            }
            if (special >= needSpecial && lowCount >= needLower && upCount >= needUpper && digit >= needDigit) {
                //System.out.println("New Password is good");
                //check old password for verification
                try {
                    ResultSet user = d.getClientById(adminid);
                    if (user.next()) {
                        String oldHash = opts[1];
                        if (oldHash.equals(user.getString("password"))) {
                            //update password here since the current password matches
                            d.updatePassword(adminid, Function.getMD5(newPass));
                            str = "success";
                        } else {
                            str = "You entered your current password incorrectly";
                        }
                    }
                } catch (SQLException ex) {
                    str = "Failed to change password.";
                    Logger.getLogger(UserProtocol.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                str = "Your password must contain at least " + min + " characters: "
                        + needDigit + " number, " + needSpecial + " special character, "
                        + needUpper + " one uppercase, and " + needLower + " lower case letter.";
            }
        } else {
            str = "Your password must contain at least " + min + " characters: "
                    + needDigit + " number, " + needSpecial + " special character, "
                    + needUpper + " one uppercase, and " + needLower + " lower case letter.";
        }

        return str;
    }

    // penalties:<@id>
    private String cmdGetPenalties(String[] opts) {
        ResultSet results = null, clientResults = null;
        String str = "";

        if (opts.length != 2) {
            return "Invalid penalties parameters";
        }

        if (!clientid.equals(opts[1])) {
            cmdGetClient(opts);
        }

        try {
            Integer.parseInt(opts[1]);

            results = d.getPenalties(opts[1]);

            while (results.next()) {
                str = str + results.getString("id") + "\t";
                str = str + results.getString("type") + "\t";
                str = str + results.getString("duration") + "\t";
                str = str + results.getString("inactive") + "\t";
                str = str + results.getString("reason") + "\t";
                str = str + results.getString("data") + "\t";
                str = str + results.getString("time_add") + "\t";
                str = str + results.getString("time_expire") + "\n";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } catch (NumberFormatException e) {
            return "Your program supplied '" + opts[1] + "' as a number. Could not parse as a number.";
        }

        if (str.equals("")) {
            str = "none";
        }

        return str;
    }

    private boolean levelVsClient(String guid) {
        d.getDetails(guid);
        ArrayList<String> results = new ArrayList<String>(d.getResults());

        try {
            if (level > parseLevel(results.get(1))) {
                return true;
            } else {
                return false;
            }
        } catch (IndexOutOfBoundsException e) { // cause: guid not in database. assume they are level 0
            System.out.println("Not in database");
            return true;
        }
    }

    private String getLevelTitle(int l) {
        String str = "";

        switch (l) {
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

    private int parseLevel(String groupBits) {
        int l = 0;

        try {

            switch (Integer.parseInt(groupBits)) {
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

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //System.out.println("level: " + l);
        return l;
    }
}
