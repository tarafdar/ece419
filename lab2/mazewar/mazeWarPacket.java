import java.io.Serializable;


public class mazeWarPacket implements Serializable{
    
    public String clientName;

    public static final int BOARD_SETUP = 0;
    public static final int CLIENT_FIRE = 10;
    public static final int CLIENT_FORWARD = 20;
    public static final int CLIENT_BACKWARD = 30;
    public static final int CLIENT_RIGHT = 40;
    public static final int CLIENT_LEFT = 50;
    public static final int CLIENT_INIT = 60;
    public static final int CLIENT_QUIT = 70;


    public int numPlayers = 0;
    public String[] players = new String[4];
    public Point[] point = new Point[4];
    public Direction[] d = new Direction[4];

    public int sequence_number;
    public int type = BOARD_SETUP;
    
    public String typeToString() {
        switch(this.type) {
            case BOARD_SETUP:
                    return "BOARD_SETUP";
            case CLIENT_FIRE:
                    return "CLIENT_FIRE";
            case CLIENT_FORWARD:
                    return "CLIENT_FORWARD";
            case CLIENT_BACKWARD:
                    return "CLIENT_BACKWARD";
            case CLIENT_RIGHT:
                    return "CLIENT_RIGHT";
            case CLIENT_LEFT:
                    return "CLIENT_LEFT";
            case CLIENT_INIT:
                    return "CLIENT_INIT";
            case CLIENT_QUIT:
                    return "CLIENT_QUIT";
        }
        //impossible
        return null;
    }
}
