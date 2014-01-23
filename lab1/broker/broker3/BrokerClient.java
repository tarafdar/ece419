import java.io.*;
import java.net.*;

public class BrokerClient{
	public static void errorHandling (String symbol, BrokerPacket packetFromServer) {
        if (packetFromServer.error_code == BrokerPacket.ERROR_INVALID_SYMBOL)
            System.out.println (symbol + " invalid.");
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
			lookup_in = new ObjectInputStream(LookupSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
        
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
        
        System.out.println("Enter command, symbol or x for exit:");
		System.out.print("> ");
		while ((userInput = stdIn.readLine()) != null && !userInput.equals("x")) {
			/* make a new request packet */
			BrokerPacket outputPacket = new BrokerPacket();
			
            String [] input_args = userInput.split(" ");
            if (input_args[0].equals("local") && input_args.length == 2) {
                outputPacket.type = BrokerPacket.LOOKUP_REQUEST;
                outputPacket.exchange = input_args[1];
                lookup_out.writeObject(outputPacket);
			    
                BrokerPacket packetFromServer;
			    packetFromServer = (BrokerPacket) lookup_in.readObject();
                if(packetFromServer.type == BrokerPacket.LOOKUP_REPLY) { 
                    try {
                        BrokerSocket = new Socket (packetFromServer.locations[0].broker_host, packetFromServer.locations[0].broker_port);
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
                else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                    errorHandling(input_args[1], packetFromServer);
            }
            else if (input_args.length == 1){
                if (BrokerSocket == null) {
                    System.out.println("No connection initialized.");
                }
                else {
                    outputPacket.type = BrokerPacket.BROKER_REQUEST;
                    outputPacket.symbol = userInput;
			        broker_out.writeObject(outputPacket);

			        /* print server reply */
			        BrokerPacket packetFromServer;
			        packetFromServer = (BrokerPacket) broker_in.readObject();

			        if (packetFromServer.type == BrokerPacket.BROKER_QUOTE)
				        System.out.println("Quote from broker: " + packetFromServer.quote);
                    else if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
                        errorHandling(userInput, packetFromServer);
                }
            }
            else {
                System.out.println("Invalid command.");
            }    
                
			/* re-print console prompt */
			System.out.print("> ");
            
		}

		/* tell server that i'm quitting */
		BrokerPacket packetToServer = new BrokerPacket();
		packetToServer.type = BrokerPacket.BROKER_BYE;
		broker_out.writeObject(packetToServer);

		broker_out.close();
		broker_in.close();
		lookup_out.close();
		lookup_in.close();
		stdIn.close();
		BrokerSocket.close();
        LookupSocket.close();
	}
}
