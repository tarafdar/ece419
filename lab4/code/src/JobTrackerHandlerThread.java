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
        
        boolean listening = true;
        try {
            ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
        
            while(listening) {
                packetFromClient = (ClientPacket) fromClient.readObject();
                if(packetFromClient.requestType == ClientPacket.JOB_SUBMIT) {
                    
                }
                if(packetFromClient.requestType == ClientPacket.JOB_QUERY) {
                    
                }
            
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
