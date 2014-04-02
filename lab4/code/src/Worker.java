
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher.Event.EventType;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

import java.io.IOException;

import java.util.ArrayList;

import java.util.concurrent.CountDownLatch;

import java.util.PriorityQueue;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

public class Worker{
    private static int numDictionaryWords = 265744;
    private static int numPartitions = 16;
    private static int numWordsPerPartition = numDictionaryWords/numPartitions;
//    private String myPathRoot = "/Workers";
//    private String myPath = "/Workers/LeafWorker";
    private String parentJob = "/JobQ/ParentJobs";
    private String childJob = "/Jobs";
    private String fileServerPath = "/FileServerP";
    private String jobTrackerPath = "/JobTrackerP";
    private static ZkConnector zkc;
    private static ZooKeeper zk = null;
    private Watcher jobTrackerWatcher;
    private Watcher fileServerWatcher;
    private static PriorityQueue <String> jobQueue = new PriorityQueue<String>();
    static CountDownLatch fileServerSignal = new CountDownLatch(1);
    static CountDownLatch jobTrackerSignal = new CountDownLatch(1);
    
    Socket socketFS;
    static ObjectOutputStream outFS = null;
    static ObjectInputStream inFS =  null;
    
    Socket socketJT;
    static ObjectOutputStream outJT = null;
    static ObjectInputStream inJT =  null;
    
    static String hostNameandPort = null;
    
    private static ServerSocket serverSocket = null;
    private static int listenPort;
    public static void main(String[] args){


        if (args.length != 2) {
            System.out.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. Worker zkServer:clientPort");
            return;
        }
        
        listenPort = Integer.parseInt(args[1]);
        Worker W = new Worker(args[0]);
        
        FileServerPacket fp = null;
        FileServerPacket packetReceivedFS = null;
        W.resetFileServerWatcher();
        
        String hashJob = null;
        String hash = null;
        String match = null; 
        int i;
        boolean found = false;
        JobPacket packetReceivedJT = null; 
        JobPacket packetToJT = null; 
        
        
        
        while(true){
             
       
            try{
               jobTrackerSignal.await();
               packetReceivedJT = (JobPacket)inJT.readObject();  
                
                try{
                    int start = packetReceivedJT.partition*(numWordsPerPartition);
                    int numWords = numWordsPerPartition;      
                    fp = new FileServerPacket(start, numWords);
                    waitAndSendFSData(fp);
                    packetReceivedFS = (FileServerPacket)inFS.readObject();
                    
                    //traverse through the dictionary words received from fileserver and calculate the hash
                    for(i=0; i < packetReceivedFS.dictWords.size(); i++){
                           
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        BigInteger hashint = new BigInteger(1, md5.digest(packetReceivedFS.dictWords.get(i).getBytes()));
                        hash = hashint.toString(16);
                        while (hash.length() < 32) hash = "0" + hash;
                        if(hash.equals(hashJob)){
                            found = true;
                            match = packetReceivedFS.dictWords.get(i);
                            break;
                        }
                             
                    }                 
                    



                }catch(IOException e){
                    waitAndSendFSData(fp);
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                }catch (NoSuchAlgorithmException nsae){
                    //ignore
                }
                
                packetToJT = packetReceivedJT; 
                if(found){
                    //Sets data in form of <hash> <done?> <value>, this is set in the parent job node
                    packetToJT.found = true;
                }
                packetToJT.done = true;

                //after processing mini job delete the node
                outFS.writeObject(packetToJT); 
                 
            }catch(Exception e){
                jobTrackerSignal = new CountDownLatch(1);
                continue;
            }
        }


   }


   public Worker(String hosts){
        zkc = new ZkConnector();
        try {
            zkc.connect(hosts);
        } catch(Exception e) {
            System.out.println("Zookeeper connect "+ e.getMessage());
        }

        
        jobTrackerWatcher = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                                handleJobTrackerEvent(event);
                        

 
                            } };
        fileServerWatcher = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                                handleFileServerEvent(event);
                        
                            } };
        Stat stat = zkc.exists(fileServerPath, fileServerWatcher);
        if(stat != null) {
            fileServerSignal.countDown();
            connectToFileServer();
        }

        stat = zkc.exists(jobTrackerPath, jobTrackerWatcher);
        if(stat != null) {
            jobTrackerSignal.countDown();
            connectToJobTracker();
        }
       

   }
    
    public void connectToFileServer () {
        String data;
        String[] tokens;
        String delims = "[ ]+";
        String hostname;
        int port;

        try{
            data = zkc.getData(fileServerPath, fileServerWatcher, null);
            tokens = data.split(delims);
            hostname = tokens[0];
            port = Integer.parseInt(tokens[1]);
			    
            socketFS = new Socket(hostname, port);
            outFS = new ObjectOutputStream(socketFS.getOutputStream());
			inFS = new ObjectInputStream(socketFS.getInputStream());
        }catch (UnknownHostException e){
            System.out.println(e.getMessage());
		    e.printStackTrace();
        }catch (IOException e){
            System.out.println(e.getMessage());
		    e.printStackTrace();
        }
    }

    public void connectToJobTracker () {
        String data;
        String[] tokens;
        String delims = "[ ]+";
        String hostname;
        int port;

        try{
            data = zkc.getData(jobTrackerPath, jobTrackerWatcher, null);
            tokens = data.split(delims);
            hostname = tokens[0];
            port = Integer.parseInt(tokens[2]);
			    
            socketJT = new Socket(hostname, port);
            outJT = new ObjectOutputStream(socketJT.getOutputStream());
			inJT = new ObjectInputStream(socketJT.getInputStream());
        }catch (UnknownHostException e){
            System.out.println(e.getMessage());
		    e.printStackTrace();
        }catch (IOException e){
            System.out.println(e.getMessage());
		    e.printStackTrace();
        }
    }
   
   
    
    public static void waitAndSendFSData(FileServerPacket fp){
    
        try{       
            fileServerSignal.await();
            outFS.writeObject(fp);    
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
    
    public void resetFileServerWatcher(){
        Stat stat = zkc.exists(fileServerPath, fileServerWatcher);
    }
   
    public void resetJobTrackerWatcher(){
        Stat stat = zkc.exists(jobTrackerPath, jobTrackerWatcher);
    }

//   private void startRootWorker(){
//        Stat stat = zkc.exists(myPathRoot, null);
//        if (stat == null) {              // znode doesn't exist; let's try creating it
//            System.out.println("Creating root worker " + myPathRoot);
//            Code ret = zkc.create(
//                        myPathRoot,         // Path of znode
//                        null,           // Data not needed.
//                        CreateMode.PERSISTENT   // Znode type, set to EPHEMERAL.
//                        );
//            if (ret == Code.OK) System.out.println("Became Primary!");
//        } 
//
//    }
//
//   private void startLeafWorker(){
//            System.out.println("Creating leaf worker " + myPath);
//            String ret = zkc.createRetPath(
//                        myPath,         // Path of znode
//                        null,           // Data not needed.
//                        CreateMode.EPHEMERAL_SEQUENTIAL   // Znode type, set to EPHEMERAL.
//                        );
//
//
//
//        }
        
    
    private void handleFileServerEvent(WatchedEvent event){
        // check for event type NodeCreated
        boolean isNodeCreated = event.getType().equals(EventType.NodeCreated);
        // verify if this is the defined znode
        String path = event.getPath();       
        //System.out.println("Receieved event");
        
        if (isNodeCreated) {
          //  System.out.println(myPath + " created!");
            fileServerSignal.countDown();
            connectToFileServer();
        }

        boolean isNodeDeleted = event.getType().equals(EventType.NodeDeleted);

        if(isNodeDeleted){
            //System.out.println(myPath + " deleted!");
            fileServerSignal = new CountDownLatch(1);
            outFS = null;
            inFS = null;
            //resetFileServerWatcher();
        }
       resetFileServerWatcher();
        
    } 

    private void handleJobTrackerEvent(WatchedEvent event){
        // check for event type NodeCreated
        boolean isNodeCreated = event.getType().equals(EventType.NodeCreated);
        // verify if this is the defined znode
        String path = event.getPath();       
        //System.out.println("Receieved event");
        
        if (isNodeCreated) {
          //  System.out.println(myPath + " created!");
            jobTrackerSignal.countDown();
            connectToJobTracker();
        }

        boolean isNodeDeleted = event.getType().equals(EventType.NodeDeleted);

        if(isNodeDeleted){
            //System.out.println(myPath + " deleted!");
            jobTrackerSignal = new CountDownLatch(1);
            outJT = null;
            inJT = null;
            //resetFileServerWatcher();
        }
       resetJobTrackerWatcher();
        
    } 

}
