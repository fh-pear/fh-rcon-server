import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class Rcon
{
   private String hostname, rconPass;
   private String rconReply = "\uFFFD\uFFFD\uFFFD\uFFFDprint\n";
   private byte rconHeader = (byte)0xFF;
   private int port;
	
   List<Client> clientList = new ArrayList<Client>();
   private List<Client> clientBuffer = new ArrayList<Client>();
	
   Rcon(String host, int pn, String pass)
   {
      hostname = host;
      port = pn;
      rconPass = pass;
   	 
      getStatus();
   }
	
   public void sendCommand(String str)
   {
      sendPacket(str);
   }
	
   private String sendPacket(String command) 
   {
      StringBuilder reply = new StringBuilder();
   	
      try
      {
         DatagramSocket socket = new DatagramSocket();
      
         String payload = "xxxx" + "rcon " + rconPass + " " + command;
            
      		// prepare request
         byte[] buf = payload.getBytes();
         buf[0] = rconHeader;
         buf[1] = rconHeader;
         buf[2] = rconHeader;
         buf[3] = rconHeader;
         InetAddress address = InetAddress.getByName(hostname);
         DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
      
            // prepare response
         socket.setSoTimeout(200);
         socket.setReceiveBufferSize(2000);
         Thread.sleep(100);
      	
         int i = 1;
         socket.send(packet);
         
         while (true)
         {
            byte[] buf_recv = new byte[13330];
         	
            try
            {
               System.out.println("Pass " + i);
               packet = new DatagramPacket(buf_recv, buf_recv.length);
               socket.receive(packet);
            }
            catch (SocketTimeoutException e) { // This should happen when the gameserver is done sending packets
						break;
            }
         	
         	
            String rec = new String(packet.getData());
         	//System.out.println(rec);
            i++;
            
            reply.append(rec);
         
               //System.out.println(reply);
            	//System.out.println(rec);
            
         }
      }
      catch (SocketTimeoutException e) { // This should happen when the gameserver is done sending packets
         System.out.println("Timeout occured");
         if ( reply.length() == 0 )
            return "Error connecting to server";
      }
      catch (SocketException e) {
         System.out.println(e);
      }
      catch (UnknownHostException e) {
         System.out.println(e);
      }
      catch (IOException e) {
         System.out.println(e);
      }
      catch (InterruptedException e) {
         System.out.println(e);
      }
   
      String str = reply.toString();
      str = str.replaceAll("\u0000", "");
      str = str.replaceAll("\uFFFD", "");
      str = str.replaceAll("print\\n", "");
   	
      return str;
   }
	
   private void getStatus()
   {
      clientBuffer = new ArrayList<Client>();
      String reply = sendPacket("status");
   	
      //System.out.println(reply);
      String[] split = reply.split("\n");
   	//System.out.println(split[3]);
   	
      for ( int i = 3; i < split.length; i++)
      {
         //clientBuffer.add(new Client(split[i], new Rcon() ));
      }
   	
		/*
      if (!clientBuffer.isEmpty())
      {
         clientList.clear();
         for ( int i = 0; i < clientBuffer.size(); i++)
         {
            Client c = clientBuffer.get(i);
            String n = c.getName();
            String eN = c.getExactName();
            String id = c.getClientId();
            String g = c.getGuid();
         	
            clientList.add(new Client(id, n, eN, g, new Rcon() ));
         }
      }
   	*/	
   }
	
   public List<Client> getClientList()
   {
      return clientList;
   }
}