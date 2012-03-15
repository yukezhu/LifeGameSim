package ca.sfu.cmpt431.message.leave;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class LeaveRequestMsg extends Message {

	private static final long serialVersionUID = 1L;
	public LeaveRequestMsg(int cid) {
		super(cid, MessageCodeDictionary.LEAVE_REQUEST);
	}

}
