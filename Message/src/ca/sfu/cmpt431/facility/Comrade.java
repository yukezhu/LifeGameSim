package ca.sfu.cmpt431.facility;

import ca.sfu.network.MessageSender;

public class Comrade {
	
	public int id;
	public MessageSender sender;
	
	public Comrade(int id, MessageSender sender) {
		this.id = id;
		this.sender = sender;
	}
	
}