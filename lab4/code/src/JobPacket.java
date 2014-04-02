import java.io.Serializable;
import java.util.List;

public class JobPacket implements Serializable {
    
    //possible request types

    public String hash;
    public boolean done = false;
    public boolean found = false; 
    public String result;
    public int partition;
    public int partitionsCompleted = 0;
}
