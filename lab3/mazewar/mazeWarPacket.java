import java.io.Serializable;


public class mazeWarPacket implements Serializable{
    
    public String clientName;

    public static final int JOIN = 0;
    public static final int FIRE = 10;
    public static final int FORWARD = 20;
    public static final int BACKWARD = 30;
    public static final int RIGHT = 40;
    public static final int LEFT = 50;
    public static final int INIT = 60;
    public static final int QUIT = 70;
    public static final int KILLED = 80;
    public static final int BULLET_TICK = 90;


    public int numPlayers = 0;
    public String[] players = new String[4];
    public Point[] point = new Point[4];
    public Direction[] d = new Direction[4];

    public int sequence_number;
    public int type = BOARD_SETUP;
    
    public String[] hostname;
    public int[] port;
    
    public String typeToString() {
        switch(this.type) {
            case JOIN:
                    return "BOARD_SETUP";
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

        }
        //impossible
        return null;
    }
}
