import java.io.Serializable;
import java.util.ArrayList;


public class mazeWarPacket implements Serializable{
    
    public String clientName;

    public static final int JOIN_REQ = 0;
    public static final int JOIN_AT = 10;
    public static final int FORWARD = 20;
    public static final int BACKWARD = 30;
    public static final int RIGHT = 40;
    public static final int LEFT = 50;
    public static final int INIT = 60;
    public static final int QUIT = 70;
    public static final int KILLED = 80;
    public static final int BULLET_TICK = 90;
    public static final int FIRE = 100;
    public static final int TOKEN = 110;
    public static final int BYE = 120;

    public boolean isAck = false;
    public int clientID;
    public int killedClientID;

    public int numPlayers = 0;
    public int acks = 0;
    public String player_name;
    public ArrayList <String> playerNames = new ArrayList<String> ();
    public ArrayList <String> hostname = new ArrayList<String> ();
    public ArrayList <Integer> port = new ArrayList <Integer> ();
    public ArrayList <Point> points = new ArrayList <Point> ();
    public ArrayList <Direction> directions = new ArrayList <Direction> ();

    public int sequence_number;
    public int type;    
    
    public String typeToString() {
        switch(this.type) {
            case JOIN_REQ:
                    return "JOIN_REQ";
            case JOIN_AT:
                    return "JOIN_AT";
            case FIRE:
                    return "FIRE";
            case FORWARD:
                    return "FORWARD";
            case BACKWARD:
                    return "BACKWARD";
            case RIGHT:
                    return "RIGHT";
            case LEFT:
                    return "LEFT";
            case INIT:
                    return "INIT";
            case QUIT:
                    return "QUIT";
            case KILLED:
                    return "KILLED";
            case TOKEN:
                    return "TOKEN";
            case BYE:
                    return "BYE";
            case BULLET_TICK:
                    return "BULLET_TICK";

        }
        //impossible
        return null;
    }
}
