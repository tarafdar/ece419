import java.net.*;
import java.io.*;

public class JobTrackerHandlerThread extends Thread {
    private Socket socket;

    public JobTrackerHandlerThread(Socket socket) {
        super("JobTrackerHandlerThread");
        this.socket = socket;
    }

    public void run() {
        ClientPacket packetFromClient;
        ClientPacket packetToClient; 
        boolean listening = true;
        try {
            ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
            
            while(listening) {
                packetFromClient = (ClientPacket) fromClient.readObject();
                packetToClient = packetFromClient;

                if(packetFromClient.requestType == ClientPacket.JOB_SUBMIT) {
                //create new job
                    System.out.println("Recieved Job submit with hash " + packetFromClient.hash);
                }
                if(packetFromClient.requestType == ClientPacket.JOB_QUERY) {
                //check the status of job
                    System.out.println("Recieved Job query with hash " + packetFromClient.hash);
                }
                toClient.writeObject(packetToClient);
            
            }    

		} catch (IOException e) {
		    e.printStackTrace();
            listening = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
            listening = false;
		}
    }
}
