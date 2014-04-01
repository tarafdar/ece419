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

public class JobTrackerHandlerThread extends Thread {
    private Socket socket;
    private String jobsPath = "/Jobs";
    private String parentPath = "/Jobs/ParentJob";
    private String childPath = "/Jobs/ParentJob/ChildJob";
    private String workersPath = "/Workers";
    public ArrayList <String> workers;
    public int numDictionaryWords; 
    public ZkConnector zkc;
    public ZooKeeper zk;
    public CountDownLatch workersExist;
    //public AtomicInteger numWorkers;
    
    public JobTrackerHandlerThread(Socket socket, ZooKeeper zk, ZkConnector zkc, ArrayList <String> workers, int numDictionaryWords, CountDownLatch workersExist) {
        super("JobTrackerHandlerThread");
        this.socket = socket;
        this.zkc = zkc;
        this.zk = zk;
        this.workers = workers;
        this.numDictionaryWords = numDictionaryWords;
        this.workersExist = workersExist;
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
                    if(createJob(packetFromClient.hash))
                       packetToClient.returnStatus = ClientPacket.JOB_SUBMITTED;
                    else
                       packetToClient.returnStatus = ClientPacket.JOB_EXISTS; 
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

    public boolean createJob(String hash) {
        String data;
        //iterate through the children of the jobs node to see whether or not the job already exists
        List<String> list = zkc.getChildren(jobsPath, null);
        

        System.out.println("creating job: number of parent jobs " + list.size());
        boolean alreadyExists = false;
        if (list.size() != 0) {
            for(String s : list){
                System.out.println("path of child " + s);
                data = zkc.getData(s, null, null);
                System.out.println("data from child " + data);
                String tokens[] = data.split(" ");
                if(tokens[0] == hash) alreadyExists = true;
            }    
        }
        if(alreadyExists) {
            System.out.println("not creating job, job already exists");
            //the parent job node already exists;
            return false;
        }    
        else {
            System.out.println("creating new job, job doesnt exist");
            zkc.create(
                        parentPath,
                        hash + " 0",
                        CreateMode.PERSISTENT_SEQUENTIAL
                      );
            //list = zk.getChildren(workersPath, null);
            //int numWorkers = list.size();
            
            
            //stall until a worker exists
            try{       
               workersExist.await();    
            } catch(Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
             
            //while(numWorkers.get() == 0);
            synchronized(workers) {
                int numWordsPerPartition = numDictionaryWords / workers.size();
                //the words leftover because of uneven division
                int numWordsmissing = numDictionaryWords - (workers.size() * numWordsPerPartition);
                int i;
                for(i=0; i<workers.size(); i++) {
                    if(i == workers.size() - 1) { 
                        System.out.println("creating child job with data " + hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition + numWordsmissing) + " " + workers.get(i));
                        zkc.create(
                                childPath,
                                hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition + numWordsmissing) + " " + workers.get(i),
                                CreateMode.PERSISTENT_SEQUENTIAL);

                    }
                    else {
                        System.out.println("creating child job with data " + hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition) + " " + workers.get(i));
                        zkc.create(
                                childPath,
                                hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition) + " " + workers.get(i),
                                CreateMode.PERSISTENT_SEQUENTIAL);
                    }
                 }
            }
            System.out.println("finished creating job");
            return true;
        }
    }    
}
