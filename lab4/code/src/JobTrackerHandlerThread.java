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
    private String jobsPath;
    private final int numPartitions;
    private static ArrayList<JobPacket> parentJobs;
    private static ArrayList<JobPacket> childJobs;
    //private String parentPath = "/Jobs/ParentJob";
    //private String childPath = "/Jobs/ParentJob/ChildJob";
    //private String workersPath = "/Workers";
    //public List <String> workers;
    //public int numDictionaryWords; 
    //public ZkConnector zkc;
    //public ZooKeeper zk;
    //public CountDownLatch workersExist;
    //public AtomicInteger numWorkers;
    
    public JobTrackerHandlerThread(Socket socket, ArrayList<JobPacket> parentJobs, ArrayList<JobPacket> childJobs, int numPartitions) {
        super("JobTrackerHandlerThread");
        this.socket = socket;
        //this.zkc = zkc;
        //this.zk = zk;
        //this.workers = workers;
        this.parentJobs = parentJobs;
        this.childJobs = childJobs;
        this.numPartitions = numPartitions;
        //this.workersExist = workersExist;
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
                    JobPacket j;
                    j = query(packetFromClient.hash);
                    if(j == null)
                        packetToClient.returnStatus = ClientPacket.JOB_NOT_FOUND;
                    else {
                        if(j.done)
                            packetToClient.returnStatus = ClientPacket.JOB_FINISHED;
                        else
                            packetToClient.returnStatus = ClientPacket.JOB_IN_PROG;
                        if (j.found) {
                            packetToClient.passFound = ClientPacket.PASS_FOUND;
                            packetToClient.password = j.result;
                        }
                        else 
                            packetToClient.passFound = ClientPacket.PASS_NOT_FOUND;
                    }    
                }
                toClient.writeObject(packetToClient);
            
            }    

		} catch (IOException e) {
		   // e.printStackTrace();
            listening = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
            listening = false;
		}
    }

    public boolean createJob(String hash) {
        int i;
        JobPacket j = null;
        //first check the parent array to see if it exists if not create it
        synchronized(parentJobs) {
            for(i=0; i<parentJobs.size(); i++) {
                if(parentJobs.get(i).hash.equals(hash))
                    j = parentJobs.get(i); 
            }
        }
        if(j!= null)
            return false;
        j = new JobPacket();
        j.hash = hash;
                
        synchronized(parentJobs) {
            parentJobs.add(j);
        }

        synchronized(childJobs) {
            for(i=0; i<numPartitions; i++) {
                j = new JobPacket();
                j.hash = hash;
                j.partition = i;
                childJobs.add(j);
            }
        }
        return true;    
    }
    
    public JobPacket query (String hash) {
        int i;
        JobPacket j = null;
        //first check the parent array to see if it exists if not create it
        synchronized(parentJobs) {
            for(i=0; i<parentJobs.size(); i++) {
                if(parentJobs.get(i).hash.equals(hash))
                    j = parentJobs.get(i); 
            }
        }
        return j;
    }    

//    public boolean createJob(String hash) {
//        String data;
//        //iterate through the children of the jobs node to see whether or not the job already exists
//        List<String> list = zkc.getChildren(jobsPath, null);
//        
//
//        System.out.println("creating job: number of parent jobs " + list.size());
//        boolean alreadyExists = false;
//        if (list.size() != 0) {
//            for(String s : list){
//                System.out.println("path of child " + s);
//                data = zkc.getData(jobsPath + "/" + s, null, null);
//                System.out.println("data from child " + data);
//                String tokens[] = data.split(" ");
//                if(tokens[0] == hash) alreadyExists = true;
//            }    
//        }
//        if(alreadyExists) {
//            System.out.println("not creating job, job already exists");
//            //the parent job node already exists;
//            return false;
//        }    
//        else {
//            System.out.println("creating new job, job doesnt exist");
//            zkc.create(
//                        parentPath,
//                        hash + " 0",
//                        CreateMode.PERSISTENT_SEQUENTIAL
//                      );
//            //list = zk.getChildren(workersPath, null);
//            //int numWorkers = list.size();
//            
//            
//            //stall until a worker exists
//            //try{       
//            //   workersExist.await();    
//            //} catch(Exception e) {
//            //    System.out.println(e.getMessage());
//            //    e.printStackTrace();
//            //}
//             
//            //while(numWorkers.get() == 0);
//            synchronized(workers) {
//                int numWordsPerPartition = numDictionaryWords / workers.size();
//                //the words leftover because of uneven division
//                int numWordsmissing = numDictionaryWords - (workers.size() * numWordsPerPartition);
//                int i;
//                for(i=0; i<workers.size(); i++) {
//                    if(i == workers.size() - 1) { 
//                        System.out.println("creating child job with data " + hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition + numWordsmissing) + " " + workers.get(i));
//                        zkc.create(
//                                childPath,
//                                hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition + numWordsmissing) + " " + workers.get(i),
//                                CreateMode.PERSISTENT_SEQUENTIAL);
//
//                    }
//                    else {
//                        System.out.println("creating child job with data " + hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition) + " " + workers.get(i));
//                        zkc.create(
//                                childPath,
//                                hash + " " + (i*numWordsPerPartition) + " " + (numWordsPerPartition) + " " + workers.get(i),
//                                CreateMode.PERSISTENT_SEQUENTIAL);
//                    }
//                 }
//            }
//            System.out.println("finished creating job");
//            return true;
//        }
//    }    
}
