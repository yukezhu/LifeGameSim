package ca.sfu.cmpt431.message.join;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class JoinSplitMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	private int newcomerId;
	
	public JoinSplitMsg() {
		super(MessageCodeDictionary.ID_SERVER, MessageCodeDictionary.JOIN_SPLIT);
	}

}
