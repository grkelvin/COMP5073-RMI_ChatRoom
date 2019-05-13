package server;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Vector;

import client.RMIClientIF;

public class Server extends UnicastRemoteObject implements RMIServerIF {
	String divide = "---------------------------------------------\n";// format symbol
	private Vector<Chatter> chatters;
	private static final long serialVersionUID = 1L;
	
	//Constructor
	public Server() throws RemoteException {
		super();
		chatters = new Vector<Chatter>(10, 1);
	}
	//-----------------------------------------------------------
	// The following is local methods
	//-----------------------------------------------------------
	/**
	 * LOCAL METHODS
	 */	
	public static void main(String[] args) {
		startRMI();	
		String hostName = "localhost";
		String serviceName = "GroupChatService";
		
		if(args.length == 2){
			hostName = args[0];
			serviceName = args[1];
		}
		
		try{
			RMIServerIF hello = new Server();
			Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);
			System.out.println("Group Chat RMI Server is running...");
		}
		catch(Exception e){
			System.out.println("Server had problems starting");
		}	
	}

	
	/**
	 * Start the RMI Registry
	 */
	public static void startRMI() {
		try{
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server ready");
		}
		catch(RemoteException e) {
			e.printStackTrace();
		}
	}
		
	
	//-----------------------------------------------------------
	/*
	 *   REMOTE METHODS
	 */
	//-----------------------------------------------------------
	
	/**
	 * Return a message to client
	 */
	public String hello(String ClientName) throws RemoteException {
		System.out.println(ClientName + " sent a message");
		return "Hello " + ClientName + " from group chat server";
	}
	

	/**
	 * Send a string ( the latest post, mostly ) 
	 * to all connected clients
	 */
	public void updateChat(String name, String nextPost) throws RemoteException {
		String message =  name + " : " + nextPost + "\n";
		// send a string to all connected clients
		groupSend(message);
	}
	
	/**
	 * Receive a new client remote reference
	 */
	@Override
	public void passID(RemoteRef ref) throws RemoteException {	
		//System.out.println("\n" + ref.remoteToString() + "\n");
		try{
			System.out.println(divide + ref.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}//end passID

	
	/**
	 * Receive a new client and display details to the console
	 * send on to register method
	 */
	@Override
	public void register(String[] details) throws RemoteException {	
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println(details[0] + " has joined the chat session");
		System.out.println(details[0] + "'s hostname : " + details[1]);
		System.out.println(details[0] + "'sRMI service : " + details[2]);
		registerChatter(details);
	}

	
	/**
	 * register the clients interface and store it in a reference for 
	 * future messages to be sent to, ie other members messages of the chat session.
	 * send a test message for confirmation / test connection
	 * @param details
	 */
	private void registerChatter(String[] details){		
		try{
			RMIClientIF nextClient = ( RMIClientIF )Naming.lookup("rmi://" + details[1] + "/" + details[2]);
			
			chatters.addElement(new Chatter(details[0], nextClient));
			
			nextClient.messageFromServer("[Server] : Hello " + details[0] + " you are now free to chat.\n");
			
			groupSend("[Server] : " + details[0] + " has joined the group.\n");
			
			updateOnlineUserList();		
		}
		catch(RemoteException | MalformedURLException | NotBoundException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Update all clients by remotely invoking their
	 * updateOnlineUserList RMI method
	 */
	private void updateOnlineUserList() {
		String[] currentUsers = getList();	
		for(Chatter c : chatters){
			try {
				c.getClient().updateOnlineUserList(currentUsers);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}
	

	/**
	 * generate a String array of current users
	 * @return
	 */
	private String[] getList(){
		// generate an array of current users
		String[] allUsers = new String[chatters.size()];
		for(int i = 0; i< allUsers.length; i++){
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
	}
	

	/**
	 * Send a message to all users
	 * @param newMessage
	 */
	public void groupSend(String newMessage){	
		for(Chatter c : chatters){
			try {
				c.getClient().messageFromServer(newMessage);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}

	
	/**
	 * remove a client from the list, notify everyone
	 */
	@Override
	public void exitChat(String userName) throws RemoteException{
		
		for(Chatter c : chatters){
			if(c.getName().equals(userName)){
				System.out.println(divide + userName + " left the chat session");
				System.out.println(new Date(System.currentTimeMillis()));
				chatters.remove(c);
				break;
			}
		}		
		if(!chatters.isEmpty()){
			updateOnlineUserList();
		}			
	}
	

	/**
	 * A method to send a private message to selected clients
	 * The integer array holds the indexes (from the chatters vector) 
	 * of the clients to send the message to
	 */
	@Override
	public void PeerToPeer(int[] privateGroup, String privateMessage) throws RemoteException{
		Chatter pc;
		for(int i : privateGroup){
			pc= chatters.elementAt(i);
			pc.getClient().messageFromServer(privateMessage);
		}
	}
	
}



