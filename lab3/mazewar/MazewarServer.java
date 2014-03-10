
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MazewarServer extends MazeImpl {
    private static final int mazeWidth = 20;
    private static final int mazeHeight = 10;
    private static final int mazeSeed = 42;
    
    public AtomicInteger numConnected;
    public int numPlayers;
     
    public String[] players;
    public Point[] point;
    public Direction[] d;
    public AtomicInteger currentSequenceNumber;
    public LinkedBlockingQueue<mazeWarPacket> actionQueue; 
//    public MazeImpl maze;
    public ArrayList <Socket> sockets;
    public ArrayList <ObjectOutputStream> outputStreams;
    public ArrayList <ObjectInputStream> inputStreams;
    
    public MazewarServer(String[] args) {
        //maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
        super(new Point(mazeWidth,mazeHeight),mazeSeed, "server");
        numConnected = new AtomicInteger();
        numPlayers = 0;
        currentSequenceNumber = new AtomicInteger();
     
        players = new String[4];
        point = new Point[4];
        d = new Direction[4];
        
        actionQueue = new LinkedBlockingQueue<mazeWarPacket>(); 
        //maze = null;
//        assert(maze != null);
        sockets = new ArrayList<Socket>();
        
        outputStreams = new ArrayList<ObjectOutputStream>();
        inputStreams = new ArrayList<ObjectInputStream>();
        
        ServerSocket serverSocket = null;
        Socket socket;
        boolean listening = true;
        try {
        	if(args.length == 2) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                numPlayers = Integer.parseInt(args[1]);
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }
        new MazewarServerMulticastThread(this).start(); 
        try{
            while (listening) {
                socket = serverSocket.accept();
                sockets.add(socket);
                outputStreams.add(new ObjectOutputStream(sockets.get(numConnected.get()).getOutputStream()));
                inputStreams.add(new ObjectInputStream(sockets.get(numConnected.get()).getInputStream()));
        	    new MazewarServerHandlerThread(this, this.numConnected.get()).start();
            }   

            serverSocket.close();
        } catch (IOException e) {
            System.err.println("ERROR: Could not create streams!");
            System.exit(-1);
        }
    }
    
    public static void main(String[] args) {
        new MazewarServer(args);
    }    
}
