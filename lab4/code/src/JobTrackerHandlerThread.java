import java.net.*;
import java.io.*;

public class JobTrackerHandlerThread extends Thread {
    private Socket socket;

    public JobTrackerHandlerThread(Socket socket) {
        super("JobTrackerHandlerThread");
        this.socket = socket;
    }

    public void run() {
        String whatIget;
        boolean listening = true;
        try {
            ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
        
            while(listening) {
                whatIget = (String) fromClient.readObject();
                System.out.println("I get from client " + whatIget);    
            
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
