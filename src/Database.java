import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class Database
{
   private String url = Config.getDatabaseUrl();
   private String user = Config.getDatabaseUser();
   private String passwd = Config.getDatabasePassword();
   private ArrayList<String> results = new ArrayList<String>();

   Connection con = null;
   PreparedStatement guidStatement = null;
   PreparedStatement idStatement = null;
   PreparedStatement queryStatement = null;
   PreparedStatement aliasStatement = null;
	PreparedStatement penaltyStatement = null;
   PreparedStatement banStatement = null;
	PreparedStatement kickStatement = null;
   
   ResultSet rs = null;

   public Database() {
      try {
         Class.forName("com.mysql.jdbc.Driver");
         con = DriverManager.getConnection(url, user, passwd);
      	
         String queryGuid = "SELECT * FROM `clients` WHERE guid=? LIMIT 0,1";
         guidStatement = con.prepareStatement(queryGuid);
      	
         String queryId = "SELECT * FROM `clients` WHERE id=? LIMIT 0,1";
         idStatement = con.prepareStatement(queryId);
      	
         String queryAlias = "SELECT * FROM `aliases` WHERE client_id=? ORDER BY `num_used` DESC";
         aliasStatement = con.prepareStatement(queryAlias);
      	
         String queryLevel = "SELECT group_bits,id,mask_level FROM `clients` WHERE guid=? LIMIT 0,1";
         queryStatement = con.prepareStatement(queryLevel);
			
			String queryPenalties = "SELECT * FROM  `penalties` WHERE `client_id`=? ORDER BY `time_add` DESC";
			penaltyStatement = con.prepareStatement(queryPenalties);
      	
         String banString = "INSERT INTO `penalties` (`id`, `type`, `client_id`, `admin_id`, `duration`, `inactive`, `keyword`, `reason`, `data`, `time_add`, `time_edit`, `time_expire`) VALUES (NULL, 'Ban', ?, ?, '0', '0', '', ?, '', ?, ?, '-1')";
         banStatement = con.prepareStatement(banString);
			
			String kickString = "INSERT INTO `penalties` (`id`, `type`, `client_id`, `admin_id`, `duration`, `inactive`, `keyword`, `reason`, `data`, `time_add`, `time_edit`, `time_expire`) VALUES (NULL, 'Kick', ?, ?, '0', '0', '', ?, '', ?, ?, '-1')";
			kickStatement = con.prepareStatement(kickString);
      }
      catch(SQLException e) {
         System.out.println(e.getMessage());
      } 
      catch (ClassNotFoundException e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }
   
   }
	
   public void close()
   {
      try {
		/*close out the prepared statements*/
         if (guidStatement != null)
            guidStatement.close();      	
         if (idStatement != null)
            idStatement.close();				
			if (queryStatement != null)
				queryStatement.close();
			if (aliasStatement != null)
      		aliasStatement.close();
			if (penaltyStatement != null)
				penaltyStatement.close();
				
         if (rs != null)
            rs.close();
      		
         if (con != null)
            con.close();
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
   }
	
   public ArrayList<String> getResults()
   {		
      return results;
   }
	
   public ResultSet getAliases(String cid) throws SQLException
   {
      ResultSet aliasResults = null;
   
      aliasStatement.setString(1, cid);
      	
      aliasResults = aliasStatement.executeQuery();
   		/*
   		while (rs.next()) { 
   			aliasResults.add(rs.getString("alias"));
   			aliasResults.add(rs.getString("time_add"));
   			aliasResults.add(rs.getString("time_edit"));
   		}*/
   	
      return aliasResults;
   }
	
   public ResultSet getClient(String guid) throws SQLException
   {
      ResultSet clientResults = null;
   	
      guidStatement.setString(1, guid);
      clientResults = guidStatement.executeQuery();
   		
      return clientResults;
   }
	
   public ResultSet getClientById(String id) throws SQLException
   {
      ResultSet clientResults = null;
   	
      idStatement.setString(1, id);
      clientResults = idStatement.executeQuery();
   		
      return clientResults;
   }
	
	public ResultSet getPenalties(String id) throws SQLException
	{
		ResultSet penaltyResults = null;
		
		penaltyStatement.setString(1, id);
		penaltyResults = penaltyStatement.executeQuery();
		
		return penaltyResults;
	}
	
	/*
	 * SQL query build order
	 * client_id, admin_id, reason, time_add, time_edit
	 *
	 * id will be filled automatic
	 */
	public void banClient(String clientid, String adminid, String reason) throws SQLException
	{
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
	
	public void kickClient(String clientid, String adminid, String reason) throws SQLException
	{
		kickStatement.setString(1, clientid);
		kickStatement.setString(2, adminid);
		kickStatement.setString(3, reason);
		
		Date now = new Date();
		long time = now.getTime() / 1000;
		kickStatement.setString(4, String.valueOf(time));
		kickStatement.setString(5, String.valueOf(time));
		
		kickStatement.executeUpdate();
	}

   public void getDetails(String guid)
   {
      
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
      } 
      catch(SQLException e) {
         System.out.println(e.getMessage());
      } 
   }
	
   public void login(String dbID)
   {
      results.clear();  
         
      try {
         idStatement.setString(1, dbID);
         rs = idStatement.executeQuery();
      	
         while (rs.next()) {       
            results.add(rs.getString("id"));
            results.add(rs.getString("password"));
            results.add(rs.getString("group_bits"));
         }
      } 
      catch(SQLException e) {
         System.out.println(e.getMessage());
         e.printStackTrace();
      } 
   }
}