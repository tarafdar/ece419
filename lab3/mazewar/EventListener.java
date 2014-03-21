
import java.net.*;
import java.io.*;

public class EventListener extends Thread {
    private Mazewar mazewar;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int playerID;


    public EventListener(Mazewar mazewar, ObjectInputStream in, ObjectOutputStream out, int playerID){
        this.mazewar = mazewar;
        this.in = in;
        this.out = out;
        this.playerID = playerID;
    }


    public void run(){
        boolean listening = true;
        mazeWarPacket packetIn;
        mazeWarPacket packetOut;
        System.out.println("In eventlistener");
        while(listening){
            try{
                packetIn = (mazeWarPacket)in.readObject();
                System.out.println("have read packet"); 
                if(!(packetIn.type == mazeWarPacket.ACK && packetIn.type == mazeWarPacket.TOKEN)){
                    packetOut = packetIn;
                    synchronized(mazewar.toProcessEventsQ){
                        mazewar.toProcessEventsQ.offer(packetIn);
                        packetOut.type = mazeWarPacket.ACK;
                        out.writeObject(packetOut);
                    }
                }
                else if(packetIn.type == mazeWarPacket.TOKEN){
                    mazewar.hasToken = true;
                }
                else if(packetIn.type == mazeWarPacket.ACK){
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
