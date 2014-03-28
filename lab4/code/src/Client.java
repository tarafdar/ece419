import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.util.concurrent.CountDownLatch;

import java.io.*;
import java.net.*;
public class Client{

    static CountDownLatch nodeCreatedSignal = new CountDownLatch(1);
    static String myPath = "/JobTrackerP";
    Socket socket;
    static ObjectOutputStream out = null;
    static ObjectInputStream in =  null;
    ZkConnector zkc = null;
    ZooKeeper zk = null;
    Watcher watcher;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. B zkServer:clientPort");
            return;
        }


        Client c = new Client(args[0]);

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
        ClientPacket packetReceived;
        
        System.out.print("Accepting Input: ");
        ClientPacket cp = null;
        String[] tokens;
        String delims = "[ ]+";
        
        try{
        while ((userInput = stdIn.readLine()) != null && !(userInput.toLowerCase().equals("quit"))) {
            
            System.out.print("Accepting Input: ");
            tokens = userInput.split(delims);
            
            if(tokens[0].toLowerCase().equals("quit")){
                System.out.println("QUITTING");
                break;
            }
            else if(tokens.length != 2){
                System.out.println("Incorrect number of tokens, please enter in form of <JOB_REQUEST> <HASH>");
                System.out.print("Accepting Input: ");
                userInput = stdIn.readLine(); 
            }
            else if(!tokens[0].toLowerCase().equals("submit") && !tokens[0].toLowerCase().equals("query")){
                System.out.println("Incorrect operation, either <lookup> for new job, or <status> for query progress");
                System.out.print("Accepting Input: ");
                userInput = stdIn.readLine(); 
            }        
          
            if(tokens[0].toLowerCase().equals("submit")){
               cp = new ClientPacket(tokens[1] , ClientPacket.JOB_SUBMIT);  
            }
            else if(tokens[0].toLowerCase().equals("query")){
               cp = new ClientPacket(tokens[1] , ClientPacket.JOB_QUERY);  
            } 
                      
            
            waitAndSendData(cp);
             
            packetReceived = (ClientPacket)in.readObject();
        }
        }catch(IOException e){
            //IOExceptin resend the data when new JobTracker goes up
            waitAndSendData(cp);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        System.out.println("DONE");
    }
  
  
    public static void waitAndSendData(ClientPacket cp){
    
        try{       
            nodeCreatedSignal.await();
            out.writeObject(cp);    
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
     
    public void resetWatch(){
        
        try {
            zk.exists(myPath, watcher);
        } catch(KeeperException e) {
            System.out.println(e.code());
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }




    }
   
    
    public Client(String hosts){
        
        zkc = new ZkConnector();
        try {
            zkc.connect(hosts);
        } catch(Exception e) {
            System.out.println("Zookeeper connect "+ e.getMessage());
        }
        zk = zkc.getZooKeeper();
        watcher = new Watcher(){      
            @Override
                public void process(WatchedEvent event) {
                    handleEvent(event);
                }
        };
        

        resetWatch();


    }



    public void handleEvent(WatchedEvent event){
        // check for event type NodeCreated
        boolean isNodeCreated = event.getType().equals(EventType.NodeCreated);
        // verify if this is the defined znode
        boolean isMyPath = event.getPath().equals(myPath);
        String data;
        String[] tokens;
        String delims = "[ ]+";
        String hostname;
        int port;
       
        System.out.println("Receieved event");
        
        if (isNodeCreated && isMyPath) {
            System.out.println(myPath + " created!");
            nodeCreatedSignal.countDown();
            try{
                data = new String(zk.getData(myPath, watcher, null), "UTF-8");
                tokens = data.split(delims);
                hostname = tokens[0];
                port = Integer.parseInt(tokens[1]);
			    
                socket = new Socket(hostname, port);
                out = new ObjectOutputStream(socket.getOutputStream());
			    in = new ObjectInputStream(socket.getInputStream());
            }catch(KeeperException e){
                System.out.println(e.getMessage());
		        e.printStackTrace();
            }catch(InterruptedException e){
                System.out.println(e.getMessage());
		        e.printStackTrace();
            }catch (UnknownHostException e){
                System.out.println(e.getMessage());
		        e.printStackTrace();
            }catch (IOException e){
                System.out.println(e.getMessage());
		        e.printStackTrace();
            }

        }

        boolean isNodeDeleted = event.getType().equals(EventType.NodeDeleted);

        if(isNodeDeleted && isMyPath){
            System.out.println(myPath + " deleted!");
            nodeCreatedSignal = new CountDownLatch(1);
            out = null;
            in = null;
            resetWatch();
        }



    }
}
