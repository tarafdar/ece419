import java.io.Serializable;
import java.util.ArrayList;

public class FileServerPacket implements Serializable {
    
    //possible request types

    public int begin;
    public int numWords;
    public ArrayList <String> dictWords;
   
    public FileServerPacket(int begin, int numWords) {
        this.begin = begin;
        this.numWords = numWords;

    } 
}
