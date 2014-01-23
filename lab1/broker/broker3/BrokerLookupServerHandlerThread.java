import java.net.*;
import java.io.*;
import java.util.*;
public class BrokerLookupServerHandlerThread extends Thread {
	private Socket socket = null;
	public ArrayList <String> server_hosts = new ArrayList<String>();
	public ArrayList <Integer> server_ports = new ArrayList<Integer>();
	public ArrayList <String> server_names = new ArrayList<String>();

	public BrokerLookupServerHandlerThread(Socket socket) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
		//System.out.println("Created new Thread to handle client");
	}

    public void read_file() {
        BufferedReader br = null;
        String line;
        this.server_hosts.clear();
        this.server_ports.clear();
        this.server_names.clear();
        try
        {
            File file = new File("servers.txt");
            file.createNewFile();
            br = new BufferedReader(new FileReader("servers.txt"));
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println(fnfe.getMessage());

        }
        catch(IOException ioe){
            System.out.println(ioe.getMessage());


        }
        try
        {
             while((line = br.readLine()) != null){
                 String [] line_array = line.split(" ");
                 server_names.add(line_array[0]);
                 
                 server_hosts.add(line_array[1]);
                 server_ports.add(Integer.parseInt(line_array[2])); 
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
        file = new File("servers.txt");
        try
        {
            writer = new PrintWriter(file);
            for(i = 0; i< server_names.size(); i++){
                writer.println(server_names.get(i) + " " + server_hosts.get(i) + " " + server_ports.get(i));
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
			    read_file();
                 
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.LOOKUP_REGISTER) {
			        server_ports.add(packetFromClient.locations[0].broker_port);
			        server_hosts.add(packetFromClient.locations[0].broker_host);
			        server_names.add(packetFromClient.exchange);
                    write_file();
                    packetToClient.type = BrokerPacket.LOOKUP_REPLY;
                    toClient.writeObject(packetToClient);
					continue;
				}
			    
                if(packetFromClient.type == BrokerPacket.LOOKUP_REQUEST){
                    i = server_names.indexOf(packetFromClient.exchange);
                    if (i != -1) { 
                        packetToClient.locations = new BrokerLocation[1];
                        packetToClient.locations[0] = new BrokerLocation(server_hosts.get(i), server_ports.get(i)); 
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
