import java.util.*;
import java.net.*;
import java.io.*;

public class ffaServer extends Server implements Runnable
{
   private String serverName;
   private int serverPort;
	
   public ffaServer(String svrN, String h, int portN, String rcon)
   {
      super(h, portN, rcon);
      serverName = svrN;
   }
	
   public void run()
   {
      int clientNumber = 0;
		ServerSocket listener = null;
		try {
      	listener = new ServerSocket(serverPort);
      
         while (true) {
            new UserThread(listener.accept(), clientNumber++).start();
         }
      
		}
		catch (IOException e) {
		}
      finally {
			try {
      	   listener.close();
			}
			catch (IOException e) {
			}
      }
		
   }
	
   public String getServerName()
   {
      return serverName;
   }
	
}