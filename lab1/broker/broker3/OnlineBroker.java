import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.*;
public class OnlineBroker {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int lookupPort = 4444;
        String lookupHost = "localhost";
        String exchange = "garbage"; 
        ServerSocket serverSocketClient = null;
        boolean listening = true;
        int listenPort = 4444;
        Socket lookupSocket = null;
        ObjectOutputStream toLookup = null;
        ObjectInputStream fromLookup = null;
        try {
        	if(args.length == 4) {
        		listenPort = Integer.parseInt(args[2]);
                lookupPort = Integer.parseInt(args[1]);
                lookupHost = args[0];
                exchange = args[3];
                lookupSocket = new Socket(lookupHost, lookupPort);
                toLookup = new ObjectOutputStream(lookupSocket.getOutputStream());
                fromLookup = new ObjectInputStream(lookupSocket.getInputStream());
        		serverSocketClient = new ServerSocket(listenPort);
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

        BrokerPacket packetToLookup = new BrokerPacket();
        packetToLookup.locations = new BrokerLocation[1];
        packetToLookup.locations[0] = new BrokerLocation(InetAddress.getLocalHost().getHostName(), listenPort);
        packetToLookup.type = BrokerPacket.LOOKUP_REGISTER;
        packetToLookup.exchange = exchange; 
        toLookup.writeObject(packetToLookup);
        BrokerPacket packetFromLookup;
        packetFromLookup = (BrokerPacket) fromLookup.readObject(); 
        
        BrokerPacket packetGoodBye = new BrokerPacket();
        packetGoodBye.type = BrokerPacket.BROKER_BYE; 
        toLookup.writeObject(packetGoodBye);
        
        toLookup.close();
        fromLookup.close();
        lookupSocket.close();
              
        while (listening) {
        	new OnlineBrokerHandlerThread(serverSocketClient.accept(),exchange, lookupHost, lookupPort).start();
        }

        serverSocketClient.close();
    }
}
