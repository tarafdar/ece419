
import java.net.*;
import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;  

public class ServerProcessThread extends Thread{
    private Mazewar mazewar;
    static class PQsort implements Comparator<mazeWarPacket>{
        public int compare(mazeWarPacket a, mazeWarPacket b){
            return a.sequence_number - b.sequence_number;
        }
    }


    public ServerProcessThread(Mazewar mazewar){
        super("ServerProcessThread");
    }

    public void run(){
        int i, j;
        boolean found = false;
        while(!mazewar.quit){
            for(i = 0; i<mazewar.q.size() ; i++){
                if(mazewar.q.get(i).sequence_number == mazewar.local_sequence_number){
                    found = true;
                    break;
                }
           }
           if(found){
                for(j=0; j<mazewar.clientList.size(); j++){
                    if(mazewar.q.get(i).clientName.equals(mazewar.clientList.get(j).getName())){
                        if(mazewar.q.get(i).type == mazeWarPacket.CLIENT_FORWARD)
                            mazewar.clientList.get(j).forward();

                        else if(mazewar.q.get(i).type == mazeWarPacket.CLIENT_BACKWARD)
                            mazewar.clientList.get(j).backup();
                        
                        else if(mazewar.q.get(i).type == mazeWarPacket.CLIENT_LEFT)
                            mazewar.clientList.get(j).turnLeft();

                        else if(mazewar.q.get(i).type == mazeWarPacket.CLIENT_LEFT)
                            mazewar.clientList.get(j).turnRight();
                        
                        else if(mazewar.q.get(i).type == mazeWarPacket.CLIENT_FIRE)
                           mazewar.clientList.get(j).fire();                         
                        
                        //else if(mazewar.q.get(i).type == mazeWarPacket.CLIENT_QUIT)
                          //  mazewar.clientList.get(j).
                        mazewar.q.remove(i);
                        mazewar.local_sequence_number++;
                    }
                         

                }


           }
       }





    }









}
