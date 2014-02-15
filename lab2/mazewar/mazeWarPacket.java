import java.io.Serializable;


public class mazeWarPacket implements Serializable{
    
    public string clientName;

    public static final int BOARD_SETUP = 0;
    public static final int CLIENT_FIRE = 10;
    public static final int CLIENT_FORWARD = 20;
    public static final int CLIENT_BACKWARD = 30;
    public static final int CLIENT_RIGHT = 40;
    public static final int CLIENT_LEFT = 50;


    public int numPlayers = 0;
    public String[] players = new String[4];
    public int[] xCoord = new int[4];
    public int[] yCoord = new int[4];


    public int type = BOARD_SETUP;


}
