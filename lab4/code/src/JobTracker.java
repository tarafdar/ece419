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
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.net.*;

import java.io.IOException;

public class JobTracker {
    
    private String myPath = "/JobTrackerP";
    private String jobsPath = "/Jobs";
    private String workersPath = "/Workers";
    public static ArrayList <String> workers = new ArrayList<String> ();
    private static ZkConnector zkc;
    private static ZooKeeper zk;
    private Watcher jwatcher;
    private Watcher workerwatcher;
    //public AtomicInteger numWorkers = new AtomicInteger();
    private static String localhost = "uninitialized";
    private static final int numDictionaryWords = 265744; 
    private static int listenPort;
    private static String hostnameAndPort;
    private static ServerSocket serverSocket = null;
    public static CountDownLatch workersExist = new CountDownLatch(1);
    public static void main(String[] args) {
      
        if (args.length != 2) {
            System.out.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. JobTracker zkServer:clientPort listenPort");
            return;
        }
        boolean listening = true; 
        JobTracker J = new JobTracker(args[0]);
        listenPort = Integer.parseInt(args[1]);
         
        try{
            serverSocket = new ServerSocket(listenPort); 
            java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
            localhost = addr.getHostName();
        } catch (UnknownHostException e) {
            System.err.println("ERROR: Couldn't resolve Hostname.");
		    e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("ERROR: Couldn't get I/O for the connection.");
		    e.printStackTrace();
            System.exit(1);
        }
        
        hostnameAndPort = localhost + " " + listenPort;
        System.out.println("My hostname and port is " + hostnameAndPort);
        
        J.becomePrimary();
        J.createJobQueue();        
        J.workerWatch();

        //System.out.println("Sleeping...");
        //try{ Thread.sleep(5000); } catch (Exception e) {}
        while (listening) {
            try{
                Socket socket = serverSocket.accept();
                new JobTrackerHandlerThread(socket, zk, zkc, workers, numDictionaryWords, workersExist).start();
            } catch (IOException e) {
                System.err.println("ERROR: Couldn't get I/O for the connection.");
		        e.printStackTrace();
                System.exit(1);
            }
        }       
    }

    public JobTracker(String hosts) {
        zkc = new ZkConnector();
        try {
            zkc.connect(hosts);
        } catch(Exception e) {
            System.out.println("Zookeeper connect "+ e.getMessage());
        }
        zk = zkc.getZooKeeper(); 
        jwatcher = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                                handleEvent(event);
                        
                            } };
        workerwatcher = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                                handleWorkerEvent(event);
                        
                            } };
    }
    
    private void becomePrimary() {

        Stat stat = zkc.exists(myPath, jwatcher);
        if (stat == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Becoming primary, creating " + myPath);
            Code ret = zkc.create(
                        myPath,         // Path of znode
                        hostnameAndPort,           // Data not needed.
                        CreateMode.EPHEMERAL   // Znode type, set to EPHEMERAL.
                        );
            if (ret == Code.OK) System.out.println("Became Primary!");
        } 
    }
    
    private void workerWatch() {
        zkc.exists(workersPath, workerwatcher);
    }

    private void createJobQueue() {
           
        Stat stat = zkc.exists(jobsPath, null);
        if (stat == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Creating JobQueue " + jobsPath);
            Code ret = zkc.create(
                        jobsPath,         // Path of znode
                        null,           // Data not needed.
                        CreateMode.PERSISTENT   // Znode type, set to EPHEMERAL.
                        );
            if (ret == Code.OK) System.out.println("Created JobQueue!");
        } 
    }     

    private void handleEvent(WatchedEvent event) {
        String path = event.getPath();
        EventType type = event.getType();
        if(path.equalsIgnoreCase(myPath)) {
            if (type == EventType.NodeDeleted) {
                System.out.println(myPath + " deleted! Let's go!");       
                becomePrimary(); // try to become the boss
            }
            if (type == EventType.NodeCreated) {
                System.out.println(myPath + " created!");       
                //try{ Thread.sleep(5000); } catch (Exception e) {}
                becomePrimary(); // re-enable the watch
            }
        }
    }

    private void handleWorkerEvent(WatchedEvent event) {
        String path = event.getPath();
        EventType type = event.getType();
        if(path.contains("LeafWorker") && type == EventType.NodeCreated) {
            System.out.println("Worker has been created");
            synchronized(workers) {
                
                workers.add(path);
                //if it is the first worker countdown the countdown latch to signal the handler threads that workers exist now   
                if(workers.size() == 1)
                   workersExist.countDown(); 
            }
              
            //synchronized(numWorkers) {
            //    numWorkers.incrementAndGet();
            //}
            workerWatch();
        }
        if(path.contains("LeafWorker") && type == EventType.NodeDeleted) {
            System.out.println("Worker has been deleted");
            //synchronized(numWorkers) {
            //    numWorkers.decrementAndGet();
            //}
            synchronized(workers) {
                workers.remove(path);
                if(workers.size() == 0)
                    workersExist = new CountDownLatch(1);

            }
            //TODO: have to handle reassignment of jobs here
            workerWatch();
        }
        
    }
}
