
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class Database {

    private String url = Config.getDatabaseUrl();
    private String user = Config.getDatabaseUser();
    private String passwd = Config.getDatabasePassword();
    private ArrayList<String> results = new ArrayList<String>();

    Connection con = null;
    PreparedStatement guidStatement = null;
    PreparedStatement guidSearchStatement = null;
    PreparedStatement idStatement = null;
    PreparedStatement queryStatement = null;
    PreparedStatement aliasStatement = null;
    PreparedStatement penaltyStatement = null;
    PreparedStatement banStatement = null;
    PreparedStatement tempbanStatement = null;
    PreparedStatement kickStatement = null;
    PreparedStatement updatePasswordStatement = null;
    PreparedStatement nameSearchStatement = null;

    ResultSet rs = null;

    public Database() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, user, passwd);

            String queryGuid = "SELECT * FROM `clients` WHERE guid=? LIMIT 0,1";
            guidStatement = con.prepareStatement(queryGuid);

            String guid = "SELECT * FROM `clients` WHERE `guid` LIKE ?";
            guidSearchStatement = con.prepareStatement(guid);

            String queryId = "SELECT * FROM `clients` WHERE id=? LIMIT 0,1";
            idStatement = con.prepareStatement(queryId);

            String queryAlias = "SELECT * FROM `aliases` WHERE client_id=? ORDER BY `num_used` DESC";
            aliasStatement = con.prepareStatement(queryAlias);

            String queryLevel = "SELECT group_bits,id,mask_level FROM `clients` WHERE guid=? LIMIT 0,1";
            queryStatement = con.prepareStatement(queryLevel);

            String queryPenalties = "SELECT * FROM  `penalties` WHERE `client_id`=? ORDER BY `time_add` DESC";
            penaltyStatement = con.prepareStatement(queryPenalties);

            String banString = "INSERT INTO `penalties` (`id`, `type`, `client_id`, `admin_id`, `duration`, `inactive`, `keyword`, `reason`, `data`, `time_add`, `time_edit`, `time_expire`) VALUES (NULL, 'Ban', ?, ?, '0', '0', 'rcon', ?, '', ?, ?, '-1')";
            banStatement = con.prepareStatement(banString);
            
            String tempbanString = "INSERT INTO `penalties` (`id`, `type`, `client_id`, `admin_id`, `duration`, `inactive`, `keyword`, `reason`, `data`, `time_add`, `time_edit`, `time_expire`) VALUES (NULL, 'TempBan', ?, ?, '?', '0', 'rcon', ?, '', ?, ?, '?')";
            tempbanStatement = con.prepareStatement(tempbanString);
            
            String kickString = "INSERT INTO `penalties` (`id`, `type`, `client_id`, `admin_id`, `duration`, `inactive`, `keyword`, `reason`, `data`, `time_add`, `time_edit`, `time_expire`) VALUES (NULL, 'Kick', ?, ?, '0', '0', 'rcon', ?, '', ?, ?, '-1')";
            kickStatement = con.prepareStatement(kickString);

            String updatePassword = "UPDATE `clients` SET `password`=? WHERE `clients`.`id`=?";
            updatePasswordStatement = con.prepareStatement(updatePassword);

            String name = "SELECT * FROM `clients` WHERE `name` LIKE ?";
            nameSearchStatement = con.prepareStatement(name);
        } catch (SQLException e) {
            System.out.println("SQLException occurred");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Gracefully close out all of the PreparedStatements and the database
     * connection
     */
    public void close() {
        try {
            /*close out the prepared statements*/
            if (guidStatement != null) {
                guidStatement.close();
            }
            if (idStatement != null) {
                idStatement.close();
            }
            if (queryStatement != null) {
                queryStatement.close();
            }
            if (aliasStatement != null) {
                aliasStatement.close();
            }
            if (penaltyStatement != null) {
                penaltyStatement.close();
            }

            if (banStatement != null) {
                banStatement.close();
            }
            
            if (tempbanStatement != null) {
                tempbanStatement.close();
            }

            if (kickStatement != null) {
                kickStatement.close();
            }

            if (updatePasswordStatement != null) {
                updatePasswordStatement.close();
            }

            if (nameSearchStatement != null) {
                nameSearchStatement.close();
            }

            if (rs != null) {
                rs.close();
            }

            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getResults() {
        return results;
    }

    /**
     *
     * @param id database id to be used in obtaining alias rows
     * @return ResultSet of aliases that belong to the key provided in parameter
     * id
     * @throws SQLException
     */
    public ResultSet getAliases(String id) throws SQLException {
        ResultSet aliasResults = null;

        aliasStatement.setString(1, id);
        aliasResults = aliasStatement.executeQuery();

        return aliasResults;
    }

    /**
     *
     * @param guid full GUID of the client that is being searched for
     * @return ResultSet containing the row with the matching guid key
     * @throws SQLException
     */
    public ResultSet getClient(String guid) throws SQLException {
        ResultSet clientResults = null;

        guidStatement.setString(1, guid);
        clientResults = guidStatement.executeQuery();

        return clientResults;
    }

    /**
     *
     * @param id database id to obtain a row against
     * @return ResultSet of the belonging to the supplied id
     * @throws SQLException
     */
    public ResultSet getClientById(String id) throws SQLException {
        ResultSet clientResults = null;

        idStatement.setString(1, id);
        clientResults = idStatement.executeQuery();

        return clientResults;
    }

    /**
     *
     * @param id the database id to query against
     * @return ResultSet containing all penalty rows belonging to parameter id
     * @throws SQLException
     */
    public ResultSet getPenalties(String id) throws SQLException {
        ResultSet penaltyResults = null;

        penaltyStatement.setString(1, id);
        penaltyResults = penaltyStatement.executeQuery();

        return penaltyResults;
    }

    /**
     *
     * @param name full or partial name to search database for clients with
     * @return ResultSet containing the returned rows from the database
     * @throws SQLException
     */
    public ResultSet searchByName(String name) throws SQLException {
        ResultSet results = null;

        nameSearchStatement.setString(1, "%" + name + "%");
        results = nameSearchStatement.executeQuery();

        return results;
    }

    /**
     *
     * @param guid full OR partial GUID to search database for
     * @return ResultSet containing the returned rows from the database
     * @throws SQLException
     */
    public ResultSet searchByGuid(String guid) throws SQLException {
        ResultSet results = null;

        guidSearchStatement.setString(1, "%" + guid + "%");
        results = guidSearchStatement.executeQuery();

        return results;
    }

    /**
     *
     * @param userid the @id in the database to be updated
     * @param newPassword new hashed password value
     * @throws SQLException
     */
    public void updatePassword(String userid, String newPassword) throws SQLException {
        updatePasswordStatement.setString(1, newPassword);
        updatePasswordStatement.setString(2, userid);

        updatePasswordStatement.executeUpdate();
    }

    /**
     *
     * @param clientid the database id to execute query against
     * @param adminid the admin id who is requesting the action
     * @param reason the reason to be recorded in the database
     * @throws SQLException
     */
    public void banClient(String clientid, String adminid, String reason) throws SQLException {
        //sets the client_id
        banStatement.setString(1, clientid);

        //set admin_id
        banStatement.setString(2, adminid);

        //set the reason
        banStatement.setString(3, reason);

        //time of penalty/time edit (since its a new penalty, they'll be the same
        //get unix time
        Date now = new Date();
        long time = now.getTime() / 1000;
        banStatement.setString(4, String.valueOf(time));
        banStatement.setString(5, String.valueOf(time));

        banStatement.executeUpdate();
    }
    
    /**
     * 
     * @param clientid @id of the client the action will be against
     * @param adminid @id of the admin calling the action
     * @param reason admin supplied reason
     * @param duration amount of time, in seconds
     * @throws SQLException 
     */
    public void tempbanClient(String clientid, String adminid, String reason, int duration) throws SQLException {
        tempbanStatement.setString(1, clientid);
        tempbanStatement.setString(2, adminid);
        
        tempbanStatement.setString(3, String.valueOf(duration));
        tempbanStatement.setString(4, reason);
        
        Date now = new Date();
        long time = now.getTime() / 1000;
        tempbanStatement.setString(5, String.valueOf(time));
        tempbanStatement.setString(6, String.valueOf(time));
        
        long expire = time + duration;
        tempbanStatement.setString(7, String.valueOf(expire));
        
        tempbanStatement.executeUpdate();
    }

    /**
     *
     * @param clientid the database id to execute the query against
     * @param adminid the database id of the admin requesting the action
     * @param reason reason for the action, to be recorded in the database
     * @throws SQLException
     */
    public void kickClient(String clientid, String adminid, String reason) throws SQLException {
        kickStatement.setString(1, clientid);
        kickStatement.setString(2, adminid);
        kickStatement.setString(3, reason);

        Date now = new Date();
        long time = now.getTime() / 1000;
        kickStatement.setString(4, String.valueOf(time));
        kickStatement.setString(5, String.valueOf(time));

        kickStatement.executeUpdate();
    }

    public void getDetails(String guid) {

        results.clear();

        try {
            guidStatement.setString(1, guid);
            rs = guidStatement.executeQuery();

            while (rs.next()) {
                results.add(rs.getString("id"));
                //System.out.println("adding: " + rs.getString("id"));
                results.add(rs.getString("group_bits"));
                //System.out.println("adding: " + rs.getString("group_bits"));
                results.add(rs.getString("mask_level"));
                //System.out.println("adding: " + rs.getString("mask_level"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void login(String dbID) {
        results.clear();

        try {
            idStatement.setString(1, dbID);
            rs = idStatement.executeQuery();

            while (rs.next()) {
                results.add(rs.getString("id"));
                results.add(rs.getString("password"));
                results.add(rs.getString("group_bits"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
