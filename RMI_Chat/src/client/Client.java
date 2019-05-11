package client;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;

import server.RMIServerIF;

public class Client  extends UnicastRemoteObject implements RMIClientIF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7468891722773409712L;
	ClientGUI chatGUI;
	private String hostName = "localhost";
	private String serviceName = "GroupChatService";
	private String clientServiceName;
	private String name;
	protected RMIServerIF serverIF;
	protected boolean connectionProblem = false;

	
	/**
	 * class constructor,
	 * note may also use an overloaded constructor with 
	 * a port no passed in argument to super
	 * @throws RemoteException
	 */
	public Client(ClientGUI aChatGUI, String userName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.name = userName;
		this.clientServiceName = "ClientListenService_" + userName;
	}

	
	/**
	 * Register our own listening service/interface
	 * lookup the server RMI interface, then send our details
	 * @throws RemoteException
	 */
	public void startClient() throws RemoteException {		
		String[] details = {name, hostName, clientServiceName};	

		try {
			Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
			serverIF = ( RMIServerIF )Naming.lookup("rmi://" + hostName + "/" + serviceName);	
		} 
		catch (ConnectException  e) {
			JOptionPane.showMessageDialog(
					chatGUI.frame, "The server seems to be unavailable\nPlease try later",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
		}
		catch(NotBoundException | MalformedURLException me){
			connectionProblem = true;
			me.printStackTrace();
		}
		if(!connectionProblem){
			registerServer(details);
		}	
		System.out.println("Client Listen RMI Server is running...\n");
	}


	/**
	 * pass our username, hostname and RMI service name to
	 * the server to register out interest in joining the chat
	 * @param details
	 */
	public void registerServer(String[] details) {		
		try{
			serverIF.passID(this.ref);//now redundant ??
			serverIF.register(details);			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//=====================================================================
	/**
	 * Receive a string from the chat server
	 * this is the clients RMI method, which will be used by the server 
	 * to send messages to us
	 */
	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println( message );
		chatGUI.textArea.append( message );
		//make the gui display the last appended text, ie scroll to bottom
		chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
	}

	/**
	 * A method to update the display of users 
	 * currently connected to the server
	 */
	@Override
	public void updateOnlineUserList(String[] currentUsers) throws RemoteException {

		if(currentUsers.length < 2){
			chatGUI.privateMsgButton.setEnabled(false);
		}
		chatGUI.userPanel.remove(chatGUI.clientPanel);
		chatGUI.setClientPanel(currentUsers);
		chatGUI.clientPanel.repaint();
		chatGUI.clientPanel.revalidate();
	}

}//end class













