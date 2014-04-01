import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class FileServerHandlerThread extends Thread {
    private Socket socket;
    private ArrayList <String> dictionary;


    public FileServerHandlerThread(Socket socket, ArrayList<String> dictionary) {
        super("JobTrackerHandlerThread");
        this.socket = socket;
        this.dictionary = dictionary;
    }

    public void run() {
        FileServerPacket packetFromWorker;
        FileServerPacket packetToWorker; 
        boolean listening = true;
        try {
            ObjectInputStream fromWorker = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream toWorker = new ObjectOutputStream(socket.getOutputStream());
            
            while(listening) {
                packetFromWorker = (FileServerPacket) fromWorker.readObject();
                packetToWorker = packetFromWorker;
                packetToWorker.dictWords = dictionary.subList(packetFromWorker.begin, packetFromWorker.begin + packetFromWorker.numWords); 
                toWorker.writeObject(packetToWorker);
            
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
