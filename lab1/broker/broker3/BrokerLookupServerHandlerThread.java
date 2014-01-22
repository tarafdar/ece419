import java.net.*;
import java.io.*;
import java.util.*;
public class BrokerLookupServerHandlerThread extends Thread {
	private Socket socket = null;
    public ArrayList <String>col1_list = new ArrayList<String>();
    public ArrayList <String>col2_list = new ArrayList<String>();
	public Arraylist <BrokerLocation> servers = new ArrayList<BrokerLocation();
	public Arraylist <BrokerLocation> server_names = new ArrayList<BrokerLocation();

	public BrokerLookupHandlerThread(Socket socket) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
		//System.out.println("Created new Thread to handle client");
	}

    public void read_file() {
        BufferedReader br = null;
        String line;
        this.col1_list.clear();
        this.col2_list.clear();
        try
        {
            br = new BufferedReader(new FileReader("input.txt"));
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println(fnfe.getMessage());

        }
        try
        {
             while((line = br.readLine()) != null){
                 String [] line_array = line.split(" ");
                 this.col1_list.add(line_array[0]);
                 this.col2_list.add(line_array[1]);
             }
             br.close();
        }
        catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());

        }
    }
    
    public void write_file(){
        int i;
        File file;
        PrintWriter writer;
        file = new File("input.txt");
        try
        {
            writer = new PrintWriter(file);
            for(i = 0; i< col1_list.size(); i++){
                writer.println(col1_list.get(i) + " " + col2_list.get(i));
            }    
            writer.close(); 
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println(fnfe.getMessage());

        }
    }

	public void run() {
	    int i;	
        boolean gotByePacket = false;
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			BrokerPacket packetFromClient;
			
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			

			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.LOOKUP_REGISTER) {
				    packetToClient.type = BrokerPacket.LOOKUP_REPLY;
			        servers.add(packetFromClient.locations[0]);
			        server_names.add(packetFromClient.symbol);
					continue;
				}
			    
                if(packetFromClient.type == BrokerPacket.LOOKUP_REQUEST){
                    i = server_names.indexOf(packetFromClient.exchange);
                    if (i != -1) { 
                        packetToClient.locations[0] = servers.get(i);  
				        packetToClient.type = BrokerPacket.LOOKUP_REPLY;
                    }
                    else {
                        packetToClient.error_code = BrokerPacket.ERROR_INVALID_EXCHANGE;
                        packetToClient.type = BrokerPacket.BROKER_ERROR;
                    }
                    toClient.writeObject(packetToClient);
                    continue;
                }	
			    
                
                /* Sending an ECHO_NULL || ECHO_BYE means quit */
				if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					gotByePacket = true;
					packetToClient = new BrokerPacket();
					packetToClient.type = BrokerPacket.BROKER_BYE;
					toClient.writeObject(packetToClient);
					break;
				}
				
				/* if code comes here, there is an error in the packet */
				System.err.println("ERROR: Unknown ECHO_* packet!!");
				System.exit(-1);
			}
			
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
			socket.close();

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
}
