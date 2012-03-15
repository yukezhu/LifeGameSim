package ca.sfu.cmpt431.message.merge;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class MergeLastMsg extends Message{

	private static final long serialVersionUID = 1L;
	
	public int thePair;
	
	public MergeLastMsg(int pair) {
		super(MessageCodeDictionary.ID_SERVER, MessageCodeDictionary.MERGE_LAST);
		thePair = pair;
	}
	
}
