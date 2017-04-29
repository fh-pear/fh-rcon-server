import java.util.ArrayList;
import javax.naming.ConfigurationException;

public class FHRcon
{
   public static void main(String[] args) throws InterruptedException
   {
      codServer ffa;
		
		try
		{
			Config.init("config.properties");
		}
		catch (ConfigurationException e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (NumberFormatException e)
		{
			System.out.println(e.getMessage());
			System.out.println("Could not parse argument to a number format. Please check your config");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Could not read config file.");
			System.exit(1);
		}
			
   		
      ffa = new codServer("Forgotten Heroes | FFA", Config.getServerHost(), 
			Config.getServerPort(), Config.getRconPassword(), Config.getListenPort());
      
		Thread ffa_thread = new Thread(ffa);
      ffa_thread.start();
   	
      Thread.sleep(50);
   		
      System.out.println(ffa.getMap());
   	
   }
}
