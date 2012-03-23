package ca.sfu.cmpt431.message.recover;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RecoverStopMsg extends Message {
	
	private static final long serialVersionUID = 1L;
	
	public int lostid;
	
	public RecoverStopMsg(int lid) {
		super(MessageCodeDictionary.ID_SERVER, MessageCodeDictionary.RECOVER_STOP);
		lostid = lid;
	}
	
}
