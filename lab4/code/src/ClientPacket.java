import java.io.Serializable;
import java.util.ArrayList;

public class ClientPacket implements Serializable {
    
    //possible request types
    public static final int JOB_SUBMIT = 0;
    public static final int JOB_QUERY = 1;
    //possible return statuses to query
    public static final int JOB_NOT_FOUND = 2;
    public static final int JOB_IN_PROG = 3;
    public static final int JOB_FINISHED = 4;
    //possible return statuses to submit
    public static final int JOB_SUBMITTED = 5;
    public static final int JOB_EXISTS = 6;
    //possible pass found values
    public static final int PASS_FOUND = 7;
    public static final int PASS_NOT_FOUND = 8;

    public String hash;
    public int requestType;
    public int returnStatus;
    public int passFound;
    public String password;
   
    public ClientPacket(String hash, int req) {
        this.hash = hash;
        this.requestType = req;
    } 
}
