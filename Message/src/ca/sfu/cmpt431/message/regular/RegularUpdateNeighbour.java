package ca.sfu.cmpt431.message.regular;

import ca.sfu.cmpt431.message.Message;
import ca.sfu.cmpt431.message.MessageCodeDictionary;

public class RegularUpdateNeighbour extends Message {
	
	private static final long serialVersionUID = 1L;
	
//	private 

	public RegularUpdateNeighbour(int cid, int postion) {
		super(cid, MessageCodeDictionary.REGULAR_UPDATENEI);
	}

}
