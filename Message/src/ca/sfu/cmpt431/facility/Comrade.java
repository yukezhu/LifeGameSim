package ca.sfu.cmpt431.facility;

import java.io.Serializable;

import ca.sfu.network.MessageSender;

public class Comrade implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public int id;
	public int port;
	public String ip;
	public MessageSender sender;
	
	public Comrade(int id, int port, String ip, MessageSender sender) {
		this.id = id;
		this.port = port;
		this.ip = ip;
		this.sender = sender;
	}
	
}
