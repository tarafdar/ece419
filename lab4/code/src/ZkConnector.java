import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.io.IOException;

public class ZkConnector implements Watcher {

    // ZooKeeper Object
    ZooKeeper zooKeeper;

    // To block any operation until ZooKeeper is connected. It's initialized
    // with count 1, that is, ZooKeeper connect state.
    CountDownLatch connectedSignal = new CountDownLatch(1);
    
    // ACL, set to Completely Open
    protected static final List<ACL> acl = Ids.OPEN_ACL_UNSAFE;

    /**
     * Connects to ZooKeeper servers specified by hosts.
     */
    public void connect(String hosts) throws IOException, InterruptedException {

        zooKeeper = new ZooKeeper(
                hosts, // ZooKeeper service hosts
                5000,  // Session timeout in milliseconds
                this); // watcher - see process method for callbacks
	    connectedSignal.await();
    }

    /**
     * Closes connection with ZooKeeper
     */
    public void close() throws InterruptedException {
	    zooKeeper.close();
    }

    /**
     * @return the zooKeeper
     */
    public ZooKeeper getZooKeeper() {
        // Verify ZooKeeper's validity
        if (null == zooKeeper || !zooKeeper.getState().equals(States.CONNECTED)) {
	        throw new IllegalStateException ("ZooKeeper is not connected.");
        }
        return zooKeeper;
    }

    protected Stat exists(String path, Watcher watch) {
        
        Stat stat =null;
        try {
            stat = zooKeeper.exists(path, watch);
        } catch(Exception e) {
        }
        
        return stat;
    }

    protected KeeperException.Code create(String path, String data, CreateMode mode) {
        
        try {
            byte[] byteData = null;
            if(data != null) {
                byteData = data.getBytes();
            }
            zooKeeper.create(path, byteData, acl, mode);
            
        } catch(KeeperException e) {
            return e.code();
        } catch(Exception e) {
            return KeeperException.Code.SYSTEMERROR;
        }
        
        return KeeperException.Code.OK;
    }

    public String createRetPath(String path, String data, CreateMode mode) {
        String retPath = null;
        try {
            byte[] byteData = null;
            if(data != null) {
                byteData = data.getBytes();
            }
            retPath = zooKeeper.create(path, byteData, acl, mode);
            
        } catch(KeeperException e) {
            //ignore
        } catch(Exception e) {
           //ignore
        }
        
        return retPath;
    }
    
    protected List <String> getChildren(String path, Watcher watcher) {
        
        List<String> list = null;
        try {
            list = zooKeeper.getChildren(path, watcher);
        } catch(Exception e) {
		    e.printStackTrace();
        }
        
        return list;
    }

    protected String getData(String path, Watcher watcher, Stat stat) {
        String data = null;
        try {
            data = new String(zooKeeper.getData(path, watcher, stat), "UTF-8");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return data;
    }
    
    protected void setData(String path, String dataStr){
        byte [] data;
        data = dataStr.getBytes();

        try{
            zooKeeper.setData(path, data, -1);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
   
    protected void delete(String path){
        
        try{
            zooKeeper.delete(path, -1);
        }catch(Exception e){
            e.printStackTrace();

        }


    }
    
    public void process(WatchedEvent event) {
        // release lock if ZooKeeper is connected.
        if (event.getState() == KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }
}

