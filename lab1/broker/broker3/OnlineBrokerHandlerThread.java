import java.net.*;
import java.io.*;
import java.util.*;
public class OnlineBrokerHandlerThread extends Thread {
	private Socket socket = null;
    public ArrayList <String>col1_list = new ArrayList<String>();
    public ArrayList <String>col2_list = new ArrayList<String>();
    public String exchange;
    public String lookupHost;
    public int lookupPort;
	public OnlineBrokerHandlerThread(Socket socket, String exchange, String lookupHost, int lookupPort) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
        this.exchange = exchange;
        this.lookupHost = lookupHost;
        this.lookupPort = lookupPort;
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
                read_file();
				BrokerPacket packetToClient = new BrokerPacket();
				
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
				    packetToClient.type = BrokerPacket.BROKER_QUOTE;
			        i = col1_list.indexOf(packetFromClient.symbol);
                    if (i != -1) {       
                        packetToClient.quote = Long.parseLong(col2_list.get(i), 10);
                    }
                    else { 
                        try{
                            Socket lookupSocket = new Socket(lookupHost, lookupPort);    
                            ObjectOutputStream toLookup = new ObjectOutputStream(lookupSocket.getOutputStream());
                            ObjectInputStream fromLookup = new ObjectInputStream(lookupSocket.getInputStream());
                            BrokerPacket packetToLookup = new BrokerPacket();
                            packetToLookup.type = BrokerPacket.LOOKUP_REQUEST;
                            if(exchange.equals("nasdaq")) 
                                packetToLookup.exchange = "tse";
                            else
                                packetToLookup.exchange = "nasdaq";
                                 
                            
                            toLookup.writeObject(packetToLookup);
                            BrokerPacket packetFromLookup = (BrokerPacket) fromLookup.readObject(); 
                           
                           
                            //now we know location of server to forward to
                            Socket forwardSocket = new Socket(packetFromLookup.locations[0].broker_host, packetFromLookup.locations[0].broker_port);
                                
                            ObjectOutputStream toForward = new ObjectOutputStream(forwardSocket.getOutputStream());
                            ObjectInputStream fromForward = new ObjectInputStream(forwardSocket.getInputStream());
                            BrokerPacket packetToForward = new BrokerPacket();
                            packetToForward.type = BrokerPacket.BROKER_FORWARD;
                            packetToForward.symbol = packetFromClient.symbol;
                            toForward.writeObject(packetToForward);
                            BrokerPacket packetFromForward = (BrokerPacket) fromForward.readObject(); 
                            packetToClient = packetFromForward;
                            
                            lookupSocket.close();
                            toLookup.close();
                            fromLookup.close();
                            forwardSocket.close();
                            toForward.close();
                            fromForward.close();  
                             
                        }
                        catch (IOException ioe){
                            System.out.println("blah");
                        }
                        catch (ClassNotFoundException e){
                             System.out.println("blah");


                         }
                    }
                            //packetToClient.quote = 0l;
    
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
			    if(packetFromClient.type == BrokerPacket.BROKER_FORWARD){
			        i = col1_list.indexOf(packetFromClient.symbol);
                    if (i != -1) {       
                        packetToClient.quote = Long.parseLong(col2_list.get(i), 10);
                    }
                    else{
                        packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
                        packetToClient.type = BrokerPacket.BROKER_ERROR;
                    }
                }
                if(packetFromClient.type == BrokerPacket.EXCHANGE_ADD){
                    i = col1_list.indexOf(packetFromClient.symbol);
                    //doesnt yet exist - add
                    if (i == -1) { 
                        col1_list.add(packetFromClient.symbol);
                        col2_list.add("0");
					    write_file();
				        packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
                    }
                    else {
                        packetToClient.error_code = BrokerPacket.ERROR_SYMBOL_EXISTS;
                        packetToClient.type = BrokerPacket.BROKER_ERROR;
                    }
                    toClient.writeObject(packetToClient);
                    continue;
                }	
			    
                if(packetFromClient.type == BrokerPacket.EXCHANGE_UPDATE){
                    String [] sym_array = packetFromClient.symbol.split(" ");
				    //System.out.println("In exchange update!! " + sym_array[0] + " to " + sym_array[1]);
                    
                    if (Long.parseLong(sym_array[1],10) >300 || Long.parseLong(sym_array[1],10) < 1) {
                        packetToClient.error_code = BrokerPacket.ERROR_OUT_OF_RANGE;
                        packetToClient.type = BrokerPacket.BROKER_ERROR;
                    }    
                    else {
                        i = col1_list.indexOf(sym_array[0]);
                        if (i!= -1) {
                            col2_list.set(i, sym_array[1]);
				            packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
                            write_file();
                        }
                        else {
                            packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
                            packetToClient.type = BrokerPacket.BROKER_ERROR;
                        }
                    }              
					toClient.writeObject(packetToClient);
                    continue;
                }	
			
                if(packetFromClient.type == BrokerPacket.EXCHANGE_REMOVE){
                    i = col1_list.indexOf(packetFromClient.symbol);    
                    if(i != -1){
                        col1_list.remove(i);
                        col2_list.remove(i);
				        packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
                        write_file();
                    }
                    
                    else {
                        packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
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
