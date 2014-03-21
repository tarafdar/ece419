
import java.net.*;
import java.io.*;

public class EventProcessThread extends Thread{
    private Mazewar mazewar;


    public EventProcessThread(Mazewar mazewar){
        super("ServerProcessThread");
        this.mazewar = mazewar;
    }

    public void run(){
        mazeWarPacket packet;

        while(!mazewar.quit){
            synchronized(mazewar.toProcessEventsQ){
                 if(mazewar.toProcessEventsQ.size()>0){
                     packet = toProcessEventsQ.poll();
                     if(packet.type == mazeWarPacket.CLIENT_FORWARD)
                         mazewar.clientList.get(packet.clientID).forward();

                     else if(packet.type == mazeWarPacket.CLIENT_BACKWARD)
                         mazewar.clientList.get(packet.clientID).backup();
                     
                     else if(packet.type == mazeWarPacket.CLIENT_LEFT)
                         mazewar.clientList.get(packet.clientID).turnLeft();

                     else if(packet.type == mazeWarPacket.CLIENT_RIGHT)
                         mazewar.clientList.get(packet.clientID).turnRight();
                     
                     else if(packet.type == mazeWarPacket.CLIENT_FIRE)
                        mazewar.clientList.get(packet.clientID).fire();                        
                        
                     else if(packet.type == mazeWarPacket.CLIENT_KILLED){
                        mazewar.clientList.get(packet.killedClientID).killed(mazewar.clientList.get(packet.killedClientID), packet.point[0], packet.d[0]);     
                     }   
                     
                     else if(packet.type == mazeWarPacket.CLIENT_QUIT){
                         mazewar.maze.removeClient(mazewar.clientList.get(packet.clientID));

                     }
                     else if(packet.type == mazeWarPacket.JOIN_AT){
                         mazewar.maze.addClient(mazewar.clientList.get(packet.clientID), packet.point, packet.d);


                     }
                              
                 }
            }







        }



    }





}
