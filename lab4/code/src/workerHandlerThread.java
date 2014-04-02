import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher.Event.EventType;
import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class workerHandlerThread extends Thread {
    private Socket socket;
    private ArrayList<JobPacket> parentJobs;
    private ArrayList<JobPacket> childJobs;
    private int numPartitions;

    public workerHandlerThread(Socket socket, ArrayList<JobPacket> parentJobs, ArrayList<JobPacket> childJobs, int numPartitions) {
        super("workerHandlerThread");
        this.socket = socket;
        this.parentJobs = parentJobs;
        this.childJobs = childJobs;
        this.numPartitions = numPartitions;
    }

    public void run() {
        JobPacket packetFromWorker = null;
        JobPacket packetToWorker = null; 
        boolean listening = true;
        try {
            ObjectInputStream fromWorker = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream toWorker = new ObjectOutputStream(socket.getOutputStream());
            
            while(listening) {
                packetToWorker = null;
                synchronized(childJobs) {
                    if(childJobs.size() > 0) {
                        packetToWorker = childJobs.remove(0);
                    }
                }
                if (packetToWorker != null) {
                    toWorker.writeObject(packetToWorker);
                    packetFromWorker = (JobPacket) fromWorker.readObject();
                    //System.out.println("recieved packet back with hash " + packetFromWorker.hash);
                    int i;
                    if(packetFromWorker.found) {
                        System.out.println("recieved found packet");
                        synchronized(parentJobs) {
                            for(i=0; i<parentJobs.size(); i++) {
                                if(packetFromWorker.hash.equals(parentJobs.get(i).hash)) {
                                    parentJobs.get(i).done = true;
                                    parentJobs.get(i).found = true;
                                    parentJobs.get(i).result = packetFromWorker.result;
                                }    
                            }    
                        }        
                    }
                    else {
                        synchronized(parentJobs) {
                            for(i=0; i<parentJobs.size(); i++) {
                                if(packetFromWorker.hash.equals(parentJobs.get(i).hash)) {
                                    parentJobs.get(i).partitionsCompleted++;
                                    //System.out.println("updating count of acks - current ack count " + parentJobs.get(i).partitionsCompleted);
                                    if(parentJobs.get(i).partitionsCompleted == numPartitions)
                                        parentJobs.get(i).done = true;
                                }        
                            }    
                        }        
                    }         
                }    
            }    

		} catch (IOException e) {
		    //e.printStackTrace();
            listening = false;
            childJobs.add(packetToWorker);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
            listening = false;
		}
    }
}
