
import java.net.*;
import java.io.*;

public class EventListener extends Thread {
    private Mazewar mazewar;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int playerID;


    public EventListener(Mazewar mazewar, ObjectInputStream in, ObjectOutputStream out){
        this.mazewar = mazewar;
        this.in = in;
        this.out = out;
        //this.playerID = playerID;
    }


    public void run(){
        int i;
        boolean listening = true;
        mazeWarPacket packetIn;
        mazeWarPacket packetOut;
        RemoteClient remoteclient;
        System.out.println("In eventlistener");
        while(listening){
            try{
                packetIn = (mazeWarPacket)in.readObject();
                System.out.println("received packet " + packetIn.typeToString() );
                if(packetIn.isAck) System.out.println("which is an ACK");
                if(!(packetIn.type == mazeWarPacket.TOKEN || packetIn.isAck == true)){
                    packetOut = packetIn;
                    synchronized(mazewar.toProcessEventsQ){
                        mazewar.toProcessEventsQ.offer(packetIn);
                        packetOut.isAck = true;
                        out.writeObject(packetOut);
                    }
                }
                else if(packetIn.type == mazeWarPacket.TOKEN){
                    System.out.println("Received token");
                    synchronized(mazewar.alreadyJoined) {
                        if(!mazewar.alreadyJoined) {
                            for (i=0;i<mazewar.clientList.size();i++) {
                                if(i != mazewar.player_id && packetIn.playerNames.get(i) != null){
                                    remoteclient = new RemoteClient(packetIn.playerNames.get(i));
                                    mazewar.maze.addClient(remoteclient , packetIn.points.get(i), packetIn.directions.get(i));
                                    mazewar.clientList.set(i,remoteclient);
                                }
                            }    
                        }
                    }
                    System.out.println("about to set token");
                    synchronized(mazewar.hasToken) {        
                        mazewar.hasToken = true;
                    }
                    System.out.println("finished setting token");
                    
                }
               /*
                else if(packetIn.type == mazeWarPacket.JOIN_REQ && packetIn.isAck == false){
                   packetOut = new mazeWarPacket();
                   packetOut.point = mazewar.guiClient.getPoint();  
                   packetOut.d = mazewar.guiClient.getOrientation();  
                   packetOut.isAck = true;
                   packetOut.clientID = mazewar.player_id;
                   out.writeObject(packetOut);
                    
                } 
                
                //should have token (when we receive ack guaranteed to have token)
                else if(packetIn.type == mazeWarPacket.JOIN_REQ && packetIn.isAck == true){
                    DirectedPoint dp = new DirectedPoint(packetIn.point, packetIn.d);
                    mazewar.currentAcks.getAndIncrement();
                    synchronized(mazewar.otherClientLocations){
                        mazewar.otherClientLocations.add(dp);
                        synchronized(mazewar.otherClientIDs){
                            mazewar.otherClientIDs.add(packetIn.clientID);
                        }
                    }
                    mazewar.maze.addClient(mazewar.clientList.get(packetIn.clientID), dp, dp.getDirection());     
                } 
               */
               
                 
                //should have token (when we receive ack guaranteed to have token)
                else if(packetIn.isAck == true ){
                    mazewar.currentAcks.getAndIncrement();
                }    

            }
            catch(IOException e){
                System.err.println("IOException in Event Listener");
                System.exit(1);



            }
            catch(ClassNotFoundException e){
                System.err.println("ClassNotFoundException in Event Listener");
                System.exit(1);



            }
        }

    }


}
