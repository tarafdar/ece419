import java.io.*;
import java.net.*;

public class ClientSocketConnection extends Thread {
    private Mazewar mazewar;
    private String hostname;
    private int port;
    private String playerName;
    private int playerID;
    
    public ClientSocketConnection(Mazewar mazewar, String hostname, int port) throws IOException, ClassNotFoundException{
        this.mazewar = mazewar;   
        this.hostname = hostname;
        this.port = port;
        playerName = mazewar.name;
    }	
    
    
    
    
    
    public void run() {

		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
        boolean listening = true;
		try {
			/* variables for hostname/port */
			
			socket = new Socket(hostname, port);

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
            synchronized(mazewar.socketList) {
                mazewar.socketList.add(socket);
            }
            synchronized(mazewar.outStreamList) {
                mazewar.outStreamList.add(out);
            }
            synchronized(mazewar.inStreamList) {
                mazewar.inStreamList.add(in);
            }

			mazeWarPacket packetToServer = new mazeWarPacket();
			packetToServer.clientName = playerName;
			out.writeObject(packetToServer);
			mazeWarPacket packetFromServer;
			packetFromServer = (mazeWarPacket) in.readObject();
            synchronized(mazewar.clientInfo){
                mazewar.clientInfo.add(packetFromServer.clientName);
                playerID = mazewar.clientInfo.size() - 1;
                System.out.println("we currently have " + mazewar.clientInfo.size() + " clients in the game and just added " + packetFromServer.clientName);
                //new EventSender(mazewar, out, in, playerID).start();
            }
			/* print server reply */
            new EventListener(mazewar, in, out, playerID).start();

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection. in Client Sender Thread");
			System.exit(1);
		} catch (ClassNotFoundException e){
			System.err.println("ERROR: Class not found");
			System.exit(1);



        }
        
        

		//while ((userInput = stdIn.readLine()) != null
//				&& userInput.toLowerCase().indexOf("bye") == -1) {
			/* make a new request packet */
        
            

			/* re-print console prompt */
		//}
//
//		/* tell server that i'm quitting */
//		EchoPacket packetToServer = new EchoPacket();
//		packetToServer.type = EchoPacket.ECHO_BYE;
//		packetToServer.message = "Bye!";
//		out.writeObject(packetToServer);
//
//		out.close();
//		in.close();
//		stdIn.close();
//		echoSocket.close();
	}
}
