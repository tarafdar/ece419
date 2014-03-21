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
                    synchronized (mazewar.outstandingLocalEvents) {    
                        while(outstandingLocalEvents.peek()) {
                            packetFromQueue = outstandingLocalEvents.poll();
                                    
                            System.out.println("Multicasting packet on client of type " +  packetFromQueue.typeToString());
                            
                            for(i=0; i<mazewar.outputStreams.size(); i++) {
                                if (!mazewar.sockets.get(i).isClosed() && mazewar.outputStreams.get(i)) {
                                    mazewar.outputStreams.get(i).writeObject(packetFromQueue);
                                }
                            }
                            numExpectedAcks += mazewar.outputStreams.size() - 1;
                            synchronized(mazewar.toProcessEventsQ) {
                                mazewar.toProcessEventsQ.offer(packetFromQueue);
                            }
                        }
                    }
                    while (mazewar.currentAcks.get() != numExpectedAcks);
                    tokenPacket = new mazeWarPacket();
                    tokenPacket.type = mazeWarPacket.TOKEN;
                    //clear the acks
                    mazewar.currentAcks.clear();
                    //send the token
                    mazewar.outputStream.get(mazewar.nextInRingIdx).writeObject(tokenPacket); 
                }
            }
        } catch (IOException e) {
            System.err.println("IOE exception in Multicast Thread")
            e.printStackTrace();     
        } 
    }
}        
