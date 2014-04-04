
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
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

import java.net.*;

import java.io.IOException;

public class waitForWorkerConnections extends Thread {

    private ZkConnector zkc;
    private ServerSocket workerSocket;
    private ArrayList<JobPacket> parentJobs;
    private ArrayList<JobPacket> childJobs;
    private int numPartitions;

    //public List <String> workers;
    //private ArrayList <ObjectInputStream> inStreamArray;
    //private ArrayList <ObjectOutputStream> outStreamArray;
    
    public waitForWorkerConnections (ServerSocket workerSocket, ArrayList<JobPacket> parentJobs, ArrayList<JobPacket> childJobs, int numPartitions, ZkConnector zkc) {
        super("waitForWorkerConnections");
        this.workerSocket = workerSocket;
        this.parentJobs = parentJobs;
        this.childJobs = childJobs;
        this.numPartitions = numPartitions;
        this.zkc = zkc;
        //this.inStreamArray = inStreamArray;
        //this.outStreamArray = outStreamArray;
    }        
    
     
    public void run() {
       boolean listening = true;
       while (listening) {
            try{
                Socket socket = workerSocket.accept();
                new workerHandlerThread(socket, parentJobs, childJobs, numPartitions, zkc).start();
            } catch (IOException e) {
                System.err.println("ERROR: Couldn't get I/O for the connection.");
		        e.printStackTrace();
                System.exit(1);
            }
       }       
    }
}  
