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
         
        synchronized (server.actionQueue) {
            server.actionQueue.offer(packet);
        }
        
        int i;
		mazeWarPacket packetFromQueue;
		try {
			/* streams to multicast to clients */
			while (true) {
                packetFromQueue = server.actionQueue.take();
                for (i=0; i<server.numPlayers; i++) {
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
