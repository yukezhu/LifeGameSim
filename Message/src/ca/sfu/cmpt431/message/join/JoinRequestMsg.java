package ca.sfu.cmpt431.message.join;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class JoinRequestMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	public int clientPort;
	
	public JoinRequestMsg(int port) {
		super(MessageCodeDictionary.ID_INVALID, MessageCodeDictionary.JOIN_REQUEST);
		clientPort = port;
	}

}
