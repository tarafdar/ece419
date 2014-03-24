import java.io.*;
import java.net.*;

public class MulticastThread extends Thread {
    private Mazewar mazewar;

    public MulticastThread (Mazewar mazewar) {
        super("Multicast Thread");
        this.mazewar = mazewar;
    }

    public void run() {
        mazeWarPacket packetFromQueue = null;
        mazeWarPacket tokenPacket;
        mazeWarPacket joinAtPacket;
        int i;
        int numExpectedAcks;
        //boolean sentJoin = false;
        try {
            while (true) {
                numExpectedAcks = 0;
                //sentJoin = false;
                //System.out.println("checking for token");
                synchronized(mazewar.hasToken) {
                    if(mazewar.hasToken) {
                        //System.out.println("in Multicast Thread and have token");
                        synchronized (mazewar.outstandingLocalEventsQ) {    
                            synchronized (mazewar.outStreamList){
                                numExpectedAcks = 0;
                                if(mazewar.outstandingLocalEventsQ.peek() != null) {
                                    numExpectedAcks = mazewar.inStreamList.size() - 1;
                                    packetFromQueue = mazewar.outstandingLocalEventsQ.poll();

                                    for(i=0; i<mazewar.outStreamList.size(); i++) {
                                        if (mazewar.outStreamList.get(i) != null) {
                                            mazewar.outStreamList.get(i).writeObject(packetFromQueue);
                                        }
                                    }
                                    // if(!sentJoin) {
                                    synchronized(mazewar.toProcessEventsQ) {                                   
                                        mazewar.toProcessEventsQ.offer(packetFromQueue);
                                    }
                                    // }
                                    while (mazewar.currentAcks.get() != numExpectedAcks);
                                    mazewar.currentAcks.set(0);
                                }
                            }
                        }
                        synchronized (mazewar.alreadyJoined) {
                            if (!mazewar.alreadyJoined) {
                                numExpectedAcks = mazewar.inStreamList.size() - 1;
                                System.out.println("in second join if"); 
                                mazewar.maze.addClient(mazewar.guiClient);    
                                joinAtPacket = new mazeWarPacket();
                                joinAtPacket.type = mazeWarPacket.JOIN_AT;
                                joinAtPacket.clientID = mazewar.player_id;
                                joinAtPacket.clientName = mazewar.name;
                                joinAtPacket.points.add(mazewar.guiClient.getPoint());
                                joinAtPacket.directions.add(mazewar.guiClient.getOrientation());
                                for(i=0; i<mazewar.outStreamList.size(); i++) {
                                    if (mazewar.outStreamList.get(i) != null) {
                                        mazewar.outStreamList.get(i).writeObject(joinAtPacket);
                                    }
                                }
                                while (mazewar.currentAcks.get() != numExpectedAcks);
                                mazewar.alreadyJoined = true;
                                mazewar.currentAcks.set(0);
                            }
                        } 

                        tokenPacket = new mazeWarPacket();
                        tokenPacket.type = mazeWarPacket.TOKEN;
                        tokenPacket.numPlayers = mazewar.clientList.size();
                        //clear the acks
                        //send the token
                        if(mazewar.outStreamList.get(mazewar.nextInRingIdx.get()) != null){
                            for(i=0; i<mazewar.clientList.size(); i++) {
                                if(mazewar.clientList.get(i) != null) {
                                    tokenPacket.playerNames.add(mazewar.clientList.get(i).getName());
                                    tokenPacket.points.add(mazewar.clientList.get(i).getPoint());
                                    tokenPacket.directions.add(mazewar.clientList.get(i).getOrientation());
                                    tokenPacket.scoreNames.add((String)mazewar.scoreModel.getValueAt(i,0));
                                    tokenPacket.scores.add((Integer)mazewar.scoreModel.getValueAt(i, 1));
                                }
                                else {
                                    tokenPacket.playerNames.add(null);
                                    tokenPacket.points.add(null);
                                    tokenPacket.directions.add(null);
                                    tokenPacket.scores.add(0);
                                }
                            }

                            mazewar.outStreamList.get(mazewar.nextInRingIdx.get()).writeObject(tokenPacket);
                            mazewar.hasToken = false;
                            //System.out.println("sending out token"); 
                            if(packetFromQueue != null && packetFromQueue.type == mazeWarPacket.QUIT){
                                mazewar.quit();
                            }
                        }
                    }
                }   
            }
        } catch (IOException e) {
            System.out.println("Someone left the game :(");
            //e.printStackTrace();     
        } 
    }
}        
