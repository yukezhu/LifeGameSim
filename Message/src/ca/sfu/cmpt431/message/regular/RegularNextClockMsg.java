package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularNextClockMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	public int clockNum;
	
	public RegularNextClockMsg(int newClock) {
		super(MessageCodeDictionary.ID_SERVER, MessageCodeDictionary.REGULAR_NEXTCLOCK);
		clockNum = newClock;
	}

}
