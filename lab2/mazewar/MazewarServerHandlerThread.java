import java.net.*;
import java.io.*;
import java.util.Random;
//import java.lang.System;
import static java.lang.System.*;

public class MazewarServerHandlerThread extends Thread {
	//private Socket socket = null;
    int id;
    private MazewarServer server;
	
    public MazewarServerHandlerThread(MazewarServer server, int id) {
		super("MazewarServerHandlerThread");
		this.id = id;
        this.server = server;
	}

	public void run() {

		boolean gotByePacket = false;
		
		try {
			/* stream to read from client */
		    ObjectInputStream fromClient = server.inputStreams.get(id);
			mazeWarPacket packetFromClient;
			
			int seqNum;
            System.out.println("Started handler thread - id " + id);
			while (( packetFromClient = (mazeWarPacket) fromClient.readObject()) != null) {
				
			    //client connecting
                if(packetFromClient.type == mazeWarPacket.CLIENT_INIT) {
                    //Random randomGen = server.maze.returnRandomGen();
                    //int maxX = server.maze.returnmaxX();
                    //int maxY = server.maze.returnmaxY();
                    // Pick a random starting point, and check to see if it is already occupied
                    Point point = new Point(server.randomGen.nextInt(server.maxX), server.randomGen.nextInt(server.maxY));
                    MazeImpl.CellImpl cell = server.getCellImpl(point);
                    // Repeat until we find an empty cell
                    while(cell.getContents() != null) {
                        point = new Point(server.randomGen.nextInt(server.maxX),server.randomGen.nextInt(server.maxY));
                        cell = server.getCellImpl(point);
                    } 
                    Direction d = Direction.random();
                    while(cell.isWall(d)) {
                        d = Direction.random();
                    }
                    System.out.println("setting client " + packetFromClient.clientName + " to " + d.toString());
                    cell.setContents(true);
                    server.point[id] = point;
                    server.d[id] = d;
                    System.out.println("recieved client init packet - id " + id + "from client" + packetFromClient.clientName);
                    
                    server.players[id] = packetFromClient.clientName;
                    server.numConnected.getAndIncrement();
                    System.err.println("finished setting up client");
                    continue; 
                }
				
                else if (packetFromClient.type == mazeWarPacket.CLIENT_FIRE) {
                    seqNum = server.currentSequenceNumber.getAndIncrement();
                    packetFromClient.sequence_number = seqNum;
                    synchronized (server.actionQueue) {
                        server.actionQueue.offer(packetFromClient);
                    }
                    continue;                    
                }
                
                else if (packetFromClient.type == mazeWarPacket.CLIENT_FORWARD) {
                    seqNum = server.currentSequenceNumber.getAndIncrement();
                    packetFromClient.sequence_number = seqNum;
                    synchronized (server.actionQueue) {
                        server.actionQueue.offer(packetFromClient);
                    }
                    continue;                    
                }

                else if (packetFromClient.type == mazeWarPacket.CLIENT_BACKWARD) {
                    seqNum = server.currentSequenceNumber.getAndIncrement();
                    packetFromClient.sequence_number = seqNum;
                    synchronized (server.actionQueue) {
                        server.actionQueue.offer(packetFromClient);
                    }
                    continue;                    
                }
                
                else if (packetFromClient.type == mazeWarPacket.CLIENT_RIGHT) {
                    seqNum = server.currentSequenceNumber.getAndIncrement();
                    packetFromClient.sequence_number = seqNum;
                    synchronized (server.actionQueue) {
                        server.actionQueue.offer(packetFromClient);
                    }
                    continue;                    
                }

                else if (packetFromClient.type == mazeWarPacket.CLIENT_LEFT) {
                    seqNum = server.currentSequenceNumber.getAndIncrement();
                    packetFromClient.sequence_number = seqNum;
                    synchronized (server.actionQueue) {
                        server.actionQueue.offer(packetFromClient);
                    }
                    continue;                    
                }

                else if (packetFromClient.type == mazeWarPacket.CLIENT_KILLED) {
                    seqNum = server.currentSequenceNumber.getAndIncrement();
                    packetFromClient.sequence_number = seqNum;
                    synchronized (server.actionQueue) {
                        server.actionQueue.offer(packetFromClient);
                    }
                    continue;                    
                }

                else if (packetFromClient.type == mazeWarPacket.CLIENT_QUIT) {
                    gotByePacket = true;
                    //synchronized (server.currentSequenceNumber) {
                    //    seqNum = server.currentSequenceNumber;
                    //    server.currentSequenceNumber++;
                    //}
                    seqNum = server.currentSequenceNumber.getAndIncrement();
                    packetFromClient.sequence_number = seqNum;
                    synchronized (server.actionQueue) {
                        server.actionQueue.offer(packetFromClient);
                    }
                    break;
                }
                /* if code comes here, there is an error in the packet */
				System.err.println("ERROR: Unknown packet!!");
				System.exit(-1);
			}
			
			/* cleanup when client exits */
            server.inputStreams.get(id).close();
            synchronized (server.outputStreams) {
                server.outputStreams.get(id).close();
            }
            server.sockets.get(id).close();
            //synchronized (server.numPlayers) {
            //    server.numPlayers--;
            //}
            server.numConnected.getAndDecrement();

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
}
