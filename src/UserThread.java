
import java.net.*;
import java.io.*;

public class UserThread extends Thread {

    private Socket socket = null;
    private codServer c = null;

    public UserThread(codServer cod, Socket socket, int i) {
        super(cod.getServerName() + ": \t User Thread: " + i);
        this.socket = socket;
        c = cod;
    }

    public void run() {

        //System.out.println("Client connection received.");
        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));) {
            String inputLine, outputLine;
            UserProtocol p = new UserProtocol(new ClientServer(c.getHost(), c.getPort(), c.getPassword()));
            outputLine = p.processInput(in.readLine());
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = p.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("exit")) {
                    break;
                }
            }
            socket.close();
            //System.out.println("user logged out");
        } catch (SocketException e) {
            //user logged out
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.gc();
    }
}
