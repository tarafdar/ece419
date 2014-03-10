
import java.net.*;
import java.io.*;

public class ServerProcessThread extends Thread{
    private Mazewar mazewar;


    public ServerProcessThread(Mazewar mazewar){
        super("ServerProcessThread");
        this.mazewar = mazewar;
    }

    public void run(){
        int i, j, k;
        boolean found = false;
        mazeWarPacket packet;
        while(!mazewar.quit){
            found = false;
            synchronized(mazewar.q){
                 
                 for(i = 0; i<mazewar.q.size() ; i++){
                     if(!mazewar.q.isEmpty() &&( mazewar.q.get(i).sequence_number == mazewar.local_sequence_number) ){
                         found = true;
                         break;
                     }
                }
                
           }
           if(found){
                synchronized(mazewar.q){
                    packet = mazewar.q.get(i);
                }    
                for(j=0; j<mazewar.clientList.size(); j++){
                    if(packet.clientName.equals(mazewar.clientList.get(j).getName())){
                        if(packet.type == mazeWarPacket.CLIENT_FORWARD)
                            mazewar.clientList.get(j).forward();

                        else if(packet.type == mazeWarPacket.CLIENT_BACKWARD)
                            mazewar.clientList.get(j).backup();
                        
                        else if(packet.type == mazeWarPacket.CLIENT_LEFT)
                            mazewar.clientList.get(j).turnLeft();

                        else if(packet.type == mazeWarPacket.CLIENT_RIGHT)
                            mazewar.clientList.get(j).turnRight();
                        
                        else if(packet.type == mazeWarPacket.CLIENT_FIRE)
                           mazewar.clientList.get(j).fire();                        
                           
                        else if(packet.type == mazeWarPacket.CLIENT_KILLED){
                           for(k=0; k<mazewar.clientList.size(); k++){
                                if(packet.players[0].equals(mazewar.clientList.get(k).getName())){
                                    mazewar.clientList.get(j).killed(mazewar.clientList.get(k), mazewar.q.get(i).point[0], mazewar.q.get(i).d[0]);     
                                    break;
                                }
                           }  
                        }   
                        
                        else if(mazewar.q.get(i).type == mazeWarPacket.CLIENT_QUIT){
                            mazewar.maze.removeClient(mazewar.clientList.get(j));

                            


                        }
                        synchronized(mazewar.q){
                            mazewar.q.remove(i);
                        }
                        mazewar.local_sequence_number++;
                        break;
                    }
                         

                }


           }
       }





    }









}
