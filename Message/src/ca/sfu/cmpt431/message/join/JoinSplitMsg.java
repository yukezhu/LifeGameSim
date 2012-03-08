package ca.sfu.cmpt431.message.join;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class JoinSplitMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	public int newcomerId;
	public int newcomerPort;
	public String newcomerIp;
	public int splitMode;
	
	public JoinSplitMsg(int newId, int newPort, String newIp, int mode) {
		super(MessageCodeDictionary.ID_SERVER, MessageCodeDictionary.JOIN_SPLIT);
		newcomerId = newId;
		newcomerPort = newPort;
		newcomerIp = newIp;
		splitMode = mode;
	}

}
