package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;

public interface RMIServerIF extends Remote {
		
	public void updateChat(String userName, String chatMessage)throws RemoteException;
	
	public void passID(RemoteRef ref)throws RemoteException;
	
	public void register(String[] details)throws RemoteException;
	
	public void exitChat(String userName)throws RemoteException;
	
	public void PeerToPeer(int[] privateGroup, String privateMessage)throws RemoteException;
}


