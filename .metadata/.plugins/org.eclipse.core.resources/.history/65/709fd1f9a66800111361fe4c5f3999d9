package ca.sfu.cmpt431.message.join;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class JoinRequestMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	private int clientPort;
	
	public JoinRequestMsg(int cid, int port) {
		super(cid, MessageCodeDictionary.JOIN_REQUEST);
		clientPort = port;
	}
	
	public int getClientPort() {
		return clientPort;
	}

}
