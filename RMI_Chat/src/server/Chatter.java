package server;

import client.RMIClientIF;


/**
 * A class used by the server program to keep
 * details of connected clients ordered
 *
 */
public class Chatter {

	public String name;
	public RMIClientIF client;
	
	//constructor
	public Chatter(String name, RMIClientIF client){
		this.name = name;
		this.client = client;
	}

	
	//getters and setters
	public String getName(){
		return name;
	}
	public RMIClientIF getClient(){
		return client;
	}
	
	
}
