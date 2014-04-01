
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

    private String myPathRoot = "/Workers";
    private String myPath = "/Workers/LeafWorker";
    private String myJobPath = "/Jobs/ParentJob/ChildJob";
    private String jobPath = "/Jobs";
    private String fileServerPath = "/FileServerP";
    private static ZkConnector zkc;
    private static ZooKeeper zk = null;
    private Watcher jobWatcher;
    private Watcher fileServerWatcher;
    private static PriorityQueue <String> jobQueue = new PriorityQueue<String>();
    static CountDownLatch nodeCreatedSignal = new CountDownLatch(1);
    
    Socket socket;
    static ObjectOutputStream out = null;
    static ObjectInputStream in =  null;
   
    private int myWorkerID = 0; 
    
    public static void main(String[] args){


        if (args.length != 2) {
            System.out.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. Worker zkServer:clientPort");
            return;
        }
        
        Worker W = new Worker(args[0]);
        W.startRootWorker();
        W.startLeafWorker();
        W.startJobWatch();
        FileServerPacket fp = null;
        FileServerPacket packetReceived = null;
        W.resetFileServerWatcher();
        String data; 
        String delims = "[ ]+";
        String hashJob = null;
        String hash = null;
        String match = null; 
        int i;
        boolean found = false;
        String currentJobPath = null;
        String parentJob = "/Jobs/ParentJob-0000000000";
        int parentJobLength = parentJob.length();
        String [] tokens; 
        while(true){
            
            if(jobQueue.size()>0){
                currentJobPath = jobQueue.poll();
                data = zkc.getData(currentJobPath, null, null);
                tokens = data.split(delims);
                fp = new FileServerPacket(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                hashJob = tokens[0];
                
                
                try{
                    
                    waitAndSendData(fp);
                    packetReceived = (FileServerPacket)in.readObject();
                    
                    //traverse through the dictionary words received from fileserver and calculate the hash
                    for(i=0; i < packetReceived.dictWords.size(); i++){
                           
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        BigInteger hashint = new BigInteger(1, md5.digest(packetReceived.dictWords.get(i).getBytes()));
                        hash = hashint.toString(16);
                        while (hash.length() < 32) hash = "0" + hash;
                        if(hash.equals(hashJob)){
                            found = true;
                            match = packetReceived.dictWords.get(i);
                            break;
                        }
                             
                    }                 
                    
                    if(found){
                        //Sets data in form of <hash> <done?> <value>, this is set in the parent job node
                        zkc.setData(currentJobPath.substring(0, parentJobLength - 1), hash + " 1 " + match);                        
                    }
                    
                    //after processing mini job delete the node
                    zkc.delete(currentJobPath); 



                }catch(IOException e){
                    waitAndSendData(fp);
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                }catch (NoSuchAlgorithmException nsae){
                    //ignore

                }
            
            
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
 
        jobWatcher = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                                handleJobEvent(event);
                        
                            } };

        fileServerWatcher = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                                handleFileServerEvent(event);
                        
                            } };
        Stat stat = zkc.exists(fileServerPath, fileServerWatcher);
        if(stat != null) {
            nodeCreatedSignal.countDown();
            connectToFileServer();
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
			    
            socket = new Socket(hostname, port);
            out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
        }catch (UnknownHostException e){
            System.out.println(e.getMessage());
		    e.printStackTrace();
        }catch (IOException e){
            System.out.println(e.getMessage());
		    e.printStackTrace();
        }
    }

    public static void waitAndSendData(FileServerPacket fp){
    
        try{       
            nodeCreatedSignal.await();
            out.writeObject(fp);    
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
    
    public void resetFileServerWatcher(){
        
        Stat stat = zkc.exists(fileServerPath, fileServerWatcher);

    }
   
   private void startJobWatch(){
        Stat stat = zkc.exists(jobPath, null);



   }

   private void startRootWorker(){
        Stat stat = zkc.exists(myPathRoot, null);
        if (stat == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Creating root worker " + myPathRoot);
            Code ret = zkc.create(
                        myPathRoot,         // Path of znode
                        null,           // Data not needed.
                        CreateMode.PERSISTENT   // Znode type, set to EPHEMERAL.
                        );
            if (ret == Code.OK) System.out.println("Became Primary!");
        } 

    }

   private void startLeafWorker(){
            System.out.println("Creating leaf worker " + myPath);
            String ret = zkc.createRetPath(
                        myPath,         // Path of znode
                        null,           // Data not needed.
                        CreateMode.EPHEMERAL_SEQUENTIAL   // Znode type, set to EPHEMERAL.
                        );



            myWorkerID = Integer.parseInt(ret.substring(ret.length()-10,ret.length()-1));
        }
        
    private void handleJobEvent(WatchedEvent event){
       
        String path = event.getPath();
        EventType type = event.getType();
        
        String[] tokens;
        String delims = "[ ]+";
        String data = zkc.getData(path, jobWatcher, null);
        tokens = data.split(delims);
        
        if(path.contains("ChildJob") && type == EventType.NodeCreated && path.equals(tokens[3])){
            jobQueue.add(path);
        }
        
        
    }
    
    
    
    private void handleFileServerEvent(WatchedEvent event){
        // check for event type NodeCreated
        boolean isNodeCreated = event.getType().equals(EventType.NodeCreated);
        // verify if this is the defined znode
        String path = event.getPath();       
        //System.out.println("Receieved event");
        
        if (isNodeCreated) {
          //  System.out.println(myPath + " created!");
            nodeCreatedSignal.countDown();
            connectToFileServer();
        }

        boolean isNodeDeleted = event.getType().equals(EventType.NodeDeleted);

        if(isNodeDeleted){
            //System.out.println(myPath + " deleted!");
            nodeCreatedSignal = new CountDownLatch(1);
            out = null;
            in = null;
            resetFileServerWatcher();
        }
        
    } 

}
