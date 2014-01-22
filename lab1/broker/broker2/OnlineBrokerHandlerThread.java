import java.net.*;
import java.io.*;
import java.util.*;
public class OnlineBrokerHandlerThread extends Thread {
	private Socket socket = null;
    public ArrayList <String>col1_list = new ArrayList<String>();
    public ArrayList <String>col2_list = new ArrayList<String>();

	public OnlineBrokerHandlerThread(Socket socket) {
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
                read_file();
				BrokerPacket packetToClient = new BrokerPacket();
				
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
				    packetToClient.type = BrokerPacket.BROKER_QUOTE;
			        //System.out.println("packet from client symbol " + packetFromClient.symbol + "END");
			        i = col1_list.indexOf(packetFromClient.symbol);
                    if (i != -1) {       
                        packetToClient.quote = Long.parseLong(col2_list.get(i), 10);
                      //      System.out.println("found!!...sending " + col2_list.get(i));
                    }
                    else { 
                        packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
                        packetToClient.type = BrokerPacket.BROKER_ERROR;
                    }
                            //packetToClient.quote = 0l;
    
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
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
