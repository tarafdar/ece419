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
        boolean sentJoin = false;
        try {
            while (true) {
                //numExpectedAcks = 0;
                sentJoin = false;
                if(mazewar.hasToken) {
                    synchronized (mazewar.outstandingLocalEventsQ) {    
                         synchronized (mazewar.outStreamList){
                             numExpectedAcks = mazewar.inStreamList.size() - 1;
                             if(mazewar.outstandingLocalEventsQ.peek() != null) {
                                 packetFromQueue = mazewar.outstandingLocalEventsQ.poll();
                                         
                                 System.out.println("Multicasting packet on client of type " +  packetFromQueue.typeToString());
                                 //if (packetFromQueue.type == mazeWarPacket.JOIN_REQ)
                                     if (mazewar.otherClientLocations.size() == numExpectedAcks && !(mazewar.alreadyJoined))
                                         sentJoin = true;
                                     System.out.println("num out streams is " + mazewar.outStreamList.size());
                                     for(i=0; i<mazewar.outStreamList.size(); i++) {
                                         if (mazewar.outStreamList.get(i) != null) {
                                             mazewar.outStreamList.get(i).writeObject(packetFromQueue);
                                         }
                                     }
                                      if(!sentJoin) {
                                          synchronized(mazewar.toProcessEventsQ) {                                   
                                              mazewar.toProcessEventsQ.offer(packetFromQueue);
                                          }
                                      }
                                      int print = 0;
                                      while (mazewar.currentAcks.get() != numExpectedAcks){
                                          print++;
                                          if(print == 2)
                                             System.out.println("waiting for acks :(, num out streams is " + mazewar.outStreamList.size());
                                      }
                                         // System.out.println("stuck waiting for acks :( " );
                                     }
//THIS IS THE PROBLEM                       
//AFTER SECOND CLIENT JOINS
//WE GET STUCK WAITING FOR ACKS!
                         }
                    }
                    mazewar.currentAcks.set(0);
                    if (sentJoin && !mazewar.alreadyJoined) {
                        System.out.println("in second join if"); 
                        synchronized(mazewar.toProcessEventsQ) {
                            mazewar.toProcessEventsQ.offer(packetFromQueue);
                        }
                        boolean found = false;
                        Point point = new Point(mazewar.maze.randomGen.nextInt(mazewar.maze.maxX),mazewar.maze.randomGen.nextInt(mazewar.maze.maxY));
                        while(!found) {
                            for(i=0; i<mazewar.otherClientLocations.size();i++) {
                                if(point.getX() == mazewar.otherClientLocations.get(i).getX() && point.getY() == mazewar.otherClientLocations.get(i).getY())
                                    break;
                            }
                            if(i==mazewar.otherClientLocations.size())
                                found = true;
                            else
                                point = new Point(mazewar.maze.randomGen.nextInt(mazewar.maze.maxX),mazewar.maze.randomGen.nextInt(mazewar.maze.maxY));
                        }
                        mazewar.maze.addClient(mazewar.guiClient, point);    
                        joinAtPacket = new mazeWarPacket();
                        joinAtPacket.type = mazeWarPacket.JOIN_AT;
                        joinAtPacket.clientID = mazewar.player_id;
                        joinAtPacket.point = point;
                        joinAtPacket.d = mazewar.guiClient.getOrientation();
                        for(i=0; i<mazewar.outStreamList.size(); i++) {
                            if (mazewar.outStreamList.get(i) != null) {
                                mazewar.outStreamList.get(i).writeObject(joinAtPacket);
                            }
                        }
                        while (mazewar.currentAcks.get() != numExpectedAcks);
                    }
                    

                    tokenPacket = new mazeWarPacket();
                    tokenPacket.type = mazeWarPacket.TOKEN;
                    //clear the acks
                    //send the token
                    if(mazewar.outStreamList.get(mazewar.nextInRingIdx) != null){
                        mazewar.outStreamList.get(mazewar.nextInRingIdx).writeObject(tokenPacket);
                        mazewar.hasToken = false; 
                    }
                    mazewar.alreadyJoined = true;
                }
            }
        } catch (IOException e) {
            System.err.println("IOE exception in Multicast Thread");
            e.printStackTrace();     
        } 
    }
}        
