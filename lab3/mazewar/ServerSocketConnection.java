
import java.net.*;
import java.io.*;

public class ServerSocketConnection extends Thread {
	private ServerSocket serversocket = null;
    private String playerName;
    private Mazewar mazewar;
    private int playerID;
    
    public ServerSocketConnection(ServerSocket serversocket, Mazewar mazewar) {
		super("Client Listener Thread");
        this.mazewar = mazewar;
	    this.serversocket = serversocket;
        this.playerName = mazewar.name;
		System.out.println("Created new Thread to listen for other client connections");
	}

	public void run() {

		boolean gotByePacket = false;
	    Socket socket = null;	
		mazeWarPacket packetToClient = new mazeWarPacket();
	    mazeWarPacket packetFromClient;
        boolean listening = true;
		try {
			
            
            while(listening){
                /* stream to read from client */
		        
                socket = serversocket.accept();    
        	    ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			
			    /* stream to write back to client */
			    ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());

                synchronized(mazewar.socketList) {
                    mazewar.socketList.add(socket);
                }
                synchronized(mazewar.outStreamList) {
                    mazewar.outStreamList.add(toClient);
                }
                synchronized(mazewar.inStreamList) {
                    mazewar.inStreamList.add(fromClient);
                }

		    
            //    packetFromClient = (mazeWarPacket) fromClient.readObject();
            //    System.out.println("receiving(server) " + packetFromClient.clientName);
                synchronized(mazewar.clientList) {
            //       mazewar.clientInfo.add(packetFromClient.clientName);
                   mazewar.clientList.add(null);
                   System.out.println("just added a client from a remote connection to listening port");
                }
            //    playerID = packetFromClient.clientID;
                if (mazewar.nextInRingIdx.get() == 0) {
                    mazewar.nextInRingIdx.set(mazewar.player_id + 1); 
                }    
                //packetToClient.clientName = playerName;
                //packetToClient.clientID = mazewar.player_id;
                //toClient.writeObject(packetToClient);
                new EventListener(mazewar, fromClient, toClient).start();
            }
            //new EventSender(mazewar, toClient, fromClient, playerID).start();
			
			/* cleanup when client exits */
			//System.out.println("Closing Sockets!!!");
            //fromClient.close();
			//toClient.close();
			//socket.close();

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			if(!gotByePacket)
//				e.printStackTrace();
		}
	}
}
