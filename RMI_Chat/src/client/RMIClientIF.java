package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for client classes
 * A method to receive a string
 * A method to update changes to user list
 *
 */
public interface RMIClientIF extends Remote{

	public void messageFromServer(String message) throws RemoteException;

	public void updateOnlineUserList(String[] currentUsers) throws RemoteException;
	
}
