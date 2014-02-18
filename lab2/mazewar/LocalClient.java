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

public abstract class LocalClient extends Client {
        public ObjectOutputStream out;
        public Mazewar mazewar;
        /** 
         * Create a {@link Client} local to this machine.
         * @param name The name of this {@link Client}.
         */
        public LocalClient(String name) {
            super(name);
            assert(name != null);
        }
        
        public LocalClient(String name, ObjectOutputStream out, Mazewar mazewar) {
            super(name);
            assert(name != null);
            this.out = out;
            this.mazewar = mazewar;
        }

        public void enqueueQuit(){
		   try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.CLIENT_QUIT;
                packetToServer.clientName = this.getName();
                out.writeObject(packetToServer);
           }
           catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		       	System.exit(1);
           }
       }

        public void enqueueForward(){
		   try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.CLIENT_FORWARD;
                packetToServer.clientName = this.getName();
                out.writeObject(packetToServer);
           }
           catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		       	System.exit(1);
           }
       }
        
        public void enqueueBackward(){
		   try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.CLIENT_BACKWARD;
                packetToServer.clientName = this.getName();
                out.writeObject(packetToServer);
           }
           catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		       	System.exit(1);
           }
       }
        
        public void enqueueRight(){
		   try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.CLIENT_RIGHT;
                packetToServer.clientName = this.getName();
                out.writeObject(packetToServer);
           }
           catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		       	System.exit(1);
           }
       }
        
        public void enqueueLeft(){
		   try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.CLIENT_LEFT;
                packetToServer.clientName = this.getName();
                out.writeObject(packetToServer);
           }
           catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		       	System.exit(1);
           }
       }
        
        public void enqueueFire(){
		   try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.CLIENT_FIRE;
                packetToServer.clientName = this.getName();
                out.writeObject(packetToServer);
           }
           catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		       	System.exit(1);
           }
       }
        
        
       public void enqueueKilled(String target, Point point, Direction d){
		   try{
                mazeWarPacket packetToServer = new mazeWarPacket();
                packetToServer.type = mazeWarPacket.CLIENT_KILLED;
                packetToServer.clientName = this.getName();
                packetToServer.players[0] = target;
                packetToServer.point[0] = point;
                packetToServer.d[0] = d;
                out.writeObject(packetToServer);
           }
           catch(IOException e){
		        System.err.println("ERROR: Couldn't get I/O for the connection.");
		       	System.exit(1);
           }
           
       }       
       
        
              
        /**
         * Fill in here??
         */
}
