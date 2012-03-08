package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class NextClockMsg extends Message {

	private static final long serialVersionUID = 1L;
	
	private int clockNum;
	
	public NextClockMsg(int newClock) {
		super(MessageCodeDictionary.SERVER_ID, MessageCodeDictionary.REGULAR_NEXTCLOCK);
		clockNum = newClock;
	}
	
	public int getClockNum() {
		return clockNum;
	}

}
