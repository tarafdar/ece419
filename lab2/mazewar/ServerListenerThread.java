
import java.net.*;
import java.io.*;

public class ServerListenerThread extends Thread {
    private Mazewar mazewar;
    private ObjectInputStream in;
    private boolean quit; 
    public ServerListenerThread(Mazewar mazewar, ObjectInputStream in){
           super("ServerListenerThread");
           this.mazewar = mazewar;
           this.quit = mazewar.quit;
           this.in = in;
    }

    public void run(){

        mazeWarPacket packetFromServer; 
        while(!mazewar.quit){
           try{
                packetFromServer = (mazeWarPacket) in.readObject(); 
                System.out.println("listener thread " + packetFromServer.clientName + " type " + packetFromServer.typeToString()); 
                synchronized(mazewar.q){
                    mazewar.q.add(packetFromServer);
                }
           }catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		        System.exit(1);
           }catch(ClassNotFoundException e){
		        System.err.println("ERROR: Class Not Found.");
		        System.exit(1);


           }
            

        }



    }




}
