import java.io.Serializable;
import java.util.List;

public class JobPacket implements Serializable {
    
    //possible request types

    public String hash;
    public int jobStatus = 0;
    public String result;
    public String childJobPath;   
    int start;
    int numWords;
    int workersAssigned;
    int workersCompleted = 0;
}
