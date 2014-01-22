import java.io.*;
import java.net.*;

public class BrokerClient{
	public static void errorHandling (String symbol, BrokerPacket packetFromServer) {
        if (packetFromServer.error_code == BrokerPacket.ERROR_INVALID_SYMBOL)
            System.out.println (symbol + " invalid.");
    }
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		Socket echoSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;
			
			if(args.length == 2 ) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
		    LookupSocket = new Socket(hostname, port);

			lookup_out = new ObjectOutputStream(LookupSocket.getOutputStream());
			lookup_in = new ObjectInputStream(Socket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
        
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
        
        System.out.println("Enter symbol or x for exit");
		System.out.print(">");
		while ((userInput = stdIn.readLine()) != null && !userInput.equals("x")) {
			/* make a new request packet */
			BrokerPacket packet = new BrokerPacket();
			
            String [] input_args = userInput.split(" ");
            if (input_args[0].equals("local")) {
                packet.type = BrokerPacket.LOOKUP_REQUEST;
                packet.exchange = input_args[1];
		        try {
                


            }
            else {
                packet.type = BrokerPacket.BROKER_REQUEST;
                packet.symbol = userInput;
			    out.writeObject(packet);

			    /* print server reply */
			    BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) in.readObject();

			    if (packetFromServer.type == BrokerPacket.BROKER_QUOTE)
				    System.out.println("Quote from broker: " + packetFromServer.quote);
                else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                    errorHandling(userInput, packetFromServer);
            }
			/* re-print console prompt */
			System.out.print(">");
            
		}

		/* tell server that i'm quitting */
		BrokerPacket packetToServer = new BrokerPacket();
		packetToServer.type = BrokerPacket.BROKER_BYE;
		out.writeObject(packetToServer);

		out.close();
		in.close();
		stdIn.close();
		echoSocket.close();
	}
}
