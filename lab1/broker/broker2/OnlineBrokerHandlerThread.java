import java.net.*;
import java.io.*;
import java.util.*;
public class OnlineBrokerHandlerThread extends Thread {
	private Socket socket = null;

	public OnlineBrokerHandlerThread(Socket socket) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
		//System.out.println("Created new Thread to handle client");
	}

	public void run() {

        BufferedReader br = null;
        String line;
        try
        {
            br = new BufferedReader(new FileReader("input.txt"));
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println(fnfe.getMessage());

        }
        ArrayList <String>col1_list = new ArrayList<String>();
        ArrayList <String>col2_list = new ArrayList<String>();
        
        try
        {
             while((line = br.readLine()) != null){
                 String [] line_array = line.split(" ");
                 String col1 = line_array[0];
                 String col2 = line_array[1];
                 col1_list.add(line_array[0]);
                 col2_list.add(line_array[1]);

             }
        }
        catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());

        }
        String[] col1_array = col1_list.toArray(new String[col1_list.size()]);
        String[] col2_array = col2_list.toArray(new String[col2_list.size()]);
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
				packetToClient.type = BrokerPacket.BROKER_QUOTE;
				
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
                    col1_array = col1_list.toArray(new String[col1_list.size()]);
                    col2_array = col2_list.toArray(new String[col2_list.size()]);
					//System.out.println("From Client: " + packetFromClient.message);
			        System.out.println("packet from client symbol " + packetFromClient.symbol + "END");
                    for(i=0;i<col2_array.length;i++){
			            System.out.println("col1[i] " + col1_array[i] + " col2 [i] " + col2_array[i]);
                        if(col1_array[i].equals(packetFromClient.symbol)){
                            packetToClient.quote = Long.parseLong(col2_array[i], 10);
                            System.out.println("found!!...sending " + col2_array[i]);
                        }
                        else{
                            System.out.println("no match 1= " + col1_array[i] + "END 2= " + packetFromClient.symbol); 

                        }
                    }       	
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
			    
                if(packetFromClient.type == BrokerPacket.EXCHANGE_ADD){
                    col1_list.add(packetFromClient.symbol);
                    col2_list.add("");
                    col1_array = col1_list.toArray(new String[col1_list.size()]);
                    col2_array = col2_list.toArray(new String[col2_list.size()]);
                }	
			    
                if(packetFromClient.type == BrokerPacket.EXCHANGE_UPDATE){
                    String [] sym_array = packetFromClient.symbol.split(" ");
                    col1_array = col1_list.toArray(new String[col1_list.size()]);
                    col2_array = col2_list.toArray(new String[col2_list.size()]);
                    for(i=0;i<col1_array.length;i++){
                        if(col1_array[i].equals(sym_array[0])){
                            col2_array[i] = sym_array[1];
                        }
                    }
                    col2_list = new ArrayList<String>(Arrays.asList(col2_array));
                
                }	
			
                if(packetFromClient.type == BrokerPacket.EXCHANGE_REMOVE){
                    col1_array = col1_list.toArray(new String[col1_list.size()]);
                    col2_array = col2_list.toArray(new String[col2_list.size()]);
                    int index = -1;
                    for(i=0;i<col1_array.length;i++){
                        if(col1_array[i].equals(packetFromClient.symbol)){
                            index = i;
                            break;
                        }
                        
                    } 
                    if(index != -1){
                        col1_list.remove(index);
                        col2_list.remove(index);
                    }
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
