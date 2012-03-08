package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularConfirmMsg extends Message {
	
	private static final long serialVersionUID = 1L;
	
	public RegularConfirmMsg(int cid) {
		super(cid, MessageCodeDictionary.REGULAR_CONFIRM);
	}

}
