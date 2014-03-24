
import java.net.*;
import java.io.*;

public class EventProcessThread extends Thread{
    private Mazewar mazewar;


    public EventProcessThread(Mazewar mazewar){
        super("EventProcessThread");
        this.mazewar = mazewar;
    }

    public void run(){
        mazeWarPacket packet;
        RemoteClient remoteclient;
        int i;
        while(!mazewar.quit){
            synchronized(mazewar.toProcessEventsQ){
                 if(mazewar.toProcessEventsQ.size()>0){
                     packet = mazewar.toProcessEventsQ.poll();
                     //System.out.println("Processing event of client " + packet.clientID);
                     if(packet.type == mazeWarPacket.FORWARD)
                         mazewar.clientList.get(packet.clientID).forward();

                     else if(packet.type == mazeWarPacket.BACKWARD)
                         mazewar.clientList.get(packet.clientID).backup();
                     
                     else if(packet.type == mazeWarPacket.LEFT)
                         mazewar.clientList.get(packet.clientID).turnLeft();

                     else if(packet.type == mazeWarPacket.RIGHT)
                         mazewar.clientList.get(packet.clientID).turnRight();
                     
                     else if(packet.type == mazeWarPacket.FIRE)
                        mazewar.clientList.get(packet.clientID).fire();                        
                        
                     else if(packet.type == mazeWarPacket.KILLED){
                        mazewar.clientList.get(packet.clientID).killed(mazewar.clientList.get(packet.killedClientID), packet.points.get(0), packet.directions.get(0));     
                     }   
                     
                     else if(packet.type == mazeWarPacket.QUIT){
                         //mazewar.maze.removeClient(mazewar.clientList.get(packet.clientID));
                        //shouldnt happen
                     }
                     else if(packet.type == mazeWarPacket.JOIN_AT){
                     //    System.out.println("Processing JOIN_AT from client ID " + packet.clientID);
                     //    if (packet.points.get(0) == null)
                     //       System.out.println("Point recvd is null");
                     //    if (packet.directions.get(0) == null)
                     //       System.out.println("directions recvd is null");
                         
                         remoteclient = new RemoteClient(packet.clientName);
                         mazewar.maze.addClient(remoteclient, packet.points.get(0), packet.directions.get(0), 0);
                         mazewar.clientList.set(packet.clientID,remoteclient);
                     
                         //mazewar.maze.addClient(mazewar.clientList.get(packet.clientID), packet.points.get(0), packet.directions.get(0));
                         
                     }
                    // else if(packet.type == mazeWarPacket.JOIN_REQ){
                    //    for(i=0; i<mazewar.otherClientLocations.size(); i++){
                    //        mazewar.maze.addClient(mazewar.clientList.get(mazewar.otherClientIDs.get(i)), mazewar.otherClientLocations.get(i), mazewar.otherClientLocations.get(i).getDirection());     
                    //        
                    //        
                    //        
                    //    }    
                             
                             
                         
                         
                //     } 
                              
                 }
            }







        }



    }





}
