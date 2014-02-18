import java.io.*;
import java.net.*;

public class BrokerExchange{
	public static void errorHandling (String symbol, BrokerPacket packetFromServer) {
        if (packetFromServer.error_code == BrokerPacket.ERROR_INVALID_SYMBOL)
            System.out.println (symbol + " invalid.");
        else if (packetFromServer.error_code == BrokerPacket.ERROR_OUT_OF_RANGE)
            System.out.println (symbol + " out of range.");
        else if (packetFromServer.error_code == BrokerPacket.ERROR_SYMBOL_EXISTS)
            System.out.println (symbol + " exists.");
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
			echoSocket = new Socket(hostname, port);

			out = new ObjectOutputStream(echoSocket.getOutputStream());
			in = new ObjectInputStream(echoSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
        System.out.println("Enter command or x for exit");
		System.out.print(">");
		while ((userInput = stdIn.readLine()) != null
				&& !userInput.equals("x")) {
			/* make a new request packet */
			BrokerPacket packetToServer = new BrokerPacket();
			
            
            String [] input_args = userInput.split(" ");

            if(input_args[0].equals("add") && input_args.length == 2){
                packetToServer.type = BrokerPacket.EXCHANGE_ADD;
                packetToServer.symbol = input_args[1];
			    out.writeObject(packetToServer);

			/* print server reply */
			    BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) in.readObject();

		    	if (packetFromServer.type == BrokerPacket.EXCHANGE_REPLY)
			    	System.out.println(input_args[1] + " added.");
                else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                    errorHandling(input_args[1], packetFromServer);
            }
            else if(input_args[0].equals("update") && input_args.length == 3){
                packetToServer.type = BrokerPacket.EXCHANGE_UPDATE;
                packetToServer.symbol = input_args[1] + " " + input_args[2];
			    out.writeObject(packetToServer);

			/* print server reply */
			    BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) in.readObject();

		    	if (packetFromServer.type == BrokerPacket.EXCHANGE_REPLY)
			    	System.out.println(input_args[1] + " updated to " + input_args[2]+ ".");
                else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                    errorHandling(input_args[1], packetFromServer);
            }
            else if(input_args[0].equals("remove") && input_args.length == 2){
                packetToServer.type = BrokerPacket.EXCHANGE_REMOVE;
                packetToServer.symbol = input_args[1];
			    out.writeObject(packetToServer);


			/* print server reply */
			    BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) in.readObject();

		    	if (packetFromServer.type == BrokerPacket.EXCHANGE_REPLY)
			    	System.out.println(input_args[1] + " removed.");
                else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                    errorHandling(input_args[1], packetFromServer);
            }
            else {
			    System.out.println("Invalid command.");
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
