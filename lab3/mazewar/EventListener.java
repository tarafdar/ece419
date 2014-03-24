
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
        int i, j;
        boolean listening = true;
        mazeWarPacket packetIn;
        mazeWarPacket packetOut;
        RemoteClient remoteclient;
        System.out.println("In eventlistener");
        while(listening){
            try{
                packetIn = (mazeWarPacket)in.readObject();
                //System.out.println("received packet " + packetIn.typeToString() );
                //if(packetIn.isAck) System.out.println("which is an ACK");
                if(!(packetIn.type == mazeWarPacket.TOKEN || packetIn.isAck == true)){
                    packetOut = packetIn;
                    synchronized(mazewar.toProcessEventsQ){
                        mazewar.toProcessEventsQ.offer(packetIn);
                        packetOut.isAck = true;
                        out.writeObject(packetOut);
                    }
                }
                else if(packetIn.type == mazeWarPacket.TOKEN){
//                    System.out.println("Received token");
                    synchronized(mazewar.alreadyJoined) {
                        if(!mazewar.alreadyJoined) {
                            for (i=0;i<mazewar.clientList.size();i++) {
                                if(i != mazewar.player_id && packetIn.playerNames.get(i) != null){
                                    remoteclient = new RemoteClient(packetIn.playerNames.get(i));
                                    for(j=0; j<packetIn.scores.size(); j++){
                                        if(packetIn.scoreNames.get(j).equals(packetIn.playerNames.get(i)))
                                            break;
                                    }
                                    mazewar.maze.addClient(remoteclient , packetIn.points.get(i), packetIn.directions.get(i), packetIn.scores.get(j));
                                    mazewar.clientList.set(i,remoteclient);
                                    //mazewar.scoreModel.setValueAt(packetIn.scores.get(i), i, 1);
                                }
                            }    
                        }
                    }
                    synchronized(mazewar.waitToClose){
                        if(mazewar.waitToClose){
                            System.out.println("closing");
                            mazewar.outStreamList.get(packetIn.clientID).close();
                            mazewar.inStreamList.get(packetIn.clientID).close();
                            mazewar.socketList.get(packetIn.clientID).close();
                            mazewar.waitToClose = false;
                        }
                    }
                   
                   // System.out.println("about to set token");
                    synchronized(mazewar.hasToken) {        
                        mazewar.hasToken = true;
                    }
                    //System.out.println("finished setting token");
                    
                }
                
                else if (packetIn.type == mazeWarPacket.QUIT && packetIn.isAck == false){
                    mazewar.maze.removeClient(mazewar.clientList.get(packetIn.clientID));
                    if(mazewar.prevInRingIdx.get() != packetIn.clientID){
                        System.out.println("recieved quit from client " + packetIn.clientID + " our prev in ring is " + mazewar.prevInRingIdx.get());
                        mazewar.outStreamList.get(packetIn.clientID).close();
                        mazewar.inStreamList.get(packetIn.clientID).close();
                        mazewar.socketList.get(packetIn.clientID).close();
                    
                    }
                    else{
                        synchronized(mazewar.waitToClose){
                            System.out.println("setting waittoclose");
                            mazewar.waitToClose = true;
                        }
                    }
                }
                 
                //should have token (when we receive ack guaranteed to have token)
                else if(packetIn.isAck == true ){
                    mazewar.currentAcks.getAndIncrement();
                }    

            }
            catch(IOException e){
                System.err.println("Closing connection");
                this.destroy();
               // System.exit(1);



            }
            catch(ClassNotFoundException e){
                System.err.println("ClassNotFoundException in Event Listener");
                System.exit(1);



            }
        }

    }


}
