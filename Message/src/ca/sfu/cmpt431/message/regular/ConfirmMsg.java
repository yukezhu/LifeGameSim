package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class ConfirmMsg extends Message {
	
	private static final long serialVersionUID = 1L;
	
	public ConfirmMsg(int cid) {
		super(cid, MessageCodeDictionary.REGULAR_REQUEST);
	}

}
