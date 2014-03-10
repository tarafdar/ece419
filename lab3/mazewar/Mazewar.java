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
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.io.IOException; 
import java.util.ArrayList;
/**
 * The entry point and glue code for the game.  It also contains some helpful
 * global utility methods.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

public class Mazewar extends JFrame {
        public static Socket clientSocket = null;
		public static ObjectOutputStream out = null;
		public static ObjectInputStream in = null;
        public boolean quit = false;
        public ArrayList<mazeWarPacket> q = new ArrayList<mazeWarPacket>();
        public ServerListenerThread serverListener = null;
        public int local_sequence_number = 1;
        ArrayList<Client> clientList = new ArrayList<Client>(); 
        
        /**
         * The default width of the {@link Maze}.
         */
        private final int mazeWidth = 20;

        /**
         * The default height of the {@link Maze}.
         */
        private final int mazeHeight = 10;

        /**
         * The default random seed for the {@link Maze}.
         * All implementations of the same protocol must use 
         * the same seed value, or your mazes will be different.
         */
        private final int mazeSeed = 42;

        /**
         * The {@link Maze} that the game uses.
         */
        public Maze maze = null;

        /**
         * The {@link GUIClient} for the game.
         */
        private GUIClient guiClient = null;

        /**
         * The panel that displays the {@link Maze}.
         */
        private OverheadMazePanel overheadPanel = null;

        /**
         * The table the displays the scores.
         */
        private JTable scoreTable = null;
        
        /** 
         * Create the textpane statically so that we can 
         * write to it globally using
         * the static consolePrint methods  
         */
        private static final JTextPane console = new JTextPane();
      
        /** 
         * Write a message to the console followed by a newline.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrintLn(String msg) {
                console.setText(console.getText()+msg+"\n");
        }
        
        /** 
         * Write a message to the console.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrint(String msg) {
                console.setText(console.getText()+msg);
        }
        
        /** 
         * Clear the console. 
         */
        public static synchronized void clearConsole() {
           console.setText("");
        }
        
        /**
         * Static method for performing cleanup before exiting the game.
         */
        public static void quit() {
                // Put any network clean-up code you might have here.
                // (inform other implementations on the network that you have 
                //  left, etc.)
                
                try{
                    out.close();
                    in.close(); 
                    clientSocket.close();
                }
                catch(IOException e){
                    System.out.println("Exit error??");
                    System.exit(1);

                }
                System.exit(0);
        }
       
        /** 
         * The place where all the pieces are put together. 
         */
        public Mazewar(String[] args){
                super("ECE419 Mazewar");
                consolePrintLn("ECE419 Mazewar started!");
                // Create the maze
                // Throw up a dialog to get the GUIClient name.
                String name = JOptionPane.showInputDialog("Enter your name");
                if((name == null) || (name.length() == 0)) {
                  Mazewar.quit();
                }
                maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed, name);
                assert(maze != null);
                
                // Have the ScoreTableModel listen to the maze to find
                // out how to adjust scores.
                ScoreTableModel scoreModel = new ScoreTableModel();
                assert(scoreModel != null);
                maze.addMazeListener(scoreModel);
                ArrayList <Socket> socketList = new ArrayList <Socket>();
                ArrayList <ObjectOutputStream> outStreamList = new ArrayList <ObjectOutputStream>();
                ArrayList <ObjectInputStream> inStreamList = new ArrayList <ObjectInputStream>();
                  
                quit = false;                
                String localhost = "blah"; 
                int listenPort = 42;
                try{
                    String hostname = args[0];
                    int port = Integer.parseInt(args[1]);
                    listenPort = Integer.parseInt(args[2]);
                
                    clientSocket = new Socket(hostname, port);
                    
			        out = new ObjectOutputStream(clientSocket.getOutputStream());
			        in = new ObjectInputStream(clientSocket.getInputStream());
                
                    java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                    localhost = addr.getHostName();
                    System.out.println("Local hostname is " + localhost);
                        


		        } catch (UnknownHostException e) {
		        	System.err.println("ERROR: Don't know where to connect!!");
		        	System.exit(1);
		        } catch (IOException e) {
		        	System.err.println("ERROR: Couldn't get I/O for the connection.");
		        	System.exit(1);
		        }
                
                try{
                    
                    //Packet sending to name server initially
                    mazeWarPacket packetToServer = new mazeWarPacket();
                    packetToServer.type = mazeWarPacket.JOIN;
                    packetToServer.hostname.add(localhost);   
                    packetToServer.port.add(listenPort);   
                    out.writeObject(packetToServer);
                    
                    guiClient = new GUIClient(name, out, this);
                    //maze.addClient(guiClient);
                    this.addKeyListener(guiClient);
                    clientList.add(guiClient);
                    mazeWarPacket packetFromServer = new mazeWarPacket();
                    packetFromServer = (mazeWarPacket) in.readObject();

                    int i,j;
                    j=0;
                    
                    for(i=0; i<packetFromServer.numPlayers; i++){
                      
                        if(!packetFromServer.hostname.get(i).equals(localhost)){
                            
                            j++;      
                            socketList.add(new Socket(packetFromServer.hostname.get(i), packetFromServer.port.get(i)));
			                outStreamList.add(new ObjectOutputStream(socketList.get(j).getOutputStream()));
			                inStreamList.add(new ObjectInputStream(socketList.get(j).getInputStream()));
                        
                        }


                    }

                   // int i;
                   // RemoteClient remoteclient; 
                   // {
                   //         for(i=0; i<packetFromServer.numPlayers; i++){
    //             //                System.out.println("Player " + i + " is " + packetFromServer.players[i] + " and is looking "  + packetFromServer.d[i].toString() + " at point " + packetFromServer.point[i].getX() + "," + packetFromServer.point[i].getY());
                   //               
                   //              if(!name.equals(packetFromServer.players[i])){
                   //                 remoteclient = new RemoteClient(packetFromServer.players[i]);
                   //                 maze.addClient(remoteclient , packetFromServer.point[i], packetFromServer.d[i]);
                   //                 clientList.add(remoteclient);
  //               //                   System.out.println("point after add " + maze.getClientPoint(remoteclient).getX() + "," + maze.getClientPoint(remoteclient).getY() + " facing " + maze.getClientOrientation(remoteclient).toString());  
                   //              }
                   //              else{
                   //                 maze.addClient(guiClient, packetFromServer.point[i], packetFromServer.d[i]);
//                 //                   System.out.println("point after add " + maze.getClientPoint(guiClient).getX() + "," + maze.getClientPoint(guiClient).getY() + " facing " + maze.getClientOrientation(guiClient).toString());  
                   //             }
                   //         }
                   // }
                   // overheadPanel = new OverheadMazePanel(maze, guiClient);
                   // assert(overheadPanel != null);
                   // maze.addMazeListener(overheadPanel);

                }
                catch (IOException e){
		        	System.err.println("ERROR: Couldn't get I/O for the connection. NUMBER 2");
		        	System.exit(1);
                }
                catch (ClassNotFoundException e){
		        	System.err.println("ERROR: Class Not Found.");
		        	System.exit(1);
                }
            
                          
                
                // You may want to put your network initialization code somewhere in
                // here.
                
                // Create the GUIClient and connect it to the KeyListener queue
               
                new ServerListenerThread(this, in).start(); 
                //new ServerProcessThread(this).start(); 
                // Use braces to force constructors not to be called at the beginning of the
                // constructor.
               
                
                
                // Create the panel that will display the maze.
                
                // Don't allow editing the console from the GUI
                console.setEditable(false);
                console.setFocusable(false);
                console.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
               
                // Allow the console to scroll by putting it in a scrollpane
                JScrollPane consoleScrollPane = new JScrollPane(console);
                assert(consoleScrollPane != null);
                consoleScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Console"));
                
                // Create the score table
                scoreTable = new JTable(scoreModel);
                assert(scoreTable != null);
                scoreTable.setFocusable(false);
                scoreTable.setRowSelectionAllowed(false);

                // Allow the score table to scroll too.
                JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
                assert(scoreScrollPane != null);
                scoreScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Scores"));
                
                // Create the layout manager
                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints c = new GridBagConstraints();
                getContentPane().setLayout(layout);
                
                // Define the constraints on the components.
                c.fill = GridBagConstraints.BOTH;
                c.weightx = 1.0;
                c.weighty = 3.0;
                c.gridwidth = GridBagConstraints.REMAINDER;
                layout.setConstraints(overheadPanel, c);
                c.gridwidth = GridBagConstraints.RELATIVE;
                c.weightx = 2.0;
                c.weighty = 1.0;
                layout.setConstraints(consoleScrollPane, c);
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.weightx = 1.0;
                layout.setConstraints(scoreScrollPane, c);
                                
                // Add the components
                getContentPane().add(overheadPanel);
                getContentPane().add(consoleScrollPane);
                getContentPane().add(scoreScrollPane);
                
                // Pack everything neatly.
                pack();

                // Let the magic begin.
                setVisible(true);
                overheadPanel.repaint();
                this.requestFocusInWindow();
        }

        
        /**
         * Entry point for the game.  
         * @param args Command-line arguments.
         */
        public static void main(String args[]) {

                /* Create the GUI */
                new Mazewar(args);
        }
}
