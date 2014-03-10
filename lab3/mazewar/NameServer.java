import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class NameServer {
    public AtomicInteger numPlayers;
    public ArrayList<String> players;
    public ArrayList<String> hostnames;
    public Arraylist<Integer> ports;
    
    public NameServer(String[] args) {
        hostnames = new ArrayList<String>();
        ports = new ArrayList<Integer>();
        numPlayers = new AtomicInteger();
        
        ServerSocket serverSocket = null;
        Socket socket;
        boolean listening = true;
        try {
        	if(args.length == 1) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }
        try{
            while (listening) {
                socket = serverSocket.accept();
                numConnected.getAndIncrement();
        	    new NameServerHandlerThread(this, socket).start();
            }   

            serverSocket.close();
        } catch (IOException e) {
            System.err.println("ERROR: Could not create streams!");
            System.exit(-1);
        }
    }
    
    public static void main(String[] args) {
        new NameServer(args);
    }    
}
