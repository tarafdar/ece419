import java.net.*;
import java.io.*;
import java.util.Random;
import static java.lang.System.*;

public class NameServerHandlerThread extends Thread {
    private NameServer server;
    private Socket socket;
	
    public NameServerHandlerThread(NameServer server, Socket socket) {
		super("NameServerHandlerThread");
		this.server = server;
        this.socket = socket;
	}

	public void run() {

		boolean done = false;
		int i;
		try {
			/* stream to read from client */
		    ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			mazeWarPacket packetFromClient;
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			mazeWarPacket packetToClient = new mazeWarPacket();
			packetFromClient = (mazeWarPacket) fromClient.readObject();
           
            System.out.println("in nameserver"); 
            synchronized (server) {
                server.hostnames.add(packetFromClient.hostname.get(0));     
                server.ports.add(packetFromClient.port.get(0));
                System.out.println("Recieved connection from "+ packetFromClient.hostname.get(0) + " who is listening on port " + packetFromClient.port.get(0));
                System.out.println("Returning connections with numplayers = " + server.numPlayers.get());
                for (i=0; i<server.numPlayers.get(); i++)
                    System.out.println("Player "+ i + " " + server.hostnames.get(i) + " " +server.ports.get(i));
                packetToClient.hostname = server.hostnames;
                packetToClient.port = server.ports;
                packetToClient.numPlayers = server.numPlayers.get();
            }
            toClient.writeObject(packetToClient);
            fromClient.close();
            toClient.close();
            socket.close();
                      
		} catch (IOException e) {
			if(!done)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!done)
				e.printStackTrace();
		}
	}
}
