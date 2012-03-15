package ca.sfu.cmpt431.message.leave;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class LeaveReceiverMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	public int rcvid;
	public int rcvport;
	public String rcvip;
	
	public LeaveReceiverMsg(int id, int port, String ip) {
		super(MessageCodeDictionary.ID_SERVER, MessageCodeDictionary.LEAVE_RECEIVER);
		rcvid = id;
		rcvport = port;
		rcvip = ip;
	}
}
