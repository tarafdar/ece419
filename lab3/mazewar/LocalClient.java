/*
Copyright (C) 2004 Geoffrey Alan Washburn
    
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
    
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
    
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/

/**
 * An abstract class for {@link Client}s in a {@link Maze} that local to the 
 * computer the game is running upon. You may choose to implement some of 
 * your code for communicating with other implementations by overriding 
 * methods in {@link Client} here to intercept upcalls by {@link GUIClient} and 
 * {@link RobotClient} and generate the appropriate network events.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: LocalClient.java 343 2004-01-24 03:43:45Z geoffw $
 */

import java.io.ObjectOutputStream;
import java.io.IOException; 
import java.util.BitSet;

public abstract class LocalClient extends Client {
        public Mazewar mazewar;
        /** 
         * Create a {@link Client} local to this machine.
         * @param name The name of this {@link Client}.
         */
        public LocalClient(String name) {
            super(name);
            assert(name != null);
        }
        
        public LocalClient(String name, Mazewar mazewar) {
            super(name);
            assert(name != null);
            this.mazewar = mazewar;
        }

        public void enqueueQuit(){
		   //try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.QUIT;
                packetToServer.clientName = this.getName();
                packetToServer.clientID = mazewar.player_id;
                synchronized(mazewar.outstandingLocalEventsQ){
                    mazewar.outstandingLocalEventsQ.offer(packetToServer);
                }
                //mazewar.quit = true;
       }

        public void enqueueForward(){
		   //try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.FORWARD;
                packetToServer.clientName = this.getName();
                packetToServer.clientID = mazewar.player_id;
                synchronized(mazewar.outstandingLocalEventsQ){
                    mazewar.outstandingLocalEventsQ.offer(packetToServer);
                }

                //out.writeObject(packetToServer);
          // }
           //catch(IOException e){
		   //     System.err.println("ERROR: Couldn't get I/O for the connection.");
		   //    	System.exit(1);
           //}
       }
        
        public void enqueueBackward(){
		   //try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.BACKWARD;
                packetToServer.clientName = this.getName();
                packetToServer.clientID = mazewar.player_id;
                synchronized(mazewar.outstandingLocalEventsQ){
                    mazewar.outstandingLocalEventsQ.offer(packetToServer);
                }
                //out.writeObject(packetToServer);
          // }
           //catch(IOException e){
		   //     System.err.println("ERROR: Couldn't get I/O for the connection.");
		   //    	System.exit(1);
           //}
       }
        
        public void enqueueRight(){
		   //try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.RIGHT;
                packetToServer.clientName = this.getName();
                packetToServer.clientID = mazewar.player_id;
                synchronized(mazewar.outstandingLocalEventsQ){
                    mazewar.outstandingLocalEventsQ.offer(packetToServer);
                }
                //out.writeObject(packetToServer);
           //}
           //catch(IOException e){
		   //     System.err.println("ERROR: Couldn't get I/O for the connection.");
		   //    	System.exit(1);
           //}
       }
        
        public void enqueueLeft(){
		   //try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.LEFT;
                packetToServer.clientName = this.getName();
                packetToServer.clientID = mazewar.player_id;
                synchronized(mazewar.outstandingLocalEventsQ){
                    mazewar.outstandingLocalEventsQ.offer(packetToServer);
                }
                //out.writeObject(packetToServer);
           //}
           //catch(IOException e){
		   //     System.err.println("ERROR: Couldn't get I/O for the connection.");
		   //    	System.exit(1);
           //}
       }
        
        public void enqueueFire(){
		   //try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.FIRE;
                packetToServer.clientName = this.getName();
                packetToServer.clientID = mazewar.player_id;
                synchronized(mazewar.outstandingLocalEventsQ){
                    mazewar.outstandingLocalEventsQ.offer(packetToServer);
                }
                //out.writeObject(packetToServer);
          // }
           //catch(IOException e){
		   //     System.err.println("ERROR: Couldn't get I/O for the connection.");
		   //    	System.exit(1);
           //}
       }
        
        
       public void enqueueKilled(Client target, Point point, Direction d){
		   //try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.KILLED;
                packetToServer.clientName = this.getName();
                packetToServer.points.add(point);
                packetToServer.directions.add(d);
                packetToServer.clientID = mazewar.player_id;
                packetToServer.killedClientID = mazewar.clientList.indexOf(target);
                synchronized(mazewar.outstandingLocalEventsQ){
                    mazewar.outstandingLocalEventsQ.offer(packetToServer);
                }
                
                //out.writeObject(packetToServer);
           //}
           //catch(IOException e){
		   //     System.err.println("ERROR: Couldn't get I/O for the connection.");
		   //    	System.exit(1);
           //}
           
       }       
       
        
              
        /**
         * Fill in here??
         */
}
