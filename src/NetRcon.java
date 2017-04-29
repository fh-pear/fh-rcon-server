import java.io.IOException;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class NetRcon
{
   private InetAddress ipAddress;
	
   private int port;
   private int receiveTimeout;
   private int	sleepTimer;
	
   private boolean returnsData;
	
   private String password;
   private String command;
   private String retStr = "", str = "";
	
	private short counter = 0;
   private DatagramPacket dataPacketOut;
   private DatagramPacket dataPacketIn;
	

   public NetRcon(String ip, int port, String password, boolean returnsData, int receiveTimeout, int sleepTimer)
   {
      this.port = port;
      this.password = password;
      this.returnsData = returnsData;
      this.receiveTimeout = receiveTimeout;
      this.sleepTimer = sleepTimer;
      parseAddress(ip);
   }
	
	
   public void sleeper() throws InterruptedException
   {
      Thread.sleep(sleepTimer);
   }
	

   private void parseAddress(String ip)
   {
	   try {
      ipAddress = InetAddress.getByName(ip);
		}
		catch (IOException e) {
		   System.out.println(e.getMessage());
		}
   		//System.out.println(ipAddress);
   }
		
	
	
   public DatagramPacket buildPacket(String rconCommand) throws IOException
   {
   	// Build the command string to be sent
   	// The leading Xs are place holders for out of bounds bytes that will be converted once we get the java bytes for the string
      command = "xxxxrcon " + password + " " + rconCommand;
   	
   	// Convert the command string to bytes
      byte[] commandBytes = command.getBytes();
   	
   	// Replace the first 4 bytes (those leading Xs) in the commandBytes with the correct bytes
      commandBytes[0] = (byte)0xff;
      commandBytes[1] = (byte)0xff;
      commandBytes[2] = (byte)0xff;
      commandBytes[3] = (byte)0xff;
   
   	
   	// Build the UDP packet that is to be sent
      dataPacketOut = new DatagramPacket(commandBytes, commandBytes.length, ipAddress, port);
   	
      return dataPacketOut;
   }
	
	

   public String sendCommand(String rconCommand)
   {
      try{
      
      	// Create a new DatagramSocket instance
         DatagramSocket dataSocket = new DatagramSocket(null);
      	
      	// Connect the new datagramSocket instance to the provided ipAddress and port
         dataSocket.connect(ipAddress, port);
      	
      	// Set the timeout of the socket connection; TODO: parameterize the timeout value.
         dataSocket.setSoTimeout(receiveTimeout);
      
      	// Send the packet (rcon command) to the server.
         dataSocket.send(buildPacket(rconCommand));
      	
			retStr = "";
         if (returnsData) {
            while (true) {
            // Create a new buffer to receive any response from the rcon command
               byte[] buffer = new byte[4000];
            
            // Create the new datagram packet to house the returned results of the command
               dataPacketIn = new DatagramPacket(buffer,buffer.length);
            
            // Receive the buffer using the datagram socket.
               dataSocket.receive(dataPacketIn);
            
               str = new String(dataPacketIn.getData(), 0, dataPacketIn.getLength());
               retStr += str;
					//System.out.println(str);
            }
         }
         else
         {
            retStr = new String("Command sent on source port: " + dataSocket.getLocalPort());
         }
      
      	
         sleeper();
      	
      }
      catch(IOException ex){
      	// yay, we made it :)
      }
      catch (InterruptedException e) {
         System.out.println(e.getMessage());
      }
		
		counter++;
		if (counter >= 10) {
   		System.gc();
			counter = 0;
		}
   	// Return the results
      return retStr;
   
   }
	
	public void setReturnData(boolean cond)
	{
	   returnsData = cond;
	}
	
	public void setTimeout(int time)
	{
		receiveTimeout = time;
	}
}