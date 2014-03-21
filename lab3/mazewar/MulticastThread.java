import java.io.*;
import java.net.*;

public class MulticastThread extends Thread {
    private Mazewar mazewar;

    public MulticastThread (Mazewar mazewar) {
        super("Multicast Thread");
        this.mazewar = mazewar;
    }

    public void run() {
        mazeWarPacket packetFromQueue;
        mazeWarPacket tokenPacket;
        int i;
        int numExpectedAcks;
        try {
            while (true) {
                numExpectedAcks = 0;
                if(mazewar.hasToken) {
                    synchronized (mazewar.outstandingLocalEventsQ) {    
                        while(mazewar.outstandingLocalEventsQ.peek() != null) {
                            packetFromQueue = mazewar.outstandingLocalEventsQ.poll();
                                    
                            System.out.println("Multicasting packet on client of type " +  packetFromQueue.typeToString());
                            
                            for(i=0; i<mazewar.outStreamList.size(); i++) {
                                if (!mazewar.socketList.get(i).isClosed() && mazewar.outStreamList.get(i) != null) {
                                    mazewar.outStreamList.get(i).writeObject(packetFromQueue);
                                }
                            }
                            numExpectedAcks += mazewar.outStreamList.size() - 1;
                            synchronized(mazewar.toProcessEventsQ) {
                                mazewar.toProcessEventsQ.offer(packetFromQueue);
                            }
                        }
                    }
                    while (mazewar.currentAcks.get() != numExpectedAcks);
                    tokenPacket = new mazeWarPacket();
                    tokenPacket.type = mazeWarPacket.TOKEN;
                    //clear the acks
                    mazewar.currentAcks.set(0);
                    //send the token
                    mazewar.outStreamList.get(mazewar.nextInRingIdx).writeObject(tokenPacket); 
                }
            }
        } catch (IOException e) {
            System.err.println("IOE exception in Multicast Thread");
            e.printStackTrace();     
        } 
    }
}        
