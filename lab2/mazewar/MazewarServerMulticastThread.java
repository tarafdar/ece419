import java.net.*;
import java.io.*;

public class MazewarServerMulticastThread extends Thread {
    private MazewarServer server;
	
    public MazewarServerMulticastThread(MazewarServer s) {
		super("MazewarServerMulticastThread");
        this.server = s;
	}

	public void run() {
        int seqNum;
        //wait until all the client info is populated
        
        while (server.numConnected.get() != server.numPlayers);
        System.out.println("recieved all of the connections"); 
        //create the first multicast packet with all of the initial positions directions and names to be broadcast to the client
        mazeWarPacket packet = new mazeWarPacket();
        //synchronized (server.currentSequenceNumber) {
        //    seqNum = server.currentSequenceNumber;
        //    server.currentSequenceNumber++;
        //}
        seqNum = server.currentSequenceNumber.getAndIncrement();
        packet.players = server.players;
        packet.d = server.d;
        packet.point = server.point;
        packet.sequence_number = seqNum;
        packet.numPlayers = server.numPlayers;
         
        synchronized (server.actionQueue) {
            server.actionQueue.offer(packet);
        }
        System.out.println("finished creating first broadcast packet"); 
        int i;
		mazeWarPacket packetFromQueue;
		try {
			/* streams to multicast to clients */
			while (true) {
                packetFromQueue = server.actionQueue.take();
                //System.out.println("number of players = " + server.numPlayers);
                System.out.println("Multicasting packet on server from client " + packetFromQueue.clientName + " of type " +  packetFromQueue.typeToString()); 
                for (i=0; i<server.numPlayers; i++) {
                   if (server.outputStreams.get(i) == null) 
                       System.out.println("outputStream of id " + i + " is null"); 
                   server.outputStreams.get(i).writeObject(packetFromQueue);
                }
            }    

		} catch (IOException e) {
				e.printStackTrace();
		} catch (InterruptedException e) {
				e.printStackTrace();
		}
	}
}
