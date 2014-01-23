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
        else if (packetFromServer.error_code == BrokerPacket.ERROR_INVALID_EXCHANGE)
            System.out.println ("invalid exchange " + symbol + ".");
    }

    public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		Socket LookupSocket = null;
        Socket BrokerSocket = null;
		
        ObjectOutputStream lookup_out = null;
		ObjectInputStream lookup_in = null;

		ObjectOutputStream broker_out = null;
		ObjectInputStream broker_in = null;
		String exchange = "not_initialized";

		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;
            	
			if(args.length == 3 ) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
                exchange = args[2];
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
			LookupSocket = new Socket(hostname, port);

			lookup_out = new ObjectOutputStream(LookupSocket.getOutputStream());
			lookup_in = new ObjectInputStream(LookupSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
        BrokerPacket lookupPacket = new BrokerPacket();
        lookupPacket.type = BrokerPacket.LOOKUP_REQUEST;
        lookupPacket.exchange = exchange;
        lookup_out.writeObject(lookupPacket);
        
        BrokerPacket packetFromLookup;
	    packetFromLookup = (BrokerPacket) lookup_in.readObject();
        
        if(packetFromLookup.type == BrokerPacket.LOOKUP_REPLY) { 
            try {
                BrokerSocket = new Socket (packetFromLookup.locations[0].broker_host, packetFromLookup.locations[0].broker_port);
			    broker_out = new ObjectOutputStream(BrokerSocket.getOutputStream());
			    broker_in = new ObjectInputStream(BrokerSocket.getInputStream());
            
                } catch (UnknownHostException e) {
			            System.err.println("ERROR: Don't know where to connect to Broker!!");
			            System.exit(1);
		        } catch (IOException e) {
			            System.err.println("ERROR: Couldn't get I/O for the connection to Broker.");
			            System.exit(1);
		        }
        }
        else if (packetFromLookup.type == BrokerPacket.BROKER_ERROR)
            errorHandling(exchange, packetFromLookup);
        LookupSocket.close();
		lookup_out.close();
		lookup_in.close();
              
        

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
        System.out.println("Enter command or x for exit");
		System.out.print(">");
		while ((userInput = stdIn.readLine()) != null
				&& !userInput.equals("x")) {
			/* make a new request packet */
			BrokerPacket packetToServer = new BrokerPacket();
			
            
            String [] input_args = userInput.split(" ");

            if (input_args[0].equals("add") && input_args.length == 2){
                packetToServer.type = BrokerPacket.EXCHANGE_ADD;
                packetToServer.symbol = input_args[1];
			    broker_out.writeObject(packetToServer);

			/* print server reply */
			    BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) broker_in.readObject();

		    	if (packetFromServer.type == BrokerPacket.EXCHANGE_REPLY)
			    	System.out.println(input_args[1] + " added.");
                else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                    errorHandling(input_args[1], packetFromServer);
            }
            else if(input_args[0].equals("update") && input_args.length == 3){
                packetToServer.type = BrokerPacket.EXCHANGE_UPDATE;
                packetToServer.symbol = input_args[1] + " " + input_args[2];
			    broker_out.writeObject(packetToServer);

			/* print server reply */
			    BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) broker_in.readObject();

		    	if (packetFromServer.type == BrokerPacket.EXCHANGE_REPLY)
			    	System.out.println(input_args[1] + " updated to " + input_args[2]+ ".");
                else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                    errorHandling(input_args[1], packetFromServer);
            }
            else if(input_args[0].equals("remove") && input_args.length == 2){
                packetToServer.type = BrokerPacket.EXCHANGE_REMOVE;
                packetToServer.symbol = input_args[1];
			    broker_out.writeObject(packetToServer);


			/* print server reply */
			    BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) broker_in.readObject();

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
		broker_out.writeObject(packetToServer);

		broker_out.close();
		broker_in.close();
		stdIn.close();
		BrokerSocket.close();
	}
}
